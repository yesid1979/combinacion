import java.sql.*;
public class TestDB {
    public static void main(String[] args) throws Exception {
        Class.forName("org.postgresql.Driver");
        Connection c = DriverManager.getConnection("jdbc:postgresql://10.30.80.53:5432/combinacion?options=-c%20client_encoding=UTF8", "adminjuridica", "Produccion2023*");
        ResultSet rs = c.createStatement().executeQuery("SELECT id, estado_radicacion, id_revisor_asignado FROM informes_supervision");
        while(rs.next()) {
            System.out.println(rs.getInt("id") + " - " + rs.getString("estado_radicacion") + " - Revisor: " + rs.getObject("id_revisor_asignado"));
        }
        c.close();
    }
}
