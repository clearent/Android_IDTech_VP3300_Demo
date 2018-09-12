package com.clearent.device.token.services;

import android.os.AsyncTask;
import android.util.Log;

import com.clearent.device.domain.CommunicationRequest;
import com.clearent.device.token.domain.ClearentTransactionTokenRequest;
import com.google.gson.Gson;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class PostTransactionTokenTask extends AsyncTask<Void, Void, String> {

    public interface AsyncResponse {
        void processFinish(String output);
    }

    private static final String RELATIVE_PATH = "/rest/v2/mobilejwt";

    private ClearentTransactionTokenRequest clearentTransactionTokenRequest;
    private CommunicationRequest communicationRequest;

    public AsyncResponse delegate = null;

    public PostTransactionTokenTask(CommunicationRequest communicationRequest, ClearentTransactionTokenRequest clearentTransactionTokenRequest, AsyncResponse delegate) {
        this.communicationRequest = communicationRequest;
        this.clearentTransactionTokenRequest = clearentTransactionTokenRequest;
        this.delegate = delegate;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            URL url = new URL(communicationRequest.getBaseUrl() + RELATIVE_PATH);

            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();

            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("public-key", communicationRequest.getPublicKey());
            urlConnection.setRequestProperty("Accept", "application/json");

            //TODO exceptions ?
            // Send the post body
            try {
                if (clearentTransactionTokenRequest != null) {
                    OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                    Gson gson = new Gson();
                    String json = gson.toJson(clearentTransactionTokenRequest);
                    writer.write(json);
                    writer.flush();
                }
            } catch(Exception e) {
                //TODO notify failure,
                return "";
            }

            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                return stringBuilder.toString();
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
