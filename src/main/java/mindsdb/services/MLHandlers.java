package mindsdb.services;

import mindsdb.connectors.RestAPI;

public class MLHandlers extends Handlers {

    public MLHandlers(RestAPI api) {
        super(api, "ml");
    }

}