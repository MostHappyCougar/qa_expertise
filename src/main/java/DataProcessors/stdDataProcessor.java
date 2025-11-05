package DataProcessors;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import Logger.stdLogger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import ResponseProviders.stdResponseProvider;
import Structures.SFunctionality;
import Structures.STestCase;
import abs.ADataProcessor;

public class stdDataProcessor extends ADataProcessor
{
    private final stdResponseProvider responseProvider;
    private final Integer historyDepth;
    private final HashMap<String, String> tmsData;
    private final HashSet<SFunctionality> functionalityTreeHashSet = new HashSet<>();
    public ArrayList<SFunctionality> getSortedFunctionalStats()
    {
        ArrayList<SFunctionality> sortedFunctionalityArray = new ArrayList<>(this.functionalityTreeHashSet);
        sortedFunctionalityArray.sort(Comparator.comparing(SFunctionality::getFunctionalName));
        return sortedFunctionalityArray;
    }

    public stdDataProcessor(Integer historyDepth, HashMap<String, String> tmsData) throws IOException, URISyntaxException, ParseException
    {
        this.responseProvider = new stdResponseProvider();
        stdLogger.log.info(String.format("Последние %d ранов будут рассмотрены для формирования истории прохождения кейсов", historyDepth));
        this.historyDepth = historyDepth;
        this.tmsData = tmsData;
        prepareFunctionalityExpertiseAcrossCasesCount(caseCountInProject());
    }
    
    private void prepareFunctionalityExpertiseAcrossCasesCount(Long casesCount) throws IOException, URISyntaxException, ParseException
    {
        stdLogger.log.info(String.format("Всего будет проанализировано %d кейсов.", casesCount));

        String caseList_request = "%sapi/rs/testcase?projectId=%s&page=%s&size=%s&sort=createdDate%%2CDESC";
        int pagesCount = (int) Math.ceil((double) casesCount / 2000);
        stdLogger.log.info(String.format("Все кейсы разделены на %d страниц.", pagesCount));

        AtomicInteger casesCounter = new AtomicInteger();
        for (int i = 0; i < pagesCount; i++)
        {
            String rawData = requestRawData(caseList_request, new String[]{this.tmsData.get("address"), this.tmsData.get("projectId"), String.valueOf(i), casesCount.toString()});
            JSONArray casesDataContent = makeArrayOfJSONObjects(rawData, "content");
            stdLogger.log.info(String.format("Для %d из %d страницы с тесткейсами выполняется анализ погружения.", i+1, pagesCount));

            casesDataContent.forEach(caseContent ->
                    {
                        try
                        {
                            makeCaseWithRelevantData((JSONObject) caseContent);
                        } catch (IOException | URISyntaxException | ParseException e)
                        {
                            throw new RuntimeException(e);
                        }
                        casesCounter.set(casesCounter.get() + 1);
                    }
            );
            stdLogger.log.info(String.format("%d из %d страница с кейсами проанализирована. Всего рассмотрено %d кейсов.", i+1, pagesCount, casesCounter.get()));
        }
    }

    private Long caseCountInProject() throws IOException, URISyntaxException, ParseException
    {
        String functionality_request = "%sapi/rs/project/%s/stats";

        String rawData = requestRawData(functionality_request, new String[] {this.tmsData.get("address"), this.tmsData.get("projectId")});

        JSONObject data = (JSONObject) new JSONParser().parse(rawData);
        Long caseCount = (Long)data.get("manualTestCases") + (Long)data.get("automatedTestCases");

        stdLogger.log.info(String.format("В проекте с ID=%s найдено %d кейсов", this.tmsData.get("projectId"), caseCount));

        return caseCount;
    }

