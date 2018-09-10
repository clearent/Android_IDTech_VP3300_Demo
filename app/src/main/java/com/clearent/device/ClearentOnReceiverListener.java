package com.clearent.device;

import android.os.AsyncTask;
import android.util.Log;

import com.clearent.device.config.ClearentConfigurator;
import com.clearent.device.config.ClearentConfiguratorImpl;
import com.clearent.device.token.domain.ClearentTransactionTokenRequest;
import com.idtechproducts.device.Common;
import com.idtechproducts.device.ErrorCode;
import com.idtechproducts.device.IDTEMVData;
import com.idtechproducts.device.IDTMSRData;
import com.idtechproducts.device.OnReceiverListener;
import com.idtechproducts.device.StructConfigParameters;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import static com.idtechproducts.device.ReaderInfo.EVENT_MSR_Types.EVENT_MSR_CARD_DATA;

public class ClearentOnReceiverListener implements OnReceiverListener {


    private static final String DEVICE_SERIAL_NUMBER_EMV_TAG = "DF78";
    private static final String KERNEL_VERSION_EMV_TAG = "DF79";

    private Clearent_VP3300 clearentVp3300;
    private PublicOnReceiverListener publicOnReceiverListener;

    private ClearentConfigurator clearentConfigurator;

    public ClearentOnReceiverListener(Clearent_VP3300 clearentVp3300, PublicOnReceiverListener publicOnReceiverListener) {
        this.clearentVp3300 = clearentVp3300;
        this.publicOnReceiverListener = publicOnReceiverListener;
        this.clearentConfigurator = new ClearentConfiguratorImpl(clearentVp3300, this);
    }

    @Override
    public void swipeMSRData(IDTMSRData idtmsrData) {

        //Common.parse_MSRData(device.device_getDeviceType(), msr_card);
        //TODO happy path call the mobile-jwt service
        publicOnReceiverListener.successfulTransactionToken("tokenFromSuccessfulSwipe");
        if (idtmsrData == null || idtmsrData.result != ErrorCode.SUCCESS || idtmsrData.event != EVENT_MSR_CARD_DATA || (idtmsrData.track2 != null || idtmsrData.encTrack2 != null)) {
            //TODO notify of error in swipe
            return;
        }

        ClearentTransactionTokenRequest clearentTransactionTokenRequest = createClearentTransactionTokenRequestForASwipe(idtmsrData);
        createTransactionToken(clearentTransactionTokenRequest);
    }

    public  ClearentTransactionTokenRequest createClearentTransactionTokenRequestForASwipe(IDTMSRData cardData) {
        ClearentTransactionTokenRequest clearentTransactionTokenRequest = new ClearentTransactionTokenRequest();
        if (cardData.encTrack2 != null) {
            //TODO do we need to utf8 encode this string ?
            String encryptedTrack2Data = Common.byteToString(cardData.encTrack2);
            clearentTransactionTokenRequest = createClearentTransactionToken(false ,true,encryptedTrack2Data.toUpperCase());
        } else if (cardData.track2 != null) {
            clearentTransactionTokenRequest = createClearentTransactionToken(false,false ,cardData.track2.toUpperCase());
        }

        //add the required tags as a tlv string for emv fallback swipe scenario (ClearentSwitch uses this)
        Map<String, byte[]> requiredTags = new HashMap<>();
        addRequiredTags(requiredTags);
        clearentTransactionTokenRequest.setTlv(convertToTlv(requiredTags));

        return clearentTransactionTokenRequest;
    }

    public  ClearentTransactionTokenRequest createClearentTransactionToken(boolean emv ,boolean encrypted, String track2Data) {
        ClearentTransactionTokenRequest clearentTransactionTokenRequest = new ClearentTransactionTokenRequest();
        clearentTransactionTokenRequest.setEmv(emv);
        clearentTransactionTokenRequest.setEncrypted(encrypted);
        clearentTransactionTokenRequest.setDeviceSerialNumber(getDeviceSerialNumber());
        clearentTransactionTokenRequest.setKernelVersion(getKernelVersion());
        clearentTransactionTokenRequest.setFirmwareVersion(getFirmwareVersion());
        clearentTransactionTokenRequest.setTrack2Data(track2Data.toUpperCase());
        return clearentTransactionTokenRequest;
    }

