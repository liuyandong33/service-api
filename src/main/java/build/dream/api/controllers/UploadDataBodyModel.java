package build.dream.api.controllers;

import build.dream.common.models.BasicModel;

import javax.validation.constraints.NotNull;

public class UploadDataBodyModel extends BasicModel {
    @NotNull
    private String data;

    @NotNull
    private String type;

    @NotNull
    private Boolean zipped;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getZipped() {
        return zipped;
    }

    public void setZipped(Boolean zipped) {
        this.zipped = zipped;
    }
}
