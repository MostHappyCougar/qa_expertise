package Structures;

import java.util.*;

public class SFunctionality
{
    public SFunctionality(String functionalName)
    {
        this.functionalName = functionalName;
    }

    //Название функциональности
    private final String functionalName;
    public String getFunctionalName()
    {
        return functionalName;
    }

    //Уровень функциональности
    private Long functionalityLevel;
    public void setFunctionalityLevel(Long functionalityLevel)
    {
        this.functionalityLevel = functionalityLevel;
    }

    //Родительская функциональность
    private SFunctionality parentFunctionality;
    public SFunctionality getParentFunctionality()
    {
        return this.parentFunctionality;
    }
    public void setParentFunctionality(SFunctionality functionality)
    {
        this.parentFunctionality = functionality;
    }


    private Integer relevantExpertsCount = 0;
    public void setRelevantExpertsCount(Integer relevantExpertsCount) {this.relevantExpertsCount = relevantExpertsCount; }
    public Integer getRelevantExpertsCount() {return this.relevantExpertsCount; }

    //Хэш Сет дочерних функциональностей
    private final HashSet<SFunctionality> daughterFunctionalities = new HashSet<>();
    public HashSet<SFunctionality> getDaughterFunctionalities()
    {
        return this.daughterFunctionalities;
    }
    public void addDaughterFunctionality (SFunctionality functionality) { this.daughterFunctionalities.add(functionality); }

    //Хэш-сет тесткейсов в функциональности
    private final HashSet<STestCase> casesList = new HashSet<>();
    //Хэшсет ниразу не пройденных кейсов
    private final HashSet<STestCase> neverExecutedCases = new HashSet<>();
    //Хэшсет неактуальных кейсов
    private final HashSet<STestCase> outdatedCases = new HashSet<>();

    public void addCaseToFunctionality(STestCase testCase)
    {
        if(!Objects.equals(testCase.getCaseStatus(), "Outdated"))
        {
            this.casesList.add(testCase);
            updateCasesExecutionsByMembers();
            updateCasesCreatedByMembers();

            if (testCase.getExecutorsList().isEmpty())
                this.neverExecutedCases.add(testCase);

            if (testCase.getOwnerName() == null)
                this.casesWithoutAutor.add(testCase);
        }

        else
        {
            this.outdatedCases.add(testCase);
        }
    }
    private void updateCasesExecutionsByMembers()
    {
        HashSet<String> executorsUniqueNames = new HashSet<>();
        ArrayList<String> allExecutorsNames = new ArrayList<>();

        this.casesList.forEach((testCase) ->
        {
            executorsUniqueNames.addAll(testCase.getExecutorsList());
            allExecutorsNames.addAll(testCase.getExecutorsList());
        });

        executorsUniqueNames.forEach((executorName) -> this.allCasesExecutionsByMembers.put(executorName, Collections.frequency(allExecutorsNames, executorName)));
    }
    private void updateCasesCreatedByMembers()
    {
        HashSet<String> ownersUniqueNames = new HashSet<>();
        ArrayList<String> allOwnersNames = new ArrayList<>();

        this.casesList.forEach((testCase) ->
        {
            String ownerName = testCase.getOwnerName();

            if (ownerName != null)
            {
                ownersUniqueNames.add(testCase.getOwnerName());
                allOwnersNames.add(testCase.getOwnerName());
            }
        });

        ownersUniqueNames.forEach((ownerName) -> this.allCasesCreatedByMembers.put(ownerName, Collections.frequency(allOwnersNames, ownerName)));
    }
    public HashSet<STestCase> getCasesList()
    {
        return this.casesList;
    }
    public HashSet<STestCase> getNeverExecutedCases()
    {
        return this.neverExecutedCases;
    }

    //Количество кейсов функциональности, пройденных каждым из сотрудников
    private final Map<String, Integer> allCasesExecutionsByMembers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    public Map<String, Integer> getAllCasesExecutionsByMembers() { return this.allCasesExecutionsByMembers; }

    //Количество кейсов функциональности, созданных каждым из сотрудников
    private final Map<String, Integer> allCasesCreatedByMembers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final HashSet<STestCase> casesWithoutAutor = new HashSet<>();
    public HashSet<STestCase> getCasesWithoutAutor() { return this.casesWithoutAutor; }
    public Map<String, Integer> getAllCasesCreatedByMembers() { return allCasesCreatedByMembers; }
    public HashSet<STestCase> getOutdatedCases() {return this.outdatedCases;}

    @Override
    public int hashCode()
    {
        return Objects.hash(this.functionalName, this.functionalityLevel, this.parentFunctionality);
    }

    @Override
    public boolean equals(Object obj)
    {
        SFunctionality functionality = (SFunctionality) obj;

        return Objects.equals(functionality.functionalName, this.functionalName) && functionality.hashCode() == this.hashCode();
    }
}
