package mindsdb.connectors;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import kong.unirest.core.HttpResponse;
import kong.unirest.core.JsonNode;
import kong.unirest.core.Unirest;
import kong.unirest.core.UnirestException;
import kong.unirest.core.UnirestInstance;
import kong.unirest.core.json.JSONArray;
import kong.unirest.core.json.JSONObject;
import lombok.NoArgsConstructor;
import mindsdb.utils.DataFrame;

@NoArgsConstructor
public final class RestAPI {

    private String url;
    private String username;
    private String password;
    private String apiKey;
    private boolean isManaged;
    private Gson gson = new Gson();
    private UnirestInstance session;

    public RestAPI(String url, String login, String password, String apiKey, boolean isManaged,
            Map<String, String> headers) {
        this.url = url;
        this.username = login;
        this.password = password;
        this.apiKey = apiKey;
        this.isManaged = isManaged;
        this.session = Unirest.spawnInstance();

        // Set user-agent
        this.session.config().addDefaultHeader("User-Agent", "java-sdk/1.0");

        // Add custom headers if provided
        if (headers != null) {
            headers.forEach((key, value) -> this.session.config().addDefaultHeader(key, value));
        }

        // Authenticate with API key if present
        if (this.apiKey != null) {
            this.session.config().addDefaultHeader("X-Api-Key", this.apiKey);
            return;
        }

        // Perform login if username and password are provided
        if (this.username != null && this.password != null) {
            this.login();
        }
    }

    private void raiseForStatus(HttpResponse<String> response) {
        if (response.getStatus() >= 400) {
            throw new RuntimeException("Error: " + response.getStatusText() + " - " + response.getBody());
        }
    }

    public void login() {
        String managedEndpoint = "/api/login";
        String cloudEndpoint = "/cloud/login";

        try {
            String endpointUrl;
            if (this.isManaged) {
                endpointUrl = this.url + managedEndpoint;
                HttpResponse<JsonNode> response = session.post(endpointUrl)
                        .header("Content-Type", "application/json")
                        .body("{\"username\":\"" + this.username + "\", \"password\":\"" + this.password + "\"}")
                        .asJson();

                checkAndHandleLoginFailure(response);
            } else {
                endpointUrl = this.url + cloudEndpoint;
                HttpResponse<JsonNode> response = session.post(endpointUrl)
                        .header("Content-Type", "application/json")
                        .body("{\"email\":\"" + this.username + "\", \"password\":\"" + this.password + "\"}")
                        .asJson();

                checkAndHandleLoginFailure(response);

                // Fallback in case isManaged is false but requires managed login
                if (response.getStatus() == 404 || response.getStatus() == 405) {
                    endpointUrl = this.url + managedEndpoint;
                    response = session.post(endpointUrl)
                            .header("Content-Type", "application/json")
                            .body("{\"username\":\"" + this.username + "\", \"password\":\"" + this.password + "\"}")
                            .asJson();

                    checkAndHandleLoginFailure(response);
                }
            }
        } catch (UnirestException e) {
            throw new RuntimeException("Failed to login: " + e.getMessage(), e);
        }
    }

    private void checkAndHandleLoginFailure(HttpResponse<JsonNode> response) {
        if (response.getStatus() >= 400) {
            throw new RuntimeException("Login failed: " + response.getBody().toString());
        }
    }

    public DataFrame sqlQuery(String sql, String database) {
        if (database == null) {
            database = "mindsdb";
        }
        String endpointUrl = this.url + "/api/sql/query";
        String sqlPayload = String.format("{\"query\": \"%s\", \"context\": {\"db\": \"%s\"}}", sql, database);
        System.out.println(sqlPayload);
        try {
            HttpResponse<JsonNode> response = session.post(endpointUrl)
                    .header("Content-Type", "application/json")
                    .body(sqlPayload)
                    .asJson();

            if (response.getStatus() >= 400) {
                throw new RuntimeException("SQL Query failed: " + response.getBody().toString());
            }

            JSONObject data = response.getBody().getObject();

            // System.out.println(data);

            if (data.getString("type").equals("table")) {
                JSONArray columns = data.getJSONArray("column_names");
                JSONArray rows = data.getJSONArray("data");
                DataFrame df = new DataFrame(columns, rows);

                return df;

            }

            // System.out.println(data);

            if (data.getString("type").equals("error")) {
                throw new RuntimeException("SQL Query failed: " + data.getString("error_message"));
            }

            return null;
        } catch (UnirestException e) {
            throw new RuntimeException("SQL Query execution failed: " + e.getMessage(), e);
        }
    }

