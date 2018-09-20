package com.clearent.sample;

public class ClearentTransactionResponse {

    private ClearentSuccessTransactionResponse clearentSuccessTransactionResponse;

    private ClearentErrorTransactionResponse clearentErrorTransactionResponse;

    public ClearentTransactionResponse(ClearentSuccessTransactionResponse clearentSuccessTransactionResponse) {
        this.clearentSuccessTransactionResponse = clearentSuccessTransactionResponse;
    }

    public ClearentTransactionResponse(ClearentErrorTransactionResponse clearentErrorTransactionResponse) {
        this.clearentErrorTransactionResponse = clearentErrorTransactionResponse;
    }

    public ClearentSuccessTransactionResponse getClearentSuccessTransactionResponse() {
        return clearentSuccessTransactionResponse;
    }

    public void setClearentSuccessTransactionResponse(ClearentSuccessTransactionResponse clearentSuccessTransactionResponse) {
        this.clearentSuccessTransactionResponse = clearentSuccessTransactionResponse;
    }

    public ClearentErrorTransactionResponse getClearentErrorTransactionResponse() {
        return clearentErrorTransactionResponse;
    }

    public void setClearentErrorTransactionResponse(ClearentErrorTransactionResponse clearentErrorTransactionResponse) {
        this.clearentErrorTransactionResponse = clearentErrorTransactionResponse;
    }
}
