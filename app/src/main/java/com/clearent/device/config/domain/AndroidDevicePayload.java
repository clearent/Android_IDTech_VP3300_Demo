package com.clearent.device.config.domain;

import com.google.gson.annotations.SerializedName;

public class AndroidDevicePayload {

    @SerializedName("android-device")
    AndroidDevice androidDevice;

    public AndroidDevicePayload() {
    }

    public AndroidDevice getAndroidDevice() {
        return androidDevice;
    }

    public void setAndroidDevice(AndroidDevice androidDevice) {
        this.androidDevice = androidDevice;
    }
}