    public DataFrame sqlQuery(String sql) {
        return sqlQuery(sql, null);
    }

    public void uploadFile(String name, Object data) throws UnirestException, IOException {
        byte[] fileData;

        if (data instanceof File) {
            fileData = Files.readAllBytes(((File) data).toPath());
        } else if (data instanceof String) {
            try {
                HttpResponse<byte[]> response = session.get((String) data).asBytes();
                fileData = response.getBody();
            } catch (UnirestException e) {
                throw new RuntimeException("Failed to download file: " + e.getMessage(), e);
            }
        } else {
            throw new IllegalArgumentException("Unsupported data type for upload");
        }

        try {

            HttpResponse<String> response = session.put(url + "/api/files/" + name)
                    .field("original_file_name", name)
                    .field("name", name)
                    .field("source_type", "file")
                    .field("file", fileData, name)
                    .asString();

            if (response.getStatus() >= 400) {
                throw new RuntimeException("File upload failed: " + response.getBody());
            }
        } catch (UnirestException e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }

    }

    public void closeSession() {
        this.session.close();
    }

    public JsonObject getFileMetadata(String name) throws UnirestException {
        HttpResponse<String> response = Unirest.get(url + "/api/files").asString();

        if (response.getStatus() >= 400) {
            throw new RuntimeException("Failed to retrieve file metadata: " + response.getBody());
        }

        JsonObject[] allFileMetadata = gson.fromJson(response.getBody(), JsonObject[].class);
        for (JsonObject metadata : allFileMetadata) {
            if (name.equals(metadata.get("name").getAsString())) {
                return metadata;
            }
        }
        throw new RuntimeException("File not found: " + name);
    }

    public void createAgent(String project, String name, String model, String provider, List<String> skills,
            Map<String, Object> params) throws UnirestException {
        JsonObject agentDetails = new JsonObject();
        agentDetails.addProperty("name", name);
        agentDetails.addProperty("model_name", model);
        agentDetails.addProperty("provider", provider);
        agentDetails.add("skills", gson.toJsonTree(skills));
        agentDetails.add("params", gson.toJsonTree(params));

        JsonObject body = new JsonObject();
        body.add("agent", agentDetails);

        HttpResponse<String> response = Unirest.post(url + "/api/projects/" + project + "/agents")
                .header("Content-Type", "application/json")
                .body(body.toString())
                .asString();

        if (response.getStatus() >= 400) {
            throw new RuntimeException("Failed to create agent: " + response.getBody());
        }
    }

    public void uploadByom(String name, String code, String requirements) throws UnirestException {
        HttpResponse<String> response = Unirest.put(this.url + "/api/handlers/byom/" + name)
                .field("code", code)
                .field("modules", requirements)
                .asString();

        raiseForStatus(response);
    }

    public JsonObject status() throws UnirestException {
        HttpResponse<String> response = Unirest.get(this.url + "/api/status").asString();

        raiseForStatus(response);

        return gson.fromJson(response.getBody(), JsonObject.class);
    }

    public JsonObject agents(String project) throws UnirestException {
        HttpResponse<String> response = Unirest.get(this.url + "/api/projects/" + project + "/agents").asString();

        raiseForStatus(response);

        return gson.fromJson(response.getBody(), JsonObject.class);
    }

    public JsonObject agent(String project, String name) throws UnirestException {
        HttpResponse<String> response = Unirest.get(this.url + "/api/projects/" + project + "/agents/" + name)
                .asString();

        raiseForStatus(response);

        return gson.fromJson(response.getBody(), JsonObject.class);
    }

