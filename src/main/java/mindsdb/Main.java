package mindsdb;

import mindsdb.client.Server;

public class Main {

        public static void main(String[] args) {
                Server server = MindsDB.connect();
                System.out.println(server.listDatabases());

                // Database slack = server.getDatabase("slack");
                // System.out.println(slack);

                // var q = slack.query("select * from messages");
                // System.out.println(q.fetch());

                // Database maria = server.createDatabase("maria", "mysql", Map.of("host",
                // "localhost", "port", "3306",
                // "user", "user", "password", "root", "database", "databasee"));
                // System.out.println(maria);

                server.dropDatabase("maria");
                System.out.println(server.listDatabases());
        }
}
