package ec.edu.sigh.modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

public final class CatalogoDepartamentos {
    private Connection conectar() throws SQLException {
        return ConexionBD.abrir();
    }

    public List<Departamento> listar() {
        String sql = "SELECT PEDEP_CODIGO, PEDEP_DESCRIP FROM pedep_depar ORDER BY PEDEP_CODIGO";
        List<Departamento> lista = new ArrayList<>();
        try (Connection c = conectar();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Departamento(rs.getString(1), rs.getString(2)));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error al listar departamentos", e);
        }
        return lista;
    }

    public Departamento buscar(String codigo) {
        String sql = "SELECT PEDEP_CODIGO, PEDEP_DESCRIP FROM pedep_depar WHERE PEDEP_CODIGO = ?";
        try (Connection c = conectar(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new Departamento(rs.getString(1), rs.getString(2)) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error al buscar departamento", e);
        }
    }

    public boolean crear(Departamento d) {
        String sql = "INSERT INTO pedep_depar (PEDEP_CODIGO, PEDEP_DESCRIP) VALUES (?, ?)";
        try (Connection c = conectar(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, d.codigo());
            ps.setString(2, d.descripcion());
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            return false; // código duplicado
        } catch (SQLException e) {
            throw new IllegalStateException("Error al crear departamento", e);
        }
    }

    public boolean editar(Departamento d) {
        String sql = "UPDATE pedep_depar SET PEDEP_DESCRIP = ? WHERE PEDEP_CODIGO = ?";
        try (Connection c = conectar(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, d.descripcion());
            ps.setString(2, d.codigo());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Error al editar departamento", e);
        }
    }

    public boolean eliminar(String codigo) {
        String sql = "DELETE FROM pedep_depar WHERE PEDEP_CODIGO = ?";
        try (Connection c = conectar(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, codigo);
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            return false; // tiene cargos asociados
        } catch (SQLException e) {
            throw new IllegalStateException("Error al eliminar departamento", e);
        }
    }
}