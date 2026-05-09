package nlu.fit.soft.gr5.precisionMail.util;

import java.sql.*;

public class DbUtil {

    private final static String URL = "jdbc:sqlite:precisionmail.db";

    public static Connection getConnect() throws SQLException {
        try {
            return DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.err.println("Connection failed");
            e.printStackTrace();
            throw new SQLException();
        }
    }

    static void main() throws SQLException {
        Connection conn = getConnect();
        if (conn != null) System.out.println("Ket noi thanh cong");
    }
}
