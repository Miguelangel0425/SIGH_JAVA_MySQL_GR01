package ec.edu.sigh.controlador;

import ec.edu.sigh.modelo.CatalogoDepartamentos;
import ec.edu.sigh.modelo.Departamento;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@WebServlet("/departamentos")
public final class DepartamentosServlet extends HttpServlet {
    private static final String RUTA = "/departamentos";
    private final CatalogoDepartamentos catalogo = new CatalogoDepartamentos();

    @Override
    protected void doGet(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
        HttpSession sesion = solicitud.getSession();
        String modo = solicitud.getParameter("modo");
        Departamento actual = null;
        if ("editar".equals(modo) || "eliminar".equals(modo)) {
            actual = catalogo.buscar(Departamento.normalizarCodigo(solicitud.getParameter("codigo")));
            if (actual == null) {
                mostrar(solicitud, respuesta, sesion, "crear", null, 404, "El departamento no existe.");
                return;
            }
        }
        mostrar(solicitud, respuesta, sesion, actual == null ? "crear" : modo, actual, 200, "");
    }

    @Override
    protected void doPost(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
        HttpSession sesion = solicitud.getSession(false);
        solicitud.setCharacterEncoding(StandardCharsets.UTF_8.name());
        if (solicitud.getContentLengthLong() > 4096 || sesion == null
                || !SesionAcceso.token(sesion).equals(solicitud.getParameter("token"))) {
            respuesta.sendError(HttpServletResponse.SC_BAD_REQUEST, "Formulario inválido o vencido.");
            return;
        }

        String modo = solicitud.getParameter("modo");
        if (!"crear".equals(modo) && !"editar".equals(modo) && !"eliminar".equals(modo)) {
            respuesta.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        String codigo = Departamento.normalizarCodigo(solicitud.getParameter("codigo"));
        String descripcion = solicitud.getParameter("descripcion");
        descripcion = descripcion == null || descripcion.isBlank() ? null : descripcion.strip();
        Departamento departamento = new Departamento(codigo, descripcion);
        if (!Departamento.codigoValido(codigo)) {
            mostrar(solicitud, respuesta, sesion, "crear", departamento, 400,
                    "El código debe tener de 1 a 3 letras o números.");
            return;
        }
        if (!"eliminar".equals(modo) && descripcion != null
                && descripcion.codePointCount(0, descripcion.length()) > 50) {
            mostrar(solicitud, respuesta, sesion, modo, departamento, 400,
                    "La descripción debe tener hasta 50 caracteres.");
            return;
        }

        boolean guardado = switch (modo) {
            case "crear" -> catalogo.crear(departamento);
            case "editar" -> catalogo.editar(departamento);
            default -> catalogo.eliminar(codigo);
        };
        if (guardado) {
            respuesta.setHeader("Cache-Control", "no-store");
            respuesta.setStatus(HttpServletResponse.SC_SEE_OTHER);
            respuesta.setHeader("Location", solicitud.getContextPath() + RUTA);
        } else if ("crear".equals(modo)) {
            mostrar(solicitud, respuesta, sesion, modo, departamento, 409, "Ese código ya está registrado.");
        } else {
            mostrar(solicitud, respuesta, sesion, "crear", null, 404,
                    "El departamento no existe o tiene cargos asociados.");
        }
    }

    private void mostrar(HttpServletRequest solicitud, HttpServletResponse respuesta, HttpSession sesion,
                         String modo, Departamento departamento, int estado, String error)
            throws ServletException, IOException {
        solicitud.setAttribute("departamentos", catalogo.listar());
        solicitud.setAttribute("departamento", departamento);
        solicitud.setAttribute("modo", modo);
        solicitud.setAttribute("error", error);
        solicitud.setAttribute("token", SesionAcceso.token(sesion));
        respuesta.setHeader("Cache-Control", "no-store");
        respuesta.setStatus(estado);
        solicitud.getRequestDispatcher("/WEB-INF/frmdepar.jsp").forward(solicitud, respuesta);
    }
}
