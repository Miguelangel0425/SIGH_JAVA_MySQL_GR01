package ec.edu.sigh.controlador;

import ec.edu.sigh.modelo.CatalogoPersona;
import ec.edu.sigh.modelo.Persona;
import ec.edu.sigh.modelo.Opcion;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@WebServlet("/persona")
@MultipartConfig(maxFileSize = 10 * 1024 * 1024, maxRequestSize = 11 * 1024 * 1024)
public final class PersonaServlet extends HttpServlet {
    private static final String RUTA = "/persona";
    private static final Set<String> NAVEGACION = Set.of("primero", "anterior", "siguiente", "ultimo");
    private static final Set<String> PESTANAS = Set.of("generales", "identificacion");
    private static final Pattern EMAIL = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");
    private final CatalogoPersona catalogo = new CatalogoPersona();

    @Override
    protected void doGet(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
        HttpSession sesion = solicitud.getSession();
        String codigo = codigo(solicitud.getParameter("codigo"));
        String nav = solicitud.getParameter("nav");
        String tab = pestana(solicitud.getParameter("tab"));
        String ok = solicitud.getParameter("ok");
        boolean listar = "listar".equals(solicitud.getParameter("modo"));
        solicitud.setAttribute("aviso", "guardado".equals(ok) ? "Persona guardada."
                : "borrado".equals(ok) ? "Persona eliminada." : "");

        if ("nuevo".equals(solicitud.getParameter("modo"))) {
            solicitud.setAttribute("bloqueado", false); // formulario en blanco y habilitado
            mostrar(solicitud, respuesta, sesion, null, 200, "", tab, false);
            return;
        }
        Persona actual = null;
        if (nav != null && NAVEGACION.contains(nav)) {
            actual = catalogo.navegar(nav, codigo);
            if (actual == null && !codigo.isEmpty()) {
                actual = catalogo.buscar(codigo); // ya está en el extremo: se queda en el mismo
            }
        } else if (!codigo.isEmpty()) {
            actual = catalogo.buscar(codigo);
            if (actual == null) {
                solicitud.setAttribute("bloqueado", true);
                mostrar(solicitud, respuesta, sesion, null, 404, "La persona no existe.", tab, listar);
                return;
            }
        }
        // Sin código ni navegación: no se muestra ninguna persona y el formulario queda bloqueado
        // hasta pulsar Nuevo o elegir una persona de la lista.
        solicitud.setAttribute("bloqueado", actual == null);
        mostrar(solicitud, respuesta, sesion, actual, 200, "", tab, listar);
    }

