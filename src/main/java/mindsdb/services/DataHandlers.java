package mindsdb.services;

import mindsdb.connectors.RestAPI;

/**
 * **DATA handlers collection**
 *
 * <p>Examples of usage:</p>
 *
 * <p>Get list:</p>
 * <pre>{@code
 * con.data_handlers.list();
 * }</pre>
 *
 * <p>Get:</p>
 * <pre>{@code
 * pg_handler = con.data_handlers.postgres;
 * pg_handler = con.data_handlers.get("postgres");
 * }</pre>
 */
public class DataHandlers extends Handlers {

    /**
     * Constructor for the `DataHandlers` class.
     * @param api - RestAPI object
     */
    public DataHandlers(RestAPI api) {
        super(api, "data");
    }

}