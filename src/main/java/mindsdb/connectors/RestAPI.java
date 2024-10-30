package mindsdb.connectors;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import kong.unirest.core.HttpResponse;
import kong.unirest.core.JsonNode;
import kong.unirest.core.Unirest;
import kong.unirest.core.UnirestException;
import kong.unirest.core.UnirestInstance;
import kong.unirest.core.json.JSONArray;
import kong.unirest.core.json.JSONObject;
import mindsdb.utils.HttpException;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

/**
 * RestAPI class for handling REST API requests.
 */
public final class RestAPI {

    private final String url;
    private final String username;
    private final String password;
    private final String apiKey;
    private final boolean isManaged;
    private final Gson gson = new Gson();
    private UnirestInstance session;

    /**
     * Constructor for RestAPI
     * @param url           URL to MindsDB server
     * @param login         User login, for cloud version it contains email
     * @param password      User password to login (for cloud version)
     * @param apiKey        API key to authenticate (for cloud version)
     * @param isManaged     Whether or not the URL points to a managed instance
     * @param headers       Additional headers to send with the connection, optional
     * 
     */
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

    /**
     * Get the URL of the MindsDB server
     * @return URL of the MindsDB server
     */
    public String getUrl() {
        return this.url;
    }

    private void raiseForStatus(HttpResponse<String> response) {
        if (response.getStatus() >= 400 && response.getStatus() < 600) {
            throw new HttpException(response.getStatus(), response.getBody());
        }
    }

    /**
     * Login to the MindsDB server
     */
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

    /**
     * Execute a SQL query to the MindsDB server
     * 
     * @param sql      SQL query to execute
     * @param database Database to execute the query on
     * @return Tablesaw Table containing the result of the query
     */
    public Table sqlQuery(String sql, String database) {
        if (database == null) {
            database = "mindsdb";
        }
        String endpointUrl = this.url + "/api/sql/query";
        String sqlPayload = String.format("{\"query\": \"%s\", \"context\": {\"db\": \"%s\"}}", sql, database);
        try {
            HttpResponse<String> response = session.post(endpointUrl)
                    .header("Content-Type", "application/json")
                    .body(sqlPayload)
                    .asString();

            if (response.getStatus() >= 400) {
                throw new RuntimeException("SQL Query failed: " + response.getBody());
            }

            JSONObject data = new JSONObject(response.getBody());

            if (data.getString("type").equals("table")) {
                JSONArray columns = data.getJSONArray("column_names");
                JSONArray rows = data.getJSONArray("data");

                Table df = Table.create();
                for (Object col : columns) {
                    df.addColumns(StringColumn.create(col.toString().toLowerCase()));
                }
                for (int index = 0; index < rows.length(); index++) {
                    for (int j = 0; j < columns.length(); j++) {
                        String r = rows.getJSONArray(index).get(j) != null ? rows.getJSONArray(index).get(j).toString()
                                : null;
                        df.column(j).appendCell(r);
                    }
                }

                return df;

            }
            if (data.getString("type").equals("error")) {
                throw new RuntimeException("SQL Query failed: " + data.getString("error_message"));
            }

            return null;
        } catch (UnirestException e) {
            throw new RuntimeException("SQL Query execution failed: " + e.getMessage(), e);
        }
    }

    /**
     * Execute a SQL query to the MindsDB server
     * 
     * @param sql SQL query to execute
     * @return Tablesaw Table containing the result of the query in form of table
     */
    public Table sqlQuery(String sql) {
        return sqlQuery(sql, null);
    }

    /**
     * Close the session
     */
    public void closeSession() {
        this.session.close();
    }

