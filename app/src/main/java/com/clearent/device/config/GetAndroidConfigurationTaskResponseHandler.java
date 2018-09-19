package com.clearent.device.config;

import android.util.Log;

import com.clearent.device.DeviceConfigurable;
import com.clearent.device.config.domain.AndroidConfigurationResponse;
import com.idtechproducts.device.StructConfigParameters;

public class GetAndroidConfigurationTaskResponseHandler {

    private static final String GENERAL_ERROR = "Android device configuration not found. Using defaults.";

    private DeviceConfigurable deviceConfigurable;

    public GetAndroidConfigurationTaskResponseHandler(DeviceConfigurable deviceConfigurable) {
        this.deviceConfigurable = deviceConfigurable;
    }

    public void handleResponse(AndroidConfigurationResponse androidConfigurationResponse) {

        if (androidConfigurationResponse == null
                || androidConfigurationResponse.getAndroidDevicePayload() == null) {
            //TODO add deeper checks ?
            notifyGeneralFailure();
            notifyReady();
            return;
        }

        try {
            //convert response to struct object
            //call device_connectWithProfile
            StructConfigParameters structConfigParameters = new StructConfigParameters();
            deviceConfigurable.device_connectWithProfile(structConfigParameters);
        } catch (Exception e) {
            Log.e("CLEARENT", "Failed to process configuration", e);
            notifyGeneralFailure();
        }
        notifyReady();
    }

    public void notifyReady() {
        deviceConfigurable.setDeviceConfigured(true);
    }

    private void notifyGeneralFailure() {
        deviceConfigurable.notifyConfigurationFailure(GENERAL_ERROR);
    }
}
