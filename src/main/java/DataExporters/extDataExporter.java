package DataExporters;

import BusFactorProcessor.stdBusFactorProcessor;
import Logger.stdLogger;
import Structures.SFunctionality;
import Structures.STestCase;
import j2html.attributes.Attr;
import j2html.attributes.Attribute;
import j2html.tags.DomContent;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import static j2html.TagCreator.*;

public class extDataExporter extends stdDataExporter
{
    private Integer functionalID = 1;
    private DomContent statisticsTable = null;
    private Integer allCasesCount = 0;
    private Integer sumOfNeverExecutedCases = 0;
    private Integer sumOfCasesWithoutAuthor = 0;
    private Integer sumOfOutdatedCases = 0;
    private Map<String, Integer> sumOfExecutedCases, sumOfCreatedCases;
    private DomContent neverExecutedCases = null;
    private DomContent casesWithoutOwner = null;
    private DomContent outdatedCases = null;
    private final String tmsAddress;
    private final String projectId;

    private final String dataCellIntegerClass = "dataCellInteger";
    private final Attribute footerDataCellIntegerClass = new Attribute(Attr.CLASS, "footerDataCellInteger");
    
    private final Integer percentageThreshold;

    public extDataExporter(HashSet<String> relevantMembers, String tmsAddress, String projectId)
    {
        super(relevantMembers);
        this.tmsAddress = tmsAddress;
        this.projectId = projectId;
        this.percentageThreshold = 0;
    }
    
    public extDataExporter(HashSet<String> relevantMembers, String tmsAddress, String projectId, Integer percentageThreshold)
    {
        super(relevantMembers);
        this.tmsAddress = tmsAddress;
        this.projectId = projectId;
        this.percentageThreshold = percentageThreshold;
    }

    @Override
    public void exportData(ArrayList<SFunctionality> functionalityArrayList) throws IOException
    {
        super.exportData(functionalityArrayList);

        stdBusFactorProcessor marker = new stdBusFactorProcessor(getRelevantMembers(), this.percentageThreshold);
        marker.fillRelevantExpertsCountForEachFunctionality(functionalityArrayList);

        saveHTMLFile(buildHTMSString(functionalityArrayList));
    }

    private String buildHTMSString(ArrayList<SFunctionality> functionalityArrayList) throws IOException {

        String neverExecutedCasesLogMessageTemplate = "%s: Исполнители %d кейсов не найдены. Вероятно, эти кейсы ниразу не были пройдены";
        String casesWithoutAuthorLogMessageTemplate = "%s: Автор %d кейсов не определен. Вероятно, он не указан в ТестОпс";
        String outdatedCasesLogTemplate = "%s: %d неактуальных кейсов будет проигнорировано при анализе погружения";

        functionalityArrayList.forEach(functionality ->
        {
            recursivelyFullFillTable(functionality, null);

            if (functionality.getParentFunctionality() == null)
            {
                this.sumOfNeverExecutedCases = this.sumOfNeverExecutedCases + functionality.getNeverExecutedCases().size();
                this.sumOfCasesWithoutAuthor = this.sumOfCasesWithoutAuthor + functionality.getCasesWithoutAutor().size();
                this.sumOfOutdatedCases = this.sumOfOutdatedCases + functionality.getOutdatedCases().size();
            }

            try
            {
                this.neverExecutedCases = join(this.neverExecutedCases, makeBadCasesStatistics(functionality, functionality.getClass().getMethod("getNeverExecutedCases"), neverExecutedCasesLogMessageTemplate, "ne", String.format("ne_f_%d", functionality.hashCode()), "neverExecuted"));
                this.casesWithoutOwner = join(this.casesWithoutOwner, makeBadCasesStatistics(functionality, functionality.getClass().getMethod("getCasesWithoutAutor"), casesWithoutAuthorLogMessageTemplate, "wa", String.format("wa_f_%d", functionality.hashCode()), "withoutOwner"));
                this.outdatedCases = join(this.outdatedCases, makeBadCasesStatistics(functionality, functionality.getClass().getMethod("getOutdatedCases"), outdatedCasesLogTemplate, "ou", String.format("ou_f_%d", functionality.hashCode()), "outdatedCases"));
            } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e)
            {
                throw new RuntimeException(e);
            }
        });

