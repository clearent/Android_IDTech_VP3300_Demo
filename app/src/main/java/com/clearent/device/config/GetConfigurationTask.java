package com.clearent.device.config;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.clearent.device.config.domain.ConfigurationResponse;
import com.clearent.device.domain.CommunicationRequest;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class GetConfigurationTask extends AsyncTask<Void, Void, ConfigurationResponse> {

    private static final String GENERAL_ERROR = "VIVOpay failed to retrieve configuration. Confirm internet access, then reconnect reader.";

    public interface AsyncResponse {
        void processFinish(ConfigurationResponse output);
    }

    private static final String RELATIVE_PATH = "/rest/v2/mobile/devices";

    private CommunicationRequest communicationRequest;
    private HttpsURLConnection httpsURLConnection;

    public AsyncResponse delegate = null;

    public GetConfigurationTask(CommunicationRequest communicationRequest, AsyncResponse delegate) {
        this.communicationRequest = communicationRequest;
        this.delegate = delegate;
    }

    @Override
    protected ConfigurationResponse doInBackground(Void... voids) {
        try {
            String encodedKernelVersion = Uri.encode(communicationRequest.getKernelVersion());
            URL url = new URL(communicationRequest.getBaseUrl() + RELATIVE_PATH + "/" + communicationRequest.getDeviceSerialNumber() + "/" + encodedKernelVersion);
            httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.setRequestProperty("public-key", communicationRequest.getPublicKey());
            httpsURLConnection.setRequestProperty("Accept", "application/json");
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                Gson gson = new Gson();
                return gson.fromJson(stringBuilder.toString(), ConfigurationResponse.class);
            } catch (Exception e) {
                Log.e("CLEARENT", GENERAL_ERROR, e);
            } finally {
                httpsURLConnection.disconnect();
            }
        } catch (Exception e) {
            Log.e("CLEARENT", GENERAL_ERROR, e);
        } finally {
            httpsURLConnection.disconnect();
        }
        return new ConfigurationResponse();
    }

    @Override
    protected void onPostExecute(ConfigurationResponse response) {
        delegate.processFinish(response);
    }
}
