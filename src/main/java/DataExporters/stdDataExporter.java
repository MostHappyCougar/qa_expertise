package DataExporters;

import Logger.stdLogger;
import Structures.SFunctionality;
import abs.ADataExporter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Реализация для экпортера данных о погруженности сотрудников в XLSX документ
 */
public class stdDataExporter extends ADataExporter
{
    private XSSFWorkbook workBook;
    private XSSFSheet lowLevelStatistics, highLevelStatistics;
    private FileOutputStream fileOut;
    private final HashSet<String> relevantMembers;
    public HashSet<String> getRelevantMembers() {return this.relevantMembers; }

    /**
     * Конструктор
     * @param relevantMembers ХэшСет сотрудников, участвующих в анализе погружения
     */
    public stdDataExporter(HashSet<String> relevantMembers)
    {
        this.relevantMembers = relevantMembers;
    }

    @Override
    public void exportData(ArrayList<SFunctionality> functionalArrayList) throws IOException
    {
        createWorkbook();
        createSheets();

        Row[] baseHeaderHigh = makeBaseHeader(this.highLevelStatistics);
        Row headerRowHigh = baseHeaderHigh[0];
        Row subHeaderRowHigh = baseHeaderHigh[1];

        Row[] baseHeaderLow = makeBaseHeader(this.lowLevelStatistics);
        Row headerRowLow = baseHeaderLow[0];
        Row subHeaderRowLow = baseHeaderLow[1];

        makeMembersHeader(this.highLevelStatistics, headerRowHigh, subHeaderRowHigh);
        makeMembersHeader(this.lowLevelStatistics, headerRowLow, subHeaderRowLow);

        stdLogger.log.info("Заполняем таблицу \"{}\"", this.highLevelStatistics.getSheetName());
        HashSet<SFunctionality> publishedHigh = new HashSet<>();
        recursivelyCreateRowsForFunctionalitiesWithStatistics(this.highLevelStatistics, headerRowHigh, functionalArrayList, publishedHigh, null, false);
        stdLogger.log.info("Таблица \"{}\" заполнена", this.highLevelStatistics.getSheetName());

        stdLogger.log.info("Заполняем таблицу \"{}\"", this.lowLevelStatistics.getSheetName());
        HashSet<SFunctionality> publishedLow = new HashSet<>();
        recursivelyCreateRowsForFunctionalitiesWithStatistics(this.lowLevelStatistics, headerRowLow, functionalArrayList, publishedLow, null, true);
        stdLogger.log.info("Таблица \"{}\" заполнена", this.lowLevelStatistics.getSheetName());

        makeFooterBasedOnHeader(this.highLevelStatistics);
        finalTable(this.lowLevelStatistics);

        this.lowLevelStatistics.createFreezePane(2, 2);
        this.highLevelStatistics.createFreezePane(2,2);

        this.workBook.write(fileOut);
        fileOut.close();
    }

    /**
     * Метод для создания футера таблицы на основе заголовка
     * @param sheet Таблица
     */
    private void makeFooterBasedOnHeader(XSSFSheet sheet)
    {
        Row footerRow = makeNextRow(sheet);
        Cell totalCaseAmountCell = footerRow.createCell(0);
        totalCaseAmountCell.setCellValue("Всего");
        XLSXTableStyler.cellStyler(totalCaseAmountCell, IndexedColors.YELLOW.index, false, HorizontalAlignment.LEFT, XLSXTableStyler.getStdHeaderStyle());

        sheet.getRow(1).forEach(cell ->
        {
            if (cell.getColumnIndex() > 0)
            {
                Cell sumCell = footerRow.createCell(cell.getColumnIndex());
                if (sumCell.getColumnIndex() % 2 == 0)
                    XLSXTableStyler.cellStyler(sumCell, IndexedColors.BLUE.index, false, HorizontalAlignment.RIGHT, XLSXTableStyler.getStdWithoutRight());
                else
                    XLSXTableStyler.cellStyler(sumCell, IndexedColors.BLUE.index, false, HorizontalAlignment.RIGHT, XLSXTableStyler.getStdWithoutLeft());

                Row rowOfFirstCellInRange = sheet.getRow(cell.getRowIndex() + 1);
                Row rowOfLastCellInRange = sheet.getRow(sumCell.getRowIndex() - 1);

                Cell startCellForSumRange = rowOfFirstCellInRange.getCell(cell.getColumnIndex());
                Cell endCellForSumRange = rowOfLastCellInRange.getCell(cell.getColumnIndex());

                sumCell.setCellFormula(String.format("SUM(%s:%s)", startCellForSumRange.getAddress(), endCellForSumRange.getAddress()));
            }
        });
    }

