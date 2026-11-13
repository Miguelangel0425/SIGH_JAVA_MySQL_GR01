package ec.edu.sigh.controlador;

import ec.edu.sigh.modelo.CatalogoCargos;
import ec.edu.sigh.modelo.CatalogoDepartamentos;
import ec.edu.sigh.modelo.Cargo;
import ec.edu.sigh.modelo.Departamento;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@WebServlet("/cargos")
public final class CargosServlet extends HttpServlet {
    private static final String RUTA = "/cargos";
    private final CatalogoCargos catalogo = new CatalogoCargos();
    private final CatalogoDepartamentos departamentos = new CatalogoDepartamentos();

    @Override
    protected void doGet(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
        HttpSession sesion = solicitud.getSession();
        String modo = solicitud.getParameter("modo");
        Cargo actual = null;
        if ("editar".equals(modo) || "eliminar".equals(modo)) {
            actual = catalogo.buscar(Departamento.normalizarCodigo(solicitud.getParameter("depto")),
                    Departamento.normalizarCodigo(solicitud.getParameter("codigo")));
            if (actual == null) {
                mostrar(solicitud, respuesta, sesion, "crear", null, 404, "El cargo no existe.");
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
        String depto = Departamento.normalizarCodigo(solicitud.getParameter("departamento"));
        Cargo cargo = new Cargo(codigo, descripcion, depto);

        if (!"crear".equals(modo) && !Departamento.codigoValido(codigo)) {
            mostrar(solicitud, respuesta, sesion, "crear", null, 400, "Cargo inválido.");
            return;
        }
        if (!Departamento.codigoValido(depto) || departamentos.buscar(depto) == null) {
            mostrar(solicitud, respuesta, sesion, "crear", cargo, 400, "Selecciona un departamento válido.");
            return;
        }
        if (!"eliminar".equals(modo)) {
            if (descripcion != null && descripcion.codePointCount(0, descripcion.length()) > 50) {
                mostrar(solicitud, respuesta, sesion, modo, cargo, 400,
                        "La descripción debe tener hasta 50 caracteres.");
                return;
            }
        }

        boolean guardado = switch (modo) {
            case "crear" -> catalogo.crear(cargo) != null;
            case "editar" -> catalogo.editar(cargo);
            default -> catalogo.eliminar(depto, codigo);
        };
        if (guardado) {
            respuesta.setHeader("Cache-Control", "no-store");
            respuesta.setStatus(HttpServletResponse.SC_SEE_OTHER);
            respuesta.setHeader("Location", solicitud.getContextPath() + RUTA);
        } else if ("crear".equals(modo)) {
            mostrar(solicitud, respuesta, sesion, modo, cargo, 409,
                    "No se pudo generar el código del cargo. Intenta de nuevo.");
        } else {
            mostrar(solicitud, respuesta, sesion, "crear", null, 404,
                    "El cargo no existe o está en uso.");
        }
    }

    private void mostrar(HttpServletRequest solicitud, HttpServletResponse respuesta, HttpSession sesion,
                         String modo, Cargo cargo, int estado, String error)
            throws ServletException, IOException {
        String filtro = Departamento.normalizarCodigo(solicitud.getParameter("filtro"));
        solicitud.setAttribute("filtro", filtro);
        solicitud.setAttribute("cargos", catalogo.listar(filtro));
        solicitud.setAttribute("siguientes", catalogo.siguientes());
        solicitud.setAttribute("departamentos", departamentos.listar());
        solicitud.setAttribute("cargo", cargo);
        solicitud.setAttribute("modo", modo);
        solicitud.setAttribute("error", error);
        solicitud.setAttribute("token", SesionAcceso.token(sesion));
        respuesta.setHeader("Cache-Control", "no-store");
        respuesta.setStatus(estado);
        solicitud.getRequestDispatcher("/WEB-INF/frmcargos.jsp").forward(solicitud, respuesta);
    }
}
