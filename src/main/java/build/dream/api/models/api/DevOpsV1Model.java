package build.dream.api.models.api;

import build.dream.common.annotations.InstantiateObjectKey;
import build.dream.common.models.BasicModel;

import javax.validation.constraints.NotNull;
import java.util.Date;

public class DevOpsV1Model extends BasicModel {
    @NotNull(message = "参数(access_token)不能为空！")
    @InstantiateObjectKey(name = "access_token")
    private String accessToken;

    @NotNull
    private String method;

    @NotNull
    private Date timestamp;

    @NotNull
    private String id;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
