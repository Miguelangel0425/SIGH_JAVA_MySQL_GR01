package ec.edu.sigh.controlador;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

// Las fotos se guardan fuera del WAR (carpeta sigh-fotos del usuario que ejecuta el servidor)
// y se sirven por /foto?archivo=PER-000001.jpg
@WebServlet("/foto")
public final class FotoServlet extends HttpServlet {
    private static final Pattern NOMBRE = Pattern.compile("[A-Za-z0-9._-]{1,100}");
    private static final long MAXIMO = 2L * 1024 * 1024;
    public static final String PREDETERMINADA = "default.png";

    private static Path carpeta() throws IOException {
        Path dir = Path.of(System.getProperty("user.home"), "sigh-fotos");
        Files.createDirectories(dir);
        return dir;
    }

    // Guarda la foto subida como CODIGO.png/.jpg. Devuelve el nombre, null si no se envió, o lanza
    // IllegalArgumentException con el motivo si es inválida.
    static String guardar(Part parte, String codigo) throws IOException {
        if (parte == null || parte.getSize() == 0) {
            return null;
        }
        if (parte.getSize() > MAXIMO) {
            throw new IllegalArgumentException("La foto no puede superar 2 MB.");
        }
        byte[] datos;
        try (InputStream in = parte.getInputStream()) {
            datos = in.readAllBytes();
        }
        String extension;
        if (datos.length > 4 && (datos[0] & 0xFF) == 0x89 && datos[1] == 'P' && datos[2] == 'N' && datos[3] == 'G') {
            extension = ".png";
        } else if (datos.length > 3 && (datos[0] & 0xFF) == 0xFF && (datos[1] & 0xFF) == 0xD8
                && (datos[2] & 0xFF) == 0xFF) {
            extension = ".jpg";
        } else {
            throw new IllegalArgumentException("La foto debe ser una imagen PNG o JPG.");
        }
        String nombre = codigo + extension;
        Path dir = carpeta();
        Files.deleteIfExists(dir.resolve(codigo + (extension.equals(".png") ? ".jpg" : ".png")));
        Files.write(dir.resolve(nombre), datos);
        return nombre;
    }

    @Override
    protected void doGet(HttpServletRequest solicitud, HttpServletResponse respuesta)
            throws ServletException, IOException {
        String archivo = solicitud.getParameter("archivo");
        Path ruta = null;
        if (archivo != null && NOMBRE.matcher(archivo).matches() && !PREDETERMINADA.equals(archivo)) {
            Path candidata = carpeta().resolve(archivo).normalize();
            if (candidata.startsWith(carpeta()) && Files.isRegularFile(candidata)) {
                ruta = candidata;
            }
        }
        byte[] datos;
        String tipo;
        if (ruta != null) {
            datos = Files.readAllBytes(ruta);
            tipo = archivo.endsWith(".png") ? "image/png" : "image/jpeg";
        } else {
            try (InputStream in = getServletContext().getResourceAsStream("/resources/" + PREDETERMINADA)) {
                if (in == null) {
                    respuesta.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
                datos = in.readAllBytes();
            }
            tipo = "image/png";
        }
        respuesta.setContentType(tipo);
        respuesta.setContentLength(datos.length);
        respuesta.setHeader("Cache-Control", "private, no-cache");
        respuesta.setHeader("X-Content-Type-Options", "nosniff");
        respuesta.getOutputStream().write(datos);
    }
}
