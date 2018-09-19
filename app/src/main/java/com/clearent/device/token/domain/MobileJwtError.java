package com.clearent.device.token.domain;

import com.google.gson.annotations.SerializedName;

public class MobileJwtError {

    @SerializedName("error-message")
    String errorMessage;

    @SerializedName("result-code")
    String resultCode;


    public MobileJwtError() {
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }
}
