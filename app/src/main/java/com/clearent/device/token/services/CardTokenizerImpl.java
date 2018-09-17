package com.clearent.device.token.services;

import android.support.annotation.NonNull;
import android.util.Log;

import com.clearent.device.HasTokenizingSupport;
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

    private HasTokenizingSupport hasTokenizingSupport;

    public CardTokenizerImpl(HasTokenizingSupport hasTokenizingSupport) {
        this.hasTokenizingSupport = hasTokenizingSupport;
    }

    @Override
    public void createTransactionToken(IDTMSRData cardData) {
        try {
            ClearentTransactionTokenRequest clearentTransactionTokenRequest = createClearentTransactionTokenRequestForASwipe(cardData);
            createTransactionToken(clearentTransactionTokenRequest);
        } catch (Exception e) {
            Log.e("ERROR", "Failed to create a transaction token for a swipe", e);
            hasTokenizingSupport.notifyTransactionTokenFailure(FAILED_TO_READ_CARD_ERROR_RESPONSE);
        }
    }

    @Override
    public void createTransactionTokenForFallback(IDTMSRData cardData) {
        try {
            ClearentTransactionTokenRequest clearentTransactionTokenRequest = createClearentTransactionTokenRequestForFallbackSwipe(cardData);
            createTransactionToken(clearentTransactionTokenRequest);
        }catch (Exception e) {
            Log.e("ERROR", "Failed to create a transaction token for a fallback swipe", e);
            hasTokenizingSupport.notifyTransactionTokenFailure(FAILED_TO_READ_CARD_ERROR_RESPONSE);
        }
    }

    @Override
    public void createTransactionToken(IDTEMVData idtemvData) {
        try {
            ClearentTransactionTokenRequest clearentTransactionTokenRequest = createClearentTransactionTokenRequest(idtemvData);
            createTransactionToken(clearentTransactionTokenRequest);
        }catch (Exception e) {
            Log.e("ERROR", "Failed to create a transaction token for emv", e);
            hasTokenizingSupport.notifyTransactionTokenFailure(FAILED_TO_READ_CARD_ERROR_RESPONSE);
        }
    }

    ClearentTransactionTokenRequest createClearentTransactionTokenRequestForASwipe(IDTMSRData cardData) {
        ClearentTransactionTokenRequest clearentTransactionTokenRequest = new ClearentTransactionTokenRequest();
        if (cardData.encTrack2 != null) {
            String encryptedTrack2Data = Common.byteToString(cardData.encTrack2);
            clearentTransactionTokenRequest = createClearentTransactionToken(false, true, encryptedTrack2Data.toUpperCase());
        } else if (cardData.track2 != null) {
            clearentTransactionTokenRequest = createClearentTransactionToken(false, false, cardData.track2.toUpperCase());
        }

        return clearentTransactionTokenRequest;
    }

    ClearentTransactionTokenRequest createClearentTransactionTokenRequestForFallbackSwipe(IDTMSRData cardData) {
        ClearentTransactionTokenRequest clearentTransactionTokenRequest = new ClearentTransactionTokenRequest();
        if (cardData.encTrack2 != null) {
            String encryptedTrack2Data = Common.byteToString(cardData.encTrack2);
            clearentTransactionTokenRequest = createClearentTransactionToken(false, true, encryptedTrack2Data.toUpperCase());
        } else if (cardData.track2 != null) {
            clearentTransactionTokenRequest = createClearentTransactionToken(false, false, cardData.track2.toUpperCase());
        }

        //add the required tags as a tlv string for emv fallback swipe scenario (ClearentSwitch uses this)
        Map<String, byte[]> requiredTags = new HashMap<>();
        addRequiredTags(requiredTags);
        clearentTransactionTokenRequest.setTlv(convertToTlv(requiredTags));

        return clearentTransactionTokenRequest;
    }

    ClearentTransactionTokenRequest createClearentTransactionToken(boolean emv, boolean encrypted, String track2Data) {
        ClearentTransactionTokenRequest clearentTransactionTokenRequest = new ClearentTransactionTokenRequest();
        clearentTransactionTokenRequest.setEmv(emv);
        clearentTransactionTokenRequest.setEncrypted(encrypted);
        clearentTransactionTokenRequest.setDeviceSerialNumber(hasTokenizingSupport.getDeviceSerialNumber());
        clearentTransactionTokenRequest.setKernelVersion(hasTokenizingSupport.getKernelVersion());
        clearentTransactionTokenRequest.setFirmwareVersion(hasTokenizingSupport.getFirmwareVersion());
        clearentTransactionTokenRequest.setTrack2Data(track2Data.toUpperCase());
        return clearentTransactionTokenRequest;
    }

    ClearentTransactionTokenRequest createClearentTransactionTokenRequest(IDTEMVData idtemvData) {

        if (idtemvData.unencryptedTags != null) {
            return createClearentTransactionTokenRequest(idtemvData.unencryptedTags, false);
        } else if (idtemvData.encryptedTags != null) {
            return createClearentTransactionTokenRequest(idtemvData.encryptedTags, true);
        } else if(idtemvData.msr_cardData != null) {
            //TODO test is this a fallback ? is this even valid ? seems like this object will always exist
            hasTokenizingSupport.notifyTransactionTokenFailure("test 1");
            return createClearentTransactionTokenRequestForFallbackSwipe(idtemvData.msr_cardData);
        }

        throw new RuntimeException("Failed to identify data required for transaction token processsing");
    }

    ClearentTransactionTokenRequest createClearentTransactionTokenRequest(Map<String, byte[]> tags, boolean encrypted) {
        ClearentTransactionTokenRequest clearentTransactionTokenRequest = new ClearentTransactionTokenRequest();

        Map<String, Map<String, byte[]>> retrievedTSYSTags = new HashMap<>();

        int emvRetrieveTransactionResultRt = hasTokenizingSupport.emv_retrieveTransactionResult(Common.getByteArray(EMV_TAGS_TO_RETRIEVE), retrievedTSYSTags);

        if (emvRetrieveTransactionResultRt == ErrorCode.SUCCESS) {
            Map<String, byte[]> unencryptedTags = retrievedTSYSTags.get("tags");
            Map<String, byte[]> encryptedTags = retrievedTSYSTags.get("encrypted");

            String tlvInHex = null;
            if (encrypted) {
                populateTags(clearentTransactionTokenRequest, encryptedTags);
                tlvInHex = createTlvHexString(encryptedTags);
            } else {
                populateTags(clearentTransactionTokenRequest, unencryptedTags);
                tlvInHex = createTlvHexString(unencryptedTags);
            }
            clearentTransactionTokenRequest.setTlv(tlvInHex);
            clearentTransactionTokenRequest.setEmv(true);
            clearentTransactionTokenRequest.setKernelVersion(hasTokenizingSupport.getKernelVersion());
            clearentTransactionTokenRequest.setDeviceSerialNumber(hasTokenizingSupport.getDeviceSerialNumber());
            clearentTransactionTokenRequest.setFirmwareVersion(hasTokenizingSupport.getFirmwareVersion());
            clearentTransactionTokenRequest.setEncrypted(encrypted);
        } else {
            String error = "Failed to get emv tags ";
            hasTokenizingSupport.notifyTransactionTokenFailure(emvRetrieveTransactionResultRt, error);
            throw new RuntimeException("Failed to get emv tags");
        }

        return clearentTransactionTokenRequest;
    }

    private void populateTags(ClearentTransactionTokenRequest clearentTransactionTokenRequest, Map<String, byte[]> tags) {
        for (Map.Entry<String, byte[]> entry : tags.entrySet()) {
            String tag = entry.getKey();
            if (tag.equals("9F12")) {
                String applicationPreferredName = Common.getHexStringFromBytes(entry.getValue());
                clearentTransactionTokenRequest.setApplicationPreferredNameTag9F12(applicationPreferredName);
            } else if (tag.equals("57")) {
                String track2Data = Common.getHexStringFromBytes(entry.getValue());
                clearentTransactionTokenRequest.setTrack2Data(track2Data);
            } else if (tag.equals("FF8105")) {
                Log.i("INFO", "ff8105 tag found");
                //TODO handle track 2 data field located in different area. Is this only on contactless ?
            }
        }
    }

    private String createTlvHexString(Map<String, byte[]> tags) {
        String tlvInHex = null;
        removeInvalidTSYSTags(tags);
        addRequiredTags(tags);
        tlvInHex = convertToTlv(tags);
        return tlvInHex.toUpperCase();
    }

    String convertToTlv(Map<String, byte[]> values) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, byte[]> entry : values.entrySet()) {
            String tag = entry.getKey();
            byte[] value = entry.getValue();
            String hexString = Common.getHexStringFromBytes(value);
            int valueLength = hexString.length() / 2;
            String length = String.format("%02X", valueLength);
            stringBuilder.append(tag + length + Common.getHexStringFromBytes(value));
        }
        return stringBuilder.toString();
    }

    void addRequiredTags(Map<String, byte[]> values) {
        values.put(DEVICE_SERIAL_NUMBER_EMV_TAG, Common.getBytesFromHexString(Common.bytesToHex(hasTokenizingSupport.getDeviceSerialNumber().getBytes())));
        values.put(KERNEL_VERSION_EMV_TAG, Common.getBytesFromHexString(Common.bytesToHex(hasTokenizingSupport.getKernelVersion().getBytes())));
    }

    void createTransactionToken(ClearentTransactionTokenRequest clearentTransactionTokenRequest) {
        if (clearentTransactionTokenRequest == null || clearentTransactionTokenRequest.getTrack2Data() == null || "".equals(clearentTransactionTokenRequest.getTrack2Data())) {
            String[] message = {FAILED_TO_READ_CARD_ERROR_RESPONSE};
            hasTokenizingSupport.notifyTransactionTokenFailure(FAILED_TO_READ_CARD_ERROR_RESPONSE);
            throw new RuntimeException("Track 2 data not present in transaction token request");
        }

        String kernelVersion = hasTokenizingSupport.getKernelVersion();
        String deviceSerialNumber = hasTokenizingSupport.getDeviceSerialNumber();
        CommunicationRequest communicationRequest = new CommunicationRequest(hasTokenizingSupport.getPaymentsBaseUrl(), hasTokenizingSupport.getPaymentsPublicKey(), deviceSerialNumber, kernelVersion);
        TransactionTokenCreatorResponseHandler transactionTokenCreatorResponseHandler = new TransactionTokenCreatorResponseHandler(hasTokenizingSupport);
        TransactionTokenCreator transactionTokenCreator = new TransactionTokenCreatorImpl(communicationRequest, clearentTransactionTokenRequest);
        transactionTokenCreator.createTransactionToken(transactionTokenCreatorResponseHandler);
    }

    public void removeInvalidTSYSTags(Map<String, byte[]> values) {

        List<String> invalidTSYSTags = getInvalidTsysTags();

        for (Iterator<Map.Entry<String, byte[]>> it = values.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, byte[]> entry = it.next();
            String tag = entry.getKey();
            if (invalidTSYSTags.contains(tag)) {
                it.remove();
            } else if (tag.equals("9F6E") && entry.getValue() == null) {
                it.remove();
            } else if (tag.equals("4F") && entry.getValue() == null) {
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
