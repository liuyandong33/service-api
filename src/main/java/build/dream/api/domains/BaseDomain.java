package build.dream.api.domains;

import java.io.Serializable;
import java.math.BigInteger;

public class BaseDomain implements Serializable {
    private BigInteger id;

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }
}
