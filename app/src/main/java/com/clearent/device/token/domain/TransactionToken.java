package com.clearent.device.token.domain;

import com.google.gson.annotations.SerializedName;

public class TransactionToken {

    @SerializedName("cvm")
    String cvm;

    @SerializedName("last-four")
    String lastFour;

    @SerializedName("track-data-hash")
    String trackDataHash;

    @SerializedName("jwt")
    String transactionToken;

    public TransactionToken() {
    }

    public String getCvm() {
        return cvm;
    }

    public void setCvm(String cvm) {
        this.cvm = cvm;
    }

    public String getLastFour() {
        return lastFour;
    }

    public void setLastFour(String lastFour) {
        this.lastFour = lastFour;
    }

    public String getTrackDataHash() {
        return trackDataHash;
    }

    public void setTrackDataHash(String trackDataHash) {
        this.trackDataHash = trackDataHash;
    }

    public String getTransactionToken() {
        return transactionToken;
    }

    public void setTransactionToken(String transactionToken) {
        this.transactionToken = transactionToken;
    }
}
