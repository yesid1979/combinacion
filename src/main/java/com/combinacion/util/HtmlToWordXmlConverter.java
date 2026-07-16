package com.combinacion.util;

import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.Base64;
import java.util.UUID;

public class HtmlToWordXmlConverter {

    public static String convertHtmlToXml(String html, XWPFDocument doc) {
        if (html == null || html.trim().isEmpty()) {
            return "<w:p><w:r><w:t></w:t></w:r></w:p>";
        }
        
        // Remove zero-width spaces or weird characters from Summernote
        html = html.replace("&nbsp;", " ").replace("\u200B", "");
        
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parseBodyFragment(html);
        StringBuilder xml = new StringBuilder();
        
        for (Node child : jsoupDoc.body().childNodes()) {
            processNodeAsBlock(child, xml, doc, 0, "");
        }
        
        if (xml.length() == 0) {
            return "<w:p><w:r><w:t></w:t></w:r></w:p>";
        }
        return xml.toString();
    }
    
    private static void processNodeAsBlock(Node node, StringBuilder xml, XWPFDocument doc, int listLevel, String inheritedAlign) {
        if (node instanceof TextNode) {
            String text = ((TextNode) node).text().trim();
            if (!text.isEmpty()) {
                xml.append("<w:p><w:r><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:cs=\"Arial\"/></w:rPr><w:t>")
                   .append(escapeXml(text))
                   .append("</w:t></w:r></w:p>");
            }
        } else if (node instanceof Element) {
            Element el = (Element) node;
            String tagName = el.tagName().toLowerCase();
            
            if (tagName.equals("table")) {
                xml.append("<w:tbl>");
                xml.append("<w:tblPr>");
                xml.append("<w:tblBorders><w:top w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/><w:left w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/><w:bottom w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/><w:right w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/><w:insideH w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/><w:insideV w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/></w:tblBorders>");
                xml.append("<w:tblW w:w=\"5000\" w:type=\"pct\"/>");
                xml.append("</w:tblPr>");
                for (Element tr : el.select("tr")) {
                    xml.append("<w:tr>");
                    for (Element td : tr.select("td, th")) {
                        xml.append("<w:tc><w:tcPr><w:tcW w:w=\"1000\" w:type=\"pct\"/></w:tcPr>");
                        boolean hasBlock = false;
                        for (Node tdChild : td.childNodes()) {
                            if (isBlockNode(tdChild)) {
                                processNodeAsBlock(tdChild, xml, doc, 0, "");
                                hasBlock = true;
                            }
                        }
                        if (!hasBlock) {
                            xml.append("<w:p>");
                            processInlineChildren(td, xml, doc, false, false, false);
                            xml.append("</w:p>");
                        }
                        xml.append("</w:tc>");
                    }
                    xml.append("</w:tr>");
                }
                xml.append("</w:tbl>");
            } else if (tagName.equals("ul") || tagName.equals("ol")) {
                String ulAlign = inheritedAlign;
                String ulStyle = el.attr("style").toLowerCase();
                String ulClazz = el.attr("class").toLowerCase();
                if (ulStyle.contains("text-align: justify") || ulStyle.contains("text-align:justify") || ulClazz.contains("text-justify")) ulAlign = "both";
                else if (ulStyle.contains("text-align: center") || ulStyle.contains("text-align:center") || ulClazz.contains("text-center")) ulAlign = "center";
                else if (ulStyle.contains("text-align: right") || ulStyle.contains("text-align:right") || ulClazz.contains("text-right")) ulAlign = "right";
                else if (ulStyle.contains("text-align: left") || ulStyle.contains("text-align:left") || ulClazz.contains("text-left")) ulAlign = "left";

                int counter = 1;
                
                xml.append("<w:tbl>");
                xml.append("<w:tblPr>");
                xml.append("<w:tblW w:w=\"0\" w:type=\"auto\"/>");
                xml.append("<w:tblBorders>");
                xml.append("<w:top w:val=\"none\" w:sz=\"0\" w:space=\"0\" w:color=\"auto\"/>");
                xml.append("<w:left w:val=\"none\" w:sz=\"0\" w:space=\"0\" w:color=\"auto\"/>");
                xml.append("<w:bottom w:val=\"none\" w:sz=\"0\" w:space=\"0\" w:color=\"auto\"/>");
                xml.append("<w:right w:val=\"none\" w:sz=\"0\" w:space=\"0\" w:color=\"auto\"/>");
                xml.append("<w:insideH w:val=\"none\" w:sz=\"0\" w:space=\"0\" w:color=\"auto\"/>");
                xml.append("<w:insideV w:val=\"none\" w:sz=\"0\" w:space=\"0\" w:color=\"auto\"/>");
                xml.append("</w:tblBorders>");
                xml.append("<w:tblCellMar>");
                xml.append("<w:top w:w=\"0\" w:type=\"dxa\"/>");
                xml.append("<w:left w:w=\"108\" w:type=\"dxa\"/>");
                xml.append("<w:bottom w:w=\"0\" w:type=\"dxa\"/>");
                xml.append("<w:right w:w=\"108\" w:type=\"dxa\"/>");
                xml.append("</w:tblCellMar>");
                xml.append("</w:tblPr>");
                xml.append("<w:tblGrid><w:gridCol w:w=\"360\"/><w:gridCol w:w=\"8640\"/></w:tblGrid>");

                for (Element li : el.children()) {
                    if (li.tagName().equals("li")) {
                        String align = "";
                        String style = li.attr("style").toLowerCase();
                        String clazz = li.attr("class").toLowerCase();
                        if (style.contains("text-align: justify") || style.contains("text-align:justify") || clazz.contains("text-justify")) align = "both";
                        else if (style.contains("text-align: center") || style.contains("text-align:center") || clazz.contains("text-center")) align = "center";
                        else if (style.contains("text-align: right") || style.contains("text-align:right") || clazz.contains("text-right")) align = "right";
                        else if (style.contains("text-align: left") || style.contains("text-align:left") || clazz.contains("text-left")) align = "left";

                        if (align.isEmpty()) align = ulAlign;

                        xml.append("<w:tr>");
                        
                        // Celda de la viñeta/número
                        xml.append("<w:tc><w:tcPr><w:tcW w:w=\"360\" w:type=\"dxa\"/><w:vAlign w:val=\"top\"/></w:tcPr>");
                        xml.append("<w:p><w:pPr><w:jc w:val=\"right\"/></w:pPr>");
                        xml.append("<w:r><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:cs=\"Arial\"/></w:rPr><w:t>");
                        if (tagName.equals("ol")) {
                            xml.append(counter).append(".");
                        } else {
                            xml.append("•");
                        }
                        xml.append("</w:t></w:r><w:r><w:t xml:space=\"preserve\"> </w:t></w:r></w:p></w:tc>");
                        
                        // Celda del contenido
                        xml.append("<w:tc><w:tcPr><w:tcW w:w=\"8640\" w:type=\"dxa\"/><w:vAlign w:val=\"top\"/></w:tcPr>");
                        xml.append("<w:p><w:pPr>");
                        if (!align.isEmpty()) {
                            xml.append("<w:jc w:val=\"").append(align).append("\"/>");
                        }
                        xml.append("</w:pPr>");
                        
                        processInlineChildren(li, xml, doc, false, false, false);
                        xml.append("</w:p></w:tc></w:tr>");
                        counter++;
                    }
                }
                xml.append("</w:tbl><w:p/>");
            } else if (tagName.equals("p") || tagName.equals("div") || tagName.equals("h1") || tagName.equals("h2") || tagName.equals("h3") || tagName.equals("h4") || tagName.equals("h5") || tagName.equals("h6")) {
                boolean containsBlock = false;
                for (Node n : el.childNodes()) {
                    if (isBlockNode(n)) { containsBlock = true; break; }
                }
                
                if (containsBlock) {
                    String align = "";
                    String style = el.attr("style").toLowerCase();
                    String clazz = el.attr("class").toLowerCase();
                    if (style.contains("text-align: justify") || style.contains("text-align:justify") || clazz.contains("text-justify")) align = "both";
                    else if (style.contains("text-align: center") || style.contains("text-align:center") || clazz.contains("text-center")) align = "center";
                    else if (style.contains("text-align: right") || style.contains("text-align:right") || clazz.contains("text-right")) align = "right";
                    else if (style.contains("text-align: left") || style.contains("text-align:left") || clazz.contains("text-left")) align = "left";
                    if (align.isEmpty()) align = inheritedAlign;

                    for (Node n : el.childNodes()) {
                        processNodeAsBlock(n, xml, doc, listLevel, align);
                    }
                } else {
                    String align = "";
                    String style = el.attr("style").toLowerCase();
                    String clazz = el.attr("class").toLowerCase();
                    if (style.contains("text-align: justify") || style.contains("text-align:justify") || clazz.contains("text-justify")) align = "both";
                    else if (style.contains("text-align: center") || style.contains("text-align:center") || clazz.contains("text-center")) align = "center";
                    else if (style.contains("text-align: right") || style.contains("text-align:right") || clazz.contains("text-right")) align = "right";
                    else if (style.contains("text-align: left") || style.contains("text-align:left") || clazz.contains("text-left")) align = "left";
                    
                    if (align.isEmpty()) align = inheritedAlign;

                    xml.append("<w:p>");
                    if (!align.isEmpty()) {
                        xml.append("<w:pPr><w:jc w:val=\"").append(align).append("\"/></w:pPr>");
                    }
                    processInlineChildren(el, xml, doc, tagName.startsWith("h"), false, false);
                    xml.append("</w:p>");
                }
            } else {
                xml.append("<w:p>");
                processInlineNode(el, xml, doc, false, false, false);
                xml.append("</w:p>");
            }
        }
    }
    
