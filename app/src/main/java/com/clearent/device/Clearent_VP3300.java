package com.clearent.device;

import android.content.Context;
import android.util.Log;

import com.clearent.device.token.domain.TransactionToken;
import com.idtechproducts.device.*;
import com.idtechproducts.device.audiojack.tools.FirmwareUpdateTool;

import java.util.Map;

public class Clearent_VP3300 implements TransactionTokenNotifier, ReaderReadyAware, DeviceConfigurable, HasDeviceMetadata, Tokenizable {

    private static IDT_VP3300 idt_VP3300;
    private byte[] tag8A = new byte[]{0x30, 0x30};

    private String paymentsBaseUrl;
    private String paymentsPublicKey;
    private String deviceSerialNumber;
    private String kernelVersion;
    private String firmareVersion;

    private PublicOnReceiverListener publicOnReceiverListener;
    private ClearentOnReceiverListener clearentOnReceiverListener;
    private boolean configured = false;
    private boolean previousDipDidNotMatchOnApp = false;

    public Clearent_VP3300(PublicOnReceiverListener publicOnReceiverListener, Context context, String paymentsBaseUrl, String paymentsPublicKey) {
        this.publicOnReceiverListener = publicOnReceiverListener;
        clearentOnReceiverListener = new ClearentOnReceiverListener(this, publicOnReceiverListener);
        idt_VP3300 = new IDT_VP3300(clearentOnReceiverListener, context);
        this.paymentsBaseUrl = paymentsBaseUrl;
        this.paymentsPublicKey = paymentsPublicKey;
    }

    public Clearent_VP3300(PublicOnReceiverListener publicOnReceiverListener, OnReceiverListenerPINRequest callback2, Context context, String paymentsBaseUrl, String paymentsPublicKey) {
        this.publicOnReceiverListener = publicOnReceiverListener;
        clearentOnReceiverListener = new ClearentOnReceiverListener(this, publicOnReceiverListener);
        idt_VP3300 = new IDT_VP3300(clearentOnReceiverListener, callback2, context);
        this.paymentsBaseUrl = paymentsBaseUrl;
        this.paymentsPublicKey = paymentsPublicKey;
    }

    public static IDT_Device getSDKInstance() {
        return idt_VP3300.getSDKInstance();
    }

    public static void useUSBIntentFilter() {
        idt_VP3300.getSDKInstance()._isDeviceFilter = true;
    }

    public boolean device_setDeviceType(ReaderInfo.DEVICE_TYPE deviceType) {
        if (deviceType == ReaderInfo.DEVICE_TYPE.DEVICE_VP3300_AJ) {
            return idt_VP3300.getSDKInstance().device_setDeviceType(ReaderInfo.DEVICE_TYPE.DEVICE_VP3300_AJ);
        } else if (deviceType == ReaderInfo.DEVICE_TYPE.DEVICE_VP3300_AJ_USB) {
            return idt_VP3300.getSDKInstance().device_setDeviceType(ReaderInfo.DEVICE_TYPE.DEVICE_VP3300_AJ_USB);
        } else if (deviceType == ReaderInfo.DEVICE_TYPE.DEVICE_VP3300_USB) {
            return idt_VP3300.getSDKInstance().device_setDeviceType(ReaderInfo.DEVICE_TYPE.DEVICE_VP3300_USB);
        } else if (deviceType == ReaderInfo.DEVICE_TYPE.DEVICE_VP3300_BT) {
            return idt_VP3300.getSDKInstance().device_setDeviceType(ReaderInfo.DEVICE_TYPE.DEVICE_VP3300_BT);
        } else {
            return deviceType == ReaderInfo.DEVICE_TYPE.DEVICE_VP3300_BT_USB ? idt_VP3300.getSDKInstance().device_setDeviceType(ReaderInfo.DEVICE_TYPE.DEVICE_VP3300_BT_USB) : false;
        }
    }

    public void setIDT_Device(FirmwareUpdateTool fwTool) {
        idt_VP3300.getSDKInstance().setIDT_Device(fwTool);
    }

    public ReaderInfo.DEVICE_TYPE device_getDeviceType() {
        return idt_VP3300.getSDKInstance().device_getDeviceType();
    }

    public void registerListen() {
        idt_VP3300.getSDKInstance().registerListen();
    }

    public static IDT_Device getIDT_Device() {
        return idt_VP3300.getSDKInstance();
    }

