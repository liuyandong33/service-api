package build.dream.api.domains;


import build.dream.common.annotations.GenerationStrategy;
import build.dream.common.annotations.Id;
import build.dream.common.basic.IdDomain;
import build.dream.common.orm.SnowflakeIdGenerator;

import java.math.BigInteger;

public class BaseDomain implements IdDomain<BigInteger> {
    @Id(strategy = GenerationStrategy.GENERATOR, idGeneratorClass = SnowflakeIdGenerator.class)
    private BigInteger id;

    @Override
    public BigInteger getId() {
        return id;
    }

    @Override
    public void setId(BigInteger id) {
        this.id = id;
    }
}
