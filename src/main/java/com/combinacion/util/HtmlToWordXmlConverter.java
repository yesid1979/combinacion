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
            return "<w:p><w:pPr><w:jc w:val=\"both\"/><w:spacing w:before=\"0\" w:after=\"40\" w:line=\"240\" w:lineRule=\"auto\"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:cs=\"Arial\"/><w:sz w:val=\"22\"/><w:szCs w:val=\"22\"/></w:rPr><w:t></w:t></w:r></w:p>";
        }
        
        // Remove zero-width spaces or weird characters from Summernote
        html = html.replace("&nbsp;", " ").replace("\u200B", "");
        
        // Limpiar basura de portapapeles de Word (mso, clip_filelist, links locales, styles)
        html = html.replaceAll("(?i)<link[^>]*>", "");
        html = html.replaceAll("(?i)<style[^>]*>[\\s\\S]*?</style>", "");
        
        // Separar comas pegadas a texto/números sin espacio (ej. radicados "123,456,789") que crean tokens gigantescos
        // y fuerzan al motor de tablas de Word a encoger la columna de obligaciones
        html = html.replaceAll(",(?=[^\\s<])", ", ");
        
        // Limpiar párrafos vacíos o que solo contienen <br> o espacios repetidos (causantes de huecos blancos feos)
        html = html.replaceAll("(?i)<p[^>]*>(\\s*|<br\\s*/?>|&nbsp;|&#160;)*</p>", "");
        html = html.replaceAll("(?i)<div[^>]*>(\\s*|<br\\s*/?>|&nbsp;|&#160;)*</div>", "");
        html = html.replaceAll("(?i)(<br\\s*/?>\\s*){2,}", "<br/>");
        html = html.replaceAll("(?i)<br\\s*/?>([\\s\\u00A0]*[•●·▪▫○⁃\\u2022\\u25cf\\u00b7\\u25aa\\u25ab\\u25cb\\u2043\\?])", "</p><p>$1");
        
        org.jsoup.nodes.Document jsoupDoc = Jsoup.parseBodyFragment(html);
        
        // Eliminar elementos vacíos al inicio y al final
        while (!jsoupDoc.body().children().isEmpty()) {
            Element last = jsoupDoc.body().children().last();
            if (last.text().trim().isEmpty() && last.select("img, table").isEmpty()) {
                last.remove();
            } else {
                break;
            }
        }
        while (!jsoupDoc.body().children().isEmpty()) {
            Element first = jsoupDoc.body().children().first();
            if (first.text().trim().isEmpty() && first.select("img, table").isEmpty()) {
                first.remove();
            } else {
                break;
            }
        }
        
        // Normalizar listas ol, ul: asegurar que todo contenido directo esté envuelto en <li>
        for (Element list : jsoupDoc.select("ol, ul")) {
            boolean hasNonLi = false;
            for (Node child : list.childNodes()) {
                if (child instanceof Element) {
                    if (!((Element) child).tagName().equalsIgnoreCase("li")) {
                        hasNonLi = true;
                        break;
                    }
                } else if (child instanceof TextNode && !((TextNode) child).text().trim().isEmpty()) {
                    hasNonLi = true;
                    break;
                }
            }
            if (hasNonLi) {
                List<Node> currentLiNodes = new ArrayList<>();
                List<Node> originalChildren = new ArrayList<>(list.childNodes());
                for (Node child : originalChildren) {
                    if (child instanceof Element && ((Element) child).tagName().equalsIgnoreCase("li")) {
                        if (!currentLiNodes.isEmpty()) {
                            Element newLi = list.ownerDocument().createElement("li");
                            child.before(newLi);
                            for (Node n : currentLiNodes) {
                                newLi.appendChild(n);
                            }
                            currentLiNodes.clear();
                        }
                    } else {
                        if (child instanceof TextNode && ((TextNode) child).text().trim().isEmpty()) {
                            continue;
                        }
                        child.remove();
                        currentLiNodes.add(child);
                    }
                }
                if (!currentLiNodes.isEmpty()) {
                    Element newLi = list.ownerDocument().createElement("li");
                    list.appendChild(newLi);
                    for (Node n : currentLiNodes) {
                        newLi.appendChild(n);
                    }
                    currentLiNodes.clear();
                }
            }
        }
        
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
            return "<w:p><w:pPr><w:jc w:val=\"both\"/><w:spacing w:before=\"0\" w:after=\"40\" w:line=\"240\" w:lineRule=\"auto\"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:cs=\"Arial\"/><w:sz w:val=\"22\"/><w:szCs w:val=\"22\"/></w:rPr><w:t></w:t></w:r></w:p>";
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
                xml.append("<w:p><w:pPr><w:jc w:val=\"both\"/><w:spacing w:before=\"0\" w:after=\"40\" w:line=\"240\" w:lineRule=\"auto\"/></w:pPr><w:r><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:cs=\"Arial\"/><w:sz w:val=\"22\"/><w:szCs w:val=\"22\"/>");
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
                // Elemento hoja (párrafo o encabezado o texto plano)
                // Si está vacío (sin texto, sin imágenes, sin tablas), NO generar párrafo vacío
                if (el.text().trim().isEmpty() && el.select("img, table").isEmpty()) {
                    return;
                }

                String align = extractAlign(el, inheritedAlign);
                if (align.isEmpty()) {
                    align = "both"; // Justificado por defecto
                }
                boolean isBold = inheritedBold || tagName.startsWith("h") || tagName.equals("b") || tagName.equals("strong");

                String rawText = el.text().trim();
                boolean isBulletPara = startsWithBullet(rawText);

                xml.append("<w:p>");
                xml.append("<w:pPr>");
                xml.append("<w:jc w:val=\"").append(align).append("\"/>");
                xml.append("<w:spacing w:before=\"0\" w:after=\"40\" w:line=\"240\" w:lineRule=\"auto\"/>");
                if (isBulletPara) {
                    xml.append("<w:ind w:left=\"180\" w:hanging=\"180\"/>");
                    xml.append("<w:tabs><w:tab w:val=\"left\" w:pos=\"180\"/></w:tabs>");
                }
                xml.append("</w:pPr>");

                if (isBulletPara) {
                    xml.append("<w:r><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:cs=\"Arial\"/><w:sz w:val=\"22\"/><w:szCs w:val=\"22\"/></w:rPr><w:t>•</w:t></w:r><w:r><w:tab/></w:r>");
                    stripLeadingBullet(el);
                }
                processInlineChildren(el, xml, doc, isBold, false, false, 22);
                xml.append("</w:p>");
            }
        }
    }

    private static boolean startsWithBullet(String text) {
        if (text == null || text.isEmpty()) return false;
        String trimmed = text.replaceFirst("^[\\s\\u00A0]+", "");
        if (trimmed.isEmpty()) return false;
        char c = trimmed.charAt(0);
        return c == '•' || c == '●' || c == '·' || c == '▪' || c == '▫' || c == '○' || c == '⁃' ||
               c == '\u2022' || c == '\u25cf' || c == '\u00b7' || c == '\u25aa' || c == '\u25ab' || c == '\u25cb' || c == '\u2043' ||
               c == '?';
    }

    private static boolean stripLeadingBullet(Node node) {
        if (node instanceof TextNode) {
            TextNode tn = (TextNode) node;
            String val = tn.text();
            String trimmed = val.replaceFirst("^[\\s\\u00A0]*[•●·▪▫○⁃\\u2022\\u25cf\\u00b7\\u25aa\\u25ab\\u25cb\\u2043\\?][\\s\\u00A0]*", "");
            if (!trimmed.equals(val)) {
                tn.text(trimmed);
                return true;
            }
        } else if (node instanceof Element) {
            for (Node child : node.childNodes()) {
                if (stripLeadingBullet(child)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void flushInlineGroup(List<Node> group, StringBuilder xml, XWPFDocument doc, String align, boolean isBold) {
        boolean hasContent = false;
        StringBuilder sbText = new StringBuilder();
        for (Node n : group) {
            if (n instanceof TextNode) {
                String t = ((TextNode) n).text();
                sbText.append(t);
                if (!t.trim().isEmpty()) hasContent = true;
            } else if (n instanceof Element) {
                Element e = (Element) n;
                sbText.append(e.text());
                if (!e.text().trim().isEmpty() || !e.select("img").isEmpty()) hasContent = true;
            }
        }
        if (!hasContent) return;

        String rawTrimmed = sbText.toString().trim();
        boolean startsWithBullet = startsWithBullet(rawTrimmed);

        xml.append("<w:p>");
        xml.append("<w:pPr>");
        xml.append("<w:jc w:val=\"").append(align.isEmpty() ? "both" : align).append("\"/>");
        xml.append("<w:spacing w:before=\"0\" w:after=\"40\" w:line=\"240\" w:lineRule=\"auto\"/>");
        if (startsWithBullet) {
            xml.append("<w:ind w:left=\"180\" w:hanging=\"180\"/>");
            xml.append("<w:tabs><w:tab w:val=\"left\" w:pos=\"180\"/></w:tabs>");
        }
        xml.append("</w:pPr>");
        if (startsWithBullet) {
            xml.append("<w:r><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:cs=\"Arial\"/><w:sz w:val=\"22\"/><w:szCs w:val=\"22\"/></w:rPr><w:t>•</w:t></w:r><w:r><w:tab/></w:r>");
            for (Node n : group) {
                if (stripLeadingBullet(n)) break;
            }
        }
        for (Node n : group) {
            processInlineNode(n, xml, doc, isBold, false, false, 22);
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
        xml.append("<w:jc w:val=\"center\"/><w:tblW w:w=\"5000\" w:type=\"pct\"/><w:tblLayout w:type=\"fixed\"/>");
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

        int containerWidthDxa = (maxImageWidth <= 320.0) ? 5800 : 9900;
        int colWidthDxa = containerWidthDxa / maxCols;

        xml.append("<w:tblGrid>");
        for (int i = 0; i < maxCols; i++) {
            int w = (i == maxCols - 1) ? (containerWidthDxa - (colWidthDxa * (maxCols - 1))) : colWidthDxa;
            xml.append("<w:gridCol w:w=\"").append(w).append("\"/>");
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

                int cellPct = (5000 * span) / maxCols;
                xml.append("<w:tc><w:tcPr>");
                xml.append("<w:tcW w:w=\"").append(cellPct).append("\" w:type=\"pct\"/>");
                xml.append("<w:noWrap w:val=\"0\"/>");
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
                if (li.text().trim().isEmpty() && li.select("img, table").isEmpty()) {
                    continue;
                }
                String align = extractAlign(li, ulAlign);
                xml.append("<w:p><w:pPr>");
                xml.append("<w:jc w:val=\"").append(align.isEmpty() ? "both" : align).append("\"/>");
                xml.append("<w:spacing w:before=\"0\" w:after=\"40\" w:line=\"240\" w:lineRule=\"auto\"/>");
                xml.append("<w:ind w:left=\"180\" w:hanging=\"180\"/>");
                xml.append("<w:tabs><w:tab w:val=\"left\" w:pos=\"180\"/></w:tabs>");
                xml.append("</w:pPr>");
                
                xml.append("<w:r><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:cs=\"Arial\"/><w:sz w:val=\"22\"/><w:szCs w:val=\"22\"/></w:rPr><w:t>");
                if (tagName.equalsIgnoreCase("ol")) {
                    xml.append(counter).append(".");
                } else {
                    xml.append("•");
                }
                xml.append("</w:t></w:r><w:r><w:tab/></w:r>");
                if (tagName.equalsIgnoreCase("ol")) {
                    stripLeadingNumber(li, counter);
                } else {
                    stripLeadingBullet(li);
                }
                processInlineChildren(li, xml, doc, false, false, false, 22);
                xml.append("</w:p>");
                counter++;
            }
        }
    }

    private static boolean stripLeadingNumber(Node node, int counter) {
        if (node instanceof TextNode) {
            TextNode tn = (TextNode) node;
            String val = tn.text();
            String trimmed = val.replaceFirst("^[\\s\\u00A0]*([0-9]+|[a-zA-Z])[.)\\-][\\s\\u00A0]*", "");
            if (!trimmed.equals(val)) {
                tn.text(trimmed);
                return true;
            }
        } else if (node instanceof Element) {
            for (Node child : node.childNodes()) {
                if (stripLeadingNumber(child, counter)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void processInlineChildren(Node parent, StringBuilder xml, XWPFDocument doc, boolean bold, boolean italic, boolean underline, int fontSize) {
        for (Node child : parent.childNodes()) {
            processInlineNode(child, xml, doc, bold, italic, underline, fontSize);
        }
    }

    private static String wrapLongTokens(String text) {
        if (text == null || text.length() < 30) return text;
        String[] parts = text.split(" ");
        boolean changed = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(" ");
            String token = parts[i];
            if (token.length() > 30 && (token.startsWith("http://") || token.startsWith("https://") || token.contains("/") || token.contains("="))) {
                // Insert zero-width space \u200B after /, ?, &, =, _, -, . to allow Word to break lines cleanly
                String broken = token.replaceAll("([/?&=_\\-.])", "$1\u200B");
                sb.append(broken);
                changed = true;
            } else {
                sb.append(token);
            }
        }
        return changed ? sb.toString() : text;
    }

    private static void processInlineNode(Node node, StringBuilder xml, XWPFDocument doc, boolean bold, boolean italic, boolean underline, int fontSize) {
        if (node instanceof TextNode) {
            String text = ((TextNode) node).text();
            if (text.isEmpty()) return;
            text = wrapLongTokens(text);
            int sz = (fontSize > 0) ? fontSize : 22;
            xml.append("<w:r><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:cs=\"Arial\"/>");
            if (bold) xml.append("<w:b/>");
            if (italic) xml.append("<w:i/>");
            if (underline) xml.append("<w:u w:val=\"single\"/>");
            xml.append("<w:sz w:val=\"").append(sz).append("\"/><w:szCs w:val=\"").append(sz).append("\"/>");
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
}
