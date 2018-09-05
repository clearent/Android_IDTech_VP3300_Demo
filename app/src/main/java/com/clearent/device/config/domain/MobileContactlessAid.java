package com.clearent.device.config.domain;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class MobileContactlessAid {

    @SerializedName("name")
    private String name;

    @SerializedName("group")
    private String group;

    @SerializedName("aid-values")
    private Map<String,String> values;

    public MobileContactlessAid() {
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getValues() {
        return values;
    }

    public void setValues(Map<String, String> values) {
        this.values = values;
    }
}
