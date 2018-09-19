package com.clearent.device.config.domain;

import com.google.gson.annotations.SerializedName;

public class AndroidConfigurationResponse {

    @SerializedName("payload")
    private AndroidDevicePayload androidDevicePayload;

    public AndroidConfigurationResponse() {

    }

    public AndroidConfigurationResponse(AndroidDevicePayload androidDevicePayload) {
        this.androidDevicePayload = androidDevicePayload;
    }

    public AndroidDevicePayload getAndroidDevicePayload() {
        return androidDevicePayload;
    }

    public void setAndroidDevicePayload(AndroidDevicePayload androidDevicePayload) {
        this.androidDevicePayload = androidDevicePayload;
    }
}
