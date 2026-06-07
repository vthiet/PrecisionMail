package nlu.fit.soft.gr5.precisionMail.uc04;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Uc0409ExportExcelTest {

    @Test
    void testCreateExcelWorkbook() {

        Workbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("Email Queue");

        assertNotNull(workbook);
        assertNotNull(sheet);

        assertEquals(
                "Email Queue",
                sheet.getSheetName()
        );
    }

    @Test
    void testCreateHeaderRow() {

        Workbook workbook = new XSSFWorkbook();

        Sheet sheet =
                workbook.createSheet("Email Queue");

        var header =
                sheet.createRow(0);

        header.createCell(0)
                .setCellValue("ID");

        header.createCell(1)
                .setCellValue("Sender");

        assertEquals(
                "ID",
                header.getCell(0).getStringCellValue()
        );

        assertEquals(
                "Sender",
                header.getCell(1).getStringCellValue()
        );
    }
}