package abs;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Абстракция для "поставщика" ответов от TMS для последующей обработки
 */
public abstract class AResponseProvider
{
    /**
     *
     * @param tmsURL URL TMSки
     * @param requestArguments Аргументы для запроса
     * @return URL
     * @throws URISyntaxException
     * @throws MalformedURLException
     */
    public URL makeRequestURL(String tmsURL, String[] requestArguments) throws URISyntaxException, MalformedURLException
    {
        URI uri = new URI(String.format(tmsURL, requestArguments));
        return uri.toURL();
    }

    /**
     *
     * @param requestURL URL запроса
     * @param auth Аутентификация
     * @return String
     * @throws IOException
     * @throws URISyntaxException
     */
    public String provideResponse(URL requestURL, String auth) throws IOException, URISyntaxException
    {
        return makeClientForRequest(requestURL, auth).getResponseBody();
    }

    /**
     * Создать HTTP клиента для конкретного запроса
     * @param requestURL URL запроса
     * @param auth аутентификация
     * @return IHTTPClient
     */
    public abstract IHTTPClient makeClientForRequest(URL requestURL, String auth);
}
