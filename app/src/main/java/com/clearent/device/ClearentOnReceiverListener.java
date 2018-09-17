package com.clearent.device;


import android.util.Log;

import com.clearent.device.config.ClearentConfigurator;
import com.clearent.device.config.ClearentConfiguratorImpl;
import com.clearent.device.domain.CommunicationRequest;
import com.clearent.device.domain.EntryMode;
import com.clearent.device.family.IDTDevice;
import com.clearent.device.token.services.CardTokenizer;
import com.clearent.device.token.services.CardTokenizerImpl;
import com.idtechproducts.device.Common;
import com.idtechproducts.device.ErrorCode;
import com.idtechproducts.device.IDTEMVData;
import com.idtechproducts.device.IDTMSRData;
import com.idtechproducts.device.OnReceiverListener;
import com.idtechproducts.device.StructConfigParameters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.idtechproducts.device.ReaderInfo.EVENT_MSR_Types.EVENT_MSR_CARD_DATA;

public class ClearentOnReceiverListener implements OnReceiverListener {

    private IDTDevice idtDevice;
    private PublicOnReceiverListener publicOnReceiverListener;
    private boolean previousDipDidNotMatchOnApp = false;

    public ClearentOnReceiverListener(IDTDevice idtDevice, PublicOnReceiverListener publicOnReceiverListener) {
        this.idtDevice = idtDevice;
        this.publicOnReceiverListener = publicOnReceiverListener;
    }

    @Override
    public void swipeMSRData(IDTMSRData idtmsrData) {

        if (idtmsrData == null || idtmsrData.result != ErrorCode.SUCCESS || idtmsrData.event != EVENT_MSR_CARD_DATA || (idtmsrData.track2 == null && idtmsrData.encTrack2 == null)) {
            notifyFailure("Invalid Swipe");
            return;
        }

        CardTokenizer cardTokenizer = new CardTokenizerImpl(idtDevice);

        if(isPreviousDipDidNotMatchOnApp()) {
            setPreviousDipDidNotMatchOnApp(false);
            cardTokenizer.createTransactionTokenForFallback(idtmsrData);
        } else {
            cardTokenizer.createTransactionToken(idtmsrData);
        }
    }

    void notifyFailure(String message) {
        String[] messageArray = {message};
        lcdDisplay(0, messageArray, 0);
    }

    @Override
    public void lcdDisplay(int i, String[] strings, int i1) {
        publicOnReceiverListener.lcdDisplay(i, muteOrConvertMessages(strings), i1);
    }

    @Override
    public void lcdDisplay(int i, String[] strings, int i1, byte[] bytes, byte b) {
        publicOnReceiverListener.lcdDisplay(i, muteOrConvertMessages(strings), i1, bytes, b);
    }

    //TODO verify these messages need to be converted (just).
    String[] muteOrConvertMessages (String[] messages) {
        if(messages == null || messages.length == 0) {
            return messages;
        }
        List<String> messageList = new ArrayList<>();
        for(String message:messages) {
            Log.i("WATCH", message);
            if(message != null && "POWERING UNIPAY".equalsIgnoreCase(message) ) {
                Log.i("INFO", "Starting VIVOpay...");
                messageList.add("Starting VIVOpay...\n");
            } else if(message != null && "RETURN_CODE_LOW_VOLUME".equalsIgnoreCase(message) ) {
                Log.i("INFO", "VIVOpay failed to connect.Turn the headphones volume all the way up and reconnect.");
                messageList.add("Starting VIVOpay...\n");
            } else if(message != null && "TERMINATE".equalsIgnoreCase(message) ) {
                Log.i("INFO", "IDTech framework terminated the request.");
                messageList.add(message);
            } else if(message != null && "DECLINED".equalsIgnoreCase(message) ) {
                Log.i("INFO", "This is not really a decline. Clearent is creating a transaction token for later use.");
            } else if(message != null && "APPROVED".equalsIgnoreCase(message) ) {
                Log.i("INFO", "This is not really an approval. Clearent is creating a transaction token for later use.");
            } else {
                messageList.add(message);
            }
        }
        if(messageList.size() == 0) {
            return new String[]{};
        }

        String[] convertedMessages = new String[messageList.size()];
        convertedMessages = messageList.toArray(convertedMessages);

        return convertedMessages;
    }

    private void notify(String message) {
        String[] messageArray = {message + "\n"};
        lcdDisplay(0,messageArray,0);
    }

