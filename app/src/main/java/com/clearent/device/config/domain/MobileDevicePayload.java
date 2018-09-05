package com.clearent.device.config.domain;

import com.google.gson.annotations.SerializedName;

public class MobileDevicePayload {


    @SerializedName("mobile-device")
    MobileDevice mobileDevice;

    public MobileDevicePayload() {
    }

    public MobileDevice getMobileDevice() {
        return mobileDevice;
    }

    public void setMobileDevice(MobileDevice mobileDevice) {
        this.mobileDevice = mobileDevice;
    }
}
