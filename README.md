# Java MindsDB SDK

The MindsDB Java SDK allows you to connect to a MindsDB server from Java using the HTTP API.

## Installation

To use the MindsDB Java SDK, include the following dependency in your `pom.xml`:

```xml
<dependency>
    <groupId>com.mindsdb</groupId>
    <artifactId>mindsdb-java-sdk</artifactId>
    <version>1.0.2</version>
</dependency>
```

## Example

### Connecting to the MindsDB server

You can establish a connection to the MindsDB server using the SDK. Here are some examples:

#### Connect to a local MindsDB server

```java
import mindsdb.MindsDB;
import mindsdb.services.Server;

public class Main {
    public static void main(String[] args) {
        // Running locally on http://127.0.0.1:47334 or http://localhost:47334
        Server server = MindsDB.connect(); 
        Server serverWithUrl = MindsDB.connect("http://127.0.0.1:47334");
    }
}
```


### Basic usage

Once connected to the server, you can perform various operations. Here are some examples:

#### Get a list of databases

```java
import mindsdb.models.Database;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Server server = MindsDB.connect();
        List<Database> databases = server.listDatabases();
    }
}
```

#### Get a specific database

```java
import mindsdb.models.Database;

public class Main {
    public static void main(String[] args) {
        Server server = MindsDB.connect();
        Database database = server.getDatabase("my_database");
    }
}
```

#### Perform an SQL query

```java
import mindsdb.models.Database;
import mindsdb.models.Query;

public class Main {
    public static void main(String[] args) {
        Server server = MindsDB.connect();
        Database database = server.getDatabase("my_database");
        Query query = database.query("SELECT * FROM table1");
        System.out.println(query.fetch());
    }
}
```

#### Create a table

```java
import mindsdb.models.Database;
import mindsdb.models.MDBTable;
import tech.tablesaw.api.Table;

public class Main {
    public static void main(String[] args) {
        Server server = MindsDB.connect();
        Database database = server.getDatabase("my_database");
        Table tableData = Table.read().csv("path/to/csv");
        MDBTable table = database.createTable("table2", tableData, true);
    }
}
```

#### Get a project

```java
import mindsdb.models.Project;

public class Main {
    public static void main(String[] args) {
        Server server = MindsDB.connect();
        Project project = server.getProject("my_project");
    }
}
```

#### Perform an SQL query within a project

```java
import mindsdb.models.Project;
import mindsdb.models.Query;

public class Main {
    public static void main(String[] args) {
        Server server = MindsDB.connect();
        Project project = server.getProject("my_project");
        Query query = project.query("SELECT * FROM database.table JOIN model1");
    }
}
```

#### Create a view

```java
import mindsdb.models.Project;
import mindsdb.models.View;
import mindsdb.models.Query;

public class Main {
    public static void main(String[] args) {
        Server server = MindsDB.connect();
        Project project = server.getProject("my_project");
        Query query = project.query("SELECT * FROM database.table JOIN model1");
        View view = project.createView("view1", query);
    }
}
```

#### Get a list of views

```java
import mindsdb.models.Project;
import mindsdb.models.View;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Server server = MindsDB.connect();
        Project project = server.getProject("my_project");
        List<View> views = project.listViews();
        View view = views.get(0);
        System.out.println(view.fetch());
    }
}
```

#### Get a list of models

```java
import mindsdb.models.Project;
import mindsdb.models.Model;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Server server = MindsDB.connect();
        Project project = server.getProject("my_project");
        List<Model> models = project.listModels();
        Model model = models.get(0);
    }
}
```

#### Use a model for prediction

```java
import mindsdb.models.Project;
import mindsdb.models.Model;
import tech.tablesaw.api.Table;

public class Main {
    public static void main(String[] args) {
        Server server = MindsDB.connect();
        Project project = server.getProject("my_project");
        Model model = project.getModel("model1");
        Table inputData = Table.read().csv("path/to/csv");
        Table result = model.predict(inputData);
        System.out.println(result);
    }
}
```

#### Create a model

```java
import mindsdb.models.Project;
import mindsdb.models.Model;
import mindsdb.models.Query;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Server server = MindsDB.connect();
        Project project = server.getProject("my_project");
        Query query = project.query("SELECT * FROM database.table");
        Map<String, Object> timeseriesOptions = new HashMap<>();
        timeseriesOptions.put("order", "date");
        timeseriesOptions.put("window", 5);
        timeseriesOptions.put("horizon", 1);
        Model model = project.createModel("rentals_model", "price", query, timeseriesOptions);
    }
}
```

#### Describe a model

```java
import mindsdb.models.Project;
import mindsdb.models.Model;

public class Main {
    public static void main(String[] args) {
        Server server = MindsDB.connect();
        Project project = server.getProject("my_project");
        Model model = project.getModel("rentals_model");
        System.out.println(model.describe());
    }
}
```

## Examples

You can find more examples in the [examples directory](https://github.com/mindsdb/mindsdb_java_sdk/tree/staging/examples).

## API Documentation

The API documentation for the MindsDB SDK can be found at [https://mindsdb.github.io/mindsdb_java_sdk/](https://mindsdb.github.io/mindsdb_java_sdk/).

### Generating API docs locally:

```sh
cd docs
mvn clean install
mvn site
```

The online documentation is automatically updated by pushing changes to the `docs` branch.

## Testing

To run all the tests for the components, use the following command:

```sh
mvn test
```

## Contributing

We welcome contributions to the MindsDB SDK. If you'd like to contribute, please refer to the contribution guidelines for more information.

## License

The MindsDB SDK is licensed under the MIT License. Feel free to use and modify it according to your needs.