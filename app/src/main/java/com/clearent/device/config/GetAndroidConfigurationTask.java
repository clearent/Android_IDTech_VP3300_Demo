package com.clearent.device.config;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.clearent.device.config.domain.AndroidConfigurationResponse;
import com.clearent.device.config.domain.ConfigurationResponse;
import com.clearent.device.domain.CommunicationRequest;
import com.google.gson.Gson;
import com.idtechproducts.device.sdkdemo.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class GetAndroidConfigurationTask extends AsyncTask<Void, Void, AndroidConfigurationResponse> {

    private static final String GENERAL_ERROR = "VIVOpay failed to retrieve configuration. Confirm internet access, then reconnect reader.";

    public interface AsyncResponse {
        void processFinish(AndroidConfigurationResponse output);
    }

    private static final String RELATIVE_PATH = "/rest/v2/mobile/android/devices";

    private CommunicationRequest communicationRequest;

    private HttpsURLConnection httpsURLConnection;

    public AsyncResponse delegate = null;

    public GetAndroidConfigurationTask(CommunicationRequest communicationRequest, AsyncResponse delegate) {
        this.communicationRequest = communicationRequest;
        this.delegate = delegate;
    }

    @Override
    protected AndroidConfigurationResponse doInBackground(Void... voids) {
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
                return gson.fromJson(stringBuilder.toString(), AndroidConfigurationResponse.class);
            } catch (Exception e) {
                Log.e("CLEARENT", GENERAL_ERROR, e);
            }
        } catch (Exception e) {
            Log.e("CLEARENT", GENERAL_ERROR,e);
        } finally {
            httpsURLConnection.disconnect();
        }
        return new AndroidConfigurationResponse();
    }

    @Override
    protected void onPostExecute(AndroidConfigurationResponse response) {
        delegate.processFinish(response);
    }


}
