package com.clearent.device;

import android.util.Log;

import com.clearent.device.family.IDTechCommonKernelDevice;
import com.clearent.device.token.domain.TransactionToken;
import com.idtechproducts.device.ErrorCode;
import com.idtechproducts.device.ErrorCodeInfo;
import com.idtechproducts.device.ICCReaderStatusStruct;
import com.idtechproducts.device.IDTMSRData;
import com.idtechproducts.device.IDT_Device;
import com.idtechproducts.device.ReaderInfo;
import com.idtechproducts.device.ResDataStruct;
import com.idtechproducts.device.StructConfigParameters;
import com.idtechproducts.device.audiojack.tools.FirmwareUpdateTool;

import java.util.Map;

/**
 * This abstract class implements wrappers for all known methods to the IDT_Device.
 * It also has an abstract method requiring the implementor to provide the IDT_Device created for the specific IDT Device wrapper (ex- IDT_VP3300).
 */
public abstract class Device implements IDTechCommonKernelDevice {

    private String paymentsBaseUrl;
    private String paymentsPublicKey;
    private String deviceSerialNumber;
    private String kernelVersion;
    private String firmwareVersion;

    private PublicOnReceiverListener publicOnReceiverListener;
    private ClearentOnReceiverListener clearentOnReceiverListener;
    private boolean configured = false;

    public Device(PublicOnReceiverListener publicOnReceiverListener, String paymentsBaseUrl, String paymentsPublicKey) {
        this.publicOnReceiverListener = publicOnReceiverListener;
        this.paymentsBaseUrl = paymentsBaseUrl;
        this.paymentsPublicKey = paymentsPublicKey;
        setClearentOnReceiverListener(new ClearentOnReceiverListener(this, publicOnReceiverListener));
    }

    abstract public IDT_Device getSDKInstance();

    public String getPaymentsBaseUrl() {
        return paymentsBaseUrl;
    }

    public String getPaymentsPublicKey() {
        return paymentsPublicKey;
    }

    public String getDeviceSerialNumber() {
        return deviceSerialNumber;
    }

    public void setDeviceSerialNumber(String deviceSerialNumber) {
        this.deviceSerialNumber = deviceSerialNumber;
    }

    public String getKernelVersion() {
        return kernelVersion;
    }

