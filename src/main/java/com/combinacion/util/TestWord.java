package com.combinacion.util;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import java.io.FileOutputStream;
import java.io.File;

public class TestWord {
    public static void main(String[] args) throws Exception {
        XWPFDocument doc = new XWPFDocument();
        String html = "<img src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=\">";
        
        String xml = HtmlToWordXmlConverter.convertHtmlToXml(html, doc);
        System.out.println("Generated XML:\n" + xml);
        
        try (FileOutputStream out = new FileOutputStream(new File("test_output.docx"))) {
            doc.write(out);
        }
        System.out.println("Done.");
    }
}
