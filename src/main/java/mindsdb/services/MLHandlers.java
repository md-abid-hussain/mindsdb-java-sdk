package mindsdb.services;

import mindsdb.connectors.RestAPI;

/**
 * MLHandlers service class for handling ML handlers.
 */
public class MLHandlers extends Handlers {

    /**
     * Constructor for MLHandlers
     * @param api   - RestAPI object
     */
    public MLHandlers(RestAPI api) {
        super(api, "ml");
    }

}