package com.clearent.device.config;

import android.util.Log;

import com.clearent.device.config.domain.ConfigFetchRequest;

public class ClearentConfigFetcherImpl implements ClearentConfigFetcher {

    private ConfigFetchRequest configFetchRequest;

    public ClearentConfigFetcherImpl(ConfigFetchRequest configFetchRequest) {
        this.configFetchRequest = configFetchRequest;
    }

    @Override
    public void fetchConfiguration(final ClearentConfigFetcherResponseHandler clearentConfigFetcherResponseHandler) {
        new GetConfigurationTask(configFetchRequest, new GetConfigurationTask.AsyncResponse() {
            @Override
            public void processFinish(String output) {
                Log.i("OUTPUT", output);
                clearentConfigFetcherResponseHandler.handleResponse(output);
            }
        }).execute();
    }
}
