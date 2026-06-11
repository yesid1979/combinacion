import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckDB {
    public static void main(String[] args) {
        try {
            Connection conn = com.combinacion.util.DBConnection.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id, contrato_id, concepto_supervisor FROM informes_supervision ORDER BY id DESC LIMIT 5");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") + 
                                   " | Contrato: " + rs.getInt("contrato_id") + 
                                   " | Concepto: [" + rs.getString("concepto_supervisor") + "]");
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
