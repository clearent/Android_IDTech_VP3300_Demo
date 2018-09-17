package com.clearent.device.config.domain;

import com.google.gson.annotations.SerializedName;

public class ConfigurationResponse {

    @SerializedName("payload")
    private MobileDevicePayload mobileDevicePayload;

    public ConfigurationResponse() {

    }

    public MobileDevicePayload getMobileDevicePayload() {
        return mobileDevicePayload;
    }

    public void setMobileDevicePayload(MobileDevicePayload mobileDevicePayload) {
        this.mobileDevicePayload = mobileDevicePayload;
    }

    @Override
    public String toString() {
        return "ConfigurationResponse{" +
                "mobileDevicePayload=" + mobileDevicePayload +
                '}';
    }
}
