package ec.edu.sigh.modelo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Conexión única a la BD sigh_gr01, compartida por login y departamentos.
public final class ConexionBD {
    private static final String URL = "jdbc:mariadb://localhost:3306/sigh_gr01?useUnicode=true&characterEncoding=UTF-8";
    private static final String USUARIO = "root"; // XAMPP por defecto
    private static final String CLAVE = "";       // XAMPP: sin contraseña

    static {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private ConexionBD() {}

    public static Connection abrir() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }
}
