package com.clearent.device;

import com.idtechproducts.device.IDTEMVData;
import com.idtechproducts.device.IDTMSRData;
import com.idtechproducts.device.OnReceiverListener;
import com.idtechproducts.device.StructConfigParameters;

public class ClearentOnReceiverListener implements OnReceiverListener {

    private PublicOnReceiverListener publicOnReceiverListener;
    private String paymentsBaseUrl;
    private String paymentsPublicKey;

    public ClearentOnReceiverListener(PublicOnReceiverListener publicOnReceiverListener, String paymentsBaseUrl, String paymentsPublicKey) {
        this.publicOnReceiverListener = publicOnReceiverListener;
        this.paymentsBaseUrl = paymentsBaseUrl;
        this.paymentsPublicKey = paymentsPublicKey;
    }

    @Override
    public void swipeMSRData(IDTMSRData idtmsrData) {
        //TODO happy path call the mobile-jwt service
        publicOnReceiverListener.successfulTransactionToken("tokenFromSuccessfulSwipe");
    }

    @Override
    public void lcdDisplay(int i, String[] strings, int i1) {
        publicOnReceiverListener.lcdDisplay(i,strings,i1);
    }

    @Override
    public void lcdDisplay(int i, String[] strings, int i1, byte[] bytes, byte b) {
        publicOnReceiverListener.lcdDisplay(i,strings,i1,bytes,b);
    }

    @Override
    public void emvTransactionData(IDTEMVData idtemvData) {
        //TODO happy path call the mobile-jwt service
        publicOnReceiverListener.successfulTransactionToken("tokenFromSuccessfulDip");
    }

    @Override
    public void deviceConnected() {
        publicOnReceiverListener.deviceConnected();
    }

    @Override
    public void deviceDisconnected() {
        publicOnReceiverListener.deviceDisconnected();
    }

    @Override
    public void timeout(int i) {
        publicOnReceiverListener.timeout(i);
    }

    @Override
    public void autoConfigCompleted(StructConfigParameters structConfigParameters) {
        publicOnReceiverListener.autoConfigCompleted(structConfigParameters);
    }

    @Override
    public void autoConfigProgress(int i) {
        publicOnReceiverListener.autoConfigProgress(i);
    }

    @Override
    public void msgRKICompleted(String s) {
        publicOnReceiverListener.msgRKICompleted(s);
    }

    @Override
    public void ICCNotifyInfo(byte[] bytes, String s) {
        publicOnReceiverListener.ICCNotifyInfo(bytes,s);
    }

    @Override
    public void msgBatteryLow() {
        publicOnReceiverListener.msgBatteryLow();
    }

    @Override
    public void LoadXMLConfigFailureInfo(int i, String s) {
        publicOnReceiverListener.LoadXMLConfigFailureInfo(i,s);
    }

    @Override
    public void msgToConnectDevice() {
        publicOnReceiverListener.msgToConnectDevice();
    }

    @Override
    public void msgAudioVolumeAjustFailed() {
        publicOnReceiverListener.msgAudioVolumeAjustFailed();
    }

    @Override
    public void dataInOutMonitor(byte[] bytes, boolean b) {
        publicOnReceiverListener.dataInOutMonitor(bytes,b);
    }
}
