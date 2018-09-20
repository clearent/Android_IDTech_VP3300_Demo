package com.clearent.sample;

import com.google.gson.annotations.SerializedName;

public class ReceiptDetail {

    @SerializedName("email-address")
    private String emailAddress;

    @SerializedName("id")
    private String transactionId;

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