    public JsonObject agentCompletion(String project, String name, List<JsonObject> messages) throws UnirestException {
        JsonObject body = new JsonObject();
        body.add("messages", gson.toJsonTree(messages));

        HttpResponse<String> response = Unirest
                .post(this.url + "/api/projects/" + project + "/agents/" + name + "/completions")
                .header("Content-Type", "application/json")
                .body(body.toString())
                .asString();

        raiseForStatus(response);

        return gson.fromJson(response.getBody(), JsonObject.class);
    }

    public void deleteAgent(String project, String name) throws UnirestException {
        HttpResponse<String> response = Unirest.delete(this.url + "/api/projects/" + project + "/agents/" + name)
                .asString();

        raiseForStatus(response);
    }

    public JsonArray skills(String project) throws UnirestException {
        HttpResponse<String> response = Unirest.get(this.url + "/api/projects/" + project + "/skills").asString();

        raiseForStatus(response);

        return gson.fromJson(response.getBody(), JsonArray.class);
    }

    public JsonObject skill(String project, String name) throws UnirestException {
        HttpResponse<String> response = Unirest.get(this.url + "/api/projects/" + project + "/skills/" + name)
                .asString();

        raiseForStatus(response);

        return gson.fromJson(response.getBody(), JsonObject.class);
    }

    public void createSkill(String project, String name, String type, JsonObject params) throws UnirestException {
        JsonObject body = new JsonObject();
        JsonObject skill = new JsonObject();
        skill.addProperty("name", name);
        skill.addProperty("type", type);
        skill.add("params", params);

        body.add("skill", skill);

        HttpResponse<String> response = Unirest.post(this.url + "/api/projects/" + project + "/skills")
                .header("Content-Type", "application/json")
                .body(body.toString())
                .asString();

        raiseForStatus(response);
    }

    public void updateSkill(String project, String name, String updatedName, String updatedType,
            JsonObject updatedParams) throws UnirestException {
        JsonObject body = new JsonObject();
        JsonObject skill = new JsonObject();
        skill.addProperty("name", updatedName);
        skill.addProperty("type", updatedType);
        skill.add("params", updatedParams);

        body.add("skill", skill);

        HttpResponse<String> response = Unirest.put(this.url + "/api/projects/" + project + "/skills/" + name)
                .header("Content-Type", "application/json")
                .body(body.toString())
                .asString();

        raiseForStatus(response);
    }

    public void deleteSkill(String project, String name) throws UnirestException {
        HttpResponse<String> response = Unirest.delete(this.url + "/api/projects/" + project + "/skills/" + name)
                .asString();

        raiseForStatus(response);
    }

    public void insertFilesIntoKnowledgeBase(String project, String knowledgeBaseName, List<String> fileNames)
            throws UnirestException {
        JsonObject body = new JsonObject();
        JsonObject knowledgeBase = new JsonObject();
        knowledgeBase.add("files", gson.toJsonTree(fileNames));

        body.add("knowledge_base", knowledgeBase);

        HttpResponse<String> response = Unirest
                .put(this.url + "/api/projects/" + project + "/knowledge_bases/" + knowledgeBaseName)
                .header("Content-Type", "application/json")
                .body(body.toString())
                .asString();

        raiseForStatus(response);
    }

    public void insertWebpagesIntoKnowledgeBase(String project, String knowledgeBaseName, List<String> urls,
            int crawlDepth, List<String> filters) throws UnirestException {
        JsonObject body = new JsonObject();
        JsonObject knowledgeBase = new JsonObject();
        knowledgeBase.add("urls", gson.toJsonTree(urls));
        knowledgeBase.addProperty("crawl_depth", crawlDepth);
        knowledgeBase.add("filters", gson.toJsonTree(filters != null ? filters : List.of()));

        body.add("knowledge_base", knowledgeBase);

        HttpResponse<String> response = Unirest
                .put(this.url + "/api/projects/" + project + "/knowledge_bases/" + knowledgeBaseName)
                .header("Content-Type", "application/json")
                .body(body.toString())
                .asString();

        raiseForStatus(response);
    }

    public String getUrl() {
        return url;
    }
}
