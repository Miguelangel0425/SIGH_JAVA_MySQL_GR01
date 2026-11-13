package ec.edu.sigh.modelo;

import java.util.Locale;

// PEDEP_DEPAR: PEDEP_CODIGO y PEDEP_DESCRIP.
public record Departamento(String codigo, String descripcion) {
    public static String normalizarCodigo(String codigo) {
        return codigo == null ? "" : codigo.strip().toUpperCase(Locale.ROOT);
    }

    public static boolean codigoValido(String codigo) {
        return codigo.matches("[A-Z0-9]{1,3}");
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
