package BusFactorProcessor;

import Structures.SFunctionality;
import abs.AMarker;

import java.util.ArrayList;
import java.util.HashSet;

import static Logs.Logs.log;

public class stdBusFactorProcessor extends AMarker
{
    private final HashSet<String> relevantMembers;
    private Integer percentageThreshold;

    public stdBusFactorProcessor(HashSet<String> relevantMembers, Integer percentageThreshold)
    {
        this.relevantMembers = relevantMembers;
        this.percentageThreshold = percentageThreshold;
        
        log.info(String.format("Сотрудники прошедшие и создавшие меньше %d%% тесткейсов не будут учтены как эксперты функционала", this.percentageThreshold));
    }

    @Override
    public void fillRelevantExpertsCountForEachFunctionality(ArrayList<SFunctionality> functionalityTree)
    {
        functionalityTree.forEach(this::makeFunctionalityBusRateForFunctionality);
    }

    private void makeFunctionalityBusRateForFunctionality(SFunctionality functionality)
    {
        this.relevantMembers.forEach(member ->
        {
            if (functionality.getAllCasesExecutionsByMembers(this.percentageThreshold).containsKey(member) || functionality.getAllCasesCreatedByMembers(this.percentageThreshold).containsKey(member))
            {
                functionality.setRelevantExpertsCount(functionality.getRelevantExpertsCount() + 1);
            }
        });

        if (functionality.getParentFunctionality() == null)
            log.info(String.format("Всего экспертов функционала \"%s\" - %d", functionality.getFunctionalName(), functionality.getRelevantExpertsCount()));

        if (!functionality.getDaughterFunctionalities().isEmpty())
        {
            functionality.getDaughterFunctionalities().forEach(this::makeFunctionalityBusRateForFunctionality);
        }
    }
}
