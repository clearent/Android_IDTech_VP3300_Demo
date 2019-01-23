package com.clearent.sample;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.clearent.idtech.android.PublicOnReceiverListener;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * This is only a sample transaction request. Do not add this code to a mobile app. It uses a secret key in a request header that only you and Clearent should know about.
 * secret keys usage is meant for server side calls.
 */
public class PostTransactionTask extends AsyncTask<Void, Void, ClearentTransactionResponse> {

    public interface AsyncResponse {
        void processFinish(ClearentTransactionResponse output);
    }

    private static final String RELATIVE_PATH = "/rest/v2/mobile/transactions/sale";
    //private static final String RELATIVE_PATH = "/rest/v2/mobile/transactions";

    private HttpsURLConnection httpsURLConnection;

    private PostTransactionRequest postTransactionRequest;

    public AsyncResponse delegate = null;

    public PostTransactionTask(PostTransactionRequest postTransactionRequest, AsyncResponse delegate) {
        this.postTransactionRequest = postTransactionRequest;
        this.delegate = delegate;
    }

    @Override
    protected ClearentTransactionResponse doInBackground(Void... voids) {
        ClearentTransactionResponse clearentTransactionResponse = null;
        try {
            createHttpsURLConnection();
            if (postTransactionRequest != null) {
                OutputStreamWriter writer = new OutputStreamWriter(httpsURLConnection.getOutputStream());
                Gson gson = new Gson();
                String json = gson.toJson(postTransactionRequest.getSaleTransaction());
                writer.write(json);
                writer.flush();

                int httpResult = httpsURLConnection.getResponseCode();
                if (httpResult == 200) {
                    clearentTransactionResponse = new ClearentTransactionResponse(getSuccessfulResponse(get200Response()));
                } else  {
                    clearentTransactionResponse = new ClearentTransactionResponse(getBadResponse(getErrorResponse()));
                }
            }
        } catch (Exception e) {
            Log.e("CLEARENT", "Failed to call mobile jwt endpoint", e);
        }
        if(clearentTransactionResponse == null) {
            clearentTransactionResponse = createGenericErrorResponse();
        }
        return clearentTransactionResponse;
    }

    @NonNull
    private ClearentTransactionResponse createGenericErrorResponse() {
        ClearentErrorTransactionResponse clearentErrorTransactionResponse = new ClearentErrorTransactionResponse();
        return new ClearentTransactionResponse(clearentErrorTransactionResponse);
    }

    private String get200Response() {
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

    private String getErrorResponse() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpsURLConnection.getErrorStream()));
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

    ClearentSuccessTransactionResponse getSuccessfulResponse(String responseAsJson) {
        Log.i("CLEARENT", "Good sample transaction response " + responseAsJson);
        ClearentSuccessTransactionResponse clearentSuccessTransactionResponse = new ClearentSuccessTransactionResponse();
        try {
            Gson gson = new Gson();
            clearentSuccessTransactionResponse = gson.fromJson(responseAsJson, ClearentSuccessTransactionResponse.class);
        } catch (Exception e) {
            Log.e("CLEARENT", e.getMessage(), e);
        }
        return clearentSuccessTransactionResponse;
    }

    ClearentErrorTransactionResponse getBadResponse(String responseAsJson) {
        Log.i("CLEARENT", "Bad sample transaction response " + responseAsJson);
        ClearentErrorTransactionResponse clearentErrorTransactionResponse = new ClearentErrorTransactionResponse();
        try {
            Gson gson = new Gson();
            clearentErrorTransactionResponse = gson.fromJson(responseAsJson, ClearentErrorTransactionResponse.class);
        } catch (Exception e) {
            Log.e("CLEARENT", e.getMessage(), e);
        }
        return clearentErrorTransactionResponse;
    }

    private void createHttpsURLConnection() throws IOException {
        URL url = new URL(postTransactionRequest.getBaseUrl() + RELATIVE_PATH);
        httpsURLConnection = (HttpsURLConnection) url.openConnection();
        httpsURLConnection.setDoInput(true);
        httpsURLConnection.setDoOutput(true);
        httpsURLConnection.setRequestProperty("Content-Type", "application/json");
        httpsURLConnection.setRequestMethod("POST");
        httpsURLConnection.setRequestProperty("mobilejwt", postTransactionRequest.getTransactionToken().getTransactionToken());
        httpsURLConnection.setRequestProperty("api-key", postTransactionRequest.getApiKey());
        httpsURLConnection.setRequestProperty("Accept", "application/json");
        httpsURLConnection.setConnectTimeout(60000);
        httpsURLConnection.setReadTimeout(60000);
    }

    @Override
    protected void onPostExecute(ClearentTransactionResponse clearentTransactionResponse) {
        delegate.processFinish(clearentTransactionResponse);
    }
}
