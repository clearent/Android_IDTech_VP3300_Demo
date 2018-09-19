package com.clearent.device.token.domain;

import com.google.gson.annotations.SerializedName;

public class MobileJwtErrorResponse {

    @SerializedName("code")
    private String code;

    @SerializedName("status")
    private String status;

    @SerializedName("exchange-id")
    private String exchangeId;

    @SerializedName("payload")
    private MobileJwtErrorPayload mobileJwtErrorPayload;

    public MobileJwtErrorResponse() {

    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public MobileJwtErrorPayload getMobileJwtErrorPayload() {
        return mobileJwtErrorPayload;
    }

    public void setMobileJwtErrorPayload(MobileJwtErrorPayload mobileJwtErrorPayload) {
        this.mobileJwtErrorPayload = mobileJwtErrorPayload;
    }

    public String getExchangeId() {
        return exchangeId;
    }

    public void setExchangeId(String exchangeId) {
        this.exchangeId = exchangeId;
    }
}
