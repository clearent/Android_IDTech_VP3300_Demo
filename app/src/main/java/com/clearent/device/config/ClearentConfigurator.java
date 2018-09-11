package com.clearent.device.config;


public interface ClearentConfigurator {
    boolean isConfigured();
    void notifyReady();
    void configure();
}
