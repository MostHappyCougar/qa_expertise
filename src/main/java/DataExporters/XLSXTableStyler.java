package DataExporters;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;

public class XLSXTableStyler
{

    private static final BorderStyle[] stdHeaderStyle = new BorderStyle[]{BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN};
    public static BorderStyle[] getStdHeaderStyle()
    {
        return stdHeaderStyle;
    }

    private static final BorderStyle[] stdRowNameStyle = new BorderStyle[]{BorderStyle.NONE, BorderStyle.THIN, BorderStyle.NONE, BorderStyle.NONE};
    public static BorderStyle[] getStdRowNameStyle()
    {
        return stdRowNameStyle;
    }

    private static final BorderStyle[] stdNoneBorders = new BorderStyle[]{BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE};
    public static BorderStyle[] getStdNoneBorder()
    {
        return stdNoneBorders;
    }

    private static final BorderStyle[] stdOnlyLeftBorders = new BorderStyle[]{BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.THIN};
    public static BorderStyle[] getStdOnlyLeftBorders()
    {
        return stdOnlyLeftBorders;
    }

    private static final BorderStyle[] stdWithoutRight = new BorderStyle[]{BorderStyle.THIN, BorderStyle.NONE, BorderStyle.THIN, BorderStyle.THIN};
    public static BorderStyle[] getStdWithoutRight()
    {
        return stdWithoutRight;
    }

    private static final BorderStyle[] stdWithoutLeft = new BorderStyle[]{BorderStyle.THIN, BorderStyle.THIN, BorderStyle.THIN, BorderStyle.NONE};
    public static BorderStyle[] getStdWithoutLeft()
    {
        return stdWithoutLeft;
    }

    private static final BorderStyle[] stdOnlyTop = new BorderStyle[]{BorderStyle.THIN, BorderStyle.NONE, BorderStyle.NONE, BorderStyle.NONE};
    public static BorderStyle[] getStdOnlyTop()
    {
        return stdOnlyTop;
    }

    public static void cellStyler(Cell cell, Short colorCode, Boolean boldFont, HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment, BorderStyle[] borderStyle)
    {
        Sheet sheet = cell.getSheet();
        Workbook wb = sheet.getWorkbook();

        Font font = wb.createFont();
        CellStyle cs = wb.createCellStyle();

        font.setBold(boldFont);

        cs.setFillForegroundColor(colorCode);
        cs.setFillPattern(FillPatternType.NO_FILL);
        cs.setFont(font);

        cs.setBorderTop(borderStyle[0]);
        cs.setBorderRight(borderStyle[1]);
        cs.setBorderBottom(borderStyle[2]);
        cs.setBorderLeft(borderStyle[3]);

        cs.setAlignment(horizontalAlignment);
        cs.setVerticalAlignment(verticalAlignment);

        sheet.autoSizeColumn(cell.getColumnIndex(), true);

        cell.setCellStyle(cs);
    }

    public static void cellStyler(Cell cell, BorderStyle[] borderStyles)
    {
        cellStyler(cell, IndexedColors.BLUE.index, false, HorizontalAlignment.LEFT, VerticalAlignment.BOTTOM, borderStyles);
    }

    public static void cellStyler(Cell cell, Short colorCode, Boolean boldFont, HorizontalAlignment horizontalAlignment, BorderStyle[] borderStyle)
    {
        cellStyler(cell, colorCode, boldFont, horizontalAlignment, VerticalAlignment.BOTTOM, borderStyle);
    }

    public static void setRegionBorders (CellRangeAddress region, BorderStyle[] borderStyle, Sheet sheet)
    {
        RegionUtil.setBorderTop(borderStyle[0], region, sheet);
        RegionUtil.setBorderRight(borderStyle[1], region, sheet);
        RegionUtil.setBorderBottom(borderStyle[2], region, sheet);
        RegionUtil.setBorderRight(borderStyle[3], region, sheet);
    }
}
