package mindsdb.services;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import kong.unirest.core.GenericType;
import kong.unirest.core.Unirest;
import mindsdb.models.Job;
import mindsdb.utils.Constants;

public class Jobs {
    public static List<Job> list(String projectName) {
        AtomicReference<List<Job>> jobListAtomicRef = new AtomicReference<>();
        Unirest.get(Constants.LIST_JOBS_ENDPOINT)
                .routeParam(Constants.PROJECT_NAME_ROUTE_PARAM, projectName)
                .asObject(new GenericType<List<Job>>() {
                })
                .ifFailure(listHttpResponse -> {
                    if (listHttpResponse.getParsingError().isPresent()) {
                        throw new RuntimeException(
                                "Not able to parse response. Error - " + listHttpResponse.getParsingError().get());
                    }
                })
                .ifSuccess(listHttpResponse -> {
                    jobListAtomicRef.set(listHttpResponse.getBody());
                });
        return jobListAtomicRef.get();
    }

    public static Job get(String projectName, String jobName) {
        AtomicReference<Job> jobAtomicRef = new AtomicReference<>();
        Unirest.get(Constants.GET_JOB_ENDPOINT)
                .routeParam(Constants.PROJECT_NAME_ROUTE_PARAM, projectName)
                .routeParam(Constants.JOB_NAME_ROUTE_PARAM, jobName)
                .asObject(Job.class)
                .ifFailure(getHttpResponse -> {
                    if (getHttpResponse.getParsingError().isPresent()) {
                        throw new RuntimeException(
                                "Not able to parse response. Error - " + getHttpResponse.getParsingError().get());
                    }
                })
                .ifSuccess(getHttpResponse -> {
                    jobAtomicRef.set(getHttpResponse.getBody());
                });

        return jobAtomicRef.get();
    }

    public static void delete(String projectName, String jobName) {
        Unirest.delete(Constants.GET_JOB_ENDPOINT)
                .routeParam(Constants.PROJECT_NAME_ROUTE_PARAM, projectName)
                .routeParam(Constants.JOB_NAME_ROUTE_PARAM, jobName)
                .asEmpty();
    }
}
