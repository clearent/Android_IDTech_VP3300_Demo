package com.clearent.device.config;

import android.os.AsyncTask;
import android.util.Log;

import com.clearent.device.config.domain.ConfigFetchRequest;
import com.idtechproducts.device.audiojack.UMLog;
import com.idtechproducts.device.audiojack.config.UmXmlParser;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class GetConfigurationTask extends AsyncTask<Void, Void, String> {

    public interface AsyncResponse {
        void processFinish(String output);
    }

    private static final String RELATIVE_PATH = "/rest/v2/mobile/devices";

    private ConfigFetchRequest configFetchRequest;

    public AsyncResponse delegate = null;

    public GetConfigurationTask(ConfigFetchRequest configFetchRequest, AsyncResponse delegate) {
        this.configFetchRequest = configFetchRequest;
        this.delegate = delegate;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            URL url = new URL(configFetchRequest.getBaseUrl() + RELATIVE_PATH + "/" + configFetchRequest.getDeviceSerialNumber() + "/" + configFetchRequest.getKernelVersion());
//            URL url = new URL("https://mobile-devices-qa.clearent.net/rest/v2/mobile/devices/737T003758/EMV%20Common%20L2%20V1.10");
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestProperty("public-key", configFetchRequest.getPublicKey());
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
                //had problems at home calling qa. so I used postman to get the json and worked with it locally
                //TODO Consider a fallback similar to android device fallback ? or do we assert "If the internet is up our services are too ?"
                //return loadJSON();
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
