package abs;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public abstract class AResponseProvider
{

    public URL makeRequestURL(String tmsURL, String[] requestArguments) throws URISyntaxException, MalformedURLException
    {
        URI uri = new URI(String.format(tmsURL, requestArguments));
        return uri.toURL();
    }

    public String provideResponse(URL requestURL, String auth) throws IOException, URISyntaxException
    {
        return makeClientForRequest(requestURL, auth).getResponseBody();
    }
    public abstract IHTTPClient makeClientForRequest(URL requestURL, String auth);
}
