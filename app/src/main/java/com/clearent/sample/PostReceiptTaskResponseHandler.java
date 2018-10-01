package com.clearent.sample;


import android.util.Log;

import com.clearent.idtech.android.PublicOnReceiverListener;

public class PostReceiptTaskResponseHandler {

    private PublicOnReceiverListener publicOnReceiverListener;

    public PostReceiptTaskResponseHandler(PublicOnReceiverListener publicOnReceiverListener) {
        this.publicOnReceiverListener = publicOnReceiverListener;
    }

    public void handleResponse(ClearentReceiptResponse clearentReceiptResponse) {
        if(clearentReceiptResponse == null) {
            publicOnReceiverListener.lcdDisplay(0, new String[]{"Sample receipt failed"}, 0);
        } else {
            publicOnReceiverListener.lcdDisplay(0, new String[]{clearentReceiptResponse.getMessage()}, 0);
            Log.i("CLEARENT", clearentReceiptResponse.getMessage());
        }
    }

}
