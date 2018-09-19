package com.clearent.device.config;

import com.clearent.device.domain.CommunicationRequest;

public interface AndroidDeviceConfigurator {
    void configure(CommunicationRequest communicationRequest);
}
