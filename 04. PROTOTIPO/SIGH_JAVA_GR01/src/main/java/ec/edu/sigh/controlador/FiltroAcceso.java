package ec.edu.sigh.controlador;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

// Protege los módulos: sin sesión válida se redirige al login.
@WebFilter({"/departamentos", "/cargos", "/foto", "/persona", "/reporte"})
public final class FiltroAcceso implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain cadena)
            throws IOException, ServletException {
        HttpServletRequest solicitud = (HttpServletRequest) req;
        HttpServletResponse respuesta = (HttpServletResponse) res;
        if (SesionAcceso.usuario(solicitud.getSession(false)) == null) {
            respuesta.setHeader("Cache-Control", "no-store");
            respuesta.setStatus(HttpServletResponse.SC_SEE_OTHER);
            respuesta.setHeader("Location", solicitud.getContextPath() + "/");
            return;
        }
        cadena.doFilter(req, res);
    }
}
