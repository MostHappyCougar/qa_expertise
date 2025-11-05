package abs;

import Structures.SFunctionality;

import java.util.ArrayList;

/**
 * Абстракция для обработчика "сырых" данных из TMS
 */
public abstract class ADataProcessor
{
    /**
     * Получить отсортированный список статистики по функциональностям из TMS
     * @return ArrayList
     */
    public abstract ArrayList<SFunctionality> getSortedFunctionalStats();
}
