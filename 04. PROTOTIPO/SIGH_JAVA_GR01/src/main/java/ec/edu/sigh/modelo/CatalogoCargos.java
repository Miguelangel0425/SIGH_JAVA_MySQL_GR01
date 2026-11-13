package ec.edu.sigh.modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

public final class CatalogoCargos {
    private static final String SELECT = """
            SELECT c.PECAR_CODIGO, c.PECAR_DESCRI, c.PEDEP_CODIGO, d.PEDEP_DESCRIP
            FROM pecar_cargo c LEFT JOIN pedep_depar d ON d.PEDEP_CODIGO = c.PEDEP_CODIGO
            """;

    private static Cargo leer(ResultSet rs) throws SQLException {
        return new Cargo(rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4));
    }

    // departamento == null o vacío: todos los cargos.
    public List<Cargo> listar(String departamento) {
        boolean filtrar = departamento != null && !departamento.isEmpty();
        String sql = SELECT + (filtrar ? " WHERE c.PEDEP_CODIGO = ?" : "")
                + " ORDER BY c.PEDEP_CODIGO, c.PECAR_CODIGO";
        List<Cargo> lista = new ArrayList<>();
        try (Connection c = ConexionBD.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (filtrar) {
                ps.setString(1, departamento);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(leer(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error al listar cargos", e);
        }
        return lista;
    }

    // Próximo código correlativo de cada departamento (001 si aún no tiene cargos).
    public Map<String, String> siguientes() {
        String sql = """
                SELECT d.PEDEP_CODIGO, COALESCE(MAX(CAST(c.PECAR_CODIGO AS UNSIGNED)), 0) + 1
                FROM pedep_depar d
                LEFT JOIN pecar_cargo c ON c.PEDEP_CODIGO = d.PEDEP_CODIGO AND c.PECAR_CODIGO REGEXP '^[0-9]+$'
                GROUP BY d.PEDEP_CODIGO
                """;
        Map<String, String> mapa = new LinkedHashMap<>();
        try (Connection c = ConexionBD.abrir();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                mapa.put(rs.getString(1), String.format("%03d", rs.getInt(2)));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error al calcular códigos", e);
        }
        return mapa;
    }

    public Cargo buscar(String departamento, String codigo) {
        try (Connection c = ConexionBD.abrir();
             PreparedStatement ps = c.prepareStatement(SELECT + " WHERE c.PEDEP_CODIGO = ? AND c.PECAR_CODIGO = ?")) {
            ps.setString(1, departamento);
            ps.setString(2, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? leer(rs) : null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error al buscar cargo", e);
        }
    }

    // Crea el cargo con el siguiente código correlativo (001, 002...) del departamento.
    // Devuelve el código generado, o null si no se pudo (departamento inexistente o sin códigos libres).
    public String crear(Cargo x) {
        String sql = "INSERT INTO pecar_cargo (PEDEP_CODIGO, PECAR_CODIGO, PECAR_DESCRI) VALUES (?, ?, ?)";
        try (Connection c = ConexionBD.abrir()) {
            for (int intento = 0; intento < 3; intento++) {
                int siguiente = siguiente(c, x.departamento());
                if (siguiente > 999) {
                    return null;
                }
                String codigo = String.format("%03d", siguiente);
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setString(1, x.departamento());
                    ps.setString(2, codigo);
                    ps.setString(3, x.descripcion());
                    ps.executeUpdate();
                    return codigo;
                } catch (SQLIntegrityConstraintViolationException e) {
                    if (!existeDepartamento(c, x.departamento())) {
                        return null;
                    } // si no, otro usuario tomó el código: reintenta
                }
            }
            return null;
        } catch (SQLException e) {
            throw new IllegalStateException("Error al crear cargo", e);
        }
    }

    private static int siguiente(Connection c, String departamento) throws SQLException {
        String sql = "SELECT COALESCE(MAX(CAST(PECAR_CODIGO AS UNSIGNED)), 0) + 1 FROM pecar_cargo "
                + "WHERE PEDEP_CODIGO = ? AND PECAR_CODIGO REGEXP '^[0-9]+$'";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, departamento);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private static boolean existeDepartamento(Connection c, String departamento) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM pedep_depar WHERE PEDEP_CODIGO = ?")) {
            ps.setString(1, departamento);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean editar(Cargo x) {
        String sql = "UPDATE pecar_cargo SET PECAR_DESCRI = ? WHERE PEDEP_CODIGO = ? AND PECAR_CODIGO = ?";
        try (Connection c = ConexionBD.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, x.descripcion());
            ps.setString(2, x.departamento());
            ps.setString(3, x.codigo());
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            return false;
        } catch (SQLException e) {
            throw new IllegalStateException("Error al editar cargo", e);
        }
    }

    public boolean eliminar(String departamento, String codigo) {
        String sql = "DELETE FROM pecar_cargo WHERE PEDEP_CODIGO = ? AND PECAR_CODIGO = ?";
        try (Connection c = ConexionBD.abrir(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, departamento);
            ps.setString(2, codigo);
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            return false; // en uso por otra tabla
        } catch (SQLException e) {
            throw new IllegalStateException("Error al eliminar cargo", e);
        }
    }
}