    /**
     * Форматирование футера
     * @param sheet
     */
    private void finalTable(XSSFSheet sheet)
    {
        Row footerRow = makeNextRow(sheet);
        sheet.getRow(1).forEach(cell ->
        {
            Cell c = footerRow.createCell(cell.getColumnIndex());
            XLSXTableStyler.cellStyler(c, XLSXTableStyler.getStdOnlyTop());
        });
    }

    /**
     * Создание книги
     * @throws FileNotFoundException
     */
    private void createWorkbook() throws FileNotFoundException
    {
        this.workBook = new XSSFWorkbook();
        File directory = new File("files");
        if(directory.mkdir())
        {
            stdLogger.log.info(String.format("Создана директория \"%s\" для сохранения результатов анализа погружения", directory));
            this.fileOut = new FileOutputStream("files/QAExpertiseStatistics.xlsx");
        }
        else
        {
            stdLogger.log.error(String.format("Для сохранения результатов анализа будет использована существующая директория \"%s\"", directory));
            this.fileOut = new FileOutputStream("files/QAExpertiseStatistics.xlsx");
        }
    }

    /**
     * Создание таблиц для низкоуровневой и высокоуровневой статистики
     */
    private void createSheets()
    {
        this.highLevelStatistics = this.workBook.createSheet("Высокоуровневая статистика");
        this.lowLevelStatistics = this.workBook.createSheet("Низкоуровневая статистика");
    }

    /**
     * Создание заголовка таблицы
     * @param sheet Лист документа
     * @return
     */
    private Row[] makeBaseHeader(XSSFSheet sheet)
    {
        stdLogger.log.info(String.format("Формируем заголовки таблицы \"%s\"", sheet.getSheetName()));
        Row header = sheet.createRow(0);
        Row subHeader = sheet.createRow(1);

        CellRangeAddress functionalRegion = new CellRangeAddress(0, 1, 0, 0);
        CellRangeAddress totalCasesCountRegion = new CellRangeAddress(0, 1, 1, 1);

        makeMergedRegion(sheet, header, 0,"Страницы/Функционал", functionalRegion);
        makeMergedRegion(sheet, header, 1,"Всего", totalCasesCountRegion);

        stdLogger.log.info(String.format("Заголовки таблицы \"%s\" сформированы", sheet.getSheetName()));

        return new Row[] {header, subHeader};
    }

    /**
     * Объединяем ячейки в заголовке
     * @param sheet Таблица
     * @param row Строка
     * @param cellNum Номер ячейки
     * @param cellValue Значение в ячейке
     * @param mergedRegion Область для объединения
     */
    private void makeMergedRegion(XSSFSheet sheet, Row row, Integer cellNum, String cellValue, CellRangeAddress mergedRegion)
    {
        Cell cell = row.createCell(cellNum);
        cell.setCellValue(cellValue);
        sheet.addMergedRegion(mergedRegion);
        XLSXTableStyler.cellStyler(cell, IndexedColors.LIGHT_BLUE.index, true, HorizontalAlignment.CENTER, VerticalAlignment.CENTER, XLSXTableStyler.getStdHeaderStyle());
        XLSXTableStyler.setRegionBorders(mergedRegion, XLSXTableStyler.getStdHeaderStyle(), sheet);
    }

    /**
     * Заполняем заголовок именами сотрудников, участвующих в анализе
     * @param sheet Таблица
     * @param headerRow Строка заголовка таблицы
     * @param subHeaderRow Строка подзаголовка таблицы
     */
    private void makeMembersHeader(XSSFSheet sheet, Row headerRow, Row subHeaderRow)
    {
        stdLogger.log.info(String.format("Записываем пользователей в заголовки таблицы \"%s\"", sheet.getSheetName()));
        this.relevantMembers.forEach((relevantMember) ->
        {
            Cell userNameHeaderCell = makeCellInHeader(headerRow);
            makeCellInHeader(headerRow);
            fillHeaderCell(sheet, userNameHeaderCell, relevantMember);
            makeSubHeader(subHeaderRow, userNameHeaderCell);
        });
        stdLogger.log.info(String.format("В заголовки таблицы \"%s\" записаны все релевантные пользователи", sheet.getSheetName()));
    }

