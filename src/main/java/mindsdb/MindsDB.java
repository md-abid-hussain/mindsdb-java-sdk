package mindsdb;

import java.util.Map;

import mindsdb.client.Server;
import mindsdb.connectors.RestAPI;

public class MindsDB {
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

    /**
     * Create a connection to local MindsDB server running on port 47334.
     */
    public static Server connect() {
        return connect(null, null, null, null, false, null);
    }

    /**
     * Create a connection to MindsDB server.
     *
     * @param url URL to MindsDB server.
     */
    public static Server connect(String url) {
        return connect(url, null, null, null, false, null);
    }

    /**
     * Create a connection to MindsDB server.
     *
     * @param url      URL to MindsDB server.
     * @param login    User login, for cloud version it contains email.
     * @param password User password to login (for cloud version).
     */
    public static Server connect(String url, String login, String password) {
        return connect(url, login, password, null, false, null);
    }

    /**
     * Create a connection to MindsDB server.
     *
     * @param url    URL to MindsDB server.
     * @param apiKey API key to authenticate (for cloud version).
     */
    public static Server connect(String url, String login, String password, String apiKey) {
        return connect(url, login, password, apiKey, false, null);
    }
}
