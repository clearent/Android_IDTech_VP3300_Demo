package com.clearent.device.token.services;

import android.util.Log;

import com.clearent.device.TransactionTokenNotifier;
import com.clearent.device.token.domain.MobileJwtResponse;
import com.clearent.device.token.domain.MobileJwtSuccessResponse;

public class TransactionTokenCreatorResponseHandler {

    public static final String GENERIC_TRANSACTION_TOKEN_ERROR_RESPONSE = "Create Transaction Token Failed";

    private TransactionTokenNotifier transactionTokenNotifier;

    public TransactionTokenCreatorResponseHandler(TransactionTokenNotifier transactionTokenNotifier) {
        this.transactionTokenNotifier = transactionTokenNotifier;
    }

    public void handleResponse(MobileJwtResponse mobileJwtResponse) {
        if(mobileJwtResponse == null || (mobileJwtResponse.getMobileJwtSuccessResponse() == null && mobileJwtResponse.getMobileJwtErrorResponse() == null)) {
            transactionTokenNotifier.notifyTransactionTokenFailure(GENERIC_TRANSACTION_TOKEN_ERROR_RESPONSE);
            return;
        }
        try {
            if(mobileJwtResponse.getMobileJwtSuccessResponse() != null) {
                transactionTokenNotifier.notifyNewTransactionToken(mobileJwtResponse.getMobileJwtSuccessResponse().getMobileJwtPayload().getTransactionToken());
            } else {
                transactionTokenNotifier.notifyTransactionTokenFailure(mobileJwtResponse.getMobileJwtErrorResponse().getMobileJwtErrorPayload().getMobileJwtError().getErrorMessage());
            }
        } catch (Exception e) {
            Log.e("CLEARENT", e.getMessage());
            transactionTokenNotifier.notifyTransactionTokenFailure(GENERIC_TRANSACTION_TOKEN_ERROR_RESPONSE);
        }
    }

}
