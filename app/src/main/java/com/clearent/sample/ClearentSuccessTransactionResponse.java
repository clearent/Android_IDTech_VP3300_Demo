package com.clearent.sample;

import com.google.gson.annotations.SerializedName;

public class ClearentSuccessTransactionResponse {

    @SerializedName("code")
    private String code;

    @SerializedName("status")
    private String status;

    @SerializedName("exchange-id")
    private String exchangeId;

    @SerializedName("payload")
    private ClearentTransactionPayload clearentTransactionPayload;

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

    public ClearentTransactionPayload getClearentTransactionPayload() {
        return clearentTransactionPayload;
    }

    public void setClearentTransactionPayload(ClearentTransactionPayload clearentTransactionPayload) {
        this.clearentTransactionPayload = clearentTransactionPayload;
    }
}