    /**
     * Создаем ячейку в заголовке
     * @param headerRow Строка заголовка
     * @return
     */
    private Cell makeCellInHeader(Row headerRow)
    {
        return headerRow.createCell(headerRow.getLastCellNum());
    }

    /**
     * Заполнить заголовок
     * @param sheet Таблица
     * @param userNameHeaderCell Ячейка, куда будет записано имя сотрудника
     * @param userName Имя сотрудника
     */
    private void fillHeaderCell(XSSFSheet sheet, Cell userNameHeaderCell, String userName)
    {
        CellRangeAddress userNameInHeaderRegion = new CellRangeAddress(userNameHeaderCell.getRowIndex(), userNameHeaderCell.getRowIndex(), userNameHeaderCell.getColumnIndex(), userNameHeaderCell.getColumnIndex()+1);

        XLSXTableStyler.cellStyler(userNameHeaderCell, IndexedColors.LIGHT_BLUE.index, true, HorizontalAlignment.CENTER, XLSXTableStyler.getStdHeaderStyle());
        sheet.addMergedRegion(userNameInHeaderRegion);

        XLSXTableStyler.setRegionBorders(userNameInHeaderRegion, XLSXTableStyler.getStdHeaderStyle(), sheet);
        userNameHeaderCell.setCellValue(userName);
    }

    /**
     * Заполнить подзаголовок
     * @param row Строка, где будет подзаголовок
     * @param headerUser Ячейка, куда записано имя конкретного сотрудника
     */
    private void makeSubHeader(Row row, Cell headerUser)
    {
        Cell casesExecutedCell =  row.createCell(headerUser.getColumnIndex());
        casesExecutedCell.setCellValue("Пройдено");
        XLSXTableStyler.cellStyler(casesExecutedCell, IndexedColors.LIGHT_BLUE.index, true, HorizontalAlignment.CENTER, XLSXTableStyler.getStdHeaderStyle());

        Cell casesCreatedCell =  row.createCell(headerUser.getColumnIndex()+1);
        casesCreatedCell.setCellValue("Создано");
        XLSXTableStyler.cellStyler(casesCreatedCell, IndexedColors.LIGHT_BLUE.index, true, HorizontalAlignment.CENTER, XLSXTableStyler.getStdHeaderStyle());
    }

    /**
     *
     * @param sheet Таблица
     * @param header Заголовок
     * @param functionalArrayList Список функциональностей со статистикой по сотрудникам
     * @param published Куда записываем функциональность, уже отраженную в статистике
     * @param parentFunctionalFullID Полный идентификатор родительской функциональности. Если вызывается для всего дерева, начиная с корневого элемента, то установить как null
     * @param recursively Будет ли отражена статистика рекурсивно или только для корневых элементов
     */
    private void recursivelyCreateRowsForFunctionalitiesWithStatistics(XSSFSheet sheet, Row header, ArrayList<SFunctionality> functionalArrayList, HashSet<SFunctionality> published, String parentFunctionalFullID, Boolean recursively)
    {
        AtomicReference<Integer> currentFunctionalNumber = new AtomicReference<>(1);

        functionalArrayList.forEach((functionality) ->
        {
            String currentFunctionalFullID = parentFunctionalFullID == null ? String.format("%s.", currentFunctionalNumber.get()) : String.format("%s%s.", parentFunctionalFullID, currentFunctionalNumber.get());

            if (!published.contains(functionality))
            {
                Row functionalityRow = makeNextRow(sheet);
                Cell[] functionalityCells = makeFunctionalityCells(functionalityRow);
                fillFunctionalityCellByFunctionalityInfo(functionalityCells, currentFunctionalFullID, functionality, functionality.getActualCasesList().size());

                makeEmptyCells(header, functionalityRow);

                fillFunctionalityStatisticsByUsers(header, functionalityRow, functionality.getAllCasesExecutionsByMembers(), 0, XLSXTableStyler.getStdOnlyLeftBorders());
                fillFunctionalityStatisticsByUsers(header, functionalityRow, functionality.getAllCasesCreatedByMembers(), 1, XLSXTableStyler.getStdRowNameStyle());

                published.add(functionality);

                if (recursively && !functionality.getDaughterFunctionalities().isEmpty())
                    recursivelyCreateRowsForFunctionalitiesWithStatistics(sheet, header, new ArrayList<>(functionality.getDaughterFunctionalities()), published, currentFunctionalFullID, true);
            }
            currentFunctionalNumber.getAndSet(currentFunctionalNumber.get() + 1);
        });
    }

