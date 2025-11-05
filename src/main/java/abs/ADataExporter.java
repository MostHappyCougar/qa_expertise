package abs;

import Structures.SFunctionality;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Абстракция для экспортера данных о погруженности сотрудников
 */
public abstract class ADataExporter
{
    /**
     * Метод для экспорта данных о погруженности в функциональность в нужном виде
     * @param functionalList Список функциональностей (директорий TMS) с данными об экспертности сотрудников. На основе этого списка и бубут строиться данные на экспорт
     * @throws IOException
     */
    public abstract void exportData(ArrayList<SFunctionality> functionalList) throws IOException;
}
