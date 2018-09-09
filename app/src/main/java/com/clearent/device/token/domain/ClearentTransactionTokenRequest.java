package com.clearent.device.token.domain;


import com.google.gson.annotations.SerializedName;

public class ClearentTransactionTokenRequest {

    @SerializedName("tlv")
    private String tlv;

    @SerializedName("encrypted")
    private boolean encrypted;

    @SerializedName("emv")
    private boolean emv;

    @SerializedName("firmware-version")
    private String firmwareVersion;

    @SerializedName("device-serial-number")
    private String deviceSerialNumber;

    @SerializedName("kernel-version")
    private String kernelVersion;

    @SerializedName("track2-data")
    private String track2Data;

    @SerializedName("application-preferred-name-tag-9f12")
    private String applicationPreferredNameTag9F12;

    @SerializedName("device-format")
    private String deviceFormat = "IDTECH";

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

    public String getDeviceFormat() {
        return deviceFormat;
    }

    public void setDeviceFormat(String deviceFormat) {
        this.deviceFormat = deviceFormat;
    }
}