    public void unregisterListen() {
        idt_VP3300.getSDKInstance().unregisterListen();
    }

    public void release() {
        idt_VP3300.getSDKInstance().release();
    }

    public String config_getSDKVersion() {
        return idt_VP3300.getSDKInstance().config_getSDKVersion();
    }

    public String config_getXMLVersionInfo() {
        return idt_VP3300.getSDKInstance().config_getXMLVersionInfo();
    }

    public String phone_getInfoManufacture() {
        return idt_VP3300.getSDKInstance().phone_getInfoManufacture();
    }

    public String phone_getInfoModel() {
        return idt_VP3300.getSDKInstance().phone_getInfoModel();
    }

    public void log_setVerboseLoggingEnable(boolean enable) {
        idt_VP3300.getSDKInstance().log_setVerboseLoggingEnable(enable);
    }

    public void log_setSaveLogEnable(boolean enable) {
        idt_VP3300.getSDKInstance().log_setSaveLogEnable(enable);
    }

    public int log_deleteLogs() {
        return idt_VP3300.getSDKInstance().log_deleteLogs();
    }

    public int emv_callbackResponsePIN(int mode, byte[] KSN, byte[] PIN) {
        return idt_VP3300.getSDKInstance().emv_callbackResponsePIN(mode, KSN, PIN);
    }

    public void config_setXMLFileNameWithPath(String path) {
        idt_VP3300.getSDKInstance().config_setXMLFileNameWithPath(path);
    }

    public boolean config_loadingConfigurationXMLFile(boolean updateAutomatically) {
        return idt_VP3300.getSDKInstance().config_loadingConfigurationXMLFile(updateAutomatically);
    }

    public boolean device_connectWithProfile(StructConfigParameters profile) {
        return idt_VP3300.getSDKInstance().device_connectWithProfile(profile);
    }

    public void device_ConnectWithoutValidation(boolean noValidate) {
        idt_VP3300.getSDKInstance().device_ConnectWithoutValidation(noValidate, ReaderInfo.DEVICE_TYPE.DEVICE_VP3300_AJ);
    }

    public boolean device_connect() {
        return idt_VP3300.getSDKInstance().device_connect();
    }

    public boolean device_isConnected() {
        return idt_VP3300.getSDKInstance().device_isConnected();
    }

    public int device_startRKI() {
        return idt_VP3300.getSDKInstance().device_startRKI();
    }

    public int autoConfig_start(String strXMLFilename) {
        return idt_VP3300.getSDKInstance().autoConfig_start(strXMLFilename);
    }

    public void autoConfig_stop() {
        idt_VP3300.getSDKInstance().autoConfig_stop();
    }

    public int device_startTransaction(double amount, double amtOther, int type, int timeout, byte[] tags) {
        return idt_VP3300.getSDKInstance().device_startTransaction(amount, amtOther, type, timeout, tags);
    }

    public int device_cancelTransaction() {
        return idt_VP3300.getSDKInstance().device_cancelTransaction();
    }

    public int device_getFirmwareVersion(StringBuilder version) {
        return idt_VP3300.getSDKInstance().device_getFirmwareVersion(version);
    }

    public int device_pingDevice() {
        return idt_VP3300.getSDKInstance().device_pingDevice();
    }

    public int device_ReviewAudioJackSetting(ResDataStruct respData) {
        return idt_VP3300.getSDKInstance().device_ReviewAudioJackSetting(respData);
    }

    public int config_getSerialNumber(StringBuilder serialNumber) {
        return idt_VP3300.getSDKInstance().config_getSerialNumber(serialNumber);
    }

    public int config_getModelNumber(StringBuilder modNumber) {
        return idt_VP3300.getSDKInstance().config_getModelNumber(modNumber);
    }

    public int device_getKSN(ResDataStruct ksn) {
        return idt_VP3300.getSDKInstance().device_getKSN(ksn);
    }

    public int device_setMerchantRecord(int index, boolean enabled, String merchantID, String merchantURL) {
        return idt_VP3300.getSDKInstance().device_setMerchantRecord(index, enabled, merchantID, merchantURL);
    }

    public int device_getMerchantRecord(int index, ResDataStruct respData) {
        return idt_VP3300.getSDKInstance().device_getMerchantRecord(index, respData);
    }

    public String device_getResponseCodeString(int errorCode) {
        return ErrorCodeInfo.getErrorCodeDescription(errorCode);
    }

