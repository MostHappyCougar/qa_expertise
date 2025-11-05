package BusFactorProcessor;

import Structures.SFunctionality;
import abs.AMarker;

import java.util.ArrayList;
import java.util.HashSet;

import Logger.stdLogger;

/**
 * Реализация для маркера данных по заданному принципу. В конкретном случае, маркируется функционал, наиболее подверженный bus-factor
 */
public class stdBusFactorProcessor extends AMarker
{
    private final HashSet<String> relevantMembers;
    private Integer percentageThreshold;

    /**
     *
     * @param relevantMembers список участвующих в анализе сотрудников
     * @param percentageThreshold минимальный порог экспертности
     */
    public stdBusFactorProcessor(HashSet<String> relevantMembers, Integer percentageThreshold)
    {
        this.relevantMembers = relevantMembers;
        this.percentageThreshold = percentageThreshold;
        
        stdLogger.log.info("Сотрудники прошедшие и создавшие меньше {}% тесткейсов не будут учтены как эксперты функционала", this.percentageThreshold);
    }

    @Override
    public void fillRelevantExpertsCountForEachFunctionality(ArrayList<SFunctionality> functionalList)
    {
        functionalList.forEach(this::makeFunctionalityBusRateForFunctionality);
    }

    /**
     * Метод для определения количества экспертов для конкретного функционала
     * @param functionality Конкретная функциональность или директория в TMS
     */
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
            stdLogger.log.info(String.format("Всего экспертов функционала \"%s\" - %d", functionality.getFunctionalName(), functionality.getRelevantExpertsCount()));

        if (!functionality.getDaughterFunctionalities().isEmpty())
        {
            functionality.getDaughterFunctionalities().forEach(this::makeFunctionalityBusRateForFunctionality);
        }
    }
}
