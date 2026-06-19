import org.apache.poi.xwpf.usermodel.XWPFDocument;
import java.io.*;
import java.util.zip.*;

public class TestPOI {
    public static void main(String[] args) throws Exception {
        System.out.println("Reading template...");
        File template = new File("plantillas/INFORME_SUPERVISION_TEMPLATE.docx");
        File out = new File("test_out.docx");
        
        try (XWPFDocument doc = new XWPFDocument(new FileInputStream(template))) {
            doc.write(new FileOutputStream(out));
        }
        
        System.out.println("Extracting document.xml from POI output...");
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(out))) {
            ZipEntry entry = zis.getNextEntry();
            while (entry != null) {
                if (entry.getName().equals("word/document.xml")) {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    byte[] data = new byte[1024];
                    int nRead;
                    while ((nRead = zis.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, nRead);
                    }
                    buffer.flush();
                    String xml = new String(buffer.toByteArray(), "UTF-8");
                    if (xml.contains("${CONCEPTO_SUPERVISOR}")) {
                        System.out.println("SUCCESS: Exact string ${CONCEPTO_SUPERVISOR} found in POI output!");
                    } else {
                        System.out.println("FAILED: Exact string not found! POI split it!");
                        int idx = xml.indexOf("CONCEPTO_SUPERVISOR");
                        if (idx != -1) {
                            System.out.println("It was found as CONCEPTO_SUPERVISOR, surrounding context:");
                            System.out.println(xml.substring(Math.max(0, idx-50), Math.min(xml.length(), idx+50)));
                        } else {
                            System.out.println("Not even CONCEPTO_SUPERVISOR was found!");
                        }
                    }
                    break;
                }
                entry = zis.getNextEntry();
            }
        }
    }
}
