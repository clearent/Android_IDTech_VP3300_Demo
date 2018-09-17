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
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestProperty("public-key", communicationRequest.getPublicKey());
            urlConnection.setRequestProperty("Accept", "application/json");
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                Gson gson = new Gson();
                ConfigurationResponse configurationResponse = gson.fromJson(stringBuilder.toString(), ConfigurationResponse.class);
                return configurationResponse;
                //TODO Remove this at some point. had problems at home calling qa. so I used postman to get the json and worked with it locally
                //return loadJSON();
            } catch (Exception e) {
                Log.e("ERROR", GENERAL_ERROR, e);
            }
        } catch (Exception e) {
            Log.e("ERROR", GENERAL_ERROR,e);
        }
        return new ConfigurationResponse();
    }

    @Override
    protected void onPostExecute(ConfigurationResponse response) {
        delegate.processFinish(response);
    }

    public String loadJSON() {
        String json = null;
        try {
            String file = "res/raw/testconfig.json";
            InputStream in = this.getClass().getClassLoader().getResourceAsStream(file);
            int size = in.available();
            byte[] buffer = new byte[size];
            in.read(buffer);
            in.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }
}
