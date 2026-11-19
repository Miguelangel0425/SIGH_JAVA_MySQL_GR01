package ec.edu.sigh.controlador;

import ec.edu.sigh.modelo.CatalogoPersona;
import ec.edu.sigh.modelo.ConexionBD;
import ec.edu.sigh.modelo.Persona;
import ec.edu.sigh.modelo.Usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

// Genera los reportes de JasperReports (diseñados en Jaspersoft Studio, carpeta WEB-INF/reportes)
// y los devuelve en PDF:
//   /reporte?r=departamentos
//   /reporte?r=cargos[&departamento=ENF]
//   /reporte?r=personas
//   /reporte?r=ficha&codigo=PER-000001
@WebServlet("/reporte")
public final class ReporteServlet extends HttpServlet {
    private static final Pattern CODIGO = Pattern.compile("[A-Z0-9-]{1,10}");
    private final Map<String, JasperReport> compilados = new ConcurrentHashMap<>();
    private final CatalogoPersona personas = new CatalogoPersona();

    static {
        System.setProperty("java.awt.headless", "true");
    }

    @Override
    protected void doGet(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
        Usuario usuario = SesionAcceso.usuario(solicitud.getSession(false));
        String tipo = solicitud.getParameter("r") == null ? "" : solicitud.getParameter("r");
        String archivo;
        String nombrePdf;
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("REPORT_LOCALE", Locale.of("es", "EC"));
        parametros.put("USUARIO", usuario == null ? "" : usuario.nombre());

        switch (tipo) {
            case "departamentos" -> {
                archivo = "departamentos";
                nombrePdf = "departamentos.pdf";
            }
            case "cargos" -> {
                archivo = "cargos";
                nombrePdf = "cargos.pdf";
                parametros.put("DEPARTAMENTO", codigo(solicitud.getParameter("departamento")));
            }
            case "personas" -> {
                archivo = "personas";
                nombrePdf = "personas.pdf";
            }
            case "ficha" -> {
                String codigo = codigo(solicitud.getParameter("codigo"));
                Persona persona = codigo.isEmpty() ? null : personas.buscar(codigo);
                if (persona == null) {
                    respuesta.sendError(HttpServletResponse.SC_NOT_FOUND, "La persona no existe.");
                    return;
                }
                archivo = "persona_ficha";
                nombrePdf = "ficha-" + persona.codigo() + ".pdf";
                parametros.put("PERSONA", persona.codigo());
                parametros.put("FOTO_RUTA", rutaFoto(persona.foto()));
            }
            default -> {
                respuesta.sendError(HttpServletResponse.SC_NOT_FOUND, "Reporte desconocido.");
                return;
            }
        }

        try (Connection conexion = ConexionBD.abrir()) {
            JasperReport reporte = cargar(archivo);
            JasperPrint impresion = JasperFillManager.fillReport(reporte, parametros, conexion);
            byte[] pdf = JasperExportManager.exportReportToPdf(impresion);
            respuesta.setContentType("application/pdf");
            respuesta.setHeader("Content-Disposition", "inline; filename=\"" + nombrePdf + "\"");
            respuesta.setHeader("Cache-Control", "no-store");
            respuesta.setContentLength(pdf.length);
            respuesta.getOutputStream().write(pdf);
        } catch (JRException | SQLException e) {
            throw new ServletException("No se pudo generar el reporte " + archivo, e);
        }
    }

    // Compila el .jrxml la primera vez y lo guarda en memoria.
    private JasperReport cargar(String nombre) throws JRException, IOException {
        JasperReport reporte = compilados.get(nombre);
        if (reporte == null) {
            try (InputStream in = getServletContext().getResourceAsStream("/WEB-INF/reportes/" + nombre + ".jrxml")) {
                if (in == null) {
                    throw new IOException("No existe WEB-INF/reportes/" + nombre + ".jrxml");
                }
                reporte = JasperCompileManager.compileReport(in);
            }
            compilados.put(nombre, reporte);
        }
        return reporte;
    }

    // Foto de la persona (carpeta sigh-fotos) o la imagen por defecto del proyecto.
    private String rutaFoto(String foto) {
        if (foto != null && !FotoServlet.PREDETERMINADA.equals(foto) && foto.matches("[A-Za-z0-9._-]{1,100}")) {
            Path ruta = Path.of(System.getProperty("user.home"), "sigh-fotos", foto);
            if (Files.isRegularFile(ruta)) {
                return ruta.toString();
            }
        }
        String predeterminada = getServletContext().getRealPath("/resources/" + FotoServlet.PREDETERMINADA);
        return predeterminada == null ? "" : predeterminada;
    }

    private static String codigo(String texto) {
        String limpio = texto == null ? "" : texto.strip().toUpperCase(Locale.ROOT);
        return CODIGO.matcher(limpio).matches() ? limpio : "";
    }
}
