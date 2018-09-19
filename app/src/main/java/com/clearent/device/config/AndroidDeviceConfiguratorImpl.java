package com.clearent.device.config;

import com.clearent.device.DeviceConfigurable;
import com.clearent.device.config.domain.AndroidConfigurationResponse;
import com.clearent.device.domain.CommunicationRequest;

public class AndroidDeviceConfiguratorImpl implements AndroidDeviceConfigurator {

    private DeviceConfigurable deviceConfigurable;

    public AndroidDeviceConfiguratorImpl(DeviceConfigurable deviceConfigurable) {
        this.deviceConfigurable = deviceConfigurable;
    }

    @Override
    public void configure(CommunicationRequest communicationRequest) {
        configureAndroid(communicationRequest);
    }

    private void configureAndroid(CommunicationRequest communicationRequest) {
        GetAndroidConfigurationTaskResponseHandler getAndroidConfigurationTaskResponseHandler = new GetAndroidConfigurationTaskResponseHandler(deviceConfigurable);
        fetchConfiguration(communicationRequest, getAndroidConfigurationTaskResponseHandler);
    }

    void fetchConfiguration(CommunicationRequest communicationRequest, final GetAndroidConfigurationTaskResponseHandler getAndroidConfigurationTaskResponseHandler) {
        new GetAndroidConfigurationTask(communicationRequest, new GetAndroidConfigurationTask.AsyncResponse() {
            @Override
            public void processFinish(AndroidConfigurationResponse androidConfigurationResponse) {
                getAndroidConfigurationTaskResponseHandler.handleResponse(androidConfigurationResponse);
            }
        }).execute();
    }
}
