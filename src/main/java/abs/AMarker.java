package abs;

import Structures.SFunctionality;

import java.util.ArrayList;

/**
 * Абстракция для маркера функциональности по заданному принципу
 */
public abstract class AMarker
{
    /**
     * Метод для определения количества экспертов для каждого фукнционала
     * @param functionalList Список функциональностей (директорий TMS) с данными об экспертности сотрудников. На основе этого списка в выходных данных будут размечаться функциональности
     */
    public abstract void fillRelevantExpertsCountForEachFunctionality(ArrayList<SFunctionality> functionalList);
}