    @Override
    public void emvTransactionData(IDTEMVData idtemvData) {
        IDTEMVData card = idtemvData;

        if (idtemvData == null) {
            return;
        }

        if (idtemvData.result == IDTEMVData.TIME_OUT) {
            notify("TIMEOUT");
            return;
        }

        if (idtemvData.result == IDTEMVData.DECLINED_OFFLINE) {
            notify("DECLINED OFFLINE");
            return;
        }

        if (idtemvData.result == IDTEMVData.MSR_CARD_ERROR) {
            notify("INVALID SWIPE");
            return;
        }

        //TODO see if we should check for a fallback flag already set (see demo). useful if this class is reused for other families
        if(idtemvData.result == IDTEMVData.APP_NO_MATCHING) {
            //TODO test this..look at the entry mode. is it a non technical fallback swipe
            EntryMode entryMode = getEntryMode(idtemvData);
            Log.i("INFO","Entry Mode is " + entryMode.name());
            setPreviousDipDidNotMatchOnApp(true);
            notify("SWIPE CARD");
            idtDevice.msr_startMSRSwipe();
            return;
        }

        //The mobile-jwt call should succeed or fail. We call the IDTech complete method every time.
        if (idtemvData.result == IDTEMVData.APPROVED || idtemvData.result == IDTEMVData.APPROVED_OFFLINE ) {
            return;
        }

        //We aren't starting an authorization so this result code should never be set. But return just in case.
        if (idtemvData.result == IDTEMVData.START_TRANS_SUCCESS) {
            return;
        }

        if (idtemvData.cardType == 1) {
            notify("CONTACTLESS NOT SUPPORTED");
            return;
        }

        EntryMode entryMode = getEntryMode(idtemvData);
        Log.i("INFO","Entry Mode is " + entryMode.name());
        if(EntryMode.INVALID.equals(entryMode)) {
            Log.i("ERROR","No entry mode found. Skip processing.");
            return;
        }

        //TODO is nontech fallback swipe handled ????
        if (idtemvData.msr_cardData != null && idtemvData.result == IDTEMVData.MSR_SUCCESS) {
            if(entryMode == EntryMode.FALLBACK_SWIPE) {
                Log.i("INFO","Trying fallback swipe...");
                CardTokenizer cardTokenizer = new CardTokenizerImpl(idtDevice);
                cardTokenizer.createTransactionTokenForFallback(idtemvData.msr_cardData);
            } else if(entryMode == EntryMode.SWIPE) {
                Log.i("INFO","Trying swipe from emv method...");
                swipeMSRData(idtemvData.msr_cardData);
            }
            //TODO test non tech fallback
        } else if (idtemvData.result == IDTEMVData.GO_ONLINE) {
            //TODo test the ios side that has this logic..are we sending dup requests ?
        //} else if (idtemvData.result == IDTEMVData.GO_ONLINE || (entryMode == EntryMode.NONTECH_FALLBACK_SWIPE || entryMode == EntryMode.EMV)) {
            Log.i("INFO","Trying to go online...");
            CardTokenizer cardTokenizer = new CardTokenizerImpl(idtDevice);
            cardTokenizer.createTransactionToken(idtemvData);
        }
    }

    private EntryMode getEntryMode(IDTEMVData idtemvData) {
        Map<String, byte[]> tags = null;
        if (idtemvData.unencryptedTags != null) {
            tags = idtemvData.unencryptedTags;
        } else if (idtemvData.encryptedTags != null) {
            tags = idtemvData.encryptedTags;
        }
        byte[] entryModeBytes = tags.get("9F39");
        String hexStringFromBytes = Common.getHexStringFromBytes(entryModeBytes);
        int entryMode = Integer.decode(hexStringFromBytes);
        return EntryMode.valueOfByInt(entryMode);
    }

    @Override
    public void deviceConnected() {
        publicOnReceiverListener.deviceConnected();
        String[] message = {"VIVOpay connected. Waiting for configuration to complete...\n"};
        publicOnReceiverListener.lcdDisplay(0,message,0);

        //temp!!! I wanted to skip the configuration during development.
       // idtDevice.setConfigured(true);

        configure();
    }

    private void configure() {
        //Grab this information only after connecting to avoid any 'busy' signals with the reader.
        String previousDeviceSerialNumber = idtDevice.getDeviceSerialNumber();
        idtDevice.setDeviceSerialNumber();
        idtDevice.setFirmwareVersion();
        idtDevice.setKernelVersion();

        //If they connect a different reader set the configure flag to false to force configuration.
        if(previousDeviceSerialNumber != null && !previousDeviceSerialNumber.equals(idtDevice.getDeviceSerialNumber())) {
            idtDevice.setConfigured(false);
        }

        if(!idtDevice.isConfigured()) {
            ClearentConfigurator clearentConfigurator = new ClearentConfiguratorImpl(idtDevice);
            clearentConfigurator.configure(createCommunicationRequest());
        } else {
            String[] readyMessage = {"VIVOpay configured and ready\n"};
            publicOnReceiverListener.lcdDisplay(0, readyMessage, 0);
        }
    }

    private CommunicationRequest createCommunicationRequest() {
        String kernelVersion = idtDevice.getKernelVersion();
        String deviceSerialNumber = idtDevice.getDeviceSerialNumber();
        return new CommunicationRequest(idtDevice.getPaymentsBaseUrl(), idtDevice.getPaymentsPublicKey(), deviceSerialNumber, kernelVersion);
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

    /**
     * Call this method to reset any state between transactions.
     */
    public void reset() {
        previousDipDidNotMatchOnApp = false;
    }

    public boolean isPreviousDipDidNotMatchOnApp() {
        return previousDipDidNotMatchOnApp;
    }

    public void setPreviousDipDidNotMatchOnApp(boolean previousDipDidNotMatchOnApp) {
        this.previousDipDidNotMatchOnApp = previousDipDidNotMatchOnApp;
    }
}
