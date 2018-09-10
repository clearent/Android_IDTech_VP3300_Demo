package com.clearent.device.config;

import com.clearent.device.ClearentOnReceiverListener;
import com.clearent.device.Clearent_VP3300;

public interface ClearentConfigurator {
    boolean isConfigured();

    void notifyReady();
    void notifyFailure(String error);

    void configure();
}