    /**
     * Create a new agent
     * 
     * @param project  Name of the project
     * @param name     Name of the Agent
     * @param model    Name of the model
     * @param provider Name of the provider
     * @param skills   List of skills
     * @param params   Additional parameters
     * @throws UnirestException when the request fails when the request fails
     * @return JsonObject containing the agent details
     */
    public JsonObject createAgent(String project, String name, String model, String provider, List<String> skills,
            Map<String, Object> params) {
        String endpoint = this.url + "/api/projects/" + project + "/agents";
        JsonObject agentDetails = new JsonObject();

        agentDetails.addProperty("name", name);
        agentDetails.addProperty("model_name", model);
        agentDetails.add("skills", gson.toJsonTree(skills));
        agentDetails.add("params", gson.toJsonTree(params));
        agentDetails.add("provider", gson.toJsonTree(provider));

        JsonObject payload = new JsonObject();
        payload.add("agent", agentDetails);

        HttpResponse<String> response = this.session.post(endpoint)
                .header("Content-Type", "application/json")
                .body(payload)
                .asString();

        raiseForStatus(response);

        return gson.fromJson(response.getBody(), JsonObject.class);
    }

    /**
     * Update an agent
     * @param project      Project name 
     * @param name          Agent name
     * @param updatedName   Updated agent name
     * @param updatedModel Updated model name
     * @param skillsToAdd  List of skills to add
     * @param skillsToRemove    List of skills to remove
     * @param updatedParams    Updated parameters
     * @return  JsonObject containing the updated agent details
     */
    public JsonObject updateAgent(String project, String name, String updatedName, String updatedModel,
            List<String> skillsToAdd, List<String> skillsToRemove, JsonObject updatedParams) {
        String endpoint = this.url + "/api/projects/" + project + "/agents/" + name;
        JsonObject agentDetails = new JsonObject();
        agentDetails.addProperty("name", updatedName);
        agentDetails.addProperty("model_name", updatedModel);
        agentDetails.add("skills_to_add", gson.toJsonTree(skillsToAdd));
        agentDetails.add("skills_to_remove", gson.toJsonTree(skillsToRemove));
        agentDetails.add("params", gson.toJsonTree(updatedParams));

        JsonObject body = new JsonObject();
        body.add("agent", agentDetails);

        HttpResponse<String> response = session.put(endpoint)
                .header("Content-Type", "application/json")
                .body(body.toString())
                .asString();
        raiseForStatus(response);

        return gson.fromJson(response.getBody(), JsonObject.class);
    }

    
    private void uploadByom(String name, String code, String requirements) throws UnirestException {
        HttpResponse<String> response = Unirest.put(this.url + "/api/handlers/byom/" + name)
                .field("code", code)
                .field("modules", requirements)
                .asString();

        raiseForStatus(response);
    }

    /**
     * Upload a custom handler to MindsDB
     * @return JsonObject containing the response
     *  @throws UnirestException  when the request fails
     */
    public JsonObject status() throws UnirestException {
        HttpResponse<String> response = session.get(this.url + "/api/status").asString();

        raiseForStatus(response);

        return gson.fromJson(response.getBody(), JsonObject.class);
    }

    /**
     * Get the status of the MindsDB server
     * @param project Name of the project
     * @return JsonObject containing the status of the project
     * @throws UnirestException when the request fails
     */
    public JsonArray agents(String project) throws UnirestException {
        HttpResponse<String> response = session.get(this.url + "/api/projects/" + project + "/agents").asString();

        raiseForStatus(response);

        return gson.fromJson(response.getBody(), JsonArray.class);
    }

    /**
     * Get the status of the MindsDB server 
     * @param project Name of the project
     * @param name  Name of the agent
     * @return JsonObject containing the status of the agent
     * @throws UnirestException when the request fails
     */
    public JsonObject agent(String project, String name) throws UnirestException {
        HttpResponse<String> response = session.get(this.url + "/api/projects/" + project + "/agents/" + name)
                .asString();

        raiseForStatus(response);

        return gson.fromJson(response.getBody(), JsonObject.class);
    }

    /**
     * Get the status of the MindsDB server
     * @param project   Name of the project
     * @param name    Name of the agent
     * @param messages  List of messages
     * @return  JsonObject containing the completion of the agent
     * @throws UnirestException when the request fails 
     */
    public JsonObject agentCompletion(String project, String name, List<JsonObject> messages) throws UnirestException {
        JsonObject body = new JsonObject();
        body.add("messages", gson.toJsonTree(messages));

        HttpResponse<String> response = session
                .post(this.url + "/api/projects/" + project + "/agents/" + name + "/completions")
                .header("Content-Type", "application/json")
                .body(body.toString())
                .asString();

        raiseForStatus(response);

        return gson.fromJson(response.getBody(), JsonObject.class);
    }

