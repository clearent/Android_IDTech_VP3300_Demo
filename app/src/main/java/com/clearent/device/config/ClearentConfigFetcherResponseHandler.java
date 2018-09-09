package com.clearent.device.config;

import com.clearent.device.Clearent_VP3300;
import com.clearent.device.config.domain.CaPublicKey;
import com.clearent.device.config.domain.ConfigurationResponse;
import com.clearent.device.config.domain.MobileContactAid;
import com.google.gson.Gson;
import com.idtechproducts.device.Common;
import com.idtechproducts.device.ErrorCode;
import com.idtechproducts.device.ResDataStruct;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ClearentConfigFetcherResponseHandler {

    private Clearent_VP3300 clearentVp3300;

    public ClearentConfigFetcherResponseHandler(Clearent_VP3300 clearentVp3300) {
        this.clearentVp3300 = clearentVp3300;
    }

    public void handleResponse(String json) {
        Gson gson = new Gson();
        try {
            ConfigurationResponse configurationResponse = gson.fromJson(json, ConfigurationResponse.class);

            configureAids(configurationResponse.getMobileDevicePayload().getMobileDevice().getContactAids());
            configureCaPublicKeys(configurationResponse.getMobileDevicePayload().getMobileDevice().getCaPublicKeys());
        } catch (Exception e) {
            System.out.println("failed to parse clearent configuration");
            //TODO notify of failure
        }
    }

    private void configureCaPublicKeys(List<CaPublicKey> caPublicKeys) {
        if(caPublicKeys == null || caPublicKeys.isEmpty()) {
            System.out.println("no ca public keys to configure...error ?");
            return;
        }

        for(CaPublicKey caPublicKey:caPublicKeys) {
            byte[] caPublickeyOrdered = Common.getByteArray(caPublicKey.getOrderedValues());
            ResDataStruct resData = new ResDataStruct();
            //TODO communicate errors
            int ret = clearentVp3300.emv_setCAPK(caPublickeyOrdered, resData);
            if (ret == ErrorCode.SUCCESS) {
                if (resData.statusCode == 0x00) {
                    System.out.println( "EMV Create RID A000009999 Index E1 Succeeded\n");
                } else {
                    String info = "EMV Create AID A000009999 Index E1 Failed\n";
                    info += "Error Code: " + String.format(Locale.US, "%02X ", resData.statusCode);
                    System.out.println(info);
                }
            } else {
                String info = "EMV Create AID A000009999 Index E1 Failed\n";
                info += "Error Code: " + String.format(Locale.US, "%02X ", resData.statusCode);
                System.out.println(info);
            }
        }
    }

    private void configureAids(List<MobileContactAid> mobileContactAids) {

        if(mobileContactAids == null || mobileContactAids.isEmpty()) {
            System.out.println("no contact aids to configure...error ?");
            return;
        }

        for(MobileContactAid mobileContactAid:mobileContactAids) {
            byte[] values = aidValuesAsByteArray(mobileContactAid.getValues());
            ResDataStruct resData = new ResDataStruct();
            int ret = clearentVp3300.emv_setApplicationData(mobileContactAid.getName(), values, resData);
            //TODO communicate errors
            if (ret == ErrorCode.SUCCESS) {
                if (resData.statusCode == 0x00) {
                    System.out.println("EMV create AID " + mobileContactAid.getName() + " Succeeded\n");
                } else {
                    String info = "EMV create AID " + mobileContactAid.getName() + " Failed\n";
                    info += "Error Code: " + String.format(Locale.US, "%02X ", resData.statusCode);
                    System.out.println(info);
                }
            } else {
                String info = "EMV create AID " + mobileContactAid.getName() + " Failed\n";
                info += "Status: " + clearentVp3300.device_getResponseCodeString(ret) + "";
                System.out.println(info);
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
}