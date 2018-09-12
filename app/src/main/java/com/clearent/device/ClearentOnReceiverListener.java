package com.clearent.device;


import android.util.Log;

import com.clearent.device.config.ClearentConfigurator;
import com.clearent.device.config.ClearentConfiguratorImpl;
import com.clearent.device.domain.CommunicationRequest;
import com.clearent.device.token.services.CardTokenizer;
import com.clearent.device.token.services.CardTokenizerImpl;
import com.idtechproducts.device.ErrorCode;
import com.idtechproducts.device.IDTEMVData;
import com.idtechproducts.device.IDTMSRData;
import com.idtechproducts.device.OnReceiverListener;
import com.idtechproducts.device.StructConfigParameters;

import java.util.ArrayList;
import java.util.List;

import static com.idtechproducts.device.ReaderInfo.EVENT_MSR_Types.EVENT_MSR_CARD_DATA;
// clearent_vp3300 needs an interface
public class ClearentOnReceiverListener implements OnReceiverListener {

    private Clearent_VP3300 clearentVp3300;
    private PublicOnReceiverListener publicOnReceiverListener;

    public ClearentOnReceiverListener(Clearent_VP3300 clearentVp3300, PublicOnReceiverListener publicOnReceiverListener) {
        this.clearentVp3300 = clearentVp3300;
        this.publicOnReceiverListener = publicOnReceiverListener;
    }

    @Override
    public void swipeMSRData(IDTMSRData idtmsrData) {

        //Common.parse_MSRData(device.device_getDeviceType(), msr_card);
        if (idtmsrData == null || idtmsrData.result != ErrorCode.SUCCESS || idtmsrData.event != EVENT_MSR_CARD_DATA || (idtmsrData.track2 == null && idtmsrData.encTrack2 == null)) {
            clearentVp3300.notifyFailure("Invalid Swipe");
            return;
        }
        CardTokenizer cardTokenizer = new CardTokenizerImpl(clearentVp3300);
        cardTokenizer.createTransactionToken(idtmsrData);
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

        if(idtemvData.result == IDTEMVData.APP_NO_MATCHING) {
            notify("FALLBACK TO SWIPE");
            //TODO Do we need to do this ? Look at the flag in the demo class
//            SEL startFallbackSwipeSelector = @selector(startFallbackSwipe);
//        [NSTimer scheduledTimerWithTimeInterval:1 target:self selector:startFallbackSwipeSelector userInfo:nil repeats:false];
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
//
//
//        int FALLBACK_SWIPE=80, NONTECH_FALLBACK_SWIPE=95, CONTACTLESS_EMV=07, CONTACTLESS_MAGNETIC_SWIPE=91;
//        int SWIPE=90;
//
//        int entryMode = 0;
//        if (idtemvData.unencryptedTags != null) {
//            entryMode = getEntryMode([[idtemvData.unencryptedTags objectForKey:@"9F39"] description]);
//        } else if (emvData.encryptedTags != nil) {
//            entryMode = getEntryMode([[emvData.encryptedTags objectForKey:@"9F39"] description]);
//        }
//
//        if(entryMode == 0) {
//            NSLog(@"No entryMode defined");
//            return;
//        } else {
//            NSLog(@"entryMode: %d", entryMode);
//        }
//

         //TODO _originalEntryMode logic to keep swipes from having to send down the kernel version in the emv tags
        //TODO is nontech fallback swipe handled ????
        if (idtemvData.msr_cardData != null && idtemvData.result == IDTEMVData.MSR_SUCCESS) {
            //if(entryMode == SWIPE) {
                swipeMSRData(idtemvData.msr_cardData);
            //} else if(isSupportedEmvEntryMode(entryMode)) {
              //  ClearentTransactionTokenRequest *clearentTransactionTokenRequest = [self createClearentTransactionTokenRequest:emvData];
           // [self createTransactionToken:clearentTransactionTokenRequest];
           // } else {
           // [self deviceMessage:GENERIC_CARD_READ_ERROR_RESPONSE];
           // }
      //  } else if (idtemvData.result == IDTEMVData.GO_ONLINE || (entryMode == NONTECH_FALLBACK_SWIPE || entryMode == CONTACTLESS_EMV || entryMode == CONTACTLESS_MAGNETIC_SWIPE || emvData.cardType == 1)) {
        } else if (idtemvData.result == IDTEMVData.GO_ONLINE) {
            CardTokenizer cardTokenizer = new CardTokenizerImpl(clearentVp3300);
            cardTokenizer.createTransactionToken(idtemvData);
        }
    }

    @Override
    public void deviceConnected() {
        publicOnReceiverListener.deviceConnected();

        String[] message = {"VIVOpay connected. Waiting for configuration to complete...\n"};
        publicOnReceiverListener.lcdDisplay(0,message,0);

        //Grab this information only after connecting to avoid any 'busy' signals with the reader.
        String previousDeviceSerialNumber = clearentVp3300.getDeviceSerialNumber();
        clearentVp3300.setDeviceSerialNumber();
        clearentVp3300.setFirmwareVersion();
        clearentVp3300.setKernelVersion();

        if(previousDeviceSerialNumber != null && !previousDeviceSerialNumber.equals(clearentVp3300.getDeviceSerialNumber())) {
            clearentVp3300.setConfigured(false);
        }

        if(!clearentVp3300.isConfigured()) {
            ClearentConfigurator clearentConfigurator = new ClearentConfiguratorImpl(clearentVp3300);
            clearentConfigurator.configure(createCommunicationRequest());
        } else {
            String[] readyMessage = {"VIVOpay configured and ready\n"};
            publicOnReceiverListener.lcdDisplay(0, readyMessage, 0);
        }
    }

    private CommunicationRequest createCommunicationRequest() {
        String kernelVersion = clearentVp3300.getKernelVersion();
        String deviceSerialNumber = clearentVp3300.getDeviceSerialNumber();
        return new CommunicationRequest(clearentVp3300.getPaymentsBaseUrl(), clearentVp3300.getPaymentsPublicKey(), deviceSerialNumber, kernelVersion);
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

//
//    - (void) startFallbackSwipe {
//        RETURN_CODE startMSRSwipeRt = [[IDT_VP3300 sharedController] msr_startMSRSwipe];
//        if (RETURN_CODE_DO_SUCCESS == startMSRSwipeRt) {
//       [self deviceMessage:@"FALLBACK TO SWIPE start success"];
//        } else{
//       [self deviceMessage:@"FALLBACK TO SWIPE start failed"];
//        }
//    }
//
//    int getEntryMode (NSString* rawEntryMode) {
//        if(rawEntryMode == nil || [rawEntryMode isEqualToString:@""]) {
//            return 0;
//        }
//        NSString *entryModeWithoutTags = [rawEntryMode stringByReplacingOccurrencesOfString:@"[\\<\\>]" withString:@"" options:NSRegularExpressionSearch range:NSMakeRange(0, [rawEntryMode length])];
//        return [entryModeWithoutTags intValue];
//    }
//
//    BOOL isSupportedEmvEntryMode (int entryMode) {
//        if(entryMode == FALLBACK_SWIPE || entryMode == NONTECH_FALLBACK_SWIPE || entryMode == CONTACTLESS_EMV || entryMode == CONTACTLESS_MAGNETIC_SWIPE) {
//            return true;
//        }
//        return false;
//    }
//

}
