package com.clearent.device.config;

import android.util.Log;

//TODO new object needed ?
public class ClearentConfigFetcherImpl implements ClearentConfigFetcher {

    private String baseUrl;
    private String publicKey;
    private String deviceSerialNumber;
    private String kernelVersion;

    public ClearentConfigFetcherImpl(String baseUrl, String publicKey, String deviceSerialNumber, String kernelVersion) {
        this.baseUrl = baseUrl;
        this.publicKey = publicKey;
        this.deviceSerialNumber = deviceSerialNumber;
        this.kernelVersion = kernelVersion;
    }

    @Override
    public void fetchConfiguration(final ClearentConfigFetcherResponseHandler clearentConfigFetcherResponseHandler) {
        new GetConfigurationTask(new GetConfigurationTask.AsyncResponse() {
            @Override
            public void processFinish(String output) {
                Log.i("OUTPUT", output);
                clearentConfigFetcherResponseHandler.handleResponse(output);
            }
        }).execute();
    }
}
