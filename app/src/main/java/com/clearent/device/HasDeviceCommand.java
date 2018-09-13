package com.clearent.device;

import com.idtechproducts.device.ResDataStruct;

public interface HasDeviceCommand {
    void notifyCommandFailure(int returnCode, String message);
    int device_sendDataCommand(String cmd, boolean calcLRC, String data, ResDataStruct respData);
}