    public void makeCaseWithRelevantData(JSONObject caseData) throws IOException, URISyntaxException, ParseException
    {
        STestCase testCase = new STestCase(Integer.valueOf(Objects.requireNonNull(getJSONObjectKeyValueByName(caseData, "id"))), getJSONObjectKeyValueByName(caseData, "name"));
        testCase.setExecutorsList(requestCaseExecutors(testCase.getCaseId()));
        testCase.setOwnerName(requestCaseOwnerName(testCase.getCaseId()));

        JSONObject caseStatusData = (JSONObject) new JSONParser().parse(getJSONObjectKeyValueByName(caseData, "status"));

        testCase.setCaseStatus(getJSONObjectKeyValueByName(caseStatusData, "name"));

        SFunctionality caseFunctionalityTree = buildCaseFunctionalityTree(requestCaseFunctionalityList(testCase.getCaseId()));

        if (caseFunctionalityTree != null)
        {
            if (!this.functionalityTreeHashSet.contains(caseFunctionalityTree))
            {
                this.functionalityTreeHashSet.add(caseFunctionalityTree);
                recursivelyDistributeCaseIntoFunctionality(this.functionalityTreeHashSet, testCase, caseFunctionalityTree);
            }
            else
            {
                recursivelyMergeFuncTreeToHashSet(this.functionalityTreeHashSet, caseFunctionalityTree);
                recursivelyDistributeCaseIntoFunctionality(this.functionalityTreeHashSet, testCase, caseFunctionalityTree);
            }
        }
        else
            stdLogger.log.warn(String.format("Нельзя определить функциональность кейса #%s. Возможно, она не указана в ТестОпс" , testCase.getCaseId()));
    }

    private ArrayList<String> requestCaseExecutors(Integer caseId) throws IOException, URISyntaxException
    {
        String history_request = "%sapi/rs/testcase/%s/history?page=0&size=%s";

        String rawData = requestRawData(history_request, new String[]{this.tmsData.get("address"), String.valueOf(caseId), this.historyDepth.toString()});

        ArrayList<String> caseExecutors = new ArrayList<>();

        fillCollectionByValuesOfContentKey(caseExecutors, rawData, "testedBy");
        return caseExecutors;
    }

    private String requestCaseOwnerName(Integer caseId) throws IOException, URISyntaxException, ParseException
    {
        String ownerName = null;

        String members_request = "%sapi/rs/testcase/%s/members";
        String rawData = requestRawData(members_request, new String[]{this.tmsData.get("address"), String.valueOf(caseId)});
        JSONArray responseData = (JSONArray) new JSONParser().parse(rawData);

        for (Object owner : responseData)
        {
            JSONObject memberObject = (JSONObject) owner;
            ownerName = (String) memberObject.get("name");
        }
        return ownerName;
    }

    private SFunctionality buildCaseFunctionalityTree(Map<Long, String> functionalityList)
    {
        AtomicReference<SFunctionality> caseFunctionalityTreeRoot = new AtomicReference<>();
        AtomicReference<SFunctionality> parentFunctionality = new AtomicReference<>();
        AtomicReference<SFunctionality> daughterFunctionality = new AtomicReference<>();

        functionalityList.forEach((level, functionalityName) ->
        {
            if (caseFunctionalityTreeRoot.get() == null)
            {
                caseFunctionalityTreeRoot.set(new SFunctionality(functionalityName));
                caseFunctionalityTreeRoot.get().setFunctionalityLevel(level);

                parentFunctionality.set(caseFunctionalityTreeRoot.get());
            }
            else
            {
                daughterFunctionality.set(new SFunctionality(functionalityName));
                daughterFunctionality.get().setFunctionalityLevel(level);
                daughterFunctionality.get().setParentFunctionality(parentFunctionality.get());

                parentFunctionality.get().addDaughterFunctionality(daughterFunctionality.get());
                parentFunctionality.set(daughterFunctionality.get());
            }
        });

        return caseFunctionalityTreeRoot.get();
    }