    /**
     * Delete an agent  
     * @param project   Name of the project
     * @param name  Name of the agent
     * @throws UnirestException when the request fails     
     */
    public void deleteAgent(String project, String name) throws UnirestException {
        HttpResponse<String> response = Unirest.delete(this.url + "/api/projects/" + project + "/agents/" + name)
                .asString();

        raiseForStatus(response);
    }

    /**
     *  Get skills of a project
     * @param project Name of the project
     * @return  JsonArray containing the skills of the project
     * @throws UnirestException when the request fails     
     */
    public JsonArray skills(String project) throws UnirestException {
        HttpResponse<String> response = session.get(this.url + "/api/projects/" + project + "/skills").asString();

        raiseForStatus(response);

        return gson.fromJson(response.getBody(), JsonArray.class);
    }

    /**
     * Get a skill of a project
     * @param project   Name of the project 
     * @param name  Name of the skill
     * @return  JsonObject containing the skill details
     * @throws UnirestException when the request fails
     */
    public JsonObject skill(String project, String name) throws UnirestException {
        HttpResponse<String> response = session.get(this.url + "/api/projects/" + project + "/skills/" + name)
                .asString();

        raiseForStatus(response);

        return gson.fromJson(response.getBody(), JsonObject.class);
    }

    /**
     * Create a skill
     * @param project  Name of the project
     * @param name  Name of the skill   
     * @param type  Type of the skill
     * @param params    Parameters of the skill
     * @throws UnirestException when the request fails
     */
    public void createSkill(String project, String name, String type, JsonObject params) throws UnirestException {
        JsonObject body = new JsonObject();
        JsonObject skill = new JsonObject();
        skill.addProperty("name", name);
        skill.addProperty("type", type);
        skill.add("params", params);

        body.add("skill", skill);

        HttpResponse<String> response = session.post(this.url + "/api/projects/" + project + "/skills")
                .header("Content-Type", "application/json")
                .body(body.toString())
                .asString();

        raiseForStatus(response);
    }

    /**
     * Update a skill
     * @param project   Name of the project
     * @param name  Name of the skill
     * @param updatedName   Updated name of the skill
     * @param updatedType   Updated type of the skill
     * @param updatedParams Updated parameters of the skill
     * @throws UnirestException when the request fails
     */
    public void updateSkill(String project, String name, String updatedName, String updatedType,
            JsonObject updatedParams) throws UnirestException {
        JsonObject body = new JsonObject();
        JsonObject skill = new JsonObject();
        skill.addProperty("name", updatedName);
        skill.addProperty("type", updatedType);
        skill.add("params", updatedParams);

        body.add("skill", skill);

        HttpResponse<String> response = session.put(this.url + "/api/projects/" + project + "/skills/" + name)
                .header("Content-Type", "application/json")
                .body(body.toString())
                .asString();

        raiseForStatus(response);
    }

    /**
     * Delete a skill
     * @param project  Name of the project
     * @param name  Name of the skill
     * @throws UnirestException when the request fails 
     */
    public void deleteSkill(String project, String name) throws UnirestException {
        HttpResponse<String> response = session.delete(this.url + "/api/projects/" + project + "/skills/" + name)
                .asString();

        raiseForStatus(response);
    }

    /**
     * Insert files into a knowledge base
     * @param project   Name of the project
     * @param knowledgeBaseName     Name of the knowledge base
     * @param fileNames     List of file names
     * @throws UnirestException when the request fails 
     */
    public void insertFilesIntoKnowledgeBase(String project, String knowledgeBaseName, List<String> fileNames)
            throws UnirestException {
        JsonObject body = new JsonObject();
        JsonObject knowledgeBase = new JsonObject();
        knowledgeBase.add("files", gson.toJsonTree(fileNames));

        body.add("knowledge_base", knowledgeBase);

        HttpResponse<String> response = session
                .put(this.url + "/api/projects/" + project + "/knowledge_bases/" + knowledgeBaseName)
                .header("Content-Type", "application/json")
                .body(body.toString())
                .asString();

        raiseForStatus(response);
    }

