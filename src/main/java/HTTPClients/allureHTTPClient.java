package HTTPClients;

import abs.IHTTPClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class allureHTTPClient implements IHTTPClient {

    private final URL requestURL;
    private HttpURLConnection connection;
    private String auth;


    public allureHTTPClient(URL requestURL, String auth)
    {
        this.requestURL = requestURL;
        this.auth = auth;
    }

    @Override
    public String getResponseBody() throws IOException
    {
        makeConnection();
        return readResponseBody(this.connection);
    }

    private void makeConnection() throws IOException
    {
        this.connection = (HttpURLConnection) this.requestURL.openConnection();
        this.connection.setRequestMethod("GET");
        this.connection.setRequestProperty("Authorization", this.auth);
    }

    private String readResponseBody(HttpURLConnection connection) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(this.connection.getInputStream()));
        String line;
        StringBuilder finalOut = new StringBuilder();

        while ((line = reader.readLine()) != null){
            finalOut.append(line);
        }

        reader.close();
        this.connection.disconnect();

        return finalOut.toString();
    }
}
