import java.sql.*;
public class TestDB {
    public static void main(String[] args) {
        try {
            Class.forName("org.postgresql.Driver");
            Connection c = DriverManager.getConnection("jdbc:postgresql://10.30.80.53:5432/combinacion?options=-c%20client_encoding=UTF8", "adminjuridica", "Produccion2023*");
            System.out.println("Connected!");
            Statement s = c.createStatement();
            ResultSet rs = s.executeQuery("SELECT table_name FROM information_schema.tables WHERE table_schema='public'");
            while(rs.next()) {
                System.out.println("Table: " + rs.getString(1));
            }
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
