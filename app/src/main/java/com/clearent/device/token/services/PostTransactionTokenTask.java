package com.clearent.device.token.services;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.clearent.device.domain.CommunicationRequest;
import com.clearent.device.token.domain.ClearentTransactionTokenRequest;
import com.clearent.device.token.domain.MobileJwtResponse;
import com.google.gson.Gson;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class PostTransactionTokenTask extends AsyncTask<Void, Void, MobileJwtResponse> {

    public interface AsyncResponse {
        void processFinish(MobileJwtResponse output);
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
    protected MobileJwtResponse doInBackground(Void... voids) {
        MobileJwtResponse mobileJwtResponse = new MobileJwtResponse();
        try {
            HttpsURLConnection urlConnection = createHttpsURLConnection();
            if (clearentTransactionTokenRequest != null) {
                OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
                Gson gson = new Gson();
                String json = gson.toJson(clearentTransactionTokenRequest);
                writer.write(json);
                writer.flush();
                mobileJwtResponse = callMobileJwt(urlConnection);
            }
        } catch (Exception e) {
            Log.e("CLEARENT", "Failed to call mobile jwt endpoint", e);
        }
        return mobileJwtResponse;
    }

    private MobileJwtResponse callMobileJwt(HttpsURLConnection urlConnection) {
        MobileJwtResponse mobileJwtResponse = new MobileJwtResponse();
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            bufferedReader.close();
            Gson gson = new Gson();
            mobileJwtResponse = gson.fromJson(stringBuilder.toString(), MobileJwtResponse.class);
            return mobileJwtResponse;
        } catch (Exception e) {
            Log.e("CLEARENT", e.getMessage(), e);
        }
        return mobileJwtResponse;
    }

    @NonNull
    private HttpsURLConnection createHttpsURLConnection() throws IOException {
        URL url = new URL(communicationRequest.getBaseUrl() + RELATIVE_PATH);
        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("public-key", communicationRequest.getPublicKey());
        urlConnection.setRequestProperty("Accept", "application/json");
        return urlConnection;
    }

    @Override
    protected void onPostExecute(MobileJwtResponse response) {
        delegate.processFinish(response);
    }
}
