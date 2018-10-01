package com.clearent.sample;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * This is only a sample receipt request. Do not add this code to a mobile app. It uses a secret key in a request header that only you and Clearent should know about.
 * secret keys usage is meant for server side calls.
 */
public class PostReceiptTask extends AsyncTask<Void, Void, ClearentReceiptResponse> {

    public interface AsyncResponse {
        void processFinish(ClearentReceiptResponse output);
    }

    private static final String RELATIVE_PATH = "/rest/v2/receipts";

    private HttpsURLConnection httpsURLConnection;

    private ReceiptRequest receiptRequest;

    public AsyncResponse delegate = null;

    public PostReceiptTask(ReceiptRequest receiptRequest, AsyncResponse delegate) {
        this.receiptRequest = receiptRequest;
        this.delegate = delegate;
    }

    @Override
    protected ClearentReceiptResponse doInBackground(Void... voids) {
        ClearentReceiptResponse clearentReceiptResponse = null;
        try {
            createHttpsURLConnection();
            if (receiptRequest != null) {
                OutputStreamWriter writer = new OutputStreamWriter(httpsURLConnection.getOutputStream());
                Gson gson = new Gson();
                String json = gson.toJson(receiptRequest.getReceiptDetail());
                writer.write(json);
                writer.flush();

                int httpResult = httpsURLConnection.getResponseCode();
                if (httpResult == 200) {
                    clearentReceiptResponse = new ClearentReceiptResponse("Sample receipt sent successfully");
                } else  {
                    clearentReceiptResponse = createGenericErrorResponse();
                }
            }
        } catch (Exception e) {
            Log.e("CLEARENT", "Failed to call receipt endpoint", e);
        }
        if(clearentReceiptResponse == null) {
            clearentReceiptResponse = createGenericErrorResponse();
        }
        return clearentReceiptResponse;
    }

    @NonNull
    private ClearentReceiptResponse createGenericErrorResponse() {
        return new ClearentReceiptResponse("Failed to send sample receipt");
    }

    private void createHttpsURLConnection() throws IOException {
        URL url = new URL(receiptRequest.getBaseUrl() + RELATIVE_PATH);
        httpsURLConnection = (HttpsURLConnection) url.openConnection();
        httpsURLConnection.setDoInput(true);
        httpsURLConnection.setDoOutput(true);
        httpsURLConnection.setRequestProperty("Content-Type", "application/json");
        httpsURLConnection.setRequestMethod("POST");
        httpsURLConnection.setRequestProperty("api-key", receiptRequest.getApiKey());
        httpsURLConnection.setRequestProperty("Accept", "application/json");
        httpsURLConnection.setConnectTimeout(60000);
        httpsURLConnection.setReadTimeout(60000);
    }

    @Override
    protected void onPostExecute(ClearentReceiptResponse clearentReceiptResponse) {
        delegate.processFinish(clearentReceiptResponse);
    }
}
