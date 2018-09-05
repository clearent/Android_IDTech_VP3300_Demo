package com.clearent.device.token.domain;

public class ClearentTransactionTokenRequest {

    private String tlv;
    private boolean encrypted;
    private boolean emv;
    private String firmwareVersion;
    private String deviceSerialNumber;
    private String kernelVersion;
    private String track2Data;
    private String applicationPreferredNameTag9F12;

    public ClearentTransactionTokenRequest() {

    }

    public String getTlv() {
        return tlv;
    }

    public void setTlv(String tlv) {
        this.tlv = tlv;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public void setEncrypted(boolean encrypted) {
        this.encrypted = encrypted;
    }

    public boolean isEmv() {
        return emv;
    }

    public void setEmv(boolean emv) {
        this.emv = emv;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getDeviceSerialNumber() {
        return deviceSerialNumber;
    }

    public void setDeviceSerialNumber(String deviceSerialNumber) {
        this.deviceSerialNumber = deviceSerialNumber;
    }

    public String getKernelVersion() {
        return kernelVersion;
    }

    public void setKernelVersion(String kernelVersion) {
        this.kernelVersion = kernelVersion;
    }

    public String getTrack2Data() {
        return track2Data;
    }

    public void setTrack2Data(String track2Data) {
        this.track2Data = track2Data;
    }

    public String getApplicationPreferredNameTag9F12() {
        return applicationPreferredNameTag9F12;
    }

    public void setApplicationPreferredNameTag9F12(String applicationPreferredNameTag9F12) {
        this.applicationPreferredNameTag9F12 = applicationPreferredNameTag9F12;
    }

    public String asJson() {
        //TODO how to convert to json
        return "";
    }

}
