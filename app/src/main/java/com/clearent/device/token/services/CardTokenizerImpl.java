package com.clearent.device.token.services;

import android.support.annotation.NonNull;

import com.clearent.device.Tokenizable;
import com.clearent.device.domain.CommunicationRequest;
import com.clearent.device.token.domain.ClearentTransactionTokenRequest;
import com.idtechproducts.device.Common;
import com.idtechproducts.device.ErrorCode;
import com.idtechproducts.device.IDTEMVData;
import com.idtechproducts.device.IDTMSRData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CardTokenizerImpl implements CardTokenizer {

    private static final String FAILED_TO_READ_CARD_ERROR_RESPONSE = "Failed to read card";
    private static final String DEVICE_SERIAL_NUMBER_EMV_TAG = "DF78";
    private static final String KERNEL_VERSION_EMV_TAG = "DF79";
    private static final String EMV_TAGS_TO_RETRIEVE = "82959A9B9C5F2A9F029F039F1A9F219F269F279F339F349F359F369F379F394F845F2D5F349F069F129F099F405F369F1E9F105657FF8106FF8105FFEE14FFEE06";

    private Tokenizable tokenizable;

    public CardTokenizerImpl(Tokenizable tokenizable) {
        this.tokenizable = tokenizable;
    }

    @Override
    public void createTransactionToken(IDTMSRData cardData) {
        ClearentTransactionTokenRequest clearentTransactionTokenRequest = createClearentTransactionTokenRequestForASwipe(cardData);
        createTransactionToken(clearentTransactionTokenRequest);
    }

    @Override
    public void createTransactionTokenForFallback(IDTMSRData cardData) {
        ClearentTransactionTokenRequest clearentTransactionTokenRequest =createClearentTransactionTokenRequestForFallbackSwipe(cardData);
        createTransactionToken(clearentTransactionTokenRequest);
    }

    @Override
    public void createTransactionToken(IDTEMVData idtemvData) {
        ClearentTransactionTokenRequest clearentTransactionTokenRequest = createClearentTransactionTokenRequest(idtemvData);
        createTransactionToken(clearentTransactionTokenRequest);
    }

    ClearentTransactionTokenRequest createClearentTransactionTokenRequestForASwipe(IDTMSRData cardData) {
        ClearentTransactionTokenRequest clearentTransactionTokenRequest = new ClearentTransactionTokenRequest();
        if (cardData.encTrack2 != null) {
            String encryptedTrack2Data = Common.byteToString(cardData.encTrack2);
            clearentTransactionTokenRequest = createClearentTransactionToken(false ,true,encryptedTrack2Data.toUpperCase());
        } else if (cardData.track2 != null) {
            clearentTransactionTokenRequest = createClearentTransactionToken(false,false ,cardData.track2.toUpperCase());
        }

        return clearentTransactionTokenRequest;
    }

    ClearentTransactionTokenRequest createClearentTransactionTokenRequestForFallbackSwipe(IDTMSRData cardData) {
        ClearentTransactionTokenRequest clearentTransactionTokenRequest = new ClearentTransactionTokenRequest();
        if (cardData.encTrack2 != null) {
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

    ClearentTransactionTokenRequest createClearentTransactionToken(boolean emv ,boolean encrypted, String track2Data) {
        ClearentTransactionTokenRequest clearentTransactionTokenRequest = new ClearentTransactionTokenRequest();
        clearentTransactionTokenRequest.setEmv(emv);
        clearentTransactionTokenRequest.setEncrypted(encrypted);
        clearentTransactionTokenRequest.setDeviceSerialNumber(tokenizable.getDeviceSerialNumber());
        clearentTransactionTokenRequest.setKernelVersion(tokenizable.getKernelVersion());
        clearentTransactionTokenRequest.setFirmwareVersion(tokenizable.getFirmwareVersion());
        clearentTransactionTokenRequest.setTrack2Data(track2Data.toUpperCase());
        return clearentTransactionTokenRequest;
    }

    ClearentTransactionTokenRequest  createClearentTransactionTokenRequest(IDTEMVData idtemvData) {
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
        //TODO handle error ? I doubt we will have tags every time.
        return new ClearentTransactionTokenRequest();
    }

    //TODO We wound up not using the incoming tags at all. Why ? the retrieveTransasctionResult method gives us everything we need ?
    //on the ios side we reference the track 2 data from this map..maybe we shouldn't ?
    ClearentTransactionTokenRequest createClearentTransactionTokenRequest(Map<String,byte[]> tags, boolean encrypted, int cardType) {
        ClearentTransactionTokenRequest clearentTransactionTokenRequest = new ClearentTransactionTokenRequest();

        if(cardType != 1) {
            Map<String, Map<String, byte[]>> retrievedTSYSTags = new HashMap<>();

            int emvRetrieveTransactionResultRt = tokenizable.emv_retrieveTransactionResult(Common.getByteArray(EMV_TAGS_TO_RETRIEVE), retrievedTSYSTags);

            if (emvRetrieveTransactionResultRt == ErrorCode.SUCCESS) {
                Map<String, byte[]> unencryptedTags = retrievedTSYSTags.get("tags");
                Map<String, byte[]> encryptedTags = retrievedTSYSTags.get("encrypted");

                //TODO handle encrypted tags
                String tlvInHex = null;
                if(encrypted) {
                    //TODO dup logic
                    for(Map.Entry<String, byte[]> entry: encryptedTags.entrySet()) {
                        String tag = entry.getKey();
                        if(tag.equals("9F12")) {
                            String applicationPreferredName = Common.getHexStringFromBytes(entry.getValue());
                            clearentTransactionTokenRequest.setApplicationPreferredNameTag9F12(applicationPreferredName);
                        } else if(tag.equals("57")) {
                            String track2Data = Common.getHexStringFromBytes(entry.getValue());
                            clearentTransactionTokenRequest.setTrack2Data(track2Data);
                        } else if(tag.equals("FF8105")) {
                            //TODO handle track 2 data field located in different area. Is this only on contactless ?
                        }
                    }
                    tlvInHex = convertToTlv(encryptedTags);
                } else{
                    //TODO dup logic
                    for(Map.Entry<String, byte[]> entry: unencryptedTags.entrySet()) {
                        String tag = entry.getKey();
                        if(tag.equals("9F12")) {
                            String applicationPreferredName = Common.getHexStringFromBytes(entry.getValue());
                            clearentTransactionTokenRequest.setApplicationPreferredNameTag9F12(applicationPreferredName);
                        } else if(tag.equals("57")) {
                            String track2Data = Common.getHexStringFromBytes(entry.getValue());
                            clearentTransactionTokenRequest.setTrack2Data(track2Data);
                        } else if(tag.equals("FF8105")) {
                            //TODO handle track 2 data field located in different area. Is this only on contactless ?
                        }
                    }
                    removeInvalidTSYSTags(unencryptedTags);
                    addRequiredTags(unencryptedTags);
                    tlvInHex = convertToTlv(unencryptedTags);
                }

                clearentTransactionTokenRequest.setTlv(tlvInHex.toUpperCase());
                clearentTransactionTokenRequest.setEmv(true);
                clearentTransactionTokenRequest.setKernelVersion(tokenizable.getKernelVersion());
                clearentTransactionTokenRequest.setDeviceSerialNumber(tokenizable.getDeviceSerialNumber());
                clearentTransactionTokenRequest.setFirmwareVersion(tokenizable.getFirmwareVersion());
                clearentTransactionTokenRequest.setEncrypted(encrypted);
            } else {
                String error = "Failed to get emv tags ";
                tokenizable.notifyTransactionTokenFailure(emvRetrieveTransactionResultRt, error);
            }
        } else {
            //TODO implement contactless support
        }

        return clearentTransactionTokenRequest;
    }


    String convertToTlv(Map<String, byte[]> values) {
        StringBuilder stringBuilder = new StringBuilder();
        for(Map.Entry<String, byte[]> entry: values.entrySet()) {
            String tag = entry.getKey();
            byte[] value = entry.getValue();
            String hexString = Common.getHexStringFromBytes(value);
            int valueLength = hexString.length()/2;
            String length = String.format("%02X", valueLength);
            stringBuilder.append(tag + length + Common.getHexStringFromBytes(value));
        }
        return stringBuilder.toString();
    }

    void addRequiredTags(Map<String, byte[]> values) {
        values.put(DEVICE_SERIAL_NUMBER_EMV_TAG,Common.getBytesFromHexString(Common.bytesToHex(tokenizable.getDeviceSerialNumber().getBytes())));
        values.put(KERNEL_VERSION_EMV_TAG,Common.getBytesFromHexString(Common.bytesToHex(tokenizable.getKernelVersion().getBytes())));
    }

    void createTransactionToken(ClearentTransactionTokenRequest clearentTransactionTokenRequest) {
        if(clearentTransactionTokenRequest == null || clearentTransactionTokenRequest.getTrack2Data() == null || "".equals(clearentTransactionTokenRequest.getTrack2Data())) {
            String[] message = {FAILED_TO_READ_CARD_ERROR_RESPONSE};
            tokenizable.notifyTransactionTokenFailure(FAILED_TO_READ_CARD_ERROR_RESPONSE);
            return;
        }

        String kernelVersion = tokenizable.getKernelVersion();
        String deviceSerialNumber = tokenizable.getDeviceSerialNumber();
        CommunicationRequest communicationRequest = new CommunicationRequest(tokenizable.getPaymentsBaseUrl(), tokenizable.getPaymentsPublicKey(), deviceSerialNumber, kernelVersion);
        TransactionTokenCreatorResponseHandler transactionTokenCreatorResponseHandler = new TransactionTokenCreatorResponseHandler(tokenizable);
        TransactionTokenCreator transactionTokenCreator = new TransactionTokenCreatorImpl(communicationRequest, clearentTransactionTokenRequest);
        transactionTokenCreator.createTransactionToken(transactionTokenCreatorResponseHandler);
    }

    public void removeInvalidTSYSTags(Map<String, byte[]> values) {

        List<String> invalidTSYSTags = getInvalidTsysTags();

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

    @NonNull
    private List<String> getInvalidTsysTags() {
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
        return invalidTSYSTags;
    }
}