    /**
     * Insert webpages into a knowledge base
     * @param project  Name of the project
     * @param knowledgeBaseName         Name of the knowledge base
     * @param urls  List of URLs
     * @param crawlDepth    Crawl depth
     * @param filters   List of filters
     * @throws UnirestException when the request fails
     */
    public void insertWebpagesIntoKnowledgeBase(String project, String knowledgeBaseName, List<String> urls,
            int crawlDepth, List<String> filters) throws UnirestException {
        JsonObject body = new JsonObject();
        JsonObject knowledgeBase = new JsonObject();
        knowledgeBase.add("urls", gson.toJsonTree(urls));
        knowledgeBase.addProperty("crawl_depth", crawlDepth);
        knowledgeBase.add("filters", gson.toJsonTree(filters != null ? filters : List.of()));

        body.add("knowledge_base", knowledgeBase);

        HttpResponse<String> response = session
                .put(this.url + "/api/projects/" + project + "/knowledge_bases/" + knowledgeBaseName)
                .header("Content-Type", "application/json")
                .body(body.toString())
                .asString();

        raiseForStatus(response);
    }

    /**
     * Insert data into a knowledge base
     * @param item Name of the item
     * @return Tablesaw Table containing the data
     */
    public Table objectsTree(String item) {
        HttpResponse<String> response = this.session.get(this.url + "/api/tree/" + item)
                .header("Content-Type", "application/json")
                .asString();
        raiseForStatus(response);
        JsonArray data = gson.fromJson(response.getBody(), JsonArray.class);

        Table table = Table.create();
        if (data.size() > 0) {
            JsonObject firstRow = data.get(0).getAsJsonObject();
            for (String key : firstRow.keySet()) {
                table.addColumns(StringColumn.create(key));
            }

            for (JsonElement element : data) {
                JsonObject row = element.getAsJsonObject();
                for (String key : row.keySet()) {
                    String value = row.get(key).isJsonNull() ? null : row.get(key).getAsString();
                    table.stringColumn(key).append(value);
                }
            }
        }

        return table;
    }

    /**
     * Insert data into a knowledge base
     * @return Tablesaw Table containing the data
     */
    public Table objectsTree() {
        return objectsTree("");
    }

    /**
     * Make a batch prediction using a MindsDB model
     * 
     * @param project Name of the project
     * @param model   Name of the model
     * @param data    Tablesaw table containing the data
     * @param params  Additional params for model
     * @param version Version of model to use
     * @return Tablesaw table object containing prediction
     */
    public Table modelPredict(String project, String model, Table data, Map<String, String> params, Integer version) {
        String modelName = model + (version != null ? "." + version : "");
        if (params == null) {
            params = Map.of();
        }

        JSONArray dataJson = new JSONArray();
        for (int i = 0; i < data.rowCount(); i++) {
            JSONObject row = new JSONObject();
            for (int j = 0; j < data.columnCount(); j++) {
                row.put(data.columnNames().get(j), data.get(i, j));
            }
            dataJson.put(row);
        }

        String endpointUrl = this.url + "/api/projects/" + project + "/models/" + modelName + "/predict";
        HttpResponse<String> response = session.post(endpointUrl)
                .header("Content-Type", "application/json")
                .body(new JSONObject().put("data", dataJson).put("params", params).toString())
                .asString();

        raiseForStatus(response);

        Table df = Table.create();
        JSONArray responseData = new JSONArray(response.getBody());
        if (responseData.length() > 0) {
            JSONObject firstRow = responseData.getJSONObject(0);
            for (String key : firstRow.keySet()) {
                df.addColumns(StringColumn.create(key));
            }

            for (int i = 0; i < responseData.length(); i++) {
                JSONObject rowData = responseData.getJSONObject(i);
                for (String key : rowData.keySet()) {
                    Object value = rowData.get(key);
                    if (value instanceof JSONArray jSONArray) {
                        // Convert JSONArray to a string representation
                        value = jSONArray.toString();
                    } else if (rowData.isNull(key)) {
                        value = null;
                    }
                    df.stringColumn(key).append(value != null ? value.toString() : null);
                }
            }
        }

        return df;

    }

