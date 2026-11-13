package ec.edu.sigh.modelo;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HexFormat;
import java.util.Optional;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class AutenticacionBD {

    private static final String SQL = """
            SELECT XEUSU_PASWD, XEUSU_SALT, XEEST_CODIGO, PEPER_CODIGO
            FROM XEUSU_USUAR WHERE XEUSU_LOGIN = ?
            """;

    public Optional<Usuario> autenticar(String nombre, String clave) {
        try (Connection cn = ConexionBD.abrir(); PreparedStatement ps = cn.prepareStatement(SQL)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                byte[] sal = HexFormat.of().parseHex(rs.getString("XEUSU_SALT"));
                byte[] hashGuardado = HexFormat.of().parseHex(rs.getString("XEUSU_PASWD"));
                byte[] derivada = derivar(clave, sal);
                Usuario u = new Usuario(nombre, rs.getString("XEEST_CODIGO").charAt(0),
                        rs.getString("PEPER_CODIGO"));
                return MessageDigest.isEqual(hashGuardado, derivada) && u.activo()
                        ? Optional.of(u) : Optional.empty();
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("No se pudo consultar la base de datos.", ex);
        }
    }

    private static byte[] derivar(String clave, byte[] sal) {
        PBEKeySpec spec = new PBEKeySpec(clave.toCharArray(), sal, 210_000, 256);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("No se pudo verificar la clave.", ex);
        } finally {
            spec.clearPassword();
        }
    }
}
