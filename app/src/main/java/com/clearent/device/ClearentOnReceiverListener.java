package com.clearent.device;

import android.os.AsyncTask;
import android.util.Log;

import com.clearent.device.config.ClearentConfigurator;
import com.clearent.device.config.ClearentConfiguratorImpl;
import com.idtechproducts.device.IDTEMVData;
import com.idtechproducts.device.IDTMSRData;
import com.idtechproducts.device.OnReceiverListener;
import com.idtechproducts.device.StructConfigParameters;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class ClearentOnReceiverListener implements OnReceiverListener {

    private Clearent_VP3300 clearentVp3300;
    private PublicOnReceiverListener publicOnReceiverListener;

    private ClearentConfigurator clearentConfigurator;

    public ClearentOnReceiverListener(Clearent_VP3300 clearentVp3300, PublicOnReceiverListener publicOnReceiverListener) {
        this.clearentVp3300 = clearentVp3300;
        this.publicOnReceiverListener = publicOnReceiverListener;
    }

    @Override
    public void swipeMSRData(IDTMSRData idtmsrData) {
        //TODO happy path call the mobile-jwt service
        publicOnReceiverListener.successfulTransactionToken("tokenFromSuccessfulSwipe");
    }

    @Override
    public void lcdDisplay(int i, String[] strings, int i1) {
        publicOnReceiverListener.lcdDisplay(i, strings, i1);
    }

    @Override
    public void lcdDisplay(int i, String[] strings, int i1, byte[] bytes, byte b) {
        publicOnReceiverListener.lcdDisplay(i, strings, i1, bytes, b);
    }

    @Override
    public void emvTransactionData(IDTEMVData idtemvData) {
        //TODO happy path call the mobile-jwt service
        IDTEMVData card = idtemvData;
        publicOnReceiverListener.successfulTransactionToken("tokenFromSuccessfulDip");
    }

    @Override
    public void deviceConnected() {
        publicOnReceiverListener.deviceConnected();
        //TODO How do we send messages back ? just use the lcdDisplay method ??
        //[self deviceMessage:@"VIVOpay connected. Waiting for configuration to complete..."];
        //TODO how do we get the kernel version and the serial number without access to the idt_vp3300 object ? add to composition ?

        ClearentConfigurator clearentConfigurator = new ClearentConfiguratorImpl();
        clearentConfigurator.configure(clearentVp3300);
        //TODO DAVE H. START HERE !!!!!
        // clearentConfigurator.configure(this);
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
        publicOnReceiverListener.ICCNotifyInfo(bytes, s);
    }

    @Override
    public void msgBatteryLow() {
        publicOnReceiverListener.msgBatteryLow();
    }

    @Override
    public void LoadXMLConfigFailureInfo(int i, String s) {
        publicOnReceiverListener.LoadXMLConfigFailureInfo(i, s);
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
        publicOnReceiverListener.dataInOutMonitor(bytes, b);
    }
}
