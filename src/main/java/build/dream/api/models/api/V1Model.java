package build.dream.api.models.api;

import build.dream.common.annotations.InstantiateObjectKey;
import build.dream.common.models.BasicModel;

import javax.validation.constraints.NotNull;
import java.util.Date;

public class V1Model extends BasicModel {
    @NotNull(message = "参数(access_token)不能为空！")
    @InstantiateObjectKey(name = "access_token")
    private String accessToken;

    @NotNull
    private String method;

    @NotNull
    private Date timestamp;

    @NotNull
    private String id;

    @NotNull
    private String signature;

    @NotNull(message = "参数(biz_content)不能为空！")
    @InstantiateObjectKey(name = "biz_content")
    private String bizContent;

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

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getBizContent() {
        return bizContent;
    }

    public void setBizContent(String bizContent) {
        this.bizContent = bizContent;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("access_token=").append(accessToken);
        stringBuilder.append("&biz_content=").append(bizContent);
        stringBuilder.append("&id=").append(id);
        stringBuilder.append("&method=").append(method);
        stringBuilder.append("&timestamp=").append(signature);
        return stringBuilder.toString();
    }
}
