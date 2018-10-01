package com.clearent.sample;


import android.util.Log;

import com.clearent.idtech.android.PublicOnReceiverListener;

public class PostTransactionTaskResponseHandler {

    private PublicOnReceiverListener publicOnReceiverListener;

    public PostTransactionTaskResponseHandler(PublicOnReceiverListener publicOnReceiverListener) {
        this.publicOnReceiverListener = publicOnReceiverListener;
    }

    public void handleResponse(ClearentTransactionResponse clearentTransactionResponse) {
        if(clearentTransactionResponse == null) {
            publicOnReceiverListener.lcdDisplay(0, new String[]{"Sample transaction failed"}, 0);
            return;
        }

        try {
            if(clearentTransactionResponse.getClearentErrorTransactionResponse() != null) {
                if(clearentTransactionResponse.getClearentErrorTransactionResponse().getErrorPayload().getClearentTransaction() != null) {
                    publicOnReceiverListener.lcdDisplay(0, new String[]{"Sample transaction failed: " + clearentTransactionResponse.getClearentErrorTransactionResponse().getErrorPayload().getClearentTransaction().getDisplayMessage()}, 0);
                    Log.i("CLEARENT", "Sample transaction failed");
                } else {
                    publicOnReceiverListener.lcdDisplay(0, new String[]{"Sample transaction failed: " + clearentTransactionResponse.getClearentErrorTransactionResponse().getErrorPayload().getClearentErrorResponse().getErrorMessage()}, 0);
                    Log.i("CLEARENT", "Sample transaction failed");
                }
            } else if(clearentTransactionResponse.getClearentSuccessTransactionResponse() != null) {
                String message = "Sample Transaction successful. Transaction Id:" + clearentTransactionResponse.getClearentSuccessTransactionResponse().getClearentTransactionPayload().getClearentTransaction().getTransactionId();
                 publicOnReceiverListener.lcdDisplay(0, new String[]{message}, 0);
                Log.i("CLEARENT", "Sample transaction successful");
            } else {
                publicOnReceiverListener.lcdDisplay(0, new String[]{"Sample transaction failed"}, 0);
            }
        } catch (Exception e) {
            Log.e("CLEARENT","Failed to process clearent sample transaction", e);
            publicOnReceiverListener.lcdDisplay(0, new String[]{"Sample transaction failed"}, 0);
        }
    }

}
