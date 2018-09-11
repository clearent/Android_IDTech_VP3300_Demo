package com.clearent.device.token.domain;

import com.google.gson.annotations.SerializedName;

public class MobileJwtPayload {

    @SerializedName("mobile-jwt")
    TransactionToken transactionToken;

    public MobileJwtPayload() {
    }

    public TransactionToken getTransactionToken() {
        return transactionToken;
    }

    public void setTransactionToken(TransactionToken transactionToken) {
        this.transactionToken = transactionToken;
    }
}
