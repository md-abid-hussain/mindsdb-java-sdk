package mindsdb.services;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import kong.unirest.core.GenericType;
import kong.unirest.core.Unirest;
import mindsdb.models.Table;
import mindsdb.utils.Constants;

public class Tables {

    public static List<Table> list(String databaseName) {
        AtomicReference<List<Table>> tableListAtomicRef = new AtomicReference<>();
        Unirest.get(Constants.LIST_MODEL_ENDPOINT)
                .routeParam(Constants.DATABASE_NAME_ROUTE_PARAM, databaseName)
                .asObject(new GenericType<List<Table>>() {
                })
                .ifFailure(listHttpResponse -> {
                    if (listHttpResponse.getParsingError().isPresent()) {
                        throw new RuntimeException(
                                "Not able to parse response. Error - " + listHttpResponse.getParsingError().get());
                    }
                })
                .ifSuccess(listHttpResponse -> {
                    tableListAtomicRef.set(listHttpResponse.getBody());
                });
        return tableListAtomicRef.get();
    }
}
