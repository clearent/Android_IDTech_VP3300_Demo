package com.clearent.device.token.domain;

import com.google.gson.annotations.SerializedName;

public class MobileJwtErrorPayload  {

    @SerializedName("error")
    MobileJwtError mobileJwtError;

    public MobileJwtErrorPayload() {
    }

    public MobileJwtError getMobileJwtError() {
        return mobileJwtError;
    }

    public void setMobileJwtError(MobileJwtError mobileJwtError) {
        this.mobileJwtError = mobileJwtError;
    }


}
