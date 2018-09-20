package com.clearent.sample;

import com.google.gson.annotations.SerializedName;

public class ClearentTransaction {

    @SerializedName("id")
    private String transactionId;
    @SerializedName("result")
    private String result;
    @SerializedName("status")
    private String status;
    @SerializedName("display-message")
    private String displayMessage;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDisplayMessage() {
        return displayMessage;
    }

    public void setDisplayMessage(String displayMessage) {
        this.displayMessage = displayMessage;
    }
}
