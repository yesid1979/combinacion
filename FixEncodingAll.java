import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

public class FixEncodingAll {
    public static void main(String[] args) throws Exception {
        Path startDir = Paths.get("c:/Users/Soporte y Desarrollo/Documents/NetBeansProjects/combinacion/src/main/webapp");
        
        Files.walk(startDir)
             .filter(p -> p.toString().endsWith(".jsp"))
             .forEach(path -> {
                 try {
                     String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
                     
                     if (content.contains("Ã³") || content.contains("Ã¡") || content.contains("Ã©") || content.contains("Ã­") || content.contains("Ã±")) {
                         content = content.replace("Ã³", "ó")
                                          .replace("Ã¡", "á")
                                          .replace("Ã©", "é")
                                          .replace("Ã­", "í")
                                          .replace("Ãº", "ú")
                                          .replace("Ã±", "ñ")
                                          .replace("Ã\u0081", "Á")
                                          .replace("Ã\u0089", "É")
                                          .replace("Ã\u0093", "Ó");
                                          
                         Files.write(path, content.getBytes(StandardCharsets.UTF_8));
                         System.out.println("Fixed: " + path.getFileName());
                     }
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             });
    }
}
