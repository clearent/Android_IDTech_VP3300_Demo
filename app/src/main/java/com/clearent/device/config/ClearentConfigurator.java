package com.clearent.device.config;

import com.clearent.device.domain.CommunicationRequest;

public interface ClearentConfigurator {
    void configure(CommunicationRequest communicationRequest);
}
