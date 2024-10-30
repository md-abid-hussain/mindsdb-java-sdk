
package mindsdb.services;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import mindsdb.connectors.RestAPI;
import mindsdb.models.Database;
import mindsdb.models.KnowledgeBase;
import mindsdb.models.Model;
import mindsdb.models.Project;
import mindsdb.models.agent.Agent;
import mindsdb.models.agent.AgentCompletion;
import mindsdb.models.skill.Skill;
import mindsdb.utils.HttpException;

/**
 * The `Agents` class provides methods to manage agents within a MindsDB
 * project.
 * It allows for creating, updating, listing, and deleting agents, as well as
 * adding files, web pages, and databases to agents. The class interacts with
 * various components such as knowledge bases, models, skills, databases, and
 * machine learning engines to facilitate these operations.
 * 
 * <p>
 * Key functionalities include:
 * </p>
 * <ul>
 * <li>Listing all agents in a project</li>
 * <li>Retrieving a specific agent by name</li>
 * <li>Creating new agents with specified models, providers, skills, and
 * parameters</li>
 * <li>Updating existing agents</li>
 * <li>Deleting agents</li>
 * <li>Adding files, web pages, and databases to agents</li>
 * <li>Validating URLs</li>
 * </ul>
 * 
 * <p>
 * The class relies on the `RestAPI` for communication with the MindsDB backend
 * and uses
 * various models such as `Agent`, `AgentCompletion`, `KnowledgeBase`, `Model`,
 * `Project`,
 * `Skill`, and `Database` to represent and manipulate data.
 * </p>
 * 
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>{@code
 * Project project = new Project("project_name");
 * RestAPI api = new RestAPI("api_url");
 * Agents agentsService = new Agents(project, api);
 * 
 * // List all agents
 * List<Agent> agents = agentsService.list();
 * 
 * // Create a new agent
 * Agent newAgent = agentsService.create("agent_name", "model_name", "provider_name", skills, params);
 * 
 * // Add a file to an agent
 * agentsService.addFile("agent_name", "file_path", "file_description", "knowledge_base_name");
 * }</pre>
 * 
 * @see mindsdb.connectors.RestAPI
 * @see mindsdb.models.agent.Agent
 * @see mindsdb.models.agent.AgentCompletion
 * @see mindsdb.models.KnowledgeBase
 * @see mindsdb.models.Model
 * @see mindsdb.models.Project
 * @see mindsdb.models.skill.Skill
 * @see mindsdb.models.Database
 * @see mindsdb.utils.HttpException
 */
public class Agents {

    private final Project project;
    private final RestAPI api;
    private final KnowledgeBases knowledgeBases;
    private final Models models;
    private final Skills skills;
    private final Databases databases;
    private final MLEngines mlEngines;

    private static final String DEFAULT_LLM_MODEL = "gpt-4o";
    private static final String DEFAULT_LLM_PROMPT = "Answer the user's question in a helpful way: {{question}}";

    /**
     * Constructor for the `Agents` class.
     * 
     * @param project - Project object
     * @param api     - RestAPI object
     */
    public Agents(Project project, RestAPI api) {
        this.project = project;
        this.api = api;

        this.knowledgeBases = project.getKnowledgeBases();
        this.models = project.getModels();
        this.skills = project.getSkills();

        this.databases = project.getDatabases();
        this.mlEngines = project.getMlEngines();
    }

