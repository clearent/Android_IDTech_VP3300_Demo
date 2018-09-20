package com.clearent.sample;

public class ReceiptRequest {

    private String baseUrl;
    private String apiKey;

    private ReceiptDetail receiptDetail;

    public ReceiptRequest() {
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

    public ReceiptDetail getReceiptDetail() {
        return receiptDetail;
    }

    public void setReceiptDetail(ReceiptDetail receiptDetail) {
        this.receiptDetail = receiptDetail;
    }
}
