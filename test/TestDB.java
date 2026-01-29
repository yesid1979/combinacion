
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestDB {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://10.30.80.53:5432/combinacion";
        String user = "adminjuridica";
        String password = "Produccion2023*";

        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("Connecting to " + url + " with user " + user);
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connection SUCCESS!");
            conn.close();
        } catch (ClassNotFoundException e) {
            System.out.println("Driver NOT FOUND: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Connection FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
