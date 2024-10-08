package mindsdb.services;

import kong.unirest.core.GenericType;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;
import mindsdb.models.Database;
import mindsdb.models.Model;
import mindsdb.utils.Constants;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class Models {

    public static List<Model> list(String projectName){
        AtomicReference<List<Model>> modelListAtomicRef = new AtomicReference<>();
        Unirest.get(Constants.LIST_MODEL_ENDPOINT)
                .routeParam(Constants.PROJECT_NAME_ROUTE_PARAM, projectName)
                .asObject(new GenericType<List<Model>>(){})
                .ifFailure(listHttpResponse -> {
                    if (listHttpResponse.getParsingError().isPresent()) {
                        throw new RuntimeException("Not able to parse response. Error - " + listHttpResponse.getParsingError().get());
                    }
                })
                .ifSuccess(listHttpResponse -> {
                    modelListAtomicRef.set(listHttpResponse.getBody());
                });
        return modelListAtomicRef.get();
    }

    public static Model get(String projectName, String modelName){
        AtomicReference<Model> modelAtomicRef = new AtomicReference<>();
        Unirest.get(Constants.GET_MODEL_ENDPOINT)
                .routeParam(Constants.PROJECT_NAME_ROUTE_PARAM, projectName)
                .routeParam(Constants.MODEL_NAME_ROUTE_PARAM, modelName)
                .asObject(Model.class)
                .ifFailure(modelHttpResponse -> {
                    if (modelHttpResponse.getParsingError().isPresent()) {
                        throw new RuntimeException("Not able to parse response. Error - " + modelHttpResponse.getParsingError().get());
                    }
                })
                .ifSuccess(modelHttpResponse -> {
                    modelAtomicRef.set(modelHttpResponse.getBody());
                });
        return modelAtomicRef.get();
    }

    public static Model describe(String projectName, String modelName){
        return null;
    }
}
