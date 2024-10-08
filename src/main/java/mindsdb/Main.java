package mindsdb;

import mindsdb.client.ApiConfig;
import mindsdb.models.ServerStatus;
import mindsdb.services.*;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws Exception {
//        String url = "https://cloud.mindsdb.com";
        String url = "http://127.0.0.1:47334";
        String user = null;
        String password = null;
        ApiConfig apiConfig = ApiConfig.builder().user(user).url(url).managed(false).password(password).build();
        MindsDb.init(apiConfig);
//        ServerStatus serverStatus = Server.status();
//        System.out.println(serverStatus);
//        System.out.println(serverStatus);
        String query = "select NAME, ENGINE from information_schema.databases where TYPE='data'";
//        Query.builder().sqlQueryString(query).build().execute();
//        System.out.println(Projects.list());
//        System.out.println(Projects.get("mindsdb"));
//        System.out.println(Databases.list());
//        System.out.println(Models.list("mindsdb"));
        System.out.println(Models.get("mindsdb", "stability_image_generation"));
    }
}
