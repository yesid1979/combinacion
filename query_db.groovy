@Grab('org.apache.poi:poi-ooxml:5.2.3')
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

def file = new File("src/main/webapp/plantillas/CUENTA_COBRO.xlsx")
def wb = new XSSFWorkbook(file)
def sheet = wb.getSheetAt(0)
for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
    def region = sheet.getMergedRegion(i)
    if (region.getFirstRow() < 10) {
        println("Merged region: " + region.formatAsString())
    }
}
