package BusFactorProcessor;

import Logger.stdLogger;
import Structures.SFunctionality;
import abs.AMarker;

import java.util.ArrayList;
import java.util.HashSet;

public class stdBusFactorProcessor extends AMarker
{
    private final HashSet<String> relevantMembers;

    public stdBusFactorProcessor(HashSet<String> relevantMembers)
    {
        this.relevantMembers = relevantMembers;
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
            if (functionality.getAllCasesExecutionsByMembers().containsKey(member) || functionality.getAllCasesCreatedByMembers().containsKey(member))
            {
                functionality.setRelevantExpertsCount(functionality.getRelevantExpertsCount() + 1);
            }
        });

        if (functionality.getParentFunctionality() == null)
            stdLogger.log.info(String.format("Всего экспертов функционала \"%s\" - %d", functionality.getFunctionalName(), functionality.getRelevantExpertsCount()));

        if (!functionality.getDaughterFunctionalities().isEmpty())
        {
            functionality.getDaughterFunctionalities().forEach(this::makeFunctionalityBusRateForFunctionality);
        }
    }
}
