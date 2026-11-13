package ec.edu.sigh.modelo;

// Proyección de XEUSU_USUAR: XEUSU_PASWD, XEEST_CODIGO y PEPER_CODIGO.
public record Usuario(String nombre, char estado, String codigoPersona) {
    public boolean activo() {
        return estado == 'A';
    }
}
