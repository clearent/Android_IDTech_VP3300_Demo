package com.clearent.device.token.services;

import android.util.Log;

import com.clearent.device.config.GetConfigurationTask;
import com.clearent.device.config.domain.ConfigFetchRequest;
import com.clearent.device.token.domain.ClearentTransactionTokenRequest;

public class TransactionTokenCreatorImpl implements TransactionTokenCreator {

    private ConfigFetchRequest configFetchRequest;
    private ClearentTransactionTokenRequest clearentTransactionTokenRequest;

    public TransactionTokenCreatorImpl(ConfigFetchRequest configFetchRequest,ClearentTransactionTokenRequest clearentTransactionTokenRequest) {
        this.configFetchRequest = configFetchRequest;
        this.clearentTransactionTokenRequest = clearentTransactionTokenRequest;
    }

    @Override
    public void createTransactionToken(final TransactionTokenCreatorResponseHandler transactionTokenCreatorResponseHandler) {
        PostTransactionTokenTask postTransactionTokenTask = new PostTransactionTokenTask(configFetchRequest, clearentTransactionTokenRequest, new PostTransactionTokenTask.AsyncResponse() {
            @Override
            public void processFinish(String output) {
                Log.i("OUTPUT", output);
                transactionTokenCreatorResponseHandler.handleResponse(output);
            }
        });
        postTransactionTokenTask.execute();
    }
}