    public void setKernelVersion(String kernelVersion) {
        this.kernelVersion = kernelVersion;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public PublicOnReceiverListener getPublicOnReceiverListener() {
        return publicOnReceiverListener;
    }

    public ClearentOnReceiverListener getClearentOnReceiverListener() {
        return clearentOnReceiverListener;
    }

    public void setClearentOnReceiverListener(ClearentOnReceiverListener clearentOnReceiverListener) {
        this.clearentOnReceiverListener = clearentOnReceiverListener;
    }

    @Override
    public void notifyTransactionTokenFailure(String message) {
        String[] messageArray = {message};
        getClearentOnReceiverListener().lcdDisplay(0, messageArray, 0);
        completeEmvTransaction();
    }

    @Override
    public void notifyTransactionTokenFailure(int returnCode, String message) {
        String errorMessage = message;
        errorMessage += " Status: " + device_getResponseCodeString(returnCode) + "";
        String[] messageArray = {errorMessage};
        getClearentOnReceiverListener().lcdDisplay(0, messageArray, 0);
        Log.e("ERROR",message);
        completeEmvTransaction();
    }

    public void notifyNewTransactionToken(TransactionToken transactionToken) {
        getPublicOnReceiverListener().successfulTransactionToken(transactionToken);
        completeEmvTransaction();
    }

    /**
     * The goal of this method is end the normal authentication flow in favor of translating the card to a transaction token.
     */
    void completeEmvTransaction() {
        byte[] authResponseCode = new byte[2];
        byte[] issuerAuthData = null;
        byte[] tlvScripts = null;
        byte[] value = null;
        int rt =  getSDKInstance().emv_completeTransaction(false, authResponseCode, issuerAuthData, tlvScripts, value);
        if(rt == ErrorCode.SUCCESS) {
            Log.i("INFO", "Completed the emv transaction");
        } else {
            String warn = "Emv transaction failed to complete. \n";
            warn += "Status: " + device_getResponseCodeString(rt) + "";
            Log.i("WARN", warn);
        }
    }

    public void notifyReaderIsReady() {
        setConfigured(true);
        String[] message = {"VIVOpay configured and ready"};
        getClearentOnReceiverListener().lcdDisplay(0, message, 0);
        getPublicOnReceiverListener().isReady();
    }

    @Override
    public void setDeviceSerialNumber() {
        StringBuilder stringBuilderSerialNumber = new StringBuilder();
        int serialNumberRt = config_getSerialNumber(stringBuilderSerialNumber);
        if (serialNumberRt == ErrorCode.SUCCESS) {
            String newDeviceSerialNumber = stringBuilderSerialNumber.toString();
            setDeviceSerialNumber(newDeviceSerialNumber);
        } else {
            String info = "GetSerialNumber: Failed\n";
            info += "Status: " + device_getResponseCodeString(serialNumberRt) + "";
            System.out.println(info);
            String[] message = {info};
            getClearentOnReceiverListener().lcdDisplay(0, message, 0);
            setDeviceSerialNumber("unknown");
        }
    }

    @Override
    public void setKernelVersion() {
        StringBuilder stringBuilderKernelVersion = new StringBuilder();
        int kernelVersionRt = emv_getEMVKernelVersion(stringBuilderKernelVersion);
        if (kernelVersionRt == ErrorCode.SUCCESS) {
            stringBuilderKernelVersion.insert(0, "EM");
            setKernelVersion(stringBuilderKernelVersion.toString());
        } else {
            String info = "Kernel version: Failed\n";
            info += "Status: " + device_getResponseCodeString(kernelVersionRt) + "";
            System.out.println(info);
            String[] message = {info};
            getClearentOnReceiverListener().lcdDisplay(0, message, 0);
            setFirmwareVersion("unknown");
        }
    }

    @Override
    public void setFirmwareVersion() {
        StringBuilder firmwareVersionSb = new StringBuilder();
        int firmwareVersionRt = device_getFirmwareVersion(firmwareVersionSb);
        if(firmwareVersionRt == ErrorCode.SUCCESS) {
            setFirmwareVersion(firmwareVersionSb.toString());
        } else {
            setFirmwareVersion("unknown");
        }
    }

    @Override
    public void notifyCommandFailure(int returnCode, String message) {
        String errorMessage = message;
        errorMessage += "Status: " + device_getResponseCodeString(returnCode) + "";
        String[] messageArray = {errorMessage};
        getClearentOnReceiverListener().lcdDisplay(0, messageArray, 0);
        Log.e("ERROR",message);
    }

    @Override
    public void notifyConfigurationFailure(String message) {
        String[] messageArray = {message};
        getClearentOnReceiverListener().lcdDisplay(0, messageArray, 0);
        Log.e("ERROR",message);
    }

    @Override
    public void notifyConfigurationFailure(int returnCode, String message) {
        String errorMessage = message;
        errorMessage += " Status: " + device_getResponseCodeString(returnCode) + "";
        String[] messageArray = {errorMessage};
        getClearentOnReceiverListener().lcdDisplay(0, messageArray, 0);
        Log.e("ERROR",message);
    }

    @Override
    public boolean isConfigured() {
        return configured;
    }

    @Override
    public void setConfigured(boolean configured) {
        this.configured = configured;
    }

    @Override
    public int device_startTransaction(double amount, double amtOther, int type, int timeout, byte[] tags) {
        return getSDKInstance().device_startTransaction(amount, amtOther, type, timeout, tags);
    }

    @Override
    public int emv_startTransaction(double amount, double amtOther, int type, int timeout, byte[] tags, boolean forceOnline) {
        getClearentOnReceiverListener().reset();
        return getSDKInstance().emv_startTransaction(amount, amtOther, type, timeout, tags, forceOnline);
    }

    @Override
    public String device_getResponseCodeString(int errorCode) {
        return ErrorCodeInfo.getErrorCodeDescription(errorCode);
    }

    @Override
    public int device_getFirmwareVersion(StringBuilder version) {
        return getSDKInstance().device_getFirmwareVersion(version);
    }

    @Override
    public int emv_getEMVKernelVersion(StringBuilder version) {
        return getSDKInstance().emv_getEMVKernelVersion(version);
    }

    @Override
    public int config_getSerialNumber(StringBuilder serialNumber) {
        return getSDKInstance().config_getSerialNumber(serialNumber);
    }


    @Override
    public void setIDT_Device(FirmwareUpdateTool fwTool) {
        getSDKInstance().setIDT_Device(fwTool);
    }

    @Override
    public ReaderInfo.DEVICE_TYPE device_getDeviceType() {
        return getSDKInstance().device_getDeviceType();
    }

    @Override
    public void registerListen() {
        getSDKInstance().registerListen();
    }

    @Override
    public void unregisterListen() {
        getSDKInstance().unregisterListen();
    }

    @Override
    public void release() {
        getSDKInstance().release();
    }

    @Override
    public String config_getSDKVersion() {
        return getSDKInstance().config_getSDKVersion();
    }

    @Override
    public String config_getXMLVersionInfo() {
        return getSDKInstance().config_getXMLVersionInfo();
    }

    @Override
    public String phone_getInfoManufacture() {
        return getSDKInstance().phone_getInfoManufacture();
    }

    @Override
    public String phone_getInfoModel() {
        return getSDKInstance().phone_getInfoModel();
    }

    @Override
    public void log_setVerboseLoggingEnable(boolean enable) {
        getSDKInstance().log_setVerboseLoggingEnable(enable);
    }

    @Override
    public void log_setSaveLogEnable(boolean enable) {
        getSDKInstance().log_setSaveLogEnable(enable);
    }

    @Override
    public int log_deleteLogs() {
        return getSDKInstance().log_deleteLogs();
    }

    @Override
    public int emv_callbackResponsePIN(int mode, byte[] KSN, byte[] PIN) {
        return getSDKInstance().emv_callbackResponsePIN(mode, KSN, PIN);
    }

    @Override
    public void config_setXMLFileNameWithPath(String path) {
        getSDKInstance().config_setXMLFileNameWithPath(path);
    }

    @Override
    public boolean config_loadingConfigurationXMLFile(boolean updateAutomatically) {
        return getSDKInstance().config_loadingConfigurationXMLFile(updateAutomatically);
    }

    @Override
    public boolean device_connectWithProfile(StructConfigParameters profile) {
        return getSDKInstance().device_connectWithProfile(profile);
    }

    @Override
    public void device_ConnectWithoutValidation(boolean noValidate) {
        getSDKInstance().device_ConnectWithoutValidation(noValidate, ReaderInfo.DEVICE_TYPE.DEVICE_VP3300_AJ);
    }

    @Override
    public boolean device_connect() {
        return getSDKInstance().device_connect();
    }

    @Override
    public boolean device_isConnected() {
        return getSDKInstance().device_isConnected();
    }

    @Override
    public int device_startRKI() {
        return getSDKInstance().device_startRKI();
    }

    @Override
    public int autoConfig_start(String strXMLFilename) {
        return getSDKInstance().autoConfig_start(strXMLFilename);
    }

    @Override
    public void autoConfig_stop() {
        getSDKInstance().autoConfig_stop();
    }

    @Override
    public int device_cancelTransaction() {
        return getSDKInstance().device_cancelTransaction();
    }

    @Override
    public int device_pingDevice() {
        return getSDKInstance().device_pingDevice();
    }

    @Override
    public int device_ReviewAudioJackSetting(ResDataStruct respData) {
        return getSDKInstance().device_ReviewAudioJackSetting(respData);
    }

    @Override
    public int config_getModelNumber(StringBuilder modNumber) {
        return getSDKInstance().config_getModelNumber(modNumber);
    }

    @Override
    public int device_getKSN(ResDataStruct ksn) {
        return getSDKInstance().device_getKSN(ksn);
    }

    @Override
    public int device_setMerchantRecord(int index, boolean enabled, String merchantID, String merchantURL) {
        return getSDKInstance().device_setMerchantRecord(index, enabled, merchantID, merchantURL);
    }

    @Override
    public int device_getMerchantRecord(int index, ResDataStruct respData) {
        return getSDKInstance().device_getMerchantRecord(index, respData);
    }

    @Override
    public int device_sendDataCommand(String cmd, boolean calcLRC, String data, ResDataStruct respData) {
        return getSDKInstance().device_sendDataCommand(cmd, calcLRC, data, respData);
    }

    @Override
    public int device_sendDataCommand(String cmd, boolean calcLRC, String data, ResDataStruct respData, int timeout) {
        return getSDKInstance().device_sendDataCommand(cmd, calcLRC, data, respData, timeout);
    }

    @Override
    public int device_updateFirmware(String[] commands) {
        return getSDKInstance().device_updateFirmware(commands);
    }

    @Override
    public int device_getTransactionResults(IDTMSRData cardData) {
        return getSDKInstance().device_getTransactionResults(cardData);
    }

    @Override
    public int device_setBurstMode(byte mode) {
        return getSDKInstance().device_setBurstMode(mode);
    }

    @Override
    public int device_setPollMode(byte mode) {
        return getSDKInstance().device_setPollMode(mode);
    }

    @Override
    public int device_getRTCDateTime(byte[] dateTime) {
        return getSDKInstance().device_getRTCDateTime(dateTime);
    }

    @Override
    public int device_setRTCDateTime(byte[] dateTime) {
        return getSDKInstance().device_setRTCDateTime(dateTime);
    }

    @Override
    public int icc_getICCReaderStatus(ICCReaderStatusStruct ICCStatus) {
        return getSDKInstance().icc_getICCReaderStatus(ICCStatus);
    }

    @Override
    public int icc_powerOnICC(ResDataStruct atrPPS) {
        return getSDKInstance().icc_powerOnICC(atrPPS);
    }

    @Override
    public int icc_passthroughOnICC() {
        return getSDKInstance().icc_passthroughOnICC();
    }

    @Override
    public int icc_passthroughOffICC() {
        return getSDKInstance().icc_passthroughOffICC();
    }

    @Override
    public int icc_powerOffICC(ResDataStruct respData) {
        return getSDKInstance().icc_powerOffICC(respData);
    }

    @Override
    public int icc_exchangeAPDU(byte[] dataAPDU, ResDataStruct response) {
        return getSDKInstance().icc_exchangeAPDU(dataAPDU, response);
    }

    @Override
    public int emv_getEMVKernelCheckValue(ResDataStruct respData) {
        return getSDKInstance().emv_getEMVKernelCheckValue(respData);
    }

    @Override
    public int emv_getEMVConfigurationCheckValue(ResDataStruct respData) {
        return getSDKInstance().emv_getEMVConfigurationCheckValue(respData);
    }

    @Override
    public void emv_allowFallback(boolean allow) {
        IDT_Device.emv_allowFallback(allow);
    }

    @Override
    public int emv_retrieveApplicationData(String aid, ResDataStruct respData) {
        return getSDKInstance().emv_retrieveApplicationData(aid, respData);
    }

    @Override
    public int emv_removeApplicationData(String aid, ResDataStruct respData) {
        return getSDKInstance().emv_removeApplicationData(aid, respData);
    }

    @Override
    public int emv_setApplicationData(String aid, byte[] TLV, ResDataStruct respData) {
        return getSDKInstance().emv_setApplicationData(aid, TLV, respData);
    }

    @Override
    public int emv_retrieveTerminalData(ResDataStruct respData) {
        return getSDKInstance().emv_retrieveTerminalData(respData);
    }

    @Override
    public int emv_removeTerminalData(ResDataStruct respData) {
        return getSDKInstance().emv_removeTerminalData(respData);
    }

    @Override
    public int emv_setTerminalData(byte[] TLV, ResDataStruct respData) {
        return getSDKInstance().emv_setTerminalData(TLV, respData);
    }

    @Override
    public int emv_retrieveAidList(ResDataStruct respData) {
        return getSDKInstance().emv_retrieveAidList(respData);
    }

    @Override
    public int emv_retrieveCAPK(byte[] data, ResDataStruct respData) {
        return getSDKInstance().emv_retrieveCAPK(data, respData);
    }

    @Override
    public int emv_removeCAPK(byte[] capk, ResDataStruct respData) {
        return getSDKInstance().emv_removeCAPK(capk, respData);
    }

    @Override
    public int emv_setCAPK(byte[] key, ResDataStruct respData) {
        return getSDKInstance().emv_setCAPK(key, respData);
    }

    @Override
    public void emv_setAutoAuthenticateTransaction(boolean auto) {
        IDT_Device.emv_setAutoAuthenticateTransaction(auto);
    }

    @Override
    public boolean emv_getAutoAuthenticateTransaction() {
        return IDT_Device.emv_getAutoAuthenticateTransaction();
    }

    @Override
    public void emv_setAutoCompleteTransaction(boolean auto) {
        IDT_Device.emv_setAutoCompleteTransaction(auto);
    }

    @Override
    public boolean emv_getAutoCompleteTransaction() {
        return IDT_Device.emv_getAutoCompleteTransaction();
    }

    @Override
    public int emv_retrieveCAPKList(ResDataStruct respData) {
        return getSDKInstance().emv_retrieveCAPKList(respData);
    }

    @Override
    public int emv_retrieveCRL(ResDataStruct respData) {
        return getSDKInstance().emv_retrieveCRL(respData);
    }

    @Override
    public int emv_removeCRL(byte[] crlList, ResDataStruct respData) {
        return getSDKInstance().emv_removeCRL(crlList, respData);
    }

    @Override
    public int emv_setCRL(byte[] crlList, ResDataStruct respData) {
        return getSDKInstance().emv_setCRL(crlList, respData);
    }

    @Override
    public int emv_cancelTransaction(ResDataStruct respData) {
        return getSDKInstance().emv_cancelTransaction(respData);
    }

    @Override
    public void emv_setTransactionParameters(double amount, double amtOther, int type, int timeout, byte[] tags) {
        getSDKInstance().emv_setTransactionParameters(amount, amtOther, type, timeout, tags);
    }

    @Override
    public void emv_lcdControlResponse(byte mode, byte data) {
        getSDKInstance().emv_lcdControlResponse(mode, data);
    }

    @Override
    public int emv_authenticateTransaction(byte[] tags) {
        return getSDKInstance().emv_authenticateTransaction(tags);
    }

    @Override
    public int emv_completeTransaction(boolean commError, byte[] authCode, byte[] iad, byte[] tlvScripts, byte[] tags) {
        return getSDKInstance().emv_completeTransaction(commError, authCode, iad, tlvScripts, tags);
    }

    @Override
    public int emv_retrieveTransactionResult(byte[] tags, Map<String, Map<String, byte[]>> retrievedTags) {
        return getSDKInstance().emv_retrieveTransactionResult(tags, retrievedTags);
    }

    @Override
    public int device_reviewAllSetting(ResDataStruct respData) {
        return getSDKInstance().device_reviewAllSetting(respData);
    }

    @Override
    public int msr_defaultAllSetting() {
        return getSDKInstance().msr_defaultAllSetting();
    }

    @Override
    public int msr_getSingleSetting(byte funcID, byte[] response) {
        return getSDKInstance().msr_getSingleSetting(funcID, response);
    }

    @Override
    public int msr_setSingleSetting(byte funcID, byte setData) {
        return getSDKInstance().msr_setSingleSetting(funcID, setData);
    }

    @Override
    public int msr_cancelMSRSwipe() {
        return getSDKInstance().msr_cancelMSRSwipe();
    }

    @Override
    public int msr_startMSRSwipe() {
        return getSDKInstance().msr_startMSRSwipe();
    }

    @Override
    public int msr_startMSRSwipe(int timeout) {
        return getSDKInstance().msr_startMSRSwipe(timeout);
    }

    @Override
    public int ctls_retrieveApplicationData(String aid, ResDataStruct respData) {
        return getSDKInstance().ctls_retrieveApplicationData(aid, respData);
    }

    @Override
    public int ctls_removeApplicationData(String aid, ResDataStruct respData) {
        return getSDKInstance().ctls_removeApplicationData(aid, respData);
    }

    @Override
    public int ctls_setApplicationData(byte[] TLV, ResDataStruct respData) {
        return getSDKInstance().ctls_setApplicationData(TLV, respData);
    }

    @Override
    public int ctls_setConfigurationGroup(byte[] TLV, ResDataStruct respData) {
        return getSDKInstance().ctls_setConfigurationGroup(TLV, respData);
    }

    @Override
    public int ctls_getConfigurationGroup(int group, ResDataStruct respData) {
        return getSDKInstance().ctls_getConfigurationGroup(group, respData);
    }

    @Override
    public int ctls_getAllConfigurationGroups(ResDataStruct respData) {
        return getSDKInstance().ctls_getAllConfigurationGroups(respData);
    }

    @Override
    public int ctls_removeConfigurationGroup(int group) {
        return getSDKInstance().ctls_removeConfigurationGroup(group);
    }

    @Override
    public int ctls_retrieveTerminalData(ResDataStruct respData) {
        return getSDKInstance().ctls_retrieveTerminalData(respData);
    }

    @Override
    public int ctls_setTerminalData(byte[] TLV, ResDataStruct respData) {
        return getSDKInstance().ctls_setTerminalData(TLV, respData);
    }

    @Override
    public int ctls_retrieveAidList(ResDataStruct respData) {
        return getSDKInstance().ctls_retrieveAidList(respData);
    }

    @Override
    public int ctls_retrieveCAPK(byte[] data, ResDataStruct respData) {
        return getSDKInstance().ctls_retrieveCAPK(data, respData);
    }

    @Override
    public int ctls_removeCAPK(byte[] capk, ResDataStruct respData) {
        return getSDKInstance().ctls_removeCAPK(capk, respData);
    }

    @Override
    public int ctls_setCAPK(byte[] key, ResDataStruct respData) {
        return getSDKInstance().ctls_setCAPK(key, respData);
    }

    @Override
    public int ctls_retrieveCAPKList(ResDataStruct respData) {
        return getSDKInstance().ctls_retrieveCAPKList(respData);
    }

    @Override
    public int ctls_removeAllApplicationData() {
        return getSDKInstance().ctls_removeAllApplicationData();
    }

    @Override
    public int ctls_removeAllCAPK() {
        return getSDKInstance().ctls_removeAllCAPK();
    }

    @Override
    public int ctls_startTransaction(double amount, double amtOther, int type, int timeout, byte[] tags) {
        return getSDKInstance().ctls_startTransaction(amount, amtOther, type, timeout, tags);
    }

    @Override
    public int ctls_cancelTransaction() {
        return getSDKInstance().ctls_cancelTransaction();
    }

}
