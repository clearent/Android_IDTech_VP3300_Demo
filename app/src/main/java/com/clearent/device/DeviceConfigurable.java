package com.clearent.device;

import com.idtechproducts.device.StructConfigParameters;

public interface DeviceConfigurable extends ReaderReadyAware {
    void notifyConfigurationFailure(String message);
    boolean device_connectWithProfile(StructConfigParameters profile);
    void setDeviceConfigured(boolean configured);
    boolean isDeviceConfigured();
}
