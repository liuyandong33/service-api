package build.dream.api.models.data;

import build.dream.api.constants.Constants;
import build.dream.common.annotations.InstantiateObjectKey;
import build.dream.common.models.BasicModel;

import javax.validation.constraints.NotNull;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UploadDataModel extends BasicModel {
    @NotNull(message = "参数(access_token)不能为空！")
    @InstantiateObjectKey(name = "access_token")
    private String accessToken;

    @NotNull
    private Date timestamp;

    @NotNull
    private String id;

    @NotNull
    private String signature;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
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

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("access_token=").append(accessToken);
        stringBuilder.append("&id=").append(id);
        stringBuilder.append("&timestamp=").append(new SimpleDateFormat(Constants.DEFAULT_DATE_PATTERN).format(timestamp));
        return stringBuilder.toString();
    }
}
