package mindsdb.client;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class ApiConfig {
    private String user;
    private String password;
    private boolean managed;
    private String apiKey;
    private String url;
}
