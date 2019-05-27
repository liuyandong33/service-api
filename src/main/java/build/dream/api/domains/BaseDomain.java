package build.dream.api.domains;

import build.dream.common.annotations.GeneratedValue;
import build.dream.common.annotations.GenerationType;
import build.dream.common.annotations.Id;

import java.io.Serializable;
import java.math.BigInteger;

public class BaseDomain implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO_INCREMENT)
    private BigInteger id;

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }
}
