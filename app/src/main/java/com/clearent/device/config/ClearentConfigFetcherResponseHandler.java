package com.clearent.device.config;

import android.util.Log;

import com.clearent.device.Clearent_VP3300;
import com.clearent.device.DeviceConfigurable;
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

public class ClearentConfigFetcherResponseHandler {

    private DeviceConfigurable deviceConfigurable;

    public ClearentConfigFetcherResponseHandler(DeviceConfigurable deviceConfigurable) {
        this.deviceConfigurable = deviceConfigurable;
    }

    public void handleResponse(String json) {
        if(json == null) {
            notifyGeneralFailure();
        }
        Gson gson = new Gson();
        try {
            ConfigurationResponse configurationResponse = gson.fromJson(json, ConfigurationResponse.class);
            configureAids(configurationResponse.getMobileDevicePayload().getMobileDevice().getContactAids());
            configureCaPublicKeys(configurationResponse.getMobileDevicePayload().getMobileDevice().getCaPublicKeys());
            deviceConfigurable.notifyReaderIsReady();
        } catch (Exception e) {
            Log.e("ERROR","Failed to process configuration", e);
            notifyFailure("Failed to process configuration");
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
            int ret = deviceConfigurable.emv_setCAPK(caPublickeyOrdered, resData);
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
                deviceConfigurable.notifyConfigurationFailure(ret, error);
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
            int ret = deviceConfigurable.emv_setApplicationData(mobileContactAid.getName(), values, resData);
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
                deviceConfigurable.notifyConfigurationFailure(ret, error);
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
        deviceConfigurable.notifyConfigurationFailure(message);
    }

    private void notifyGeneralFailure() {
        deviceConfigurable.notifyConfigurationFailure("VIVOpay failed to retrieve configuration");
    }
}
