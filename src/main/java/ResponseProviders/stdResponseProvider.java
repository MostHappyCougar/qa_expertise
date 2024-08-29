package ResponseProviders;

import abs.AResponseProvider;
import HTTPClients.allureHTTPClient;

import java.net.URL;

public class stdResponseProvider extends AResponseProvider
{
    @Override
    public allureHTTPClient makeClientForRequest(URL requestURL, String auth)
    {
        return new allureHTTPClient(requestURL, auth);
    }
}
