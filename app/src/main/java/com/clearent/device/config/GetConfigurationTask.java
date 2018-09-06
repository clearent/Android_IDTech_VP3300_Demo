package com.clearent.device.config;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class GetConfigurationTask extends AsyncTask<Void, Void, String> {

    public interface AsyncResponse {
        void processFinish(String output);
    }

    public AsyncResponse delegate = null;

    public GetConfigurationTask(AsyncResponse delegate) {
        this.delegate = delegate;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            URL url = new URL("https://mobile-devices-qa.clearent.net/rest/v2/mobile/devices/737T003758/EMV%20Common%20L2%20V1.10");
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestProperty("api-key", "12fa1a5617464354a72b3c9eb92d4f3b");
            urlConnection.setRequestProperty("Accept", "application/json");
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                return stringBuilder.toString();
            } finally {
                urlConnection.disconnect();
            }
        } catch (Exception e) {
            Log.e("ERROR", e.getMessage(), e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(String response) {
        if (response == null) {
            response = "THERE WAS AN ERROR";
        }
        Log.i("INFO", response);
        delegate.processFinish(response);
    }

}
