package mindsdb.client;

import java.util.Map;

import mindsdb.connectors.RestAPI;

public class Connect {
    private static final String DEFAULT_LOCAL_API_URL = "http://127.0.0.1:47334";
    private static final String DEFAULT_CLOUD_API_URL = "https://cloud.mindsdb.com";

    /**
     * Create a connection to MindsDB server.
     *
     * @param url       URL to MindsDB server.
     * @param login     User login, for cloud version it contains email.
     * @param password  User password to login (for cloud version).
     * @param apiKey    API key to authenticate (for cloud version).
     * @param isManaged Whether or not the URL points to a managed instance.
     * @param headers   Additional headers to send with the connection, optional.
     * @return Server object.
     */

    public static Server connect(String url, String login, String password, String apiKey, boolean isManaged,
            Map<String, String> headers) {
        if (url == null) {
            if (login != null) {
                // default is cloud
                url = DEFAULT_CLOUD_API_URL;
            } else {
                // is local
                url = DEFAULT_LOCAL_API_URL;
            }
        }

        RestAPI api = new RestAPI(url, login, password, apiKey, isManaged, headers);
        return new Server(api);
    }
}
