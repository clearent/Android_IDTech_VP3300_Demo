package com.clearent.device.config.domain;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class MobileContactAid {

    @SerializedName("name")
    private String name;

    @SerializedName("aid-values")
    private Map<String,String> values;

    public MobileContactAid() {
    }

    public String getName() {
        return name;
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
