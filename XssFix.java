import java.nio.file.*;
import java.util.regex.*;
import java.io.IOException;

public class XssFix {
    public static void main(String[] args) throws IOException {
        String dir = "src/main/webapp";
        Pattern pattern = Pattern.compile("value=\"\\$\\{(.+?)\\}\"");
        
        Files.walk(Paths.get(dir))
            .filter(p -> p.toString().endsWith(".jsp") && !p.toString().contains("form_supervision.jsp") && !p.toString().contains("form_verbo.jsp") && !p.toString().contains("ver_presupuesto.jsp"))
            .forEach(p -> {
                try {
                    String content = new String(Files.readAllBytes(p));
                    if (!content.contains("http://java.sun.com/jsp/jstl/core")) {
                        return; // Skip if no JSTL core
                    }
                    String newContent = pattern.matcher(content).replaceAll("value=\"<c:out value='\\${$1}' />\"");
                    if (!content.equals(newContent)) {
                        Files.write(p, newContent.getBytes());
                        System.out.println("Fixed: " + p);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
    }
}