    private static void processInlineChildren(Node parent, StringBuilder xml, XWPFDocument doc, boolean bold, boolean italic, boolean underline) {
        for (Node child : parent.childNodes()) {
            processInlineNode(child, xml, doc, bold, italic, underline);
        }
    }
    
    private static void processInlineNode(Node node, StringBuilder xml, XWPFDocument doc, boolean bold, boolean italic, boolean underline) {
        if (node instanceof TextNode) {
            String text = ((TextNode) node).text();
            if (text.isEmpty()) return;
            xml.append("<w:r><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:cs=\"Arial\"/>");
            if (bold) xml.append("<w:b/>");
            if (italic) xml.append("<w:i/>");
            if (underline) xml.append("<w:u w:val=\"single\"/>");
            xml.append("</w:rPr><w:t xml:space=\"preserve\">").append(escapeXml(text)).append("</w:t></w:r>");
        } else if (node instanceof Element) {
            Element el = (Element) node;
            String tag = el.tagName().toLowerCase();
            
            if (tag.equals("b") || tag.equals("strong")) bold = true;
            if (tag.equals("i") || tag.equals("em")) italic = true;
            if (tag.equals("u")) underline = true;
            if (tag.equals("br")) {
                xml.append("<w:r><w:br/></w:r>");
                return;
            }
            
            if (tag.equals("img")) {
                String src = el.attr("src");
                if (src.startsWith("data:image")) {
                    try {
                        String base64Data = src.substring(src.indexOf(",") + 1);
                        byte[] imgBytes = Base64.getDecoder().decode(base64Data);
                        
                        int format = Document.PICTURE_TYPE_PNG;
                        if (src.contains("jpeg") || src.contains("jpg")) format = Document.PICTURE_TYPE_JPEG;
                        
                        String rId = doc.addPictureData(imgBytes, format);
                        
                        int originalWidth = -1;
                        int originalHeight = -1;
                        
                        // Intentar leer estilos o atributos del HTML primero
                        try {
                            String wStr = el.attr("width");
                            if (!wStr.isEmpty()) originalWidth = Integer.parseInt(wStr.replaceAll("[^0-9]", ""));
                            String hStr = el.attr("height");
                            if (!hStr.isEmpty()) originalHeight = Integer.parseInt(hStr.replaceAll("[^0-9]", ""));
                            
                            String style = el.attr("style");
                            if (style != null && style.contains("width:")) {
                                String sw = style.split("width:")[1].split(";")[0].replaceAll("[^0-9]", "");
                                if (!sw.isEmpty()) originalWidth = Integer.parseInt(sw);
                            }
                        } catch (Exception e) {}
                        
                        // Si no pudimos leer del HTML o falta alguno, usar ImageIO
                        if (originalWidth <= 0 || originalHeight <= 0) {
                            try {
                                java.awt.image.BufferedImage bimg = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(imgBytes));
                                if (bimg != null) {
                                    originalWidth = bimg.getWidth();
                                    originalHeight = bimg.getHeight();
                                }
                            } catch (Exception e) {}
                        }
                        
                        // Fallback final si todo falla
                        if (originalWidth <= 0) originalWidth = 280;
                        if (originalHeight <= 0) originalHeight = 180;
                        
                        double maxWidth = 280.0; // Max width for Word cell (reduced to fit perfectly in 60% column)
                        long widthPx = originalWidth;
                        long heightPx = originalHeight;
                        
                        if (originalWidth > maxWidth) {
                            double ratio = maxWidth / originalWidth;
                            widthPx = (long) maxWidth;
                            heightPx = (long) (originalHeight * ratio);
                        }
                        
                        long widthEmu = widthPx * 9525;
                        long heightEmu = heightPx * 9525; 
                        
                        int id = (int) (Math.random() * 100000);
                        
                        xml.append("<w:r><w:drawing><wp:inline distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\" xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\">")
                           .append("<wp:extent cx=\"").append(widthEmu).append("\" cy=\"").append(heightEmu).append("\"/>")
                           .append("<wp:effectExtent l=\"0\" t=\"0\" r=\"0\" b=\"0\"/>")
                           .append("<wp:docPr id=\"").append(id).append("\" name=\"Picture ").append(id).append("\"/>")
                           .append("<wp:cNvGraphicFramePr><a:graphicFrameLocks xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" noChangeAspect=\"1\"/></wp:cNvGraphicFramePr>")
                           .append("<a:graphic xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\">")
                           .append("<a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">")
                           .append("<pic:pic xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\">")
                           .append("<pic:nvPicPr><pic:cNvPr id=\"").append(id).append("\" name=\"img.png\"/><pic:cNvPicPr/></pic:nvPicPr>")
                           .append("<pic:blipFill><a:blip r:embed=\"").append(rId).append("\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill>")
                           .append("<pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\"").append(widthEmu).append("\" cy=\"").append(heightEmu).append("\"/></a:xfrm>")
                           .append("<a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></pic:spPr>")
                           .append("</pic:pic></a:graphicData></a:graphic>")
                           .append("</wp:inline></w:drawing></w:r>");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                processInlineChildren(el, xml, doc, bold, italic, underline);
            }
        }
    }
    
    private static boolean isBlockNode(Node node) {
        if (node instanceof Element) {
            String tag = ((Element) node).tagName().toLowerCase();
            return tag.equals("p") || tag.equals("div") || tag.equals("table") || tag.equals("ul") || tag.equals("ol") || tag.equals("h1") || tag.equals("h2") || tag.equals("h3");
        }
        return false;
    }
    
    private static String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
