package com.clearent.device.config.domain;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AndroidDevice {

    @SerializedName("InputFreq")
    int inputFreq;


    public AndroidDevice() {
    }

    public int getInputFreq() {
        return inputFreq;
    }

    public void setInputFreq(int inputFreq) {
        this.inputFreq = inputFreq;
    }
}
