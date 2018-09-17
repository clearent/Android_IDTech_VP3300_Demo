package com.clearent.device.config;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.clearent.device.domain.CommunicationRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class GetConfigurationTask extends AsyncTask<Void, Void, String> {

    public interface AsyncResponse {
        void processFinish(String output);
    }

    private static final String RELATIVE_PATH = "/rest/v2/mobile/devices";

    private CommunicationRequest communicationRequest;

    public AsyncResponse delegate = null;

    public GetConfigurationTask(CommunicationRequest communicationRequest, AsyncResponse delegate) {
        this.communicationRequest = communicationRequest;
        this.delegate = delegate;
    }

    @Override
    protected String doInBackground(Void... voids) {
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
                return stringBuilder.toString();
                ////had problems at home calling qa. so I used postman to get the json and worked with it locally
                //TODO Consider a fallback similar to android device fallback ? or do we assert "If the internet is up our services are too ?"
                //return loadJSON();
            } catch (Exception e) {
                //TODO handle error
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        } catch (MalformedURLException e) {
            //TODO handle error
            e.printStackTrace();
        } catch (IOException e) {
            //TODO handle error
            e.printStackTrace();
        }
        //TODO return ?
        return null;
    }

    @Override
    protected void onPostExecute(String response) {
        if (response == null) {
            //TODO handle error
            response = "THERE WAS AN ERROR";
        }
        Log.i("INFO", response);
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