    /**
     * Make a single prediction using a MindsDB model
     * 
     * @param project Name of field to predict
     * @param model   Name of the model
     * @param data    Data provided to model
     * @param params  Additional parameters
     * @param version Version of the model
     * @return Tablesaw Table containing the prediction
     */
    public Table modelPredict(String project, String model, Map<String, String> data, Map<String, String> params,
            Integer version) {
        String modelName = model + (version != null ? "." + version : "");
        if (params == null) {
            params = Map.of();
        }

        JSONArray dataJson = new JSONArray();

        JSONObject row = new JSONObject();
        for (String key : data.keySet()) {
            row.put(key, data.get(key));
        }
        dataJson.put(row);

        String endpointUrl = this.url + "/api/projects/" + project + "/models/" + modelName + "/predict";
        HttpResponse<String> response = session.post(endpointUrl)
                .header("Content-Type", "application/json")
                .body(new JSONObject().put("data", dataJson).put("params", params).toString())
                .asString();

        raiseForStatus(response);

        Table df = Table.create();
        JSONArray responseData = new JSONArray(response.getBody());
        if (responseData.length() > 0) {
            JSONObject firstRow = responseData.getJSONObject(0);
            for (String key : firstRow.keySet()) {
                df.addColumns(StringColumn.create(key));
            }

            for (int i = 0; i < responseData.length(); i++) {
                JSONObject rowData = responseData.getJSONObject(i);
                for (String key : rowData.keySet()) {
                    Object value = rowData.get(key);
                    if (value instanceof JSONArray jSONArray) {
                        // Convert JSONArray to a string representation
                        value = jSONArray.toString();
                    } else if (rowData.isNull(key)) {
                        value = null;
                    }
                    df.stringColumn(key).append(value != null ? value.toString() : null);
                }
            }
        }
        return df;
    }

    /**
     * Read the content of a file as bytes
     * @param filePath  Path to the file
     * @return  Byte array of the file
     */
    public static byte[] readFileAsBytes(String filePath) {
        try {
            return Files.readAllBytes(Paths.get(filePath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + e.getMessage(), e);
        }
    }

    /**
     * Read the content of dataframe as csv in bytes
     * 
     * @param dataframe DataFrame to read
     * @return DataFrame as csv in bytes
     */
    public static byte[] readDataFrameAsCSV(Table dataframe) {
        dataframe.write().csv("temp.csv");
        byte[] fileData = readFileAsBytes("temp.csv");
        File file = new File("temp.csv");
        file.delete();
        return fileData;
    }

    /**
     * Read the content of a file as a webpage
     * @param url        URL of the webpage
     * @return  Content of the webpage
     */
    public static String readFileAsWebpage(String url) {
        HttpResponse<String> response = Unirest.get(url).asString();
        return response.getBody();
    }

    /**
     * Upload a file to the MindsDB server
     * 
     * @param fileName Name of the file
     * @param data     Byte array of the file
     */
    public void uploadData(String fileName, byte[] data) {
        if (fileName.contains(".")) {
            fileName = fileName.split("\\.")[0];
        }

        String endpoint = this.url + "/api/files/" + fileName;
        var response = session.put(endpoint)
                .field("original_file_name", fileName)
                .field("name", fileName)
                .field("source_type", "file")
                .field("file", data, fileName)
                .asString();

        raiseForStatus(response);
    }

    /**
     * Upload a file to the MindsDB server
     * 
     * @param name Name of the file
     * @param data Data of the file
     */
    public void uploadFile(String name, String data) {
        byte[] fileData = readFileAsBytes(data);
        uploadData(name, fileData);
    }

    /**
     * Upload a file to the MindsDB server
     * @param name  Name of the file
     * @param data  Tablesaw Table containing the data
     */
    public void uploadFile(String name, Table data) {
        byte[] fileData = readDataFrameAsCSV(data);
        uploadData(name, fileData);
    }

    /**
     * Upload a file to the MindsDB server
     * @param name  Name of the file
     * @return  Tablesaw Table containing the data
     * @throws UnirestException when the request fails
     */
    public JsonObject getFileMetadata(String name) throws UnirestException {
        HttpResponse<String> response = session.get(url + "/api/files").asString();

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

}
