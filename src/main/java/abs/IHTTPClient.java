package abs;

import java.io.IOException;

public interface IHTTPClient
{
    String getResponseBody() throws IOException;
}