    public int device_sendDataCommand(String cmd, boolean calcLRC, String data, ResDataStruct respData) {
        return idt_VP3300.getSDKInstance().device_sendDataCommand(cmd, calcLRC, data, respData);
    }

    public int device_sendDataCommand(String cmd, boolean calcLRC, String data, ResDataStruct respData, int timeout) {
        return idt_VP3300.getSDKInstance().device_sendDataCommand(cmd, calcLRC, data, respData, timeout);
    }

    public int device_updateFirmware(String[] commands) {
        return idt_VP3300.getSDKInstance().device_updateFirmware(commands);
    }

    public int device_getTransactionResults(IDTMSRData cardData) {
        return idt_VP3300.getSDKInstance().device_getTransactionResults(cardData);
    }

    public int device_setBurstMode(byte mode) {
        return idt_VP3300.getSDKInstance().device_setBurstMode(mode);
    }

    public int device_setPollMode(byte mode) {
        return idt_VP3300.getSDKInstance().device_setPollMode(mode);
    }

    public int device_getRTCDateTime(byte[] dateTime) {
        return idt_VP3300.getSDKInstance().device_getRTCDateTime(dateTime);
    }

    public int device_setRTCDateTime(byte[] dateTime) {
        return idt_VP3300.getSDKInstance().device_setRTCDateTime(dateTime);
    }

    public int icc_getICCReaderStatus(ICCReaderStatusStruct ICCStatus) {
        return idt_VP3300.getSDKInstance().icc_getICCReaderStatus(ICCStatus);
    }

    public int icc_powerOnICC(ResDataStruct atrPPS) {
        return idt_VP3300.getSDKInstance().icc_powerOnICC(atrPPS);
    }

    public int icc_passthroughOnICC() {
        return idt_VP3300.getSDKInstance().icc_passthroughOnICC();
    }

    public int icc_passthroughOffICC() {
        return idt_VP3300.getSDKInstance().icc_passthroughOffICC();
    }

    public int icc_powerOffICC(ResDataStruct respData) {
        return idt_VP3300.getSDKInstance().icc_powerOffICC(respData);
    }

    public int icc_exchangeAPDU(byte[] dataAPDU, ResDataStruct response) {
        return idt_VP3300.getSDKInstance().icc_exchangeAPDU(dataAPDU, response);
    }

    public int emv_getEMVKernelVersion(StringBuilder version) {
        return idt_VP3300.getSDKInstance().emv_getEMVKernelVersion(version);
    }

    public int emv_getEMVKernelCheckValue(ResDataStruct respData) {
        return idt_VP3300.getSDKInstance().emv_getEMVKernelCheckValue(respData);
    }

    public int emv_getEMVConfigurationCheckValue(ResDataStruct respData) {
        return idt_VP3300.getSDKInstance().emv_getEMVConfigurationCheckValue(respData);
    }

    public static void emv_allowFallback(boolean allow) {
        IDT_Device.emv_allowFallback(allow);
    }

    public int emv_retrieveApplicationData(String aid, ResDataStruct respData) {
        return idt_VP3300.getSDKInstance().emv_retrieveApplicationData(aid, respData);
    }

    public int emv_removeApplicationData(String aid, ResDataStruct respData) {
        return idt_VP3300.getSDKInstance().emv_removeApplicationData(aid, respData);
    }

    public int emv_setApplicationData(String aid, byte[] TLV, ResDataStruct respData) {
        return idt_VP3300.getSDKInstance().emv_setApplicationData(aid, TLV, respData);
    }

    public int emv_retrieveTerminalData(ResDataStruct respData) {
        return idt_VP3300.getSDKInstance().emv_retrieveTerminalData(respData);
    }

    public int emv_removeTerminalData(ResDataStruct respData) {
        return idt_VP3300.getSDKInstance().emv_removeTerminalData(respData);
    }

    public int emv_setTerminalData(byte[] TLV, ResDataStruct respData) {
        return idt_VP3300.getSDKInstance().emv_setTerminalData(TLV, respData);
    }

    public int emv_retrieveAidList(ResDataStruct respData) {
        return idt_VP3300.getSDKInstance().emv_retrieveAidList(respData);
    }

    public int emv_retrieveCAPK(byte[] data, ResDataStruct respData) {
        return idt_VP3300.getSDKInstance().emv_retrieveCAPK(data, respData);
    }

