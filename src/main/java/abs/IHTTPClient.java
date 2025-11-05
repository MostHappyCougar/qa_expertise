package abs;

import java.io.IOException;

/**
 * Интерфейс для HTTP клиента, используемых в запросах к TMS
 */
public interface IHTTPClient
{
    /**
     * Метод для получения тела ответа на запрос от TMS
     * @return String
     * @throws IOException
     */
    String getResponseBody() throws IOException;
}
