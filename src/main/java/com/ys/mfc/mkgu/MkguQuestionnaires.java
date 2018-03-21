package com.ys.mfc.mkgu;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MkguQuestionnaires {
    @SerializedName("serviceType")
    @Expose
    public String serviceType;
    @SerializedName("version")
    @Expose
    public String version;
    @SerializedName("xml")
    @Expose
    public String xml;

    public MkguQuestionnaires() {
    }

    public String getServiceType() {
        return this.serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getXml() {
        return this.xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }
}
