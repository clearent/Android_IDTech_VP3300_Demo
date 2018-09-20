package com.clearent.sample;

import com.google.gson.annotations.SerializedName;

public class ErrorPayload {

    @SerializedName("error")
    ClearentErrorResponse clearentErrorResponse;

    @SerializedName("transaction")
    ClearentTransaction clearentTransaction;

    public ClearentErrorResponse getClearentErrorResponse() {
        return clearentErrorResponse;
    }

    public void setClearentErrorResponse(ClearentErrorResponse clearentErrorResponse) {
        this.clearentErrorResponse = clearentErrorResponse;
    }

    public ClearentTransaction getClearentTransaction() {
        return clearentTransaction;
    }

    public void setClearentTransaction(ClearentTransaction clearentTransaction) {
        this.clearentTransaction = clearentTransaction;
    }
}
