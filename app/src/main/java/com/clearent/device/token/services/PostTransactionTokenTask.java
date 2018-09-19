package com.clearent.device.token.services;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.clearent.device.domain.CommunicationRequest;
import com.clearent.device.token.domain.ClearentTransactionTokenRequest;
import com.clearent.device.token.domain.MobileJwtError;
import com.clearent.device.token.domain.MobileJwtErrorPayload;
import com.clearent.device.token.domain.MobileJwtErrorResponse;
import com.clearent.device.token.domain.MobileJwtResponse;
import com.clearent.device.token.domain.MobileJwtSuccessResponse;
import com.google.gson.Gson;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class PostTransactionTokenTask extends AsyncTask<Void, Void, MobileJwtResponse> {

    public interface AsyncResponse {
        void processFinish(MobileJwtResponse output);
    }

    private static final String RELATIVE_PATH = "/rest/v2/mobilejwt";

    private ClearentTransactionTokenRequest clearentTransactionTokenRequest;
    private CommunicationRequest communicationRequest;
    private HttpsURLConnection httpsURLConnection;

    public AsyncResponse delegate = null;

    public PostTransactionTokenTask(CommunicationRequest communicationRequest, ClearentTransactionTokenRequest clearentTransactionTokenRequest, AsyncResponse delegate) {
        this.communicationRequest = communicationRequest;
        this.clearentTransactionTokenRequest = clearentTransactionTokenRequest;
        this.delegate = delegate;
    }

    @Override
    protected MobileJwtResponse doInBackground(Void... voids) {
        MobileJwtResponse mobileJwtResponse = null;
        try {
            createHttpsURLConnection();
            if (clearentTransactionTokenRequest != null) {
                OutputStreamWriter writer = new OutputStreamWriter(httpsURLConnection.getOutputStream());
                Gson gson = new Gson();
                String json = gson.toJson(clearentTransactionTokenRequest);
                writer.write(json);
                writer.flush();
                int httpResult = httpsURLConnection.getResponseCode();
                if (httpResult == 200) {
                    mobileJwtResponse = new MobileJwtResponse(getSuccessfulResponse(getResponse()));
                } else if(httpResult == 400) {
                    mobileJwtResponse = new MobileJwtResponse(getBadResponse(getResponse()));
                }
            }
        } catch (Exception e) {
            Log.e("CLEARENT", "Failed to call mobile jwt endpoint", e);
        }
        if(mobileJwtResponse == null) {
            mobileJwtResponse = createGenericErrorResponse();
        }
        return mobileJwtResponse;
    }

    @NonNull
    private MobileJwtResponse createGenericErrorResponse() {
        MobileJwtResponse mobileJwtResponse;MobileJwtErrorResponse mobileJwtErrorResponse = new MobileJwtErrorResponse();
        MobileJwtErrorPayload mobileJwtErrorPayload = new MobileJwtErrorPayload();
        MobileJwtError mobileJwtError = new MobileJwtError();
        mobileJwtError.setErrorMessage("Failed to create a transaction token");
        mobileJwtErrorPayload.setMobileJwtError(mobileJwtError);
        mobileJwtErrorResponse.setMobileJwtErrorPayload(mobileJwtErrorPayload);
        mobileJwtResponse = new MobileJwtResponse(mobileJwtErrorResponse);
        return mobileJwtResponse;
    }

    private String getResponse() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            bufferedReader.close();
            return stringBuilder.toString();
        } catch (Exception e) {
            Log.e("CLEARENT", e.getMessage(), e);
        } finally {
            httpsURLConnection.disconnect();
        }
        return null;
    }

    MobileJwtSuccessResponse getSuccessfulResponse(String responseAsJson) {
        MobileJwtSuccessResponse mobileJwtSuccessResponse = new MobileJwtSuccessResponse();
        try {
            Gson gson = new Gson();
            mobileJwtSuccessResponse = gson.fromJson(responseAsJson, MobileJwtSuccessResponse.class);
        } catch (Exception e) {
            Log.e("CLEARENT", e.getMessage(), e);
        }
        return mobileJwtSuccessResponse;
    }

    MobileJwtErrorResponse getBadResponse(String responseAsJson) {
        MobileJwtErrorResponse mobileJwtErrorResponse = new MobileJwtErrorResponse();
        try {
            Gson gson = new Gson();
            mobileJwtErrorResponse = gson.fromJson(responseAsJson, MobileJwtErrorResponse.class);
        } catch (Exception e) {
            Log.e("CLEARENT", e.getMessage(), e);
        }
        return mobileJwtErrorResponse;
    }

    private void createHttpsURLConnection() throws IOException {
        URL url = new URL(communicationRequest.getBaseUrl() + RELATIVE_PATH);
        httpsURLConnection = (HttpsURLConnection) url.openConnection();
        httpsURLConnection.setDoInput(true);
        httpsURLConnection.setDoOutput(true);
        httpsURLConnection.setRequestProperty("Content-Type", "application/json");
        httpsURLConnection.setRequestMethod("POST");
        httpsURLConnection.setRequestProperty("public-key", communicationRequest.getPublicKey());
        httpsURLConnection.setRequestProperty("Accept", "application/json");
    }

    @Override
    protected void onPostExecute(MobileJwtResponse mobileJwtResponse) {
        delegate.processFinish(mobileJwtResponse);
    }
}
