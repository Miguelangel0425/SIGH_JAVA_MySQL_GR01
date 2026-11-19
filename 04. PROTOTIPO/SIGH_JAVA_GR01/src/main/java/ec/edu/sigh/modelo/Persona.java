package ec.edu.sigh.modelo;

import java.time.LocalDate;

public record Persona(String codigo, String genero, String estadoCivil, String apellidos, String nombres,
        LocalDate fechaNacimiento, String cedula, String pasaporte, String email,
        int cargasFamiliares, String foto, String discapacidad) {

    public String getCodigo() {
        return codigo;
    }

    public String getGenero() {
        return genero;
    }

    public String getEstadoCivil() {
        return estadoCivil;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getNombres() {
        return nombres;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public String getCedula() {
        return cedula;
    }

    public String getPasaporte() {
        return pasaporte;
    }

    public String getEmail() {
        return email;
    }

    public int getCargasFamiliares() {
        return cargasFamiliares;
    }

    public String getFoto() {
        return foto;
    }

    public String getDiscapacidad() {
        return discapacidad;
    }
}