    /**
     * Create a default knowledge base
     * 
     * @param agent - Agent object
     * @param name  - Name of the knowledge base
     * @return KnowledgeBase object
     */
    private KnowledgeBase createDefaultKnowledgeBase(Agent agent, String name) {
        try {
            this.mlEngines.get("langchain_embedding");
        } catch (IllegalArgumentException e) {
            this.mlEngines.create("langchain_embedding", "langchain_embedding", null);
        }

        KnowledgeBase kb;

        if (agent.getProvider().equals("mindsdb")) {
            Model agentModel = this.models.getModel(agent.getModelName());
            Gson gson = new Gson();
            JsonElement jsonElement = gson
                    .toJsonTree(agentModel.getData().getOrDefault("training_options", new HashMap<String, Object>()));
            Map<String, Object> trainingOptions = gson.fromJson(jsonElement,
                    new com.google.gson.reflect.TypeToken<Map<String, Object>>() {
                    }.getType());

            JsonElement jsonElementUsing = gson
                    .toJsonTree(trainingOptions.getOrDefault("using", new HashMap<String, Object>()));
            Map<String, Object> trainingOptionsUsing = gson.fromJson(jsonElementUsing,
                    new com.google.gson.reflect.TypeToken<Map<String, Object>>() {
                    }.getType());

            Map<String, Object> apiKeyParams = new HashMap<>();
            for (Map.Entry<String, Object> entry : trainingOptionsUsing.entrySet()) {
                if (entry.getKey().contains("api_key")) {
                    apiKeyParams.put(entry.getKey(), (String) entry.getValue());
                }
            }
            kb = this.knowledgeBases.create(name, null, null, null, null, null, apiKeyParams);
        } else {
            kb = this.knowledgeBases.create(name, null, null, null, null, null, null);
        }

        try {
            kb.getModel().waitComplete();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return kb;

    }

    /**
     * Validate a URL
     * 
     * @param url - URL
     * @return boolean
     */
    private boolean validateUrl(String url) {
        try {
            URI uri = new URI(url);
            uri.getScheme();
            uri.getHost();
            uri.getPath();
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    /**
     * List all agents
     * 
     * @return List of agents
     */
    public List<Agent> list() {
        JsonArray jsonAgents = api.agents(project.getName());
        List<Agent> agents = new ArrayList<>();
        for (JsonElement jsonAgent : jsonAgents) {
            JsonObject agentData = jsonAgent.getAsJsonObject();
            Agent agent = Agent.fromJson(agentData, this);
            agents.add(agent);
        }

        return agents;
    }

    /**
     * Get an agent by name
     * 
     * @param name Name of the agent
     * @return Agent
     */
    public Agent get(String name) {
        JsonObject jsonAgent = api.agent(project.getName(), name);
        return Agent.fromJson(jsonAgent, this);
    }

    /**
     * Get the completion of an agent
     * 
     * @param name     - Name of the agent
     * @param messages - List of messages
     * @return AgentCompletion object
     */
    public AgentCompletion completion(String name, List<JsonObject> messages) {
        JsonObject data = api.agentCompletion(this.project.getName(), name, messages);

        JsonObject message = data.getAsJsonObject("message");

        String content = message.get("content").getAsString();
        if (message.has("context")) {
            List<String> context = new ArrayList<>();
            message.getAsJsonArray("context").forEach(jsonElement -> context.add(jsonElement.getAsString()));
            return new AgentCompletion(content, context);
        }

        return new AgentCompletion(content, null);
    }

    /**
     * Add files to an agent
     * 
     * @param name          - Name of the agent
     * @param filePaths     - List of file paths
     * @param description   - Description of the files
     * @param knowledgeBase - Name of the knowledge base
     */
    public void addFiles(String name, List<String> filePaths, String description, String knowledgeBase) {
        if (filePaths == null) {
            return;
        }

        String fileNameNoExtension = "";
        List<String> allFileNames = new ArrayList<>();

        for (String filePath : filePaths) {
            String fileName = filePath.substring(filePath.lastIndexOf('/') + 1).toLowerCase();
            fileNameNoExtension = fileName.substring(0, fileName.lastIndexOf("."));
            allFileNames.add(fileNameNoExtension);

            try {
                this.api.getFileMetadata(fileNameNoExtension);
            } catch (HttpException e) {
                if (e.getStatusCode() >= 400 && e.getStatusCode() != 404) {
                    throw e;
                }
                this.api.uploadFile(fileName, filePath);
            }
        }

        Agent agent = this.get(name);
        KnowledgeBase kb;
        if (knowledgeBase != null) {
            kb = this.knowledgeBases.get(knowledgeBase);
        } else {
            String kbName = String.format("%s_%s_%s_kb", name.toLowerCase(), fileNameNoExtension,
                    UUID.randomUUID().toString().replace("-", ""));
            kb = createDefaultKnowledgeBase(agent, kbName);
        }

        kb.insertFiles(allFileNames);
        String skillName = String.format("%s_retrieval_skill_%s", fileNameNoExtension,
                UUID.randomUUID().toString().replace("-", ""));
        Map<String, Object> retrievalParams = Map.of(
                "source", kb.getName(),
                "description", description);
        Skill fileRetrievalSkill = this.skills.create(skillName, "retrieval", retrievalParams);
        agent.getSkills().add(fileRetrievalSkill);
        this.update(agent.getName(), agent);
    }

    /**
     * Add a file to an agent
     * 
     * @param name          - Name of the agent
     * @param filePath      - File path
     * @param description   - Description of the file
     * @param knowledgeBase - Name of the knowledge base
     */
    public void addFile(String name, String filePath, String description, String knowledgeBase) {
        this.addFiles(name, List.of(filePath), description, knowledgeBase);
    }

    /**
     * Add web pages to an agent
     * 
     * @param name          - Name of the agent
     * @param urls          - List of URLs
     * @param description   - Description of the web pages
     * @param knowledgeBase - Name of the knowledge base
     * @param crawlDepth    - Crawl depth
     * @param filters       - List of filters
     */
    public void addWebPages(String name, List<String> urls, String description, String knowledgeBase,
            Integer crawlDepth, List<String> filters) {
        if (urls == null || urls.isEmpty()) {
            return;
        }

        for (String url : urls) {
            if (!validateUrl(url)) {
                throw new IllegalArgumentException("Invalid URL");
            }
        }

        Agent agent = this.get(name);
        KnowledgeBase kb;

        if (knowledgeBase != null) {
            kb = this.knowledgeBases.get(knowledgeBase);
        } else {
            String kbName = String.format("%s_kb_%s", name, UUID.randomUUID().toString().replace("-", ""));
            kb = createDefaultKnowledgeBase(agent, kbName);
        }

        if (crawlDepth == null) {
            crawlDepth = 1;
        }

        kb.insertWebpages(urls, crawlDepth, filters);

        String skillName = String.format("web_retrieval_skill_%s", UUID.randomUUID().toString().replace("-", ""));
        Map<String, Object> retrievalParams = Map.of(
                "source", kb.getName(),
                "description", description);

        Skill webRetrievalSkill = this.skills.create(skillName, "retrieval", retrievalParams);
        agent.getSkills().add(webRetrievalSkill);
        this.update(agent.getName(), agent);

    }

    /**
     * Add a web page to an agent
     * 
     * @param name          - Name of the agent
     * @param url           - URL
     * @param description   - Description of the web page
     * @param knowledgeBase - Name of the knowledge base
     * @param crawlDepth    - Crawl depth
     * @param filters       - List of filters
     */
    public void addWebPage(String name, String url, String description, String knowledgeBase, Integer crawlDepth,
            List<String> filters) {
        this.addWebPages(name, List.of(url), description, knowledgeBase, crawlDepth, filters);
    }

    /**
     * Add databases to an agent
     * @param name        - Name of the agent
     * @param database    - Name of the database
     * @param tables      - List of tables
     * @param description - Description of the database
     */
    public void addDatabase(String name, String database, List<String> tables, String description) {
        Database db = this.databases.get(name);
        Set<String> tableSet = Set.of(
                db.listTables().stream().map(t -> t.getName()).toArray(String[]::new));

        for (String table : tables) {
            if (!tableSet.contains(table)) {
                throw new IllegalArgumentException("Table not found");
            }
        }

        String skillName = String.format("%s_sql_skill_%s", database, UUID.randomUUID().toString().replace("-", ""));
        Map<String, Object> sqlParams = Map.of(
                "database", database,
                "tables", tables,
                "description", description);

        Skill databaseSqlSkill = this.skills.create(skillName, "sql", sqlParams);
        Agent agent = this.get(name);
        if (agent.getParams() == null) {
            agent.setParams(new JsonObject());
        }

        if (!agent.getParams().has("prompt_template")) {
            agent.getParams().addProperty("prompt_template", "using mindsdb sqltoolbox");
        }

        agent.getSkills().add(databaseSqlSkill);
        this.update(agent.getName(), agent);
    }

    /**
     * Create an agent
     * 
     * @param name     - Name of the agent
     * @param model    - Model name
     * @param provider - Provider
     * @param skills   - List of skills
     * @param params   - Parameters
     * @return Agent
     */
    public Agent create(String name, String model, String provider, List<String> skills, Map<String, Object> params) {
        List<String> skillNames = new ArrayList<>();

        if (skills != null && !skills.isEmpty()) {
            for (String skill : skills) {
                this.skills.get(skill);
                skillNames.add(skill);
            }
        }

        Map<String, Object> agentParams;
        if (params != null) {
            agentParams = new HashMap<>(params);
        } else {
            agentParams = new HashMap<>();
        }
        if (!agentParams.containsKey("prompt_template")) {
            agentParams.put("prompt_template", DEFAULT_LLM_PROMPT);
        }

        if (model == null) {
            model = DEFAULT_LLM_MODEL;
        }

        JsonObject agentData = this.api.createAgent(this.project.getName(), name, model, provider, skillNames, agentParams);

        Agent agent = Agent.fromJson(agentData, this);

        return agent;
    }

    /**
     * Create an agent
     * 
     * @param name - Name of the agent
     * @return  Agent
     */
    public Agent create(String name){
        return create(name, null, null, null, null);
    }

    /**
     * Update an agent
     * 
     * @param name         - Name of the agent
     * @param updatedAgent - Updated agent
     */
    public void update(String name, Agent updatedAgent) {
        Set<String> updatedSkills = new HashSet<>();

        for (Skill skill : updatedAgent.getSkills()) {
            try {
                this.skills.get(skill.getName());
                updatedSkills.add(skill.getName());
            } catch (HttpException e) {
                if (e.getStatusCode() != 404) {
                    throw e;

                } else {
                    this.skills.create(skill.getName(), skill.getType(), skill.getParams());
                    updatedSkills.add(skill.getName());
                }
            }
        }

        JsonObject existingAgent = this.api.agent(this.project.getName(), name);
        Set<String> existingSkills = new HashSet<>();
        for (JsonElement skillElement : existingAgent.getAsJsonArray("skills")) {
            existingSkills.add(skillElement.getAsJsonObject().get("name").getAsString());
        }

        Set<String> skillsToAdd = new HashSet<>(updatedSkills);
        skillsToAdd.removeAll(existingSkills);
        Set<String> skillsToRemove = new HashSet<>(existingSkills);
        skillsToRemove.removeAll(updatedSkills);

        this.api.updateAgent(this.project.getName(), name, updatedAgent.getName(), updatedAgent.getModelName(),
                skillsToAdd.stream().collect(Collectors.toList()), skillsToRemove.stream().collect(Collectors.toList()),
                updatedAgent.getParams());
    }

    /**
     * Drop an agent
     * 
     * @param name Name of the agent
     */
    public void drop(String name) {
        this.api.deleteAgent(this.project.getName(), name);
    }

}