        Attribute lang = new Attribute(Attr.LANG, "en");
        Attribute charset = new Attribute(Attr.CHARSET, "UTF-8");
        Attribute tableClass = new Attribute(Attr.CLASS, "tree table table-hover table-sm");

        DomContent attentionHeader = text("Для информации");
        DomContent neverExecutedHeader = join(text("Не пройденные кейсы"), p().with(rawHtml("&nbsp")), span(String.valueOf(this.sumOfNeverExecutedCases)).attr("class", "badge bg-secondary"));
        DomContent withoutAuthorHeader = join(text("Кейсы без автора"), p().with(rawHtml("&nbsp")), span(String.valueOf(this.sumOfCasesWithoutAuthor)).attr("class", "badge bg-secondary"));
        DomContent outdatedCasesHeader = join(text("Неактуальные кейсы"), p().with(rawHtml("&nbsp")), span(String.valueOf(this.sumOfOutdatedCases)).attr("class", "badge bg-secondary"));

        return html(
                head(meta().attr(charset),
                        title("Статистика погружения"),
                        style(getScriptString("jquery.treegrid.css")+
                                getScriptString("bootstrap/css/bootstrap.css")+
                                getScriptString("styles.css")
                        )),
                body(
                    div(
                        div(
                            script(getScriptString("bootstrap/js/bootstrap.bundle.js")),
                            script(getScriptString("jquery-1.12.0.min.js")),
                            script(getScriptString("jquery.treegrid.min.js")),
                            table(thead(buildHeader(), buildSubHeader()).attr("class", "table-light"),
                            tbody(this.statisticsTable, tr()),
                            tfoot(tr(td("Всего"), td(""), td(String.valueOf(this.allCasesCount)).attr(this.footerDataCellIntegerClass), fillStatisticsForEachRelevantMember(0, this.sumOfCreatedCases, "", this.sumOfExecutedCases, "", true))).attr("class", "foot")).attr(tableClass)
                        ).attr("class", "p-1 border border-3 rounded-1"),
                        script("$('.tree').treegrid();"),
                        script("var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle=\"tooltip\"]'))\nvar tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {return new bootstrap.Tooltip(tooltipTriggerEl)})"),
                            this.sumOfCasesWithoutAuthor + this.sumOfNeverExecutedCases + this.sumOfOutdatedCases > 0 ? div(
                                 this.sumOfCasesWithoutAuthor + this.sumOfNeverExecutedCases + this.sumOfOutdatedCases > 0 ? div(
                                    div(attentionHeader).attr("class", "card-header bg-info").attr("style", "grid-row: 1"),
                                    div(
                                        div(
                                            this.sumOfNeverExecutedCases > 0 ? makeAccordionElement(neverExecutedHeader, div(this.neverExecutedCases).attr("class", "accordion accordion-flush").attr("id", "neverExecuted"), "nev_ex", "nev_ex_collapse", "badCases") : null,
                                            this.sumOfCasesWithoutAuthor > 0 ? makeAccordionElement(withoutAuthorHeader, div(this.casesWithoutOwner).attr("class", "accordion accordion-flush").attr("id", "withoutOwner"), "w_a", "w_a_collapse", "badCases") : null,
                                            this.sumOfOutdatedCases > 0 ? makeAccordionElement(outdatedCasesHeader, div(this.outdatedCases).attr("class", "accordion accordion-flush").attr("id", "outdatedCases"), "ou", "ou_collapse", "badCases") : null
                                        ).attr("class", "accordion accordion-flush").attr("id", "badCases")
                                    ).attr("class", "card-body")
                                ).attr("class", "card") : null
                            ).attr("class", "p-1 border border-3 rounded-1") : null
                    ).attr("class", "d-grid gap-2 p-2")
                )).attr(lang).renderFormatted();
    }

    private void recursivelyFullFillTable(SFunctionality functionality, Integer parent)
    {
        final String errorColor = "bg-danger";
        final String warningColor = "bg-warning";
        final String neutralColor = "";

        Attribute rowAttribute;

        Integer currentFunctionalID = this.functionalID;

        String legendRowAttributeCasesCount = returnColorAttributeBasedOnExpertsCount(functionality.getRelevantExpertsCount(), errorColor, warningColor, neutralColor);
        String legendRowAttributeExecuted = returnColorAttributeBasedOnExpertsCount(functionality.getRelevantExpertsCount(), errorColor, warningColor, warningColor);
        String legendRowAttributeCreated = returnColorAttributeBasedOnExpertsCount(functionality.getRelevantExpertsCount(), errorColor, neutralColor, neutralColor);


        if (parent == null)
        {
            rowAttribute = new Attribute(Attr.CLASS, String.format("treegrid-%s", this.functionalID));

            this.allCasesCount = this.allCasesCount + functionality.getActualCasesList().size();

            if (this.sumOfExecutedCases != null)
                functionality.getAllCasesExecutionsByMembers().forEach((user, stats) -> this.sumOfExecutedCases.merge(user, stats, Integer::sum));
            else
                this.sumOfExecutedCases = functionality.getAllCasesExecutionsByMembers();

            if(this.sumOfCreatedCases != null)
                functionality.getAllCasesCreatedByMembers().forEach((user, stats) -> this.sumOfCreatedCases.merge(user, stats, Integer::sum));
            else
                this.sumOfCreatedCases = functionality.getAllCasesCreatedByMembers();
        }
        else
            rowAttribute = new Attribute(Attr.CLASS, String.format("treegrid-%s treegrid-parent-%s", this.functionalID, parent));

        DomContent functionalityRow = null;

        if (!functionality.getActualCasesList().isEmpty())
        {
            functionalityRow = tr(join(functionalityRow, td(text(functionality.getFunctionalName())), td(functionality.getRelevantExpertsCount().toString()).attr("class", String.format("%s %s", this.dataCellIntegerClass, legendRowAttributeCasesCount)),
                    join(td(String.valueOf(functionality.getActualCasesList().size())).attr("class", String.format("%s %s", this.dataCellIntegerClass, legendRowAttributeCasesCount)),
                            fillStatisticsForEachRelevantMember(functionality.getActualCasesList().size(), functionality.getAllCasesCreatedByMembers(this.percentageThreshold), legendRowAttributeCreated, functionality.getAllCasesExecutionsByMembers(this.percentageThreshold), legendRowAttributeExecuted, false)))).attr(rowAttribute);
            this.functionalID++;
            this.statisticsTable = join(this.statisticsTable, functionalityRow);
        }

        if (!functionality.getDaughterFunctionalities().isEmpty())
            functionality.getDaughterFunctionalities().forEach(daughterFunctionality -> recursivelyFullFillTable(daughterFunctionality, currentFunctionalID));
    }

    private String returnColorAttributeBasedOnExpertsCount(Integer expertsCount, String noExpertsColor, String fewExpertsColor, String normalExpertsCountlColor)
    {
        if (expertsCount == 0)
            return noExpertsColor;
        else if (expertsCount == 1)
            return fewExpertsColor;
        else
            return normalExpertsCountlColor;
    }

    private DomContent fillStatisticsForEachRelevantMember(Integer casesCount, Map<String, Integer> created, String createdLegendAttribute, Map<String, Integer> executed, String executedLegendAttribute, Boolean footer)
    {
        return join(each(getRelevantMembers(), member -> join(fillStatisticsCell(casesCount, member, executed, footer, executedLegendAttribute), fillStatisticsCell(casesCount, member, created, footer, createdLegendAttribute))));
    }

    private DomContent fillStatisticsCell(Integer casesCount, String name, Map<String, Integer> data, Boolean footer, String legendRowAttribute)
    {
        if (data.containsKey(name))
            if (footer)
                return td(String.valueOf(data.get(name))).attr(this.footerDataCellIntegerClass);
            else
            {
                float percentage = data.get(name).floatValue() / casesCount.floatValue() * 100;
                return td(div(a(text(String.format("%s %%", (int) percentage))).attr("href", "#").attr("data-bs-toggle", "tooltip").attr("data-bs-placement", "auto").attr("title", String.format("Кейсов: %s", (data.get(name).toString())))).attr("class", "static")).attr("class", this.dataCellIntegerClass);
            }
        else
        {
            if (footer)
                return td().attr(this.footerDataCellIntegerClass);
            else
                return td().attr("class", String.format("%s %s", this.dataCellIntegerClass, legendRowAttribute));
        }
    }

    private String getScriptString(String scriptName) throws IOException
    {
        String scriptsStorage = "src/main/resources/scripts";
        return Files.readString(Path.of(String.format(scriptsStorage + "/%s", scriptName)));
    }

    private DomContent buildHeader()
    {
        Attribute colspan = new Attribute(Attr.COLSPAN, "2");
        Attribute rowspan = new Attribute(Attr.ROWSPAN, "2");

        return tr(th("Страницы/Функционал").attr(rowspan).attr("class", "head"), th(text("Всего"), br(), text("экспертов")).attr(rowspan).attr("class", "head"), th(text("Всего"), br(), text("кейсов")).attr(rowspan).attr("class", "head headSeparator"), each(getRelevantMembers(), member -> th(member).attr(colspan).attr("class", "headSeparator head-person")));
    }

    private DomContent buildSubHeader()
    {
        return tr(each(getRelevantMembers(), member -> join(th("пройдено"), th("создано").attr("class", "headSeparator"))));
    }

    private DomContent makeBadCasesStatistics(SFunctionality functionality, Method functionalityMethod, String logMessageTemplate, String elementPrefix, String id, String parent) throws InvocationTargetException, IllegalAccessException {

        HashSet<STestCase> casesList = (HashSet<STestCase>) functionalityMethod.invoke(functionality);

        if (!casesList.isEmpty())
        {
            if (functionality.getParentFunctionality() == null)
                stdLogger.log.warn(String.format(logMessageTemplate, functionality.getFunctionalName(), casesList.size()));

            String parentID = String.format("%s", parent);
            String funcHeader = String.format("%s_head_%s", elementPrefix, functionality.hashCode());
            String collapsableElementId = String.format("%s_col_%s", elementPrefix, id);

            DomContent elementHeader = join(text(functionality.getFunctionalName()), p().with(rawHtml("&nbsp")), span(String.valueOf(casesList.size())).attr("class", "badge bg-secondary"));

            DomContent testsList = ul(
                                        each(casesList, testCase ->
                                                li(
                                                    div(a(testCase.getCaseId().toString()).attr("target", "_blank").attr("href", String.format("%sproject/%s/test-cases/%s", this.tmsAddress, this.projectId, testCase.getCaseId())), text(String.format(" - %s", testCase.getCaseName())))
                                                ).attr("class", "list-group-item")
                                        )
                                    ).attr("class", "list-group");

            DomContent elementBody = join(makeAccordionElement(text("Тесткейсы"), testsList, "cases" + funcHeader, "cases" + collapsableElementId, collapsableElementId),
                                        each(
                                            functionality.getDaughterFunctionalities(), d -> {
                                                    try
                                                    {
                                                        return makeBadCasesStatistics(d, functionalityMethod, logMessageTemplate, elementPrefix, String.valueOf(d.hashCode()), collapsableElementId);
                                                    } catch (InvocationTargetException | IllegalAccessException e)
                                                    {
                                                        throw new RuntimeException(e);
                                                    }
                                                }
                                        )
                                    );

            return makeAccordionElement(elementHeader, elementBody, funcHeader, collapsableElementId, parentID);
        }
        else
            return null;
    }

    private DomContent makeAccordionElement(DomContent itemName, DomContent accordionBody, String headerId, String collapsableContentId, String parentId)
    {
        return div(
                    h2(
                       button(
                            itemName
                       ).attr("class", "accordion-button collapsed").attr("type", "button").attr("data-bs-toggle", "collapse").attr("data-bs-target", "#" + collapsableContentId).attr("aria-controls", collapsableContentId)
                    ).attr("class", "accordion-header").attr("id", headerId).attr("aria-expanded", "false"),
                    div(
                        div(
                            accordionBody
                        ).attr("class", "accordion-body")
                    ).attr("id", collapsableContentId).attr("class", "accordion-collapse collapse").attr("aria-labelledby", headerId).attr("data-bs-parent", "#"+parentId)
        ).attr("class", "accordion-item");
    }

    private void saveHTMLFile(String htmlString) throws IOException
    {
        String outDirectory = "files";
        OutputStream out = new FileOutputStream(outDirectory + "/LowLevelStatistics.html");
        out.write(htmlString.getBytes());
        out.close();
    }
}
