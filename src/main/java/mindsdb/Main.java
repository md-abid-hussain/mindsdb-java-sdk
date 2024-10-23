package mindsdb;

import mindsdb.models.KnowledgeBase;
import mindsdb.models.Project;
import mindsdb.models.agent.Agent;
import mindsdb.services.Server;

public class Main {

	public static void main(String[] args) {
		Server server = MindsDB.connect();
		// Model ligtwood = server.getModelWithVersion("home_rentals_model", 1);

		// // Make prediction with the model
		// System.out.println(ligtwood);
		// System.out.println("DATA: " + ligtwood.data);
		// System.out.println(ligtwood.version);

		// var pred = ligtwood.predict(Map.of(
		// "sqft", "823",
		// "location", "good",
		// "neighborhood", "downtown",
		// "days_on_market", "10"));

		// System.out.println(pred.column("rental_price").get(0));

		// System.out.println(ligtwood.listVersions());""

		// ligtwood.retrain("SELECT * FROM example_db.home_rentals LIMIT 10", null,
		// null, null);

		// ligtwood.setActive(1);
		// System.out.println(ligtwood.predict(
		// Map.of(
		// "sqft", "823",
		// "location", "good")));

		// String s = createModelQuery("time_series_model", "target", null, "SELECT *
		// from mindsdb.dance", null, null,
		// Map.of(
		// "order", "order_date",
		// "group", "category",
		// "window", 30,
		// "horizon", 4));
		// System.out.println(s);

		// Model m = server.models.create("home_rental_model", "home_rentals",
		// "lightwood",
		// "SELECT * FROM home_rentals", "example_db", null, Map.of("order",
		// "order_date",
		// "group", "category",
		// "window", 30,
		// "horizon", 4));

		// System.out.println(m);
		// System.out.println(m.data);
		// System.out.println(m.version);
		// System.out.println(m.getStatus());

		// Database webCrawler = server.getDatabase("crawler");

		// tech.tablesaw.api.Table tb = webCrawler
		// .query("SELECT * FROM crawler.crawler WHERE url = 'docs.mindsdb.com' LIMIT
		// 1").fetch();

		// System.out.println(tb);

		// // mindsdb.services.Query q = webCrawler
		// // .query("SELECT * FROM crawler WHERE url = 'docs.mindsdb.com' LIMIT 1");

		// Database myDb = server.getDatabase("files");

		// Table t = myDb.createTable("testing", tb, false);

		// System.out.println(t.fetch());

		// Model ollamaModel = server.models.create("ollama_embedding", "embeddings",
		// "langchain_embedding", null, null,
		// Map.of(
		// "class", "OllamaEmbeddings",
		// "model", "mistral",
		// "input_columns", List.of("content")),
		// null);

		// System.out.println(ollamaModel);

		Project mdb = server.getProject("mindsdb");

		// Skill sk = mdb.skills.get("kb_skill");
		// mdb.skills.create("kb_skill", "knowledge_base", Map.of(
		// "source", "web_kb",
		// "description", "Website data of mindsdb"));
		// System.out.println(server.agents.list());
		// Agent ag = server.agents.get("web_agent");

		// MLEngine langEmbeddingEngine = server.createMLEngine("langEmbedding",
		// "langchain_embedding");
		// Model llamaEmbedding = server.models.create("llamaEmbedding", "embeddings",
		// langEmbeddingEngine.getName(), null,
		// null, Map.of(
		// "class", "OllamaEmbeddings",
		// "model", "llama3.2"),
		// null);

		// KnowledgeBase agentKb = server.knowledgeBases.create("agent_kb",
		// llamaEmbedding, null, null, null, null, null);

		// agentKb.insertWebpages(List.of("https://mindsdb.com/blog/guide-to-building-ai-agents-key-steps-for-success",
		// "https://mindsdb.com/blog/types-of-ai-agents"), 1, null);

		// Skill kbSkill = server.skills.create("agent_kb_skill", "knowledge_base",
		// Map.of("description", "Knowledge base skill for agent", "source",

		// "agent_kb"));
		// Agent myAgent = server.agents.create("myAgent", "llama3", null,
		// List.of("agent_kb_skill"),
		// Map.of("source", "agent_kb"));

		Agent myAgent = server.agents.get("myAgent");
		// Agent myAgent = server.agents.get("myAgent");
		// Agent magent = server.agents.get("myAgent");
		// JsonObject data = new JsonObject();
		// data.addProperty("name", "myAgent");
		// data.addProperty("model_name", "llama3");
		// JsonArray skillsArray = new JsonArray();
		// skillsArray.add("agent_kb");
		// data.add("skills", skillsArray);

		// Agent updatedAgent = Agent.fromJson(data, null);

		System.out.println(myAgent.describe());

		myAgent.params.addProperty("source", "web_kb");

		server.agents.update("myAgent", myAgent);
		Agent updatedAgent = server.agents.get("myAgent");

		System.out.println(updatedAgent.describe());

		// server.agents.addWebPage("myAgent",
		// "https://docs.mindsdb.com/sdks/python/agents", "agent web", "agent_kb", 1,
		// null);

		KnowledgeBase kn = server.knowledgeBases.get("agent_kb");

		System.out.println(kn.find("mindsdb", 1).fetch());

		// System.out.println(server.listDataHandlers());
		// System.out.println(server.listMLHandlers());
		// System.out.println(kn.find(
		// "With MindsDB, you can create and deploy AI agents that comprise AI models
		// and customizable skills such as knowledge bases and text-to-SQL.",
		// 1).fetch());

		// System.out.println(myAgent.completion(List.of(Map.of("question", "What are
		// the types of ai agent?"))));

		// JSONObject kwargs = new JSONObject();
		// kwargs.put("name", "testHandler");
		// kwargs.put("title", "Test Handler");
		// kwargs.put("version", "1.0");
		// kwargs.put("description", "This is a test handler.");

		// Map<String, String> connectionArgs = new HashMap<>();
		// connectionArgs.put("username", "testUser");
		// connectionArgs.put("password", "testPass");
		// kwargs.put("connection_args", new JSONObject(connectionArgs));

		// kwargs.put("import_success", true);
		// kwargs.put("import_error", "No error");

		// Handler handler = new Handler(kwargs);
	}
}