    public int emv_removeCAPK(byte[] capk, ResDataStruct respData) {
        return idt_VP3300.getSDKInstance().emv_removeCAPK(capk, respData);
    }

    public int emv_setCAPK(byte[] key, ResDataStruct respData) {
        return idt_VP3300.getSDKInstance().emv_setCAPK(key, respData);
    }

    public static void emv_setAutoAuthenticateTransaction(boolean auto) {
        IDT_Device.emv_setAutoAuthenticateTransaction(auto);
    }

    public static boolean emv_getAutoAuthenticateTransaction() {
        return IDT_Device.emv_getAutoAuthenticateTransaction();
    }

    public static void emv_setAutoCompleteTransaction(boolean auto) {
        IDT_Device.emv_setAutoCompleteTransaction(auto);
    }

    public static boolean emv_getAutoCompleteTransaction() {
        return IDT_Device.emv_getAutoCompleteTransaction();
    }

    public int emv_retrieveCAPKList(ResDataStruct respData) {
        return idt_VP3300.getSDKInstance().emv_retrieveCAPKList(respData);
    }

    public int emv_retrieveCRL(ResDataStruct respData) {
        return idt_VP3300.getSDKInstance().emv_retrieveCRL(respData);
    }

    public int emv_removeCRL(byte[] crlList, ResDataStruct respData) {
        return idt_VP3300.getSDKInstance().emv_removeCRL(crlList, respData);
    }

    public int emv_setCRL(byte[] crlList, ResDataStruct respData) {
        return idt_VP3300.getSDKInstance().emv_setCRL(crlList, respData);
    }

    public int emv_startTransaction(double amount, double amtOther, int type, int timeout, byte[] tags, boolean forceOnline) {
        previousDipDidNotMatchOnApp = false;
        return idt_VP3300.getSDKInstance().emv_startTransaction(amount, amtOther, type, timeout, tags, forceOnline);
    }

    public int emv_cancelTransaction(ResDataStruct respData) {
        return idt_VP3300.getSDKInstance().emv_cancelTransaction(respData);
    }

    public void emv_setTransactionParameters(double amount, double amtOther, int type, int timeout, byte[] tags) {
        idt_VP3300.getSDKInstance().emv_setTransactionParameters(amount, amtOther, type, timeout, tags);
    }

    public void emv_lcdControlResponse(byte mode, byte data) {
        idt_VP3300.getSDKInstance().emv_lcdControlResponse(mode, data);
    }

    public int emv_authenticateTransaction(byte[] tags) {
        return idt_VP3300.getSDKInstance().emv_authenticateTransaction(tags);
    }

    public int emv_completeTransaction(boolean commError, byte[] authCode, byte[] iad, byte[] tlvScripts, byte[] tags) {
        return idt_VP3300.getSDKInstance().emv_completeTransaction(commError, authCode, iad, tlvScripts, tags);
    }

    public int emv_retrieveTransactionResult(byte[] tags, Map<String, Map<String, byte[]>> retrievedTags) {
        return idt_VP3300.getSDKInstance().emv_retrieveTransactionResult(tags, retrievedTags);
    }

    public int device_reviewAllSetting(ResDataStruct respData) {
        return idt_VP3300.getSDKInstance().device_reviewAllSetting(respData);
    }

    public int msr_defaultAllSetting() {
        return idt_VP3300.getSDKInstance().msr_defaultAllSetting();
    }

    public int msr_getSingleSetting(byte funcID, byte[] response) {
        return idt_VP3300.getSDKInstance().msr_getSingleSetting(funcID, response);
    }

    public int msr_setSingleSetting(byte funcID, byte setData) {
        return idt_VP3300.getSDKInstance().msr_setSingleSetting(funcID, setData);
    }

    public int msr_cancelMSRSwipe() {
        return idt_VP3300.getSDKInstance().msr_cancelMSRSwipe();
    }

    public int msr_startMSRSwipe() {
        return idt_VP3300.getSDKInstance().msr_startMSRSwipe();
    }

    public int msr_startMSRSwipe(int timeout) {
        return idt_VP3300.getSDKInstance().msr_startMSRSwipe(timeout);
    }

    public int ctls_retrieveApplicationData(String aid, ResDataStruct respData) {
        return idt_VP3300.getSDKInstance().ctls_retrieveApplicationData(aid, respData);
    }

    public int ctls_removeApplicationData(String aid, ResDataStruct respData) {
        return idt_VP3300.getSDKInstance().ctls_removeApplicationData(aid, respData);
    }

