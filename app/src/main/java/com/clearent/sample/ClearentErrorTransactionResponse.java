package com.clearent.sample;

import com.google.gson.annotations.SerializedName;

public class ClearentErrorTransactionResponse {

    @SerializedName("code")
    private String code;

    @SerializedName("status")
    private String status;

    @SerializedName("exchange-id")
    private String exchangeId;

    @SerializedName("payload")
    private ErrorPayload errorPayload;

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

    public String getExchangeId() {
        return exchangeId;
    }

    public void setExchangeId(String exchangeId) {
        this.exchangeId = exchangeId;
    }

    public ErrorPayload getErrorPayload() {
        return errorPayload;
    }

    public void setErrorPayload(ErrorPayload errorPayload) {
        this.errorPayload = errorPayload;
    }
}
