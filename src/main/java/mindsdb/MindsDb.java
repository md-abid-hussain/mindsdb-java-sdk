package mindsdb;

import kong.unirest.core.Cache;
import kong.unirest.core.Unirest;
import mindsdb.client.ApiConfig;

import java.util.concurrent.TimeUnit;

public class MindsDb {

//    private static volatile RestApiClient restClient;
//    private static ApiConfig apiConfig;
//
//    private MindsDb() {
//    }
//
//    public static synchronized void init(ApiConfig config) {
//        apiConfig = config;
//        buildRestClient(apiConfig);
////        MindsDb.getRestClient().login();
//    }
//
//    public static RestApiClient getRestClient() {
//        if (MindsDb.restClient == null) {
//            synchronized (MindsDb.class) {
//                if (MindsDb.restClient == null) {
//                    MindsDb.restClient = buildRestClient(apiConfig);
//                }
//            }
//        }
//        return MindsDb.restClient;
//    }
//
//    private static RestApiClient buildRestClient(ApiConfig apiConfig) {
//        RestApiClient restApiClient = new RestApiClient(apiConfig);
//        return restApiClient;
//    }
    private static ApiConfig apiConfig;

    private MindsDb() {
    }

    public static synchronized void init(ApiConfig config) {
        apiConfig = config;
        Unirest.config()
                .enableCookieManagement(true)
                .defaultBaseUrl(apiConfig.getUrl())
                .addDefaultHeader("Content-Type", "application/json")
                .cacheResponses(new Cache.Builder().maxAge(1, TimeUnit.MINUTES))
                .retryAfter(true, 2);
//        buildRestClient(apiConfig);
//        MindsDb.getRestClient().login();
    }
}
