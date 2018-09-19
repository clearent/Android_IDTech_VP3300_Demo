package com.clearent.device.config;

import android.util.Log;

import com.clearent.device.Configurable;
import com.clearent.device.DeviceConfigurable;
import com.clearent.device.config.domain.AndroidConfigurationResponse;
import com.clearent.device.config.domain.CaPublicKey;
import com.clearent.device.config.domain.ConfigurationResponse;
import com.clearent.device.config.domain.MobileContactAid;
import com.idtechproducts.device.Common;
import com.idtechproducts.device.ErrorCode;
import com.idtechproducts.device.ResDataStruct;
import com.idtechproducts.device.StructConfigParameters;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GetAndroidConfigurationTaskResponseHandler {

    private static final String GENERAL_ERROR = "VIVOpay failed to retrieve android configuration. Confirm internet access, then reconnect reader.";

    private DeviceConfigurable deviceConfigurable;

    public GetAndroidConfigurationTaskResponseHandler(DeviceConfigurable deviceConfigurable) {
        this.deviceConfigurable = deviceConfigurable;
    }

    public void handleResponse(AndroidConfigurationResponse androidConfigurationResponse) {
        //TODO TEMP set this to true for now.
        deviceConfigurable.setDeviceConfigured(true);
        deviceConfigurable.notifyReaderIsReady();

        if(androidConfigurationResponse == null
                || androidConfigurationResponse.getAndroidDevicePayload() == null) {
            notifyGeneralFailure();
            return;
        }

        try {
            //convert response to struct object
            //call device_connectWithProfile
            StructConfigParameters structConfigParameters = new StructConfigParameters();
            boolean isDeviceConfigured = deviceConfigurable.device_connectWithProfile(structConfigParameters);
            //TODO do we check this isDeviceConfigured ?
            deviceConfigurable.setDeviceConfigured(true);
            deviceConfigurable.notifyReaderIsReady();
        } catch (Exception e) {
            Log.e("ERROR","Failed to process configuration", e);
            notifyFailure(GENERAL_ERROR);
        }
    }

    public void notifyFailure(String message) {
        notifyGeneralFailure();
        deviceConfigurable.notifyConfigurationFailure(message);
    }

    private void notifyGeneralFailure() {
        deviceConfigurable.notifyConfigurationFailure(GENERAL_ERROR);
    }
}
