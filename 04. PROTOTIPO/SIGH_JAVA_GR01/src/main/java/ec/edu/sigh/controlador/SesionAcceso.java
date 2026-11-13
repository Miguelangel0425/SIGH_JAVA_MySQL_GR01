package ec.edu.sigh.controlador;

import ec.edu.sigh.modelo.Usuario;
import jakarta.servlet.http.HttpSession;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

// Sesión compartida: el login la crea y los demás módulos la consultan.
public final class SesionAcceso {
    private static final String ATRIBUTO = "acceso";
    private static final Duration DURACION = Duration.ofMinutes(20);

    private record Acceso(Usuario usuario, Instant vence) {}

    private SesionAcceso() {}

    public static void iniciar(HttpSession sesion, Usuario usuario) {
        sesion.setAttribute(ATRIBUTO, new Acceso(usuario, Instant.now().plus(DURACION)));
    }

    public static Usuario usuario(HttpSession sesion) {
        if (sesion == null) {
            return null;
        }
        Acceso acceso = (Acceso) sesion.getAttribute(ATRIBUTO);
        return acceso != null && acceso.vence().isAfter(Instant.now()) ? acceso.usuario() : null;
    }

    public static String token(HttpSession sesion) {
        synchronized (sesion) {
            String token = (String) sesion.getAttribute("token");
            if (token == null) {
                token = UUID.randomUUID().toString();
                sesion.setAttribute("token", token);
            }
            return token;
        }
    }
}
