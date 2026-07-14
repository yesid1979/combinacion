import java.io.FileInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.HashSet;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class TestDocx {
    public static void main(String[] args) throws Exception {
        String path = "C:\\Users\\yesid.piedrahita\\Documents\\NetBeansProjects\\combinacion\\plantillas\\INFORME_GESTION_TEMPLATE.docx";
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(path))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals("word/document.xml")) {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    byte[] data = new byte[1024];
                    int count;
                    while ((count = zis.read(data, 0, 1024)) != -1) {
                        buffer.write(data, 0, count);
                    }
                    String xml = new String(buffer.toByteArray(), StandardCharsets.UTF_8);
                    
                    // Strip all tags to find the variables easily.
                    String cleanText = xml.replaceAll("<[^>]+>", "");
                    
                    Pattern p = Pattern.compile("\\$\\{[^}]+\\}");
                    Matcher m = p.matcher(cleanText);
                    Set<String> vars = new HashSet<>();
                    while (m.find()) {
                        vars.add(m.group());
                    }
                    
                    System.out.println("Variables encontradas:");
                    for (String v : vars) {
                        System.out.println(v);
                    }
                    break;
                }
            }
        }
    }
}
