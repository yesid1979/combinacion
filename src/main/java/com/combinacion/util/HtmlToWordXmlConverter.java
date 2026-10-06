package com.combinacion.util;

import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class HtmlToWordXmlConverter {

    public static String convertHtmlToXml(String html, XWPFDocument doc) {
        return convertHtmlToXml(html, doc, 450.0);
    }

    public static String convertHtmlToXml(String html, XWPFDocument doc, double maxImageWidth) {
        if (html == null || html.trim().isEmpty()) {
            return "<w:p><w:r><w:t></w:t></w:r></w:p>";
        }
        
        // Limpiar espacios de más, saltos redundantes y párrafos vacíos
        html = cleanHtmlSpaces(html);
        if (html.isEmpty()) {
            return "<w:p><w:r><w:t></w:t></w:r></w:p>";
        }
        
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parseBodyFragment(html);
        StringBuilder xml = new StringBuilder();
        
        List<Node> inlineGroup = new ArrayList<>();
        for (Node child : jsoupDoc.body().childNodes()) {
            if (hasBlockDescendant(child) || isBlockNode(child)) {
                if (!inlineGroup.isEmpty()) {
                    flushInlineGroup(inlineGroup, xml, doc, "", false);
                    inlineGroup.clear();
                }
                processNodeAsBlock(child, xml, doc, 0, "", false, maxImageWidth);
            } else {
                inlineGroup.add(child);
            }
        }
        if (!inlineGroup.isEmpty()) {
            flushInlineGroup(inlineGroup, xml, doc, "", false);
            inlineGroup.clear();
        }
        
        if (xml.length() == 0) {
            return "<w:p><w:r><w:t></w:t></w:r></w:p>";
        }
        
        String res = xml.toString().trim();
        // Si termina en </w:tbl>, agregar un <w:p/> para cumplir estándar ECMA-376 dentro de celdas Word
        if (res.endsWith("</w:tbl>")) {
            res += "<w:p/>";
        }
        return res;
    }

    private static boolean hasBlockDescendant(Node node) {
        if (node instanceof Element) {
            Element el = (Element) node;
            String tag = el.tagName().toLowerCase();
            if (isBlockTag(tag)) {
                return true;
            }
            for (Node child : el.childNodes()) {
                if (hasBlockDescendant(child)) return true;
            }
        }
        return false;
    }

    private static boolean isBlockNode(Node node) {
        if (node instanceof Element) {
            return isBlockTag(((Element) node).tagName().toLowerCase());
        }
        return false;
    }

    private static boolean isBlockTag(String tag) {
        return tag.equals("table") || tag.equals("ul") || tag.equals("ol") || 
               tag.equals("p") || tag.equals("div") || tag.matches("^h[1-6]$") || 
               tag.equals("blockquote") || tag.equals("hr") || tag.equals("pre") ||
               tag.equals("img");
    }

    private static void processNodeAsBlock(Node node, StringBuilder xml, XWPFDocument doc, int listLevel, String inheritedAlign, boolean inheritedBold, double maxImageWidth) {
        if (node instanceof TextNode) {
            String text = ((TextNode) node).text().trim();
            if (!text.isEmpty()) {
                xml.append("<w:p><w:r><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:cs=\"Arial\"/>");
                if (inheritedBold) xml.append("<w:b/>");
                xml.append("</w:rPr><w:t xml:space=\"preserve\">")
                   .append(escapeXml(text))
                   .append("</w:t></w:r></w:p>");
            }
        } else if (node instanceof Element) {
            Element el = (Element) node;
            String tagName = el.tagName().toLowerCase();

            if (tagName.equals("table")) {
                processTable(el, xml, doc, inheritedBold, maxImageWidth);
            } else if (tagName.equals("img")) {
                processImage(el, xml, doc, maxImageWidth);
            } else if (tagName.equals("ul") || tagName.equals("ol")) {
                processList(el, xml, doc, inheritedAlign, tagName);
            } else if (hasBlockDescendant(el)) {
                String align = extractAlign(el, inheritedAlign);
                boolean isBold = inheritedBold || tagName.equals("b") || tagName.equals("strong") || 
                                 el.attr("style").toLowerCase().contains("font-weight: bold") ||
                                 el.attr("style").toLowerCase().contains("font-weight:bold");

                List<Node> inlineGroup = new ArrayList<>();
                for (Node child : el.childNodes()) {
                    if (hasBlockDescendant(child) || isBlockNode(child)) {
                        if (!inlineGroup.isEmpty()) {
                            flushInlineGroup(inlineGroup, xml, doc, align, isBold);
                            inlineGroup.clear();
                        }
                        processNodeAsBlock(child, xml, doc, listLevel, align, isBold, maxImageWidth);
                    } else {
                        inlineGroup.add(child);
                    }
                }
                if (!inlineGroup.isEmpty()) {
                    flushInlineGroup(inlineGroup, xml, doc, align, isBold);
                    inlineGroup.clear();
                }
            } else {
                // Si el elemento bloque no tiene contenido, no emitir párrafo vacío
                if (isBlockEmpty(el)) {
                    return;
                }
                // Elemento hoja (párrafo o encabezado o texto plano)
                String align = extractAlign(el, inheritedAlign);
                boolean isBold = inheritedBold || tagName.startsWith("h") || tagName.equals("b") || tagName.equals("strong");

                xml.append("<w:p>");
                if (!align.isEmpty()) {
                    xml.append("<w:pPr><w:jc w:val=\"").append(align).append("\"/></w:pPr>");
                }
                processInlineChildren(el, xml, doc, isBold, false, false, -1);
                xml.append("</w:p>");
            }
        }
    }

    private static void flushInlineGroup(List<Node> group, StringBuilder xml, XWPFDocument doc, String align, boolean isBold) {
        boolean hasContent = false;
        for (Node n : group) {
            if (n instanceof TextNode && !((TextNode) n).text().trim().isEmpty()) {
                hasContent = true;
                break;
            }
            if (n instanceof Element) {
                hasContent = true;
                break;
            }
        }
        if (!hasContent) return;

        xml.append("<w:p>");
        if (!align.isEmpty()) {
            xml.append("<w:pPr><w:jc w:val=\"").append(align).append("\"/></w:pPr>");
        }
        for (Node n : group) {
            processInlineNode(n, xml, doc, isBold, false, false, -1);
        }
        xml.append("</w:p>");
    }

    private static void processImage(Element el, StringBuilder xml, XWPFDocument doc, double maxWidth) {
        String src = el.attr("src");
        if (src == null || src.trim().isEmpty()) return;

        try {
            byte[] imgBytes = null;
            if (src.startsWith("data:image")) {
                String base64Data = src.substring(src.indexOf(",") + 1);
                imgBytes = Base64.getDecoder().decode(base64Data);
            } else if (src.contains("ImageServlet?id=")) {
                String fileId = src.split("id=")[1].split("&")[0];
                try (java.io.InputStream in = com.combinacion.services.GoogleDriveService.downloadFile(fileId)) {
                    java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
                    int nRead;
                    byte[] data = new byte[4096];
                    while ((nRead = in.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, nRead);
                    }
                    imgBytes = buffer.toByteArray();
                }
            }

            if (imgBytes != null) {
                int format = Document.PICTURE_TYPE_PNG;
                if (src.contains("jpeg") || src.contains("jpg")) format = Document.PICTURE_TYPE_JPEG;

                String rId = doc.addPictureData(imgBytes, format);

                int originalWidth = -1;
                int originalHeight = -1;

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
                } catch (Exception ignore) {}

                if (originalWidth <= 0 || originalHeight <= 0) {
                    try {
                        java.awt.image.BufferedImage bimg = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(imgBytes));
                        if (bimg != null) {
                            originalWidth = bimg.getWidth();
                            originalHeight = bimg.getHeight();
                        }
                    } catch (Exception ignore) {}
                }

                if (originalWidth <= 0) originalWidth = (int) maxWidth;
                if (originalHeight <= 0) originalHeight = (int) (maxWidth * 0.65);

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

                // La imagen se posiciona en su propio párrafo centrado con espaciado vertical
                xml.append("<w:p>")
                   .append("<w:pPr><w:jc w:val=\"center\"/><w:spacing w:before=\"120\" w:after=\"120\"/></w:pPr>")
                   .append("<w:r><w:drawing><wp:inline distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\" xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\">")
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
                   .append("</wp:inline></w:drawing></w:r>")
                   .append("</w:p>");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void processTable(Element el, StringBuilder xml, XWPFDocument doc, boolean inheritedBold, double maxImageWidth) {
        xml.append("<w:tbl>");
        xml.append("<w:tblPr>");
        xml.append("<w:tblBorders>")
           .append("<w:top w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/>")
           .append("<w:left w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/>")
           .append("<w:bottom w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/>")
           .append("<w:right w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/>")
           .append("<w:insideH w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/>")
           .append("<w:insideV w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"000000\"/>")
           .append("</w:tblBorders>");
        xml.append("<w:tblCellMar>")
           .append("<w:top w:w=\"80\" w:type=\"dxa\"/>")
           .append("<w:left w:w=\"120\" w:type=\"dxa\"/>")
           .append("<w:bottom w:w=\"80\" w:type=\"dxa\"/>")
           .append("<w:right w:w=\"120\" w:type=\"dxa\"/>")
           .append("</w:tblCellMar>");
        xml.append("<w:jc w:val=\"center\"/><w:tblLayout w:type=\"autofit\"/><w:tblW w:w=\"5000\" w:type=\"pct\"/>");
        xml.append("</w:tblPr>");

        // Calcular número de columnas teniendo en cuenta colspans
        int maxCols = 0;
        for (Element tr : el.select("tr")) {
            int rowCols = 0;
            for (Element cell : tr.children()) {
                if (cell.tagName().equalsIgnoreCase("td") || cell.tagName().equalsIgnoreCase("th")) {
                    int span = 1;
                    String cs = cell.attr("colspan");
                    if (!cs.isEmpty()) {
                        try { span = Math.max(1, Integer.parseInt(cs.replaceAll("[^0-9]", ""))); } catch (Exception ignore) {}
                    }
                    rowCols += span;
                }
            }
            if (rowCols > maxCols) maxCols = rowCols;
        }
        if (maxCols == 0) maxCols = 1;

        xml.append("<w:tblGrid>");
        int colWidth = 10000 / maxCols;
        for (int i = 0; i < maxCols; i++) {
            xml.append("<w:gridCol w:w=\"").append(colWidth).append("\"/>");
        }
        xml.append("</w:tblGrid>");

        for (Element tr : el.select("tr")) {
            xml.append("<w:tr><w:trPr><w:cantSplit/></w:trPr>");
            for (Element cell : tr.children()) {
                if (!cell.tagName().equalsIgnoreCase("td") && !cell.tagName().equalsIgnoreCase("th")) continue;

                boolean isHeader = cell.tagName().equalsIgnoreCase("th");
                int span = 1;
                String cs = cell.attr("colspan");
                if (!cs.isEmpty()) {
                    try { span = Math.max(1, Integer.parseInt(cs.replaceAll("[^0-9]", ""))); } catch (Exception ignore) {}
                }

                String bg = extractBgColor(cell);
                if (bg.isEmpty() && isHeader) bg = "F2F2F2";

                String align = extractAlign(cell, isHeader ? "center" : "");

                xml.append("<w:tc><w:tcPr><w:tcW w:w=\"0\" w:type=\"auto\"/>");
                if (span > 1) {
                    xml.append("<w:gridSpan w:val=\"").append(span).append("\"/>");
                }
                if (!bg.isEmpty()) {
                    xml.append("<w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"").append(bg).append("\"/>");
                }
                xml.append("<w:vAlign w:val=\"center\"/>");
                xml.append("</w:tcPr>");

                boolean hasBlock = false;
                for (Node cellChild : cell.childNodes()) {
                    if (hasBlockDescendant(cellChild)) {
                        processNodeAsBlock(cellChild, xml, doc, 0, align, isHeader || inheritedBold, maxImageWidth);
                        hasBlock = true;
                    }
                }
                if (!hasBlock) {
                    xml.append("<w:p>");
                    if (!align.isEmpty()) {
                        xml.append("<w:pPr><w:jc w:val=\"").append(align).append("\"/><w:spacing w:before=\"20\" w:after=\"20\"/></w:pPr>");
                    } else {
                        xml.append("<w:pPr><w:spacing w:before=\"20\" w:after=\"20\"/></w:pPr>");
                    }
                    processInlineChildren(cell, xml, doc, isHeader || inheritedBold, false, false, 18);
                    xml.append("</w:p>");
                }
                xml.append("</w:tc>");
            }
            xml.append("</w:tr>");
        }
        xml.append("</w:tbl>");
    }

    private static String extractBgColor(Element cell) {
        String bg = "";
        if (cell.hasAttr("bgcolor")) {
            bg = cell.attr("bgcolor").replace("#", "").trim();
        } else {
            String style = cell.attr("style").toLowerCase();
            if (style.contains("background-color:")) {
                String sub = style.split("background-color:")[1].split(";")[0].replace("#", "").trim();
                if (sub.matches("^[0-9a-fA-F]{6}$")) bg = sub.toUpperCase();
                else if (sub.equals("yellow")) bg = "FFFF00";
                else if (sub.equals("lightgray") || sub.equals("lightgrey")) bg = "D3D3D3";
            }
        }
        if (bg.length() == 6) return bg.toUpperCase();
        return "";
    }

    private static String extractAlign(Element el, String fallback) {
        String align = "";
        String style = el.attr("style").toLowerCase();
        String clazz = el.attr("class").toLowerCase();
        String alignAttr = el.attr("align").toLowerCase();

        if (style.contains("text-align: justify") || style.contains("text-align:justify") || clazz.contains("text-justify")) align = "both";
        else if (style.contains("text-align: center") || style.contains("text-align:center") || clazz.contains("text-center") || alignAttr.equals("center")) align = "center";
        else if (style.contains("text-align: right") || style.contains("text-align:right") || clazz.contains("text-right") || alignAttr.equals("right")) align = "right";
        else if (style.contains("text-align: left") || style.contains("text-align:left") || clazz.contains("text-left") || alignAttr.equals("left")) align = "left";

        return align.isEmpty() ? fallback : align;
    }

    private static void processList(Element el, StringBuilder xml, XWPFDocument doc, String inheritedAlign, String tagName) {
        String ulAlign = extractAlign(el, inheritedAlign);
        int counter = 1;
        for (Element li : el.children()) {
            if (li.tagName().equalsIgnoreCase("li")) {
                String align = extractAlign(li, ulAlign);
                xml.append("<w:p><w:pPr>");
                xml.append("<w:tabs><w:tab w:val=\"left\" w:pos=\"360\"/></w:tabs>");
                if (!align.isEmpty()) {
                    xml.append("<w:jc w:val=\"").append(align).append("\"/>");
                }
                xml.append("</w:pPr>");
                
                xml.append("<w:r><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:cs=\"Arial\"/></w:rPr><w:t>");
                if (tagName.equalsIgnoreCase("ol")) {
                    xml.append(counter).append(".");
                } else {
                    xml.append("•");
                }
                xml.append("</w:t></w:r><w:r><w:tab/></w:r>");
                processInlineChildren(li, xml, doc, false, false, false, -1);
                xml.append("</w:p>");
                counter++;
            }
        }
    }

    private static void processInlineChildren(Node parent, StringBuilder xml, XWPFDocument doc, boolean bold, boolean italic, boolean underline, int fontSize) {
        for (Node child : parent.childNodes()) {
            processInlineNode(child, xml, doc, bold, italic, underline, fontSize);
        }
    }

    private static void processInlineNode(Node node, StringBuilder xml, XWPFDocument doc, boolean bold, boolean italic, boolean underline, int fontSize) {
        if (node instanceof TextNode) {
            String text = ((TextNode) node).text();
            if (text.isEmpty()) return;
            xml.append("<w:r><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:cs=\"Arial\"/>");
            if (bold) xml.append("<w:b/>");
            if (italic) xml.append("<w:i/>");
            if (underline) xml.append("<w:u w:val=\"single\"/>");
            if (fontSize > 0) xml.append("<w:sz w:val=\"").append(fontSize).append("\"/>");
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
            
            for (Node child : el.childNodes()) {
                processInlineNode(child, xml, doc, bold, italic, underline, fontSize);
            }
        }
    }

    private static String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    public static String cleanHtmlSpaces(String html) {
        if (html == null || html.trim().isEmpty()) {
            return "";
        }

        // 1. Reemplazar caracteres invisibles y no separables
        html = html.replace("\u200B", "")
                   .replace("\uFEFF", "")
                   .replace("\u200C", "")
                   .replace("\u200D", "")
                   .replace("&nbsp;", " ")
                   .replace("\u00A0", " ");

        org.jsoup.nodes.Document doc = Jsoup.parseBodyFragment(html);
        Element body = doc.body();

        // 2. Limpiar espacios múltiples y saltos innecesarios dentro de nodos
        cleanNodes(body);

        // 3. Eliminar bloques vacíos redundantes (párrafos sin texto, imágenes ni tablas)
        cleanEmptyBlocks(body);

        return body.html();
    }

    private static void cleanNodes(Node parent) {
        List<Node> toRemove = new ArrayList<>();
        Node prevChild = null;

        for (Node child : new ArrayList<>(parent.childNodes())) {
            if (child instanceof TextNode) {
                TextNode tn = (TextNode) child;
                String text = tn.getWholeText();
                // Colapsar espacios múltiples horizontales a uno solo
                text = text.replaceAll("[ \\t\\u00A0]{2,}", " ");
                // Si es el primer inline de un bloque, recortar espacios iniciales
                if (isFirstInline(child)) {
                    text = text.replaceAll("^[ \\t\\u00A0]+", "");
                }
                // Si es el último inline de un bloque, recortar espacios finales
                if (isLastInline(child)) {
                    text = text.replaceAll("[ \\t\\u00A0]+$", "");
                }
                if (text.isEmpty()) {
                    toRemove.add(child);
                } else {
                    tn.text(text);
                }
            } else if (child instanceof Element) {
                Element el = (Element) child;
                String tag = el.tagName().toLowerCase();

                // Reducir etiquetas <br> consecutivas o <br> al inicio/final de un párrafo
                if (tag.equals("br")) {
                    if (isFirstInline(child) || isLastInline(child) || (prevChild != null && isBr(prevChild))) {
                        toRemove.add(child);
                    }
                } else {
                    cleanNodes(el);
                }
            }
            if (!toRemove.contains(child)) {
                prevChild = child;
            }
        }

        for (Node n : toRemove) {
            n.remove();
        }
    }

    private static boolean isBr(Node node) {
        return (node instanceof Element) && ((Element) node).tagName().equalsIgnoreCase("br");
    }

    private static boolean isFirstInline(Node node) {
        Node cur = node.previousSibling();
        while (cur != null) {
            if (cur instanceof TextNode && !((TextNode) cur).text().trim().isEmpty()) return false;
            if (cur instanceof Element && !((Element) cur).tagName().equalsIgnoreCase("br")) return false;
            cur = cur.previousSibling();
        }
        return true;
    }

    private static boolean isLastInline(Node node) {
        Node cur = node.nextSibling();
        while (cur != null) {
            if (cur instanceof TextNode && !((TextNode) cur).text().trim().isEmpty()) return false;
            if (cur instanceof Element && !((Element) cur).tagName().equalsIgnoreCase("br")) return false;
            cur = cur.nextSibling();
        }
        return true;
    }

    private static void cleanEmptyBlocks(Element root) {
        for (Element child : new ArrayList<>(root.children())) {
            String tag = child.tagName().toLowerCase();
            // Proteger estructura de tablas para no descuadrar celdas
            if (tag.equals("table") || tag.equals("tbody") || tag.equals("thead") || 
                tag.equals("tfoot") || tag.equals("tr") || tag.equals("td") || tag.equals("th")) {
                cleanEmptyBlocks(child);
                continue;
            }
            if (isBlockEmpty(child)) {
                child.remove();
            } else {
                cleanEmptyBlocks(child);
            }
        }
    }

    private static boolean isBlockEmpty(Element el) {
        if (!el.select("img, table, ul, ol, hr, iframe").isEmpty()) {
            return false;
        }
        String text = el.text().replace("\u00A0", " ").trim();
        return text.isEmpty();
    }
}
