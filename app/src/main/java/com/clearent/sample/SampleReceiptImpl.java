package com.clearent.sample;

import com.clearent.idtech.android.PublicOnReceiverListener;

public class SampleReceiptImpl implements SampleReceipt {

    public SampleReceiptImpl() {
    }

    @Override
    public void doReceipt(ReceiptRequest receiptRequest, PublicOnReceiverListener publicOnReceiverListener) {
        PostReceiptTaskResponseHandler postReceiptTaskResponseHandler = new PostReceiptTaskResponseHandler(publicOnReceiverListener);
        asyncSale(receiptRequest, postReceiptTaskResponseHandler);
    }

    void asyncSale(ReceiptRequest receiptRequest, final PostReceiptTaskResponseHandler postReceiptTaskResponseHandler) {
        PostReceiptTask postReceiptTask = new PostReceiptTask(receiptRequest, new PostReceiptTask.AsyncResponse() {
            @Override
            public void processFinish(ClearentReceiptResponse clearentReceiptResponse) {
                postReceiptTaskResponseHandler.handleResponse(clearentReceiptResponse);
            }
        });
        postReceiptTask.execute();
    }


}
