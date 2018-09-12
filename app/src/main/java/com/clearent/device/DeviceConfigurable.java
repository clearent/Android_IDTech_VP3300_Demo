package com.clearent.device;

import com.idtechproducts.device.ResDataStruct;

public interface DeviceConfigurable extends HasDeviceCommand, ReaderReadyAware {

    void notifyConfigurationFailure(String message);
    void notifyConfigurationFailure(int returnCode, String message);
    int emv_setCAPK(byte[] key, ResDataStruct respData);
    int emv_setApplicationData(String aid, byte[] TLV, ResDataStruct respData);
}
