import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.DriverManager;

public class FixDB {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/combinacion?useSSL=false&serverTimezone=America/Bogota", "root", "");
            PreparedStatement ps = conn.prepareStatement("UPDATE informes_supervision SET observaciones_revision = ? WHERE id = 6");
            ps.setString(1, "[19/07/2026 10:20 PM - ÓSCAR MAURICIO BETANCUR JIMÉNEZ] Por favor subir los anexos de las evidencias");
            ps.executeUpdate();
            System.out.println("Actualizado correctamente.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
