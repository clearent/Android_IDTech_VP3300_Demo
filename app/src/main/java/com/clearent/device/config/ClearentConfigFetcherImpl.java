package com.clearent.device.config;

import android.util.Log;

import com.clearent.device.domain.CommunicationRequest;

public class ClearentConfigFetcherImpl implements ClearentConfigFetcher {

    private CommunicationRequest communicationRequest;

    public ClearentConfigFetcherImpl(CommunicationRequest communicationRequest) {
        this.communicationRequest = communicationRequest;
    }

    @Override
    public void fetchConfiguration(final ClearentConfigFetcherResponseHandler clearentConfigFetcherResponseHandler) {
        new GetConfigurationTask(communicationRequest, new GetConfigurationTask.AsyncResponse() {
            @Override
            public void processFinish(String output) {
                Log.i("OUTPUT", output);
                clearentConfigFetcherResponseHandler.handleResponse(output);
            }
        }).execute();
    }
}