    /**
     * Создаем следующую строку
     * @param sheet Таблица
     * @return Row
     */
    private Row makeNextRow(XSSFSheet sheet)
    {
        return sheet.createRow(sheet.getLastRowNum()+1);
    }

    /**
     * Создать массив ячеек, относящихся к конкретной функциональности
     * @param functionalityRow Строка, созданная для конкретной функциональности
     * @return Cell[]
     */
    private Cell[] makeFunctionalityCells(Row functionalityRow)
    {
        return new Cell[] {functionalityRow.createCell(0), functionalityRow.createCell(1)};
    }

    /**
     * Заполнить ячейки таблицы данными конкретной функциональности
     * @param functionalityCells Массив ячеек для функциональности
     * @param ID Идентификатор функциональности
     * @param functionality Функциональность
     * @param casesForFunctionalityCount Количество тесткейсов, созданных для конкретной функциональности
     */
    private void fillFunctionalityCellByFunctionalityInfo(Cell[] functionalityCells, String ID, SFunctionality functionality, Integer casesForFunctionalityCount)
    {
        functionalityCells[0].setCellValue(String.format("%s - %s", ID, functionality.getFunctionalName()));
        XLSXTableStyler.cellStyler(functionalityCells[0], IndexedColors.LIGHT_ORANGE.index, false, HorizontalAlignment.LEFT, XLSXTableStyler.getStdRowNameStyle());

        functionalityCells[1].setCellValue(casesForFunctionalityCount);
        XLSXTableStyler.cellStyler(functionalityCells[1], IndexedColors.LIGHT_ORANGE.index, false, HorizontalAlignment.RIGHT, XLSXTableStyler.getStdRowNameStyle());
    }

    /**
     * Создать пустые ячейки
     * @param headerRow Строка заголовка
     * @param functionalityRow Строка конкретной функциональности
     */
    private void makeEmptyCells(Row headerRow, Row functionalityRow)
    {
        headerRow.forEach((member) ->
        {
            if (this.relevantMembers.contains(member.getStringCellValue()))
            {
                Cell casesExecutedEmptyCell = functionalityRow.createCell(member.getColumnIndex());
                XLSXTableStyler.cellStyler(casesExecutedEmptyCell, IndexedColors.LIGHT_YELLOW.index, false, HorizontalAlignment.RIGHT, XLSXTableStyler.getStdOnlyLeftBorders());

                Cell casesCreatedEmptyCell = functionalityRow.createCell(member.getColumnIndex() + 1);
                XLSXTableStyler.cellStyler(casesCreatedEmptyCell, IndexedColors.LIGHT_YELLOW.index, false, HorizontalAlignment.RIGHT, XLSXTableStyler.getStdRowNameStyle());
            }
        });
    }

    /**
     * Заполнить тело таблицы статистикой функционала
     * @param headerRow Строка заголовка
     * @param functionalityRow Строка конкретной функциональности
     * @param statisticsMap Статистика конкретной функциональности по сотрудникам
     * @param columnIndexOffset Смещение ячеек
     * @param borderStyles Стиль границ таблицы
     */
    private void fillFunctionalityStatisticsByUsers(Row headerRow, Row functionalityRow, Map<String, Integer> statisticsMap, Integer columnIndexOffset, BorderStyle[] borderStyles)
    {
        statisticsMap.forEach((user, statisticsValue) ->
                headerRow.forEach((headerUser) ->
                {
                    if (Objects.equals(headerUser.getStringCellValue(), user))
                    {
                        putCellValue(functionalityRow, headerUser.getColumnIndex() + columnIndexOffset, statisticsValue, borderStyles);
                    }
                }));
    }

    /**
     * Записать в конкретную ячейку данные по конкретному сотруднику в конкретном функционале
     * @param functionalityRow Строка для функциональности
     * @param cellUserNumber Номер ячейки для конкретного пользователя
     * @param content Данные для заполнения ячейки
     * @param borderStyles стиль границы ячейки
     */
    private void putCellValue(Row functionalityRow, Integer cellUserNumber, Integer content, BorderStyle[] borderStyles)
    {
        Cell contentCell = functionalityRow.createCell(cellUserNumber);
        contentCell.setCellValue(Objects.requireNonNullElse(content, 0));

        XLSXTableStyler.cellStyler(contentCell, IndexedColors.LIGHT_YELLOW.index, false, HorizontalAlignment.RIGHT, borderStyles);
    }
}