    public int ctls_setApplicationData(byte[] TLV, ResDataStruct respData) {
        return idt_VP3300.getSDKInstance().ctls_setApplicationData(TLV, respData);
    }

    public int ctls_setConfigurationGroup(byte[] TLV, ResDataStruct respData) {
        return idt_VP3300.getSDKInstance().ctls_setConfigurationGroup(TLV, respData);
    }

    public int ctls_getConfigurationGroup(int group, ResDataStruct respData) {
        return idt_VP3300.getSDKInstance().ctls_getConfigurationGroup(group, respData);
    }

    public int ctls_getAllConfigurationGroups(ResDataStruct respData) {
        return idt_VP3300.getSDKInstance().ctls_getAllConfigurationGroups(respData);
    }

    public int ctls_removeConfigurationGroup(int group) {
        return idt_VP3300.getSDKInstance().ctls_removeConfigurationGroup(group);
    }

    public int ctls_retrieveTerminalData(ResDataStruct respData) {
        return idt_VP3300.getSDKInstance().ctls_retrieveTerminalData(respData);
    }

    public int ctls_setTerminalData(byte[] TLV, ResDataStruct respData) {
        return idt_VP3300.getSDKInstance().ctls_setTerminalData(TLV, respData);
    }

    public int ctls_retrieveAidList(ResDataStruct respData) {
        return idt_VP3300.getSDKInstance().ctls_retrieveAidList(respData);
    }

    public int ctls_retrieveCAPK(byte[] data, ResDataStruct respData) {
        return idt_VP3300.getSDKInstance().ctls_retrieveCAPK(data, respData);
    }

    public int ctls_removeCAPK(byte[] capk, ResDataStruct respData) {
        return idt_VP3300.getSDKInstance().ctls_removeCAPK(capk, respData);
    }

    public int ctls_setCAPK(byte[] key, ResDataStruct respData) {
        return idt_VP3300.getSDKInstance().ctls_setCAPK(key, respData);
    }

    public int ctls_retrieveCAPKList(ResDataStruct respData) {
        return idt_VP3300.getSDKInstance().ctls_retrieveCAPKList(respData);
    }

    public int ctls_removeAllApplicationData() {
        return idt_VP3300.getSDKInstance().ctls_removeAllApplicationData();
    }

    public int ctls_removeAllCAPK() {
        return idt_VP3300.getSDKInstance().ctls_removeAllCAPK();
    }

    public int ctls_startTransaction(double amount, double amtOther, int type, int timeout, byte[] tags) {
        return idt_VP3300.getSDKInstance().ctls_startTransaction(amount, amtOther, type, timeout, tags);
    }

    public int ctls_cancelTransaction() {
        return idt_VP3300.getSDKInstance().ctls_cancelTransaction();
    }

    public String getPaymentsBaseUrl() {
        return paymentsBaseUrl;
    }

    public void setPaymentsBaseUrl(String paymentsBaseUrl) {
        this.paymentsBaseUrl = paymentsBaseUrl;
    }

    public String getPaymentsPublicKey() {
        return paymentsPublicKey;
    }

    public void setPaymentsPublicKey(String paymentsPublicKey) {
        this.paymentsPublicKey = paymentsPublicKey;
    }

    public ClearentOnReceiverListener getClearentOnReceiverListener() {
        return clearentOnReceiverListener;
    }

    public void setClearentOnReceiverListener(ClearentOnReceiverListener clearentOnReceiverListener) {
        this.clearentOnReceiverListener = clearentOnReceiverListener;
    }

    public void notifyFailure(String message) {
        String[] messageArray = {message};
        clearentOnReceiverListener.lcdDisplay(0, messageArray, 0);
    }

    @Override
    public void notifyTransactionTokenFailure(String message) {
        String[] messageArray = {message};
        clearentOnReceiverListener.lcdDisplay(0, messageArray, 0);
        completeEmvTransaction();
    }

    @Override
    public void notifyTransactionTokenFailure(int returnCode, String message) {
        String errorMessage = message;
        errorMessage += " Status: " + device_getResponseCodeString(returnCode) + "";
        String[] messageArray = {errorMessage};
        clearentOnReceiverListener.lcdDisplay(0, messageArray, 0);
        Log.e("ERROR",message);
        completeEmvTransaction();
    }


