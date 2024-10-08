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
public class Auth implements Serializable {
    private boolean confirmed;
    private boolean http_auth_enabled;
    private String provider;
}