    @Override
    protected void doPost(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
        HttpSession sesion = solicitud.getSession(false);
        solicitud.setCharacterEncoding(StandardCharsets.UTF_8.name());
        if (sesion == null || !SesionAcceso.token(sesion).equals(solicitud.getParameter("token"))) {
            respuesta.sendError(HttpServletResponse.SC_BAD_REQUEST, "Formulario inválido o vencido.");
            return;
        }
        String accion = solicitud.getParameter("accion");
        String codigo = codigo(solicitud.getParameter("codigo"));
        String tab = pestana(solicitud.getParameter("tab"));
        solicitud.setAttribute("aviso", "");

        if ("borrar".equals(accion)) {
            if (catalogo.eliminar(codigo)) {
                redirigir(solicitud, respuesta, RUTA + "?ok=borrado");
            } else {
                mostrar(solicitud, respuesta, sesion, catalogo.buscar(codigo), 409,
                        "No se puede eliminar: la persona no existe o está en uso (usuario, profesional...).",
                        tab, false);
            }
            return;
        }
        if (!"guardar".equals(accion)) {
            respuesta.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        boolean nuevo = codigo.isEmpty();
        Persona anterior = nuevo ? null : catalogo.buscar(codigo);
        if (!nuevo && anterior == null) {
            solicitud.setAttribute("bloqueado", true);
            mostrar(solicitud, respuesta, sesion, null, 404, "La persona no existe.", tab, false);
            return;
        }
        String foto = anterior == null ? FotoServlet.PREDETERMINADA : anterior.foto();
        String error = null;
        LocalDate nacimiento = null;
        String texto = solicitud.getParameter("fechaNacimiento");
        try {
            nacimiento = texto == null || texto.isBlank() ? null : LocalDate.parse(texto.strip());
        } catch (DateTimeParseException e) {
            error = "La fecha de nacimiento no es válida.";
        }
        int cargas = 0;
        try {
            cargas = Integer.parseInt(vacio(solicitud.getParameter("cargasFamiliares"), "0"));
        } catch (NumberFormatException e) {
            cargas = -1;
        }
        // Documento: se elige cédula o pasaporte y se llena solo uno de los dos campos de la tabla.
        String tipoDoc = "PASAPORTE".equals(solicitud.getParameter("tipoDocumento")) ? "PASAPORTE"
                : "CEDULA".equals(solicitud.getParameter("tipoDocumento")) ? "CEDULA" : "";
        String documento = nulo(solicitud.getParameter("documento"));
        String cedula = "CEDULA".equals(tipoDoc) ? documento : null;
        String pasaporte = "PASAPORTE".equals(tipoDoc) ? documento : null;
        solicitud.setAttribute("tipoDocumento", tipoDoc);
        Persona datos = new Persona(codigo, valor(solicitud, "genero"), valor(solicitud, "estadoCivil"),
                valor(solicitud, "apellidos"), valor(solicitud, "nombres"), nacimiento,
                cedula, pasaporte,
                nulo(solicitud.getParameter("email")), cargas, foto,
                "S".equals(solicitud.getParameter("discapacidad")) ? "S" : "N");

        if (error == null) {
            error = validar(datos);
        }
        if (error != null) {
            mostrar(solicitud, respuesta, sesion, datos, 400, error, tab, false);
            return;
        }

        // Foto (opcional): se guarda con el código de la persona.
        String codigoFinal = codigo;
        if (nuevo) {
            codigoFinal = catalogo.crear(datos);
            if (codigoFinal == null) {
                mostrar(solicitud, respuesta, sesion, datos, 409,
                        "No se pudo generar el código de la persona. Intenta de nuevo.", tab, false);
                return;
            }
        } else if (!catalogo.actualizar(datos)) {
            solicitud.setAttribute("bloqueado", true);
            mostrar(solicitud, respuesta, sesion, null, 404, "La persona no existe.", tab, false);
            return;
        }
        try {
            String nombreFoto = FotoServlet.guardar(solicitud.getPart("foto"), codigoFinal);
            if (nombreFoto != null) {
                catalogo.actualizarFoto(codigoFinal, nombreFoto);
            }
        } catch (IllegalArgumentException e) {
            // La persona ya se guardó; se avisa solo de la foto.
            mostrar(solicitud, respuesta, sesion, catalogo.buscar(codigoFinal), 400,
                    "Datos guardados, pero la foto no: " + e.getMessage(), tab, false);
            return;
        }
        // Tras guardar o actualizar se vuelve a la pantalla vacía y bloqueada (sin código en la dirección).
        redirigir(solicitud, respuesta, RUTA + "?tab=" + tab + "&ok=guardado");
    }

    private String validar(Persona e) {
        if (e.apellidos().isEmpty() || e.apellidos().codePointCount(0, e.apellidos().length()) > 50) {
            return "Los apellidos son obligatorios (hasta 50 caracteres).";
        }
        if (e.nombres().isEmpty() || e.nombres().codePointCount(0, e.nombres().length()) > 50) {
            return "Los nombres son obligatorios (hasta 50 caracteres).";
        }
        if (!existe(catalogo.generos(), e.genero())) {
            return "Selecciona el sexo.";
        }
        if (!existe(catalogo.estadosCiviles(), e.estadoCivil())) {
            return "Selecciona el estado civil.";
        }
        if (e.fechaNacimiento() == null || e.fechaNacimiento().isAfter(LocalDate.now())) {
            return "La fecha de nacimiento es obligatoria y no puede ser futura.";
        }
        if ((e.cedula() == null) == (e.pasaporte() == null)) {
            return "Selecciona el tipo de documento e ingresa su número (cédula o pasaporte, solo uno).";
        }
        if (e.cedula() != null && !e.cedula().matches("[0-9]{10}")) {
            return "La cédula debe tener 10 dígitos.";
        }
        if (e.pasaporte() != null && !e.pasaporte().matches("[A-Za-z0-9]{1,15}")) {
            return "El pasaporte debe tener hasta 15 letras o números.";
        }
        if (e.email() != null && (e.email().length() > 50 || !EMAIL.matcher(e.email()).matches())) {
            return "El correo electrónico no es válido (máximo 50 caracteres).";
        }
        if (e.cargasFamiliares() < 0 || e.cargasFamiliares() > 99) {
            return "Las cargas familiares deben estar entre 0 y 99.";
        }
        return null;
    }

    private static boolean existe(List<Opcion> opciones, String codigo) {
        return opciones.stream().anyMatch(o -> o.codigo().equals(codigo));
    }

    private void mostrar(HttpServletRequest solicitud, HttpServletResponse respuesta, HttpSession sesion,
                         Persona persona, int estado, String error, String tab, boolean listar)
            throws ServletException, IOException {
        if (solicitud.getAttribute("bloqueado") == null) {
            solicitud.setAttribute("bloqueado", false); // llamadas desde doPost
        }
        solicitud.setAttribute("persona", persona);
        boolean nuevo = persona == null || persona.codigo().isEmpty();
        solicitud.setAttribute("nuevo", nuevo);
        solicitud.setAttribute("siguiente", nuevo ? catalogo.siguienteCodigo() : "");
        solicitud.setAttribute("generos", catalogo.generos());
        solicitud.setAttribute("estadosCiviles", catalogo.estadosCiviles());
        if (listar) {
            solicitud.setAttribute("lista", catalogo.listar());
        }
        solicitud.setAttribute("error", error);
        solicitud.setAttribute("tab", tab);
        solicitud.setAttribute("token", SesionAcceso.token(sesion));
        respuesta.setHeader("Cache-Control", "no-store");
        respuesta.setStatus(estado);
        solicitud.getRequestDispatcher("/WEB-INF/frmperso.jsp").forward(solicitud, respuesta);
    }

    private static void redirigir(HttpServletRequest solicitud, HttpServletResponse respuesta, String ruta) {
        respuesta.setHeader("Cache-Control", "no-store");
        respuesta.setStatus(HttpServletResponse.SC_SEE_OTHER);
        respuesta.setHeader("Location", solicitud.getContextPath() + ruta);
    }

    private static String codigo(String texto) {
        return texto == null ? "" : texto.strip().toUpperCase(Locale.ROOT);
    }

    private static String pestana(String texto) {
        return texto != null && PESTANAS.contains(texto) ? texto : "generales";
    }

    private static String valor(HttpServletRequest solicitud, String nombre) {
        String texto = solicitud.getParameter(nombre);
        return texto == null ? "" : texto.strip();
    }

    private static String nulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.strip();
    }

    private static String vacio(String texto, String porDefecto) {
        return texto == null || texto.isBlank() ? porDefecto : texto.strip();
    }
}