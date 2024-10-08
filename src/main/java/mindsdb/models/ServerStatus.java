package mindsdb.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class ServerStatus implements Serializable{
    private Auth auth;
    private String environment;
    private String mindsdb_version;
}
