package ec.edu.sigh.modelo;

// PECAR_CARGO: PECAR_CODIGO, PECAR_DESCRI y PEDEP_CODIGO. Clave primaria: (departamento, código).
public record Cargo(String codigo, String descripcion, String departamento, String departamentoDescripcion) {
    public Cargo(String codigo, String descripcion, String departamento) {
        this(codigo, descripcion, departamento, null);
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getDepartamento() {
        return departamento;
    }

    public String getDepartamentoDescripcion() {
        return departamentoDescripcion;
    }
}
