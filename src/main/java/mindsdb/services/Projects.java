package mindsdb.services;

import kong.unirest.core.GenericType;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;
import mindsdb.models.Project;
import mindsdb.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class Projects {

    public static List<Project> list() {
        AtomicReference<List<Project>> projectList = new AtomicReference<>();
        HttpResponse<List<Project>> response = Unirest.get(Constants.LIST_PROJECT_ENDPOINT)
                .asObject(new GenericType<List<Project>>(){})
                .ifFailure(listHttpResponse -> {
                    if (listHttpResponse.getParsingError().isPresent()) {
                        throw new RuntimeException("Not able to parse response. Error - " + listHttpResponse.getParsingError().get());
                    }
                })
                .ifSuccess(listHttpResponse -> {
                    projectList.set(listHttpResponse.getBody());
                });
        return projectList.get();
    }

    public static Project get(String projectName) {
        AtomicReference<Project> project = new AtomicReference<>();
        HttpResponse<Project> response = Unirest.get(Constants.GET_PROJECT_ENDPOINT)
                .routeParam(Constants.PROJECT_NAME_ROUTE_PARAM, projectName)
                .asObject(Project.class)
                .ifFailure(projectHttpResponse -> {
                    if (projectHttpResponse.getParsingError().isPresent()) {
                        throw new RuntimeException("Not able to parse response. Error - " + projectHttpResponse.getParsingError().get());
                    }
                })
                .ifSuccess(projectHttpResponse -> {
                    project.set(projectHttpResponse.getBody());
                });
        return project.get();
    }
}
