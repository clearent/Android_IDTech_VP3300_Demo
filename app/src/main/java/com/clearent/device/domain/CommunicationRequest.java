package com.clearent.device.domain;

public class CommunicationRequest {

    private String baseUrl;
    private String publicKey;
    private String deviceSerialNumber;
    private String kernelVersion;

    public CommunicationRequest(String baseUrl, String publicKey, String deviceSerialNumber, String kernelVersion) {
        this.baseUrl = baseUrl;
        this.publicKey = publicKey;
        this.deviceSerialNumber = deviceSerialNumber;
        this.kernelVersion = kernelVersion;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
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
}
