package com.clearent.sample;

import com.clearent.idtech.android.PublicOnReceiverListener;

public class SampleTransactionImpl implements SampleTransaction {

    public SampleTransactionImpl() {
    }

    @Override
    public void doSale(PostTransactionRequest postTransactionRequest, PublicOnReceiverListener publicOnReceiverListener) {
        PostTransactionTaskResponseHandler postTransactionTaskResponseHandler = new PostTransactionTaskResponseHandler(publicOnReceiverListener);
        asyncSale(postTransactionRequest, postTransactionTaskResponseHandler);
    }

    void asyncSale(PostTransactionRequest postTransactionRequest, final PostTransactionTaskResponseHandler postTransactionTaskResponseHandler) {
        PostTransactionTask postTransactionTask = new PostTransactionTask(postTransactionRequest, new PostTransactionTask.AsyncResponse() {
            @Override
            public void processFinish(ClearentTransactionResponse clearentTransactionResponse) {
                postTransactionTaskResponseHandler.handleResponse(clearentTransactionResponse);
            }
        });
        postTransactionTask.execute();
    }


}
