package com.clearent.device.token.domain;

import com.google.gson.annotations.SerializedName;

public class MobileJwtResponse {

    @SerializedName("payload")
    private MobileJwtPayload mobileJwtPayload;

    public MobileJwtResponse() {

    }

    public MobileJwtPayload getMobileJwtPayload() {
        return mobileJwtPayload;
    }

    public void setMobileJwtPayload(MobileJwtPayload mobileJwtPayload) {
        this.mobileJwtPayload = mobileJwtPayload;
    }
}
