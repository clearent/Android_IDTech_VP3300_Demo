package com.clearent.device.config;

import com.clearent.device.domain.CommunicationRequest;

public interface DeviceConfigurator {
    void configure(CommunicationRequest communicationRequest);
}
