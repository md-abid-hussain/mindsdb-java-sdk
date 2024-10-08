package mindsdb.services;

import kong.unirest.core.GenericType;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;
import mindsdb.models.Database;
import mindsdb.utils.Constants;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class Databases {

    public static List<Database> list(){
        AtomicReference<List<Database>> databaseListAtomicRef = new AtomicReference<>();
        Unirest.get(Constants.LIST_DATABASE_ENDPOINT)
                .asObject(new GenericType<List<Database>>(){})
                .ifFailure(listHttpResponse -> {
                    if (listHttpResponse.getParsingError().isPresent()) {
                        throw new RuntimeException("Not able to parse response. Error - " + listHttpResponse.getParsingError().get());
                    }
                })
                .ifSuccess(listHttpResponse -> {
                    databaseListAtomicRef.set(listHttpResponse.getBody());
                });
        return databaseListAtomicRef.get();
    }

    public static Database get(String databaseName) {
        AtomicReference<Database> databaseAtomicRef = new AtomicReference<>();
        Unirest.get(Constants.GET_DATABASE_ENDPOINT)
                .routeParam(Constants.DATABASE_NAME_ROUTE_PARAM, databaseName)
                .asObject(Database.class)
                .ifFailure(databaseHttpResponse -> {
                    if (databaseHttpResponse.getParsingError().isPresent()) {
                        throw new RuntimeException("Not able to parse response. Error - " + databaseHttpResponse.getParsingError().get());
                    }
                })
                .ifSuccess(databaseHttpResponse -> {
                    databaseAtomicRef.set(databaseHttpResponse.getBody());
                });
        return databaseAtomicRef.get();
    }
}
