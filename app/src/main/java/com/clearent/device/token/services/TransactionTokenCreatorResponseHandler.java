package com.clearent.device.token.services;

import android.util.Log;

import com.clearent.device.TransactionTokenNotifier;
import com.clearent.device.token.domain.MobileJwtResponse;
import com.google.gson.Gson;

public class TransactionTokenCreatorResponseHandler {

    public static final String GENERIC_TRANSACTION_TOKEN_ERROR_RESPONSE = "Create Transaction Token Failed";

    private TransactionTokenNotifier transactionTokenNotifier;

    public TransactionTokenCreatorResponseHandler(TransactionTokenNotifier transactionTokenNotifier) {
        this.transactionTokenNotifier = transactionTokenNotifier;
    }

    public void handleResponse(String json) {
        Gson gson = new Gson();
        try {
            MobileJwtResponse mobileJwtResponse = gson.fromJson(json, MobileJwtResponse.class);
            transactionTokenNotifier.notifyNewTransactionToken(mobileJwtResponse.getMobileJwtPayload().getTransactionToken());
        } catch (Exception e) {
            Log.e("TRANSACTIONTOKEN", e.getMessage());
            transactionTokenNotifier.notifyTransactionTokenFailure(GENERIC_TRANSACTION_TOKEN_ERROR_RESPONSE);
        }
    }

}
