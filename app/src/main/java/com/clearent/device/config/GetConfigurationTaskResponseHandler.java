package com.clearent.device.config;

import android.util.Log;

import com.clearent.device.Configurable;
import com.clearent.device.config.domain.CaPublicKey;
import com.clearent.device.config.domain.ConfigurationResponse;
import com.clearent.device.config.domain.MobileContactAid;
import com.google.gson.Gson;
import com.idtechproducts.device.Common;
import com.idtechproducts.device.ErrorCode;
import com.idtechproducts.device.ResDataStruct;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GetConfigurationTaskResponseHandler {

    private static final String GENERAL_ERROR = "VIVOpay failed to retrieve configuration. Confirm internet access, then reconnect reader.";

    private Configurable configurable;

    public GetConfigurationTaskResponseHandler(Configurable configurable) {
        this.configurable = configurable;
    }

    public void handleResponse(ConfigurationResponse configurationResponse) {
        if(configurationResponse == null
                || configurationResponse.getMobileDevicePayload() == null
                || configurationResponse.getMobileDevicePayload().getMobileDevice() == null
                || configurationResponse.getMobileDevicePayload().getMobileDevice().getContactAids() == null
                || configurationResponse.getMobileDevicePayload().getMobileDevice().getCaPublicKeys() == null
                || configurationResponse.getMobileDevicePayload().getMobileDevice().getCaPublicKeys().size() == 0
                || configurationResponse.getMobileDevicePayload().getMobileDevice().getContactAids().size() == 0
                || configurationResponse.getMobileDevicePayload().getMobileDevice() == null) {
            notifyGeneralFailure();
            return;
        }

        try {
            configureAids(configurationResponse.getMobileDevicePayload().getMobileDevice().getContactAids());
            configureCaPublicKeys(configurationResponse.getMobileDevicePayload().getMobileDevice().getCaPublicKeys());
            configurable.setReaderConfigured(true);
            configurable.notifyReaderIsReady();
        } catch (Exception e) {
            Log.e("ERROR","Failed to process configuration", e);
            notifyFailure(GENERAL_ERROR);
        }
    }

    private void configureCaPublicKeys(List<CaPublicKey> caPublicKeys) {
        if(caPublicKeys == null || caPublicKeys.isEmpty()) {
            Log.i("INFO", "No ca public keys to configure");
            return;
        }

        for(CaPublicKey caPublicKey:caPublicKeys) {
            byte[] caPublickeyOrdered = Common.getByteArray(caPublicKey.getOrderedValues());
            ResDataStruct resData = new ResDataStruct();
            int ret = configurable.emv_setCAPK(caPublickeyOrdered, resData);
            if (ret == ErrorCode.SUCCESS) {
                if (resData.statusCode == 0x00) {
                    Log.i("INFO","EMV Ca Public Key " + caPublicKey.getName() + " Added ");
                } else {
                    String error = "EMV Ca Public Key " + caPublicKey.getName() + " Failed. ";
                    error += "Error Code: " + String.format(Locale.US, "%02X ", resData.statusCode);
                    notifyFailure(error);
                }
            } else {
                String error = "EMV Ca Public Key " + caPublicKey.getName() + " Failed. ";
                notifyGeneralFailure();
                configurable.notifyConfigurationFailure(ret, error);
            }
        }
    }

    private void configureAids(List<MobileContactAid> mobileContactAids) {

        if(mobileContactAids == null || mobileContactAids.isEmpty()) {
            Log.i("INFO", "No contact aids to configure");
            return;
        }

        for(MobileContactAid mobileContactAid:mobileContactAids) {
            byte[] values = aidValuesAsByteArray(mobileContactAid.getValues());
            ResDataStruct resData = new ResDataStruct();
            int ret = configurable.emv_setApplicationData(mobileContactAid.getName(), values, resData);
            if (ret == ErrorCode.SUCCESS) {
                if (resData.statusCode == 0x00) {
                    Log.i("INFO","EMV Contact Aid " + mobileContactAid.getName() + " Added ");
                } else {
                    String error = "EMV create AID " + mobileContactAid.getName() + " Failed. ";
                    error += "Error Code: " + String.format(Locale.US, "%02X ", resData.statusCode);
                    notifyFailure(error);
                }
            } else {
                String error = "EMV create AID " + mobileContactAid.getName() + " Failed. ";
                notifyGeneralFailure();
                configurable.notifyConfigurationFailure(ret, error);
            }
        }
    }

    public byte[] aidValuesAsByteArray(Map<String,String> values) {
        String tlv = convertToTlv(values);
        return Common.getByteArray(tlv);
    }

    public String convertToTlv(Map<String,String> values) {
        StringBuilder stringBuilder = new StringBuilder();
        for(Map.Entry<String, String> entry: values.entrySet()) {
            String tag = entry.getKey();
            String value = entry.getValue();
            int valueLength = value.length()/2;
            String length = String.format("%02X", valueLength);
            stringBuilder.append(tag + length + value);
        }
        return stringBuilder.toString();
    }

    public void notifyFailure(String message) {
        notifyGeneralFailure();
        configurable.notifyConfigurationFailure(message);
    }

    private void notifyGeneralFailure() {
        configurable.notifyConfigurationFailure(GENERAL_ERROR);
    }
}
