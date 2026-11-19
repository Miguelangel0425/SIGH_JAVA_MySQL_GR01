package ec.edu.sigh.modelo;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

public final class CatalogoPersona {

    private static final String SELECT = """
            SELECT PEPER_CODIGO, PEGEN_CODIGO, PEESC_CODIGO, PEPER_APELLI, PEPER_NOMBRE, PEPER_FECNAC,
                   PEPER_CEDULA, PEPER_PASAPO, PEPER_EMAIL, PEPER_CARFAM, PEPER_FOTO, PEPER_DISCAPACIDAD
            FROM peper_perso
            """;

    private static Persona leer(ResultSet rs) throws SQLException {
        return new Persona(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
                rs.getString(5), rs.getDate(6).toLocalDate(), rs.getString(7), rs.getString(8),
                rs.getString(9), rs.getInt(10), rs.getString(11), rs.getString(12));
    }

    public List<Persona> listar() {
        List<Persona> lista = new ArrayList<>();
        try (Connection c = ConexionBD.abrir(); PreparedStatement ps = c.prepareStatement(SELECT + " ORDER BY PEPER_APELLI, PEPER_NOMBRE"); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(leer(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error al listar personas", e);
        }
        return lista;
    }

    public Persona buscar(String codigo) {
        return uno(SELECT + " WHERE PEPER_CODIGO = ? ", codigo);
    }
    // como: primero, anterior, siguiente o ultimo (según el código actual). null si no hay.

    public Persona navegar(String como, String actual) {
        return switch (como) {
            case "primero" ->
                uno(SELECT + " ORDER BY PEPER_CODIGO LIMIT 1", null);
            case "ultimo" ->
                uno(SELECT + " ORDER BY PEPER_CODIGO DESC LIMIT 1", null);
            case "siguiente" ->
                uno(SELECT + " WHERE PEPER_CODIGO > ? ORDER BY PEPER_CODIGO LIMIT 1", actual);
            case "anterior" ->
                uno(SELECT + " WHERE PEPER_CODIGO < ? ORDER BY PEPER_CODIGO DESC LIMIT 1", actual);
            default ->
                null;
        };
    }

    private Persona uno(String sql, String parametro) {
        try (Connection c = ConexionBD.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (parametro != null) {
                ps.setString(1, parametro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? leer(rs) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error al buscar a la persona", e);
        }
    }

    // Código único por persona: PER-000001, PER-000002... (10 caracteres).
    public String siguienteCodigo() {
        try (Connection c = ConexionBD.abrir()) {
            return siguienteCodigo(c);
        } catch (SQLException e) {
            throw new IllegalStateException("Error al calcular el código", e);
        }
    }

    private static String siguienteCodigo(Connection c) throws SQLException {
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(PEPER_CODIGO, 5) AS UNSIGNED)), 0) + 1 "
                + "FROM peper_perso WHERE PEPER_CODIGO REGEXP '^PER-[0-9]{6}$'";
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            rs.next();
            return String.format("PER-%06d", rs.getInt(1));
        }
    }

    public String crear(Persona p) {
        String sql = """
                INSERT INTO peper_perso (PEPER_CODIGO, PEGEN_CODIGO, PEESC_CODIGO, PEPER_APELLI, PEPER_NOMBRE,
                    PEPER_FECNAC, PEPER_CEDULA, PEPER_PASAPO, PEPER_EMAIL, PEPER_CARFAM, PEPER_FOTO,
                    PEPER_DISCAPACIDAD)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection c = ConexionBD.abrir()) {
            for (int intento = 0; intento < 3; intento++) {
                String codigo = siguienteCodigo(c);
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setString(1, codigo);
                    ps.setString(2, p.genero());
                    ps.setString(3, p.estadoCivil());
                    ps.setString(4, p.apellidos());
                    ps.setString(5, p.nombres());
                    ps.setDate(6, Date.valueOf(p.fechaNacimiento()));
                    ps.setString(7, p.cedula());
                    ps.setString(8, p.pasaporte());
                    ps.setString(9, p.email());
                    ps.setInt(10, p.cargasFamiliares());
                    ps.setString(11, p.foto());
                    ps.setString(12, p.discapacidad());
                    ps.executeUpdate();
                    return codigo;
                } catch (SQLIntegrityConstraintViolationException ex) {
                    // otro usuario tomó el código: reintenta con el siguiente
                }
            }
            return null;
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al registrar persona", ex);
        }
    }

    public boolean actualizar(Persona p) {
        String sql = """
                UPDATE peper_perso SET PEGEN_CODIGO = ?, PEESC_CODIGO = ?, PEPER_APELLI = ?, PEPER_NOMBRE = ?,
                    PEPER_FECNAC = ?, PEPER_CEDULA = ?, PEPER_PASAPO = ?, PEPER_EMAIL = ?, PEPER_CARFAM = ?,
                    PEPER_FOTO = ?, PEPER_DISCAPACIDAD = ?
                WHERE PEPER_CODIGO = ?
                """;
        try (Connection c = ConexionBD.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.genero());
            ps.setString(2, p.estadoCivil());
            ps.setString(3, p.apellidos());
            ps.setString(4, p.nombres());
            ps.setDate(5, Date.valueOf(p.fechaNacimiento()));
            ps.setString(6, p.cedula());
            ps.setString(7, p.pasaporte());
            ps.setString(8, p.email());
            ps.setInt(9, p.cargasFamiliares());
            ps.setString(10, p.foto());
            ps.setString(11, p.discapacidad());
            ps.setString(12, p.codigo());
            // Si no cambió ningún dato algunos drivers devuelven 0: se confirma que la persona exista.
            return ps.executeUpdate() > 0 || buscar(p.codigo()) != null;
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al actualizar persona", ex);
        }
    }

    public void actualizarFoto(String codigo, String foto) {
        try (Connection c = ConexionBD.abrir(); PreparedStatement ps = c.prepareStatement("UPDATE peper_perso SET PEPER_FOTO = ? WHERE PEPER_CODIGO = ?")) {
            ps.setString(1, foto);
            ps.setString(2, codigo);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al guardar la foto", ex);
        }
    }

    // false si no existe o la persona está en uso (usuario, profesional, etc.).
    public boolean eliminar(String codigo) {
        try (Connection c = ConexionBD.abrir(); PreparedStatement ps = c.prepareStatement("DELETE FROM peper_perso WHERE PEPER_CODIGO = ?")) {
            ps.setString(1, codigo);
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException ex) {
            return false;
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al eliminar persona", ex);
        }
    }

    public List<Opcion> generos() {
        return opciones("SELECT PEGEN_CODIGO, PEGEN_DESCRI FROM pegen_genero ORDER BY PEGEN_DESCRI");
    }

    public List<Opcion> estadosCiviles() {
        return opciones("SELECT PEESC_CODIGO, PEESC_DESCRI FROM peesc_civil ORDER BY PEESC_DESCRI");
    }
        
    private static List<Opcion> opciones(String sql) {
        List<Opcion> lista = new ArrayList<>();
        try (Connection c = ConexionBD.abrir(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Opcion(rs.getString(1), rs.getString(2)));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error al leer el catálogo", e);
        }
        return lista;
    }
}