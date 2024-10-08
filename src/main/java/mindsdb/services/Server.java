package mindsdb.services;

import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;
import lombok.Builder;
import mindsdb.models.ServerStatus;
import mindsdb.utils.Constants;

import java.io.IOException;
import java.util.function.Consumer;

@Builder
public class Server {

    public static ServerStatus status() throws IOException, InterruptedException {
//        RestApiClient restApiClient = MindsDb.getRestClient();
//        ApiConfig apiConfig = restApiClient.getApiConfig();
//        String serviceEndpoint = apiConfig.getUrl() + Constants.SERVER_STATUS_ENDPOINT;
//        HttpResponse<String> response = restApiClient.httpClientGetRequest(URI.create(serviceEndpoint));
//        ServerStatus serverStatus = Constants.gson.fromJson(response.body(), ServerStatus.class);
//        return serverStatus;
        HttpResponse<ServerStatus> response = Unirest.get(Constants.SERVER_STATUS_ENDPOINT)
                .asObject(ServerStatus.class)
                .ifFailure(new Consumer<HttpResponse<ServerStatus>>() {
                        @Override
                        public void accept(HttpResponse<ServerStatus> serverStatusHttpResponse) {
                            System.out.println(serverStatusHttpResponse.getStatus());
                            if(!serverStatusHttpResponse.isSuccess())
                                System.out.println("Unable to connect to server. " + serverStatusHttpResponse.getStatus());
                            if(serverStatusHttpResponse.isSuccess() && serverStatusHttpResponse.getParsingError().isPresent()){
                                throw new RuntimeException("Not able to parse response. Error - " + serverStatusHttpResponse.getParsingError().get());
                            }
                        }
                });
        return response.getBody();
    }
}