    public void notifyNewTransactionToken(TransactionToken transactionToken) {
        publicOnReceiverListener.successfulTransactionToken(transactionToken);
        completeEmvTransaction();
    }

    /*
     * The goal of this method is to
     */
    void completeEmvTransaction() {
        byte[] authResponseCode = new byte[2];
        byte[] issuerAuthData = null;
        byte[] tlvScripts = null;
        byte[] value = null;
        int rt =  idt_VP3300.getSDKInstance().emv_completeTransaction(false, authResponseCode, issuerAuthData, tlvScripts, value);
        if(rt == ErrorCode.SUCCESS) {
            Log.i("INFO", "Completed the emv transaction");
        } else {
            String warn = "Emv transaction failed to complete. \n";
            warn += "Status: " + device_getResponseCodeString(rt) + "";
            Log.i("WARN", warn);
        }
    }

    public void notifyReaderIsReady() {
        configured = true;
        String[] message = {"VIVOpay configured and ready"};
        clearentOnReceiverListener.lcdDisplay(0, message, 0);
    }

    public void setDeviceSerialNumber() {
        StringBuilder stringBuilderSerialNumber = new StringBuilder();
        int serialNumberRt = config_getSerialNumber(stringBuilderSerialNumber);
        if (serialNumberRt == ErrorCode.SUCCESS) {
            String newDeviceSerialNumber = stringBuilderSerialNumber.toString();
            deviceSerialNumber = newDeviceSerialNumber;
        } else {
            String info = "GetSerialNumber: Failed\n";
            info += "Status: " + device_getResponseCodeString(serialNumberRt) + "";
            System.out.println(info);
            String[] message = {info};
            clearentOnReceiverListener.lcdDisplay(0, message, 0);
            deviceSerialNumber = "unknown";
        }
    }

    public void setKernelVersion() {
        StringBuilder stringBuilderKernelVersion = new StringBuilder();
        int kernelVersionRt = emv_getEMVKernelVersion(stringBuilderKernelVersion);
        if (kernelVersionRt == ErrorCode.SUCCESS) {
            stringBuilderKernelVersion.insert(0, "EM");
            kernelVersion = stringBuilderKernelVersion.toString();
        } else {
            String info = "Kernel version: Failed\n";
            info += "Status: " + device_getResponseCodeString(kernelVersionRt) + "";
            System.out.println(info);
            String[] message = {info};
            clearentOnReceiverListener.lcdDisplay(0, message, 0);
            kernelVersion = "unknown";
        }
    }

    public void setFirmwareVersion() {
        StringBuilder firmwareVersionSb = new StringBuilder();
        int firmwareVersionRt = device_getFirmwareVersion(firmwareVersionSb);
        if(firmwareVersionRt == ErrorCode.SUCCESS) {
            firmareVersion= firmwareVersionSb.toString();
        } else {
            firmareVersion = "unknown";
        }
    }

    @Override
    public void notifyCommandFailure(int returnCode, String message) {
        String errorMessage = message;
        errorMessage += "Status: " + device_getResponseCodeString(returnCode) + "";
        String[] messageArray = {errorMessage};
        clearentOnReceiverListener.lcdDisplay(0, messageArray, 0);
        Log.e("ERROR",message);
    }

    @Override
    public void notifyConfigurationFailure(String message) {
        String[] messageArray = {message};
        clearentOnReceiverListener.lcdDisplay(0, messageArray, 0);
        Log.e("ERROR",message);
    }

    @Override
    public void notifyConfigurationFailure(int returnCode, String message) {
        String errorMessage = message;
        errorMessage += " Status: " + device_getResponseCodeString(returnCode) + "";
        String[] messageArray = {errorMessage};
        clearentOnReceiverListener.lcdDisplay(0, messageArray, 0);
        Log.e("ERROR",message);
    }

    public String getDeviceSerialNumber() {
        return deviceSerialNumber;
    }

    public String getKernelVersion() {
        return kernelVersion;
    }

    public String getFirmwareVersion() {
        return firmareVersion;
    }

    public boolean isConfigured() {
        return configured;
    }

    public void setConfigured(boolean configured) {
        this.configured = configured;
    }

    public boolean isPreviousDipDidNotMatchOnApp() {
        return previousDipDidNotMatchOnApp;
    }

    public void setPreviousDipDidNotMatchOnApp(boolean previousDipDidNotMatchOnApp) {
        this.previousDipDidNotMatchOnApp = previousDipDidNotMatchOnApp;
    }
}

