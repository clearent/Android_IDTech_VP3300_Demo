package com.clearent.device.token.domain;

import com.google.gson.annotations.SerializedName;

public class MobileJwtSuccessResponse {

    @SerializedName("code")
    private String code;

    @SerializedName("status")
    private String status;

    @SerializedName("exchange-id")
    private String exchangeId;

    @SerializedName("payload")
    private MobileJwtPayload mobileJwtPayload;

    public MobileJwtSuccessResponse() {

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

    public MobileJwtPayload getMobileJwtPayload() {
        return mobileJwtPayload;
    }

    public void setMobileJwtPayload(MobileJwtPayload mobileJwtPayload) {
        this.mobileJwtPayload = mobileJwtPayload;
    }

    public String getExchangeId() {
        return exchangeId;
    }

    public void setExchangeId(String exchangeId) {
        this.exchangeId = exchangeId;
    }

}