    public String getDeviceSerialNumber() {
        StringBuilder serialNumberSb = new StringBuilder();
        int serialNumberRt = clearentVp3300.config_getSerialNumber(serialNumberSb);
        if(serialNumberRt == ErrorCode.SUCCESS) {
           return serialNumberSb.toString();
        }
        return "IDTech Device Serial Number Unknown";
    }

    public String getKernelVersion() {
        StringBuilder kernelVersionSb = new StringBuilder();
        int kernelVersionRt = clearentVp3300.emv_getEMVKernelVersion(kernelVersionSb);
        if(kernelVersionRt == ErrorCode.SUCCESS) {
            return kernelVersionSb.toString();
        }
        return "IDTech Kernel Version Unknown";
    }

    public String getFirmwareVersion() {

        StringBuilder firmwareVersionSb = new StringBuilder();
        int firmwareVersionRt = clearentVp3300.device_getFirmwareVersion(firmwareVersionSb);
        if(firmwareVersionRt == ErrorCode.SUCCESS) {
            return firmwareVersionSb.toString();
        }
        return "IDTech Firmware Version Unknown";
    }


//TODO we need to inspect the messages coming back in lcdDisplay methods and changed them to the messages we are sending back
    //in the ios framework too

    //add this to lcdDisplay because it looks like we need to use that method to communicate through the listener
//    - (void) deviceMessage:(NSString*)message {
//        if(message != nil && [message isEqualToString:@"POWERING UNIPAY"]) {
//       [self.publicDelegate deviceMessage:@"Starting VIVOpay..."];
//            return;
//        }
//        if(message != nil && [message isEqualToString:@"RETURN_CODE_LOW_VOLUME"]) {
//        [self.publicDelegate deviceMessage:@"VIVOpay failed to connect.Turn the headphones volume all the way up and reconnect."];
//            return;
//        }
//    [self.publicDelegate deviceMessage:message];
//    }

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

//
//        NSLog(@"EMV Transaction Data Response: = %@",[[IDT_VP3300 sharedController] device_getResponseCodeString:error]);
//
        //TODO As we test we could implement each of these short circuits or leave them out

//        if (emvData.resultCodeV2 != EMV_RESULT_CODE_V2_NO_RESPONSE) {
//            NSLog(@"emvData.resultCodeV2: = %@",[NSString stringWithFormat:@"EMV_RESULT_CODE_V2_response = %2X",emvData.resultCodeV2]);
//        }
//
        if (idtemvData == null) {
            return;
        }

//
//        if (emvData.resultCodeV2 == EMV_RESULT_CODE_V2_DECLINED_OFFLINE) {
//        [self deviceMessage:CARD_OFFLINE_DECLINED];
//            return;
//        }
//
//        if (emvData.resultCodeV2 == EMV_RESULT_CODE_V2_MSR_CARD_ERROR) {
//        [self deviceMessage:GENERIC_CARD_READ_ERROR_RESPONSE];
//            return;
//        }
//
//        if(emvData.resultCodeV2 == EMV_RESULT_CODE_V2_APP_NO_MATCHING) {
//        [self deviceMessage:@"FALLBACK TO SWIPE"];
//            SEL startFallbackSwipeSelector = @selector(startFallbackSwipe);
//        [NSTimer scheduledTimerWithTimeInterval:1 target:self selector:startFallbackSwipeSelector userInfo:nil repeats:false];
//            return;
//        }
//
//        if (emvData.resultCodeV2 == EMV_RESULT_CODE_V2_TIME_OUT) {
//        [self deviceMessage:TIMEOUT_ERROR_RESPONSE];
//            return;
//        }
//
//        //The mobile-jwt call should succeed or fail. We call the IDTech complete method every time.
//        if (emvData.resultCodeV2 == EMV_RESULT_CODE_V2_APPROVED || emvData.resultCodeV2 == EMV_RESULT_CODE_V2_APPROVED_OFFLINE ) {
//            return;
//        }
//        //We aren't starting an authorization so this result code should never be set. But return just in case.
//        if (emvData.resultCodeV2 == EMV_RESULT_CODE_V2_START_TRANS_SUCCESS) {
//            return;
//        }
//
//        if (emvData.cardType == 1) {
//            NSLog(@"CONTACTLESS");
//        }
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
            ClearentTransactionTokenRequest clearentTransactionTokenRequest = createClearentTransactionTokenRequest(idtemvData);
            createTransactionToken(clearentTransactionTokenRequest);
        }
    }

    @Override
    public void deviceConnected() {
        publicOnReceiverListener.deviceConnected();
        String[] message = {"VIVOpay connected. Waiting for configuration to complete..."};
        publicOnReceiverListener.lcdDisplay(0,message,0);

        if(!clearentConfigurator.isConfigured()) {
            clearentConfigurator.configure();
        }
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
      public ClearentTransactionTokenRequest  createClearentTransactionTokenRequest(IDTEMVData idtemvData) {
//        if(idtemvData.cardData != nil) {
//            if(emvData.cardData.encTrack2 != nil) {
//            [emvData.encryptedTags setValue:emvData.cardData.encTrack2 forKey:TRACK2_DATA_EMV_TAG];
//                return [self createClearentTransactionTokenRequest:emvData.encryptedTags isEncrypted: true cardType:emvData.cardType];
//            } else if(emvData.cardData.track2 != nil) {
//            [emvData.unencryptedTags setValue:emvData.cardData.track2 forKey:TRACK2_DATA_EMV_TAG];
//                return [self createClearentTransactionTokenRequest:emvData.unencryptedTags isEncrypted: false cardType:emvData.cardType];
//            }
//        } else if (emvData.unencryptedTags != nil) {
//            return [self createClearentTransactionTokenRequest:emvData.unencryptedTags isEncrypted: false cardType:emvData.cardType];
//        } else if (emvData.encryptedTags != nil) {
//            return [self createClearentTransactionTokenRequest:emvData.encryptedTags isEncrypted: true cardType:emvData.cardType];
//        }

          if (idtemvData.unencryptedTags != null) {
              return createClearentTransactionTokenRequest(idtemvData.unencryptedTags,false, idtemvData.cardType);
          } else if (idtemvData.encryptedTags != null) {
              return createClearentTransactionTokenRequest(idtemvData.encryptedTags,true,idtemvData.cardType);
          }
          throw new RuntimeException("no tags found");
          //return new ClearentTransactionTokenRequest();
    }

    public  ClearentTransactionTokenRequest createClearentTransactionTokenRequest(Map<String,byte[]> tags, boolean encrypted, int cardType) {
        ClearentTransactionTokenRequest clearentTransactionTokenRequest = new ClearentTransactionTokenRequest();

         if(cardType != 1) {
             Map<String, Map<String, byte[]>> retrievedTSYSTags = null;
             byte[] requestedTags = Common.getByteArray("82959A9B9C5F2A9F029F039F1A9F219F269F279F339F349F359F369F379F394F845F2D5F349F069F129F099F405F369F1E9F105657FF8106FF8105FFEE14FFEE06");
             int emvRetrieveTransactionResultRt = clearentVp3300.emv_retrieveTransactionResult(requestedTags, retrievedTSYSTags);
             if (emvRetrieveTransactionResultRt == ErrorCode.SUCCESS) {
                 //Map<String, Map<String, byte[]>> TLVDict = Common.processTLV(tlvData);
                 Map<String, byte[]> unencryptedTags = retrievedTSYSTags.get("tags");
                // Map<String, byte[]> maskedTags = TLVDict.get("masked");
                 Map<String, byte[]> encryptedTags = retrievedTSYSTags.get("encrypted");


                 //TODO strip out tags we should not send after we've retrieve the data we need (ex track 2 data)
                 //TODO handle encrypted tags
                 String tlvInHex = null;
                 if(encrypted) {
                     tlvInHex = convertToTlv(encryptedTags);
                 } else{

                     for(Map.Entry<String, byte[]> entry: unencryptedTags.entrySet()) {
                         String tag = entry.getKey();
                         if(tag.equals("9F12")) {
                             clearentTransactionTokenRequest.setApplicationPreferredNameTag9F12(Common.byteToString(entry.getValue()));
                         } else if(tag.equals("57")) {
                             clearentTransactionTokenRequest.setTrack2Data(Common.byteToString(entry.getValue()));
                         } else if(tag.equals("FF8105")) {
                              //TODO handle contactless track 2 data field
                         }
                     }
                     removeInvalidTSYSTags(unencryptedTags);
                     addRequiredTags(unencryptedTags);
                     tlvInHex =convertToTlv(unencryptedTags);
                 }

                 clearentTransactionTokenRequest.setTlv(tlvInHex.toUpperCase());
                 clearentTransactionTokenRequest.setEmv(true);
                 clearentTransactionTokenRequest.setKernelVersion(getKernelVersion());
                 clearentTransactionTokenRequest.setDeviceSerialNumber(getDeviceSerialNumber());
                 clearentTransactionTokenRequest.setFirmwareVersion(getFirmwareVersion());
                 clearentTransactionTokenRequest.setEncrypted(encrypted);
             } else {
                 throw new RuntimeException("Failed to retrieve the emv transaction results");
             }
         } else {
             //TODO handle cardtype = 1 ??
         }
//        NSMutableDictionary *outgoingTags;
//        NSData *tagsAsNSData;
//        NSString *tlvInHex;
//        if (cardType == 1) {
//            outgoingTags = [tags mutableCopy];
//        } else {
//            NSDictionary *transactionResultDictionary;
//            NSData *tsysTags = [IDTUtility hexToData:@"82959A9B9C5F2A9F029F039F1A9F219F269F279F339F349F359F369F379F394F845F2D5F349F069F129F099F405F369F1E9F105657FF8106FF8105FFEE14FFEE06"];
//            RETURN_CODE emvRetrieveTransactionResultRt = [[IDT_VP3300 sharedController] emv_retrieveTransactionResult:tsysTags retrievedTags:&transactionResultDictionary];
//            if(RETURN_CODE_DO_SUCCESS == emvRetrieveTransactionResultRt) {
//                outgoingTags = [transactionResultDictionary objectForKey:@"tags"];
//            } else {
//                tlvInHex = @"Failed to retrieve tlv from VIVOpay";
//                //TODO handle error?
//            }
//        }
//        //TODO Search for this data element(DFEF18) Track2 Data during MC contactless swipe
//        NSString *track2Data57 = [IDTUtility dataToHexString:[tags objectForKey:TRACK2_DATA_EMV_TAG]];
//        if(track2Data57 != nil && !([track2Data57 isEqualToString:@""])) {
//            clearentTransactionTokenRequest.track2Data = track2Data57.uppercaseString;
//        [outgoingTags setObject:track2Data57 forKey:TRACK2_DATA_EMV_TAG];
//        } else {
//            NSDictionary *ff8105 = [IDTUtility TLVtoDICT_HEX_ASCII:[tags objectForKey:@"FF8105"]];
//            if(ff8105 != nil) {
//                NSString *track2Data9F6B = [ff8105 objectForKey:TRACK2_DATA_CONTACTLESS_NON_CHIP_TAG];
//                if(track2Data9F6B != nil && !([track2Data9F6B isEqualToString:@""])) {
//                    NSLog(@"Use the track 2 data from tag 9F6B");
//                    clearentTransactionTokenRequest.track2Data = track2Data9F6B.uppercaseString;
//                } else {
//                    NSLog(@"Mobile SDK failed to read Track2Data");
//                    clearentTransactionTokenRequest.track2Data = @"Mobile SDK failed to read Track2Data";
//                }
//            }
//        }
//
//    [self addRequiredTags: outgoingTags];
//        clearentTransactionTokenRequest.applicationPreferredNameTag9F12 = [IDTUtility dataToString:[outgoingTags objectForKey:@"9F12"]];
//
//    [self removeInvalidTSYSTags: outgoingTags];
//
//        tagsAsNSData = [IDTUtility DICTotTLV:outgoingTags];
//        tlvInHex = [IDTUtility dataToHexString:tagsAsNSData];
//
//        clearentTransactionTokenRequest.tlv = tlvInHex.uppercaseString;
//        clearentTransactionTokenRequest.emv = true;
//        clearentTransactionTokenRequest.kernelVersion = [self kernelVersion];
//        clearentTransactionTokenRequest.deviceSerialNumber = [self deviceSerialNumber];
//        clearentTransactionTokenRequest.firmwareVersion = [self firmwareVersion];
//        clearentTransactionTokenRequest.encrypted = isEncrypted;
//
       return clearentTransactionTokenRequest;
    }

    public String convertToTlv(Map<String, byte[]> values) {
        StringBuilder stringBuilder = new StringBuilder();
        for(Map.Entry<String, byte[]> entry: values.entrySet()) {
            String tag = entry.getKey();
            String length = Integer.toHexString(tag.length());
            byte[] value = entry.getValue();
            stringBuilder.append(tag+length+Common.byteToString(value));
        }
        return stringBuilder.toString();
    }


   public void addRequiredTags(Map<String, byte[]> values) {
        values.put(DEVICE_SERIAL_NUMBER_EMV_TAG,Common.getBytesFromHexString(Common.bytesToHex(getDeviceSerialNumber().getBytes())));
        values.put(KERNEL_VERSION_EMV_TAG,Common.getBytesFromHexString(Common.bytesToHex(getKernelVersion().getBytes())));
    }
//
////Remove any tags that would make the request fail in TSYS.
public void removeInvalidTSYSTags(Map<String, byte[]> values) {

        List<String> invalidTSYSTags = new ArrayList<>();
        invalidTSYSTags.add("DFEF4D");
        invalidTSYSTags.add("DFEF4C");
        invalidTSYSTags.add("FFEE06");
        invalidTSYSTags.add("FFEE13");
        invalidTSYSTags.add("FFEE14");
        invalidTSYSTags.add("FF8106");
        invalidTSYSTags.add("FF8105");
        invalidTSYSTags.add("56");
        invalidTSYSTags.add("57");
        invalidTSYSTags.add("DFEE26");
        invalidTSYSTags.add("FFEE01");
        invalidTSYSTags.add("DF8129");
        invalidTSYSTags.add("9F12");

        for (Iterator<Map.Entry<String, byte[]>> it = values.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, byte[]> entry = it.next();
            String tag = entry.getKey();
            if(invalidTSYSTags.contains(tag)) {
                it.remove();
            } else if(tag.equals("9F6E") && entry.getValue() == null) {
                it.remove();
            } else if(tag.equals("4F") && entry.getValue() == null) {
            }

        }
    }
//
     public void createTransactionToken(ClearentTransactionTokenRequest clearentTransactionTokenRequest) {
        if(clearentTransactionTokenRequest == null || clearentTransactionTokenRequest.getTrack2Data() == null || "".equals(clearentTransactionTokenRequest.getTrack2Data())) {
        //TODO notify [self deviceMessage:FAILED_TO_READ_CARD_ERROR_RESPONSE];
            return;
        }
//        NSString *targetUrl = [NSString stringWithFormat:@"%@/%@", self.baseUrl, @"rest/v2/mobilejwt"];
//        NSLog(@"targetUrl: %@", targetUrl);
//        NSMutableURLRequest *request = [[NSMutableURLRequest alloc] init];
//        NSError *error;
//        NSData *postData = [NSJSONSerialization dataWithJSONObject:clearentTransactionTokenRequest.asDictionary options:0 error:&error];
//
//        if (error) {
//        [self deviceMessage:GENERIC_TRANSACTION_TOKEN_ERROR_RESPONSE];
//            return;
//        }
//
//    [request setHTTPBody:postData];
//    [request setHTTPMethod:@"POST"];
//    [request setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
//    [request setValue:@"application/json" forHTTPHeaderField:@"Accept"];
//    [request setValue:self.publicKey forHTTPHeaderField:@"public-key"];
//    [request setURL:[NSURL URLWithString:targetUrl]];
//
//    [[[NSURLSession sharedSession] dataTaskWithRequest:request completionHandler:
//      ^(NSData * _Nullable data,
//                NSURLResponse * _Nullable response,
//                NSError * _Nullable error) {
//            NSHTTPURLResponse *httpResponse = (NSHTTPURLResponse *) response;
//            NSString *responseStr = nil;
//            if(error != nil) {
//              [self deviceMessage:error.description];
//              [[IDT_VP3300 sharedController] emv_completeOnlineEMVTransaction:false hostResponseTags:nil];
//            } else if(data != nil) {
//                responseStr = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
//                if(200 == [httpResponse statusCode]) {
//                    //[[IDT_VP3300 sharedController] emv_completeOnlineEMVTransaction:true hostResponseTags:[IDTUtility hexToData:@"8A023030"]];
//                  [[IDT_VP3300 sharedController] emv_completeOnlineEMVTransaction:false hostResponseTags:nil];
//                  [self handleResponse:responseStr];
//                } else {
//                  [[IDT_VP3300 sharedController] emv_completeOnlineEMVTransaction:false hostResponseTags:nil];
//                  [self handleError:responseStr];
//                }
//            }
//            data = nil;
//            response = nil;
//            error = nil;
//        }] resume];
    }
//
//- (void) handleError:(NSString*)response {
//        NSData *data = [response dataUsingEncoding:NSUTF8StringEncoding];
//        NSError *error;
//        NSDictionary *jsonDictionary = [NSJSONSerialization JSONObjectWithData:data
//        options:0
//        error:&error];
//        if (error) {
//        [self deviceMessage:GENERIC_TRANSACTION_TOKEN_ERROR_RESPONSE];
//        } else {
//            NSDictionary *payloadDictionary = [jsonDictionary objectForKey:@"payload"];
//            NSDictionary *errorDictionary = [payloadDictionary objectForKey:@"error"];
//            NSString *errorMessage = [errorDictionary objectForKey:@"error-message"];
//            if(errorMessage != nil) {
//             [self deviceMessage:[NSString stringWithFormat:@"%@. %@.", GENERIC_TRANSACTION_TOKEN_ERROR_RESPONSE, errorMessage]];
//            } else {
//           [self deviceMessage:GENERIC_TRANSACTION_TOKEN_ERROR_RESPONSE];
//            }
//        }
//    }
//
//- (void) handleResponse:(NSString *)response {
//        NSData *data = [response dataUsingEncoding:NSUTF8StringEncoding];
//        NSError *error;
//        NSDictionary *jsonDictionary = [NSJSONSerialization JSONObjectWithData:data
//        options:0
//        error:&error];
//        if (error) {
//        [self deviceMessage:GENERIC_TRANSACTION_TOKEN_ERROR_RESPONSE];
//        }
//        NSString *responseCode = [jsonDictionary objectForKey:@"code"];
//        if([responseCode isEqualToString:@"200"]) {
//        [self.publicDelegate successfulTransactionToken:response];
//        } else {
//        [self deviceMessage:GENERIC_TRANSACTION_TOKEN_ERROR_RESPONSE];
//        }
//    }
}
