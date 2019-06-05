package build.dream.api.models.demo;

import build.dream.api.constants.Constants;
import build.dream.common.annotations.JsonSchema;
import build.dream.common.models.BasicModel;

import javax.validation.constraints.NotNull;
import java.math.BigInteger;

@JsonSchema(value = Constants.DEMO_MODEL_JSON_SCHEMA_FILE_PATH)
public class DemoModel extends BasicModel {
    @NotNull
    private BigInteger id;

    @NotNull
    private String name;

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
