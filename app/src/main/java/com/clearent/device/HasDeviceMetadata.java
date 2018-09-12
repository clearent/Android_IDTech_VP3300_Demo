package com.clearent.device;

public interface HasDeviceMetadata {

    String getDeviceSerialNumber();

    String getKernelVersion();

    String getFirmwareVersion();
}
