package ec.edu.sigh.controlador;
import ec.edu.sigh.modelo.AutenticacionBD;
import ec.edu.sigh.modelo.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

@WebServlet(urlPatterns = {"", "/acceso", "/login", "/inicio", "/salir"})
public final class AccesoServlet extends HttpServlet {
    private final AutenticacionBD autenticacion = new AutenticacionBD();

    @Override
    protected void doGet(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
        HttpSession sesion = solicitud.getSession(false);
        Usuario usuario = SesionAcceso.usuario(sesion);
        switch (solicitud.getServletPath()) {
            case "", "/acceso" -> {
                if (usuario != null) {
                    redirigir(solicitud, respuesta, "/inicio");
                } else {
                    mostrarAcceso(solicitud, respuesta, "", "");
                }
            }
            case "/inicio" -> {
                if (usuario == null) {
                    redirigir(solicitud, respuesta, "/");
                } else {
                    solicitud.setAttribute("nombre", usuario.nombre());
                    solicitud.setAttribute("token", SesionAcceso.token(sesion));
                    mostrar(solicitud, respuesta, "/frminicio.jsp");
                }
            }
            default -> respuesta.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Override
    protected void doPost(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
        String ruta = solicitud.getServletPath();
        if (!"/login".equals(ruta) && !"/salir".equals(ruta)) {
            respuesta.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        solicitud.setCharacterEncoding(StandardCharsets.UTF_8.name());
        HttpSession sesion = solicitud.getSession(false);
        if (solicitud.getContentLengthLong() > 4096 || sesion == null
                || !SesionAcceso.token(sesion).equals(solicitud.getParameter("token"))) {
            respuesta.sendError(HttpServletResponse.SC_BAD_REQUEST, "Formulario inválido o vencido.");
            return;
        }

        if ("/salir".equals(ruta)) {
            sesion.invalidate();
            redirigir(solicitud, respuesta, "/");
            return;
        }
        ingresar(solicitud, respuesta, sesion);
    }

    private void ingresar(HttpServletRequest solicitud, HttpServletResponse respuesta, HttpSession sesion)
            throws ServletException, IOException {
        String nombre = Optional.ofNullable(solicitud.getParameter("nombre")).orElse("").trim();
        String clave = Optional.ofNullable(solicitud.getParameter("clave")).orElse("");
        if (nombre.isEmpty() || nombre.length() > 64 || clave.isEmpty() || clave.length() > 128) {
            respuesta.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mostrarAcceso(solicitud, respuesta, nombre, "Ingresa un usuario y una contraseña válidos.");
            return;
        }
        Optional<Usuario> usuario = autenticacion.autenticar(nombre, clave);
        if (usuario.isEmpty()) {
            mostrarAcceso(solicitud, respuesta, nombre, "Usuario o contraseña incorrectos.");
            return;
        }

        solicitud.changeSessionId();
        SesionAcceso.iniciar(sesion, usuario.get());
        sesion.removeAttribute("token");
        redirigir(solicitud, respuesta, "/inicio");
    }

    private void mostrarAcceso(HttpServletRequest solicitud, HttpServletResponse respuesta, String nombre, String error)
            throws ServletException, IOException {
        solicitud.setAttribute("nombre", nombre);
        solicitud.setAttribute("error", error);
        solicitud.setAttribute("token", SesionAcceso.token(solicitud.getSession()));
        mostrar(solicitud, respuesta, "/frmlogin.jsp");
    }

    private static void mostrar(HttpServletRequest solicitud, HttpServletResponse respuesta, String vista)
            throws ServletException, IOException {
        respuesta.setHeader("Cache-Control", "no-store");
        solicitud.getRequestDispatcher(vista).forward(solicitud, respuesta);
    }

    private static void redirigir(HttpServletRequest solicitud, HttpServletResponse respuesta, String ruta) {
        respuesta.setHeader("Cache-Control", "no-store");
        respuesta.setStatus(HttpServletResponse.SC_SEE_OTHER);
        respuesta.setHeader("Location", solicitud.getContextPath() + ruta);
    }
}
