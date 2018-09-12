package com.clearent.device.token.services;

import android.util.Log;

import com.clearent.device.domain.CommunicationRequest;
import com.clearent.device.token.domain.ClearentTransactionTokenRequest;

public class TransactionTokenCreatorImpl implements TransactionTokenCreator {

    private CommunicationRequest communicationRequest;
    private ClearentTransactionTokenRequest clearentTransactionTokenRequest;

    public TransactionTokenCreatorImpl(CommunicationRequest communicationRequest, ClearentTransactionTokenRequest clearentTransactionTokenRequest) {
        this.communicationRequest = communicationRequest;
        this.clearentTransactionTokenRequest = clearentTransactionTokenRequest;
    }

    @Override
    public void createTransactionToken(final TransactionTokenCreatorResponseHandler transactionTokenCreatorResponseHandler) {
        PostTransactionTokenTask postTransactionTokenTask = new PostTransactionTokenTask(communicationRequest, clearentTransactionTokenRequest, new PostTransactionTokenTask.AsyncResponse() {
            @Override
            public void processFinish(String output) {
                Log.i("OUTPUT", output);
                transactionTokenCreatorResponseHandler.handleResponse(output);
            }
        });
        postTransactionTokenTask.execute();
    }
}
