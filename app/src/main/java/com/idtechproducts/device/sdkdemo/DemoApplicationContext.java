package com.idtechproducts.device.sdkdemo;

import android.content.Context;

import com.clearent.idtech.android.PublicOnReceiverListener;
import com.clearent.idtech.android.family.ApplicationContext;
import com.idtechproducts.device.ReaderInfo;

public class DemoApplicationContext implements ApplicationContext {

    private ReaderInfo.DEVICE_TYPE deviceType;
    private PublicOnReceiverListener publicOnReceiverListener;
    private Context context;
    private String paymentsBaseUrl;
    private String paymentsPublicKey;

    public DemoApplicationContext(ReaderInfo.DEVICE_TYPE deviceType, PublicOnReceiverListener publicOnReceiverListener, Context context, String paymentsBaseUrl, String paymentsPublicKey) {
        this.deviceType = deviceType;
        this.publicOnReceiverListener = publicOnReceiverListener;
        this.context = context;
        this.paymentsBaseUrl = paymentsBaseUrl;
        this.paymentsPublicKey = paymentsPublicKey;
    }

    @Override
    public ReaderInfo.DEVICE_TYPE getDeviceType() {
        return deviceType;
    }

    @Override
    public PublicOnReceiverListener getPublicOnReceiverListener() {
        return publicOnReceiverListener;
    }

    @Override
    public Context getAndroidContext() {
        return context;
    }

    @Override
    public String getPaymentsBaseUrl() {
        return paymentsBaseUrl;
    }

    @Override
    public String getPaymentsPublicKey() {
        return paymentsPublicKey;
    }

}
