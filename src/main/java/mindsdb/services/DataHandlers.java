package mindsdb.services;

import mindsdb.connectors.RestAPI;

public class DataHandlers extends Handlers {

    public DataHandlers(RestAPI api) {
        super(api, "data");
    }

}