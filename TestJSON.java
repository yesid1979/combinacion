import com.combinacion.dao.InformeSupervisionDAO;
import com.combinacion.models.InformeSupervision;
import com.combinacion.models.Contrato;
import com.combinacion.services.InformeSupervisionService;
import java.util.List;

public class TestJSON {
    public static void main(String[] args) {
        InformeSupervisionService srv = new InformeSupervisionService();
        InformeSupervision info = srv.obtenerPorId(1);
        if (info != null) {
            System.out.println("Contrato ID: " + info.getContratoId());
            System.out.println("Concepto Supervisor: " + info.getConceptoSupervisor());
            if (info.getContratoId() != null) {
                Contrato c = srv.obtenerContrato(info.getContratoId());
                if (c != null) {
                    System.out.println("Actividades Contrato: " + c.getActividadesEntregables());
                } else {
                    System.out.println("Contrato no encontrado.");
                }
            }
        } else {
            System.out.println("Informe 1 no encontrado.");
        }
    }
}
