package com.clearent.sample;

import com.google.gson.annotations.SerializedName;

public class ClearentTransactionPayload {


    @SerializedName("transaction")
    ClearentTransaction clearentTransaction;


    public ClearentTransaction getClearentTransaction() {
        return clearentTransaction;
    }

    public void setClearentTransaction(ClearentTransaction clearentTransaction) {
        this.clearentTransaction = clearentTransaction;
    }
}