    private Map<Long, String> requestCaseFunctionalityList(Integer caseId) throws IOException, URISyntaxException
    {
        String functionality_request = "%sapi/rs/testcase/%s/cfv";
        String rawData = requestRawData(functionality_request, new String[] {this.tmsData.get("address"), String.valueOf(caseId)});

        Map<Long, String> caseFunctionalityMap = new TreeMap<>();

        try
        {
            JSONArray responseData = (JSONArray) new JSONParser().parse(rawData);

            for (Object customField : responseData)
            {
                JSONObject customFieldObject = (JSONObject) customField;
                JSONObject customFieldData = (JSONObject) customFieldObject.get("customField");

                caseFunctionalityMap.put((Long)customFieldData.get("id"), customFieldObject.get("name").toString());
            }

            return caseFunctionalityMap;
        }
        catch (ParseException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void recursivelyMergeFuncTreeToHashSet(HashSet<SFunctionality> functionalityTreeHashSet, SFunctionality caseFunctionalityTree)
    {
        functionalityTreeHashSet.forEach(existingFunctionalityTree ->
        {
            if (existingFunctionalityTree.equals(caseFunctionalityTree))
            {
                existingFunctionalityTree.getDaughterFunctionalities().addAll(caseFunctionalityTree.getDaughterFunctionalities());

                if (!caseFunctionalityTree.getDaughterFunctionalities().isEmpty())
                    caseFunctionalityTree.getDaughterFunctionalities().forEach(daughterFunctionality ->
                            recursivelyMergeFuncTreeToHashSet(existingFunctionalityTree.getDaughterFunctionalities(), daughterFunctionality));
            }
        });
    }

    private String requestRawData(String request, String[] args) throws IOException, URISyntaxException
    {
        URL requestURL = this.responseProvider.makeRequestURL(request, args);
        return this.responseProvider.provideResponse(requestURL, readAuthProperties());
    }
    
    private String readAuthProperties() throws FileNotFoundException, IOException
    {
    	Properties prop = new Properties();
    	prop.load(new FileInputStream("src/main/resources/users.properties"));
    	return prop.getProperty("auth");
    }

    private void recursivelyDistributeCaseIntoFunctionality(HashSet<SFunctionality> functionalityHashSet, STestCase testCase, SFunctionality caseFunctionality)
    {
        functionalityHashSet.forEach(functionality ->
        {
            if (functionality.equals(caseFunctionality))
                functionality.addCaseToFunctionality(testCase);

            if (!caseFunctionality.getDaughterFunctionalities().isEmpty())
            {
                caseFunctionality.getDaughterFunctionalities().forEach(caseFunc ->
                        recursivelyDistributeCaseIntoFunctionality(functionality.getDaughterFunctionalities(), testCase, caseFunc)
                );
            }
        });
    }

    private void fillCollectionByValuesOfContentKey(Collection<String> collection, String response, String key)
    {
        try
        {
            JSONObject rawResponse = (JSONObject) new JSONParser().parse(response);
            JSONArray responseContentArray = (JSONArray) rawResponse.get("content");

            for (Object contentElement : responseContentArray) {
                JSONObject contentElementObject = (JSONObject) contentElement;
                Object valueOfContentElementKey = contentElementObject.get(key);

                if (valueOfContentElementKey != null)
                    collection.add(valueOfContentElementKey.toString());
            }
        }
        catch (ParseException e)
        {
            throw new RuntimeException(e);
        }
    }

    private JSONArray makeArrayOfJSONObjects (String response, String JSONObjectName) throws ParseException
    {
        JSONObject rawResponse = (JSONObject) new JSONParser().parse(response);
        return (JSONArray) rawResponse.get(JSONObjectName);
    }

    private String getJSONObjectKeyValueByName(JSONObject JSONContentElementObject, String key)
    {
        Object valueOfContentElementKey = JSONContentElementObject.get(key);

        if (valueOfContentElementKey != null)
            return valueOfContentElementKey.toString();
        else
            return null;
    }
}
