package com.clearent.sample;

import com.clearent.idtech.android.token.domain.TransactionToken;

public class PostTransactionRequest {

    private String baseUrl;
    private String apiKey;

    private TransactionToken transactionToken;

    private SaleTransaction saleTransaction;

    public PostTransactionRequest() {
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public TransactionToken getTransactionToken() {
        return transactionToken;
    }

    public void setTransactionToken(TransactionToken transactionToken) {
        this.transactionToken = transactionToken;
    }

    public SaleTransaction getSaleTransaction() {
        return saleTransaction;
    }

    public void setSaleTransaction(SaleTransaction saleTransaction) {
        this.saleTransaction = saleTransaction;
    }
}
