package mindsdb.services;

import com.google.gson.JsonObject;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;
import lombok.Builder;
import lombok.NonNull;
import mindsdb.MindsDb;
import mindsdb.utils.Constants;

import java.io.IOException;

@Builder
public class Query {

    @Builder.Default
    private String database = Constants.MINDS_DB;

    @NonNull private String sqlQueryString;

    public void execute() throws IOException, InterruptedException {
        if(sqlQueryString == null || sqlQueryString.isEmpty()) {
            return;
        }
//        RestApiClient restApiClient = MindsDb.getRestClient();
//
//        String serviceUrl = restApiClient.getApiConfig().getUrl() + Constants.SQL_QUERY_ENDPOINT;
        JsonObject jsonParams = new JsonObject();
        jsonParams.addProperty(Constants.QUERY, sqlQueryString);
        JsonObject contextParams = new JsonObject();
        contextParams.addProperty(Constants.DB, database);
        jsonParams.add(Constants.CONTEXT, contextParams);
        String jsonBodyString = jsonParams.toString();

//        HttpResponse<String> response = restApiClient.httpClientPostRequest(URI.create(serviceUrl), jsonBodyString);
//        System.out.println(response.body());
        HttpResponse<String> response = Unirest.post(Constants.SQL_QUERY_ENDPOINT)
                .body(jsonBodyString)
                .asString();
//                .ifFailure(new Consumer<kong.unirest.core.HttpResponse<ServerStatus>>() {
//                    @Override
//                    public void accept(HttpResponse<ServerStatus> serverStatusHttpResponse) {
//                        System.out.println(serverStatusHttpResponse.getStatus());
//                        if(!serverStatusHttpResponse.isSuccess())
//                            System.out.println("Unable to connect to server. " + serverStatusHttpResponse.getStatus());
//                        if(serverStatusHttpResponse.isSuccess() && serverStatusHttpResponse.getParsingError().isPresent()){
//                            throw new RuntimeException("Not able to parse response. Error - " + serverStatusHttpResponse.getParsingError().get());
//                        }
//                    }
//                });
        System.out.println(response.getBody());
    }
}
