package com.clearent.device;

import android.util.Log;

import com.clearent.device.family.IDTDevice;
import com.clearent.device.token.domain.TransactionToken;
import com.idtechproducts.device.APDUResponseStruct;
import com.idtechproducts.device.Common;
import com.idtechproducts.device.ErrorCode;
import com.idtechproducts.device.ErrorCodeInfo;
import com.idtechproducts.device.ICCReaderStatusStruct;
import com.idtechproducts.device.ICCSettingStruct;
import com.idtechproducts.device.IDTMSRData;
import com.idtechproducts.device.IDT_Device;
import com.idtechproducts.device.MSRSettingStruct;
import com.idtechproducts.device.OnReceiverListener;
import com.idtechproducts.device.PowerOnStructure;
import com.idtechproducts.device.ReaderInfo;
import com.idtechproducts.device.ResDataStruct;
import com.idtechproducts.device.StructConfigParameters;
import com.idtechproducts.device.USBBypassListener;
import com.idtechproducts.device.audiojack.tasks.TaskManager;
import com.idtechproducts.device.audiojack.tools.FirmwareUpdateTool;

import java.io.InputStream;
import java.util.Map;

/**
 * This abstract class implements wrappers for all known methods to the IDT_Device.
 * It also has an abstract method requiring the implementor to provide the IDT_Device created for the specific IDT Device wrapper (ex- IDT_VP3300).
 *
 * IDTech exposes a method called getSDKInstance allowing direct access to the IDT_Device. I can only speculate this was done just in case a method
 * was not exposed correctly in each of its concrete implementations (ex IDT_VP3300, IDT_Augusta).
 */
public abstract class Device implements IDTDevice {

    private String paymentsBaseUrl;
    private String paymentsPublicKey;
    private String deviceSerialNumber;
    private String kernelVersion;
    private String firmwareVersion;

    private PublicOnReceiverListener publicOnReceiverListener;
    private ClearentOnReceiverListener clearentOnReceiverListener;
    private boolean readerConfigured = false;
    private boolean deviceConfigured = false;

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
        if(readerConfigured && deviceConfigured) {
            String[] message = {"VIVOpay configured and ready"};
            getClearentOnReceiverListener().lcdDisplay(0, message, 0);
            getPublicOnReceiverListener().isReady();
        }
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
    public boolean isReaderConfigured() {
        return readerConfigured;
    }

    @Override
    public void setReaderConfigured(boolean readerConfigured) {
        this.readerConfigured = readerConfigured;
    }

    @Override
    public boolean isDeviceConfigured() {
        return deviceConfigured;
    }

    @Override
    public void setDeviceConfigured(boolean deviceConfigured) {
        this.deviceConfigured = deviceConfigured;
    }

    @Override
    public int device_startTransaction(double amount, double amtOther, int type, int timeout, byte[] tags) {
        getClearentOnReceiverListener().reset();
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
    public boolean device_setDeviceType(ReaderInfo.DEVICE_TYPE deviceType) {
        return false;
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

    //start here


    @Override
    public void btle_WakeUp() {
        getSDKInstance().btle_WakeUp();
    }

    @Override
    public boolean device_setDeviceType(ReaderInfo.DEVICE_TYPE deviceType, int PID) {
        return getSDKInstance().device_setDeviceType(deviceType, PID);
    }

    @Override
    public boolean device_setDeviceType(ReaderInfo.DEVICE_TYPE deviceType, boolean isSRED) {
        return getSDKInstance().device_setDeviceType(deviceType, isSRED);
    }

    @Override
    public boolean device_setDeviceType(ReaderInfo.DEVICE_TYPE deviceType, boolean isTTK, boolean isSRED) {
        return getSDKInstance().device_setDeviceType(deviceType,  isTTK,  isSRED);
    }

    @Override
    public boolean device_setDeviceType(ReaderInfo.DEVICE_TYPE deviceType, boolean isTTK, boolean isSRED, boolean isThales) {
        return getSDKInstance().device_setDeviceType( deviceType,  isTTK,  isSRED,  isThales);
    }

    @Override
    public boolean device_setDeviceType(ReaderInfo.DEVICE_TYPE deviceType, boolean isTTK, boolean isSRED, boolean isThales, int PID) {
        return getSDKInstance().device_setDeviceType( deviceType,  isTTK,  isSRED,  isThales,  PID);
    }

    @Override
    public int device_setDisplayReady() {
        return getSDKInstance().device_setDisplayReady();
    }

    @Override
    public String log_exportLogs() {
        return getSDKInstance().log_exportLogs();
    }

    @Override
    public void device_ConnectWithoutValidation(boolean noValidate, ReaderInfo.DEVICE_TYPE deviceType) {
        getSDKInstance().device_ConnectWithoutValidation( noValidate,  deviceType);
    }

    @Override
    public void device_disconnect() {
        getSDKInstance().device_disconnect();
    }

    @Override
    public boolean emv_getDisplayMessage(int timeout, long startTime, ResDataStruct resData) {
        return getSDKInstance().emv_getDisplayMessage(timeout, startTime, resData);
    }

    @Override
    public boolean msr_isSwipeCardRunning() {
        return getSDKInstance().msr_isSwipeCardRunning();
    }

    @Override
    public void setBypassListener(USBBypassListener callback) {
         getSDKInstance().setBypassListener(callback);
    }

    @Override
    public void clearBypassListener() {
        getSDKInstance().clearBypassListener();
    }

    @Override
    public void externalConnect() {
         getSDKInstance().externalConnect();
    }

    @Override
    public void externalDisconnect() {
         getSDKInstance().externalDisconnect();
    }

    @Override
    public void externalDeviceNotFound() {
         getSDKInstance().externalDeviceNotFound();
    }

    @Override
    public TaskManager.TaskStartRet sendAudioCommand_helper(String cmd, int timeout, ResDataStruct respData) {
        return getSDKInstance().sendAudioCommand_helper( cmd,  timeout,  respData);
    }

    @Override
    public byte[] getFirmwareVersionForTTK() {
        return getSDKInstance().getFirmwareVersionForTTK();
    }

    @Override
    public int device_controlBeep(int index, int frequency, int duration) {
        return getSDKInstance().device_controlBeep( index,  frequency,  duration);
    }

    @Override
    public int device_controlLED(byte indexLED, byte control, int intervalOn, int intervalOff) {
        return getSDKInstance().device_controlLED( indexLED,  control,  intervalOn,  intervalOff);
    }

    @Override
    public int device_controlLED_ICC(int controlMode, int interval) {
        return getSDKInstance().device_controlLED_ICC( controlMode,  interval);
    }

    @Override
    public int device_selfCheck() {
        return getSDKInstance().device_selfCheck();
    }

    @Override
    public int device_setBluetoothParameters(String name, byte[] oldPassword, byte[] newPassword) {
        return getSDKInstance().device_setBluetoothParameters( name,  oldPassword, newPassword);
    }

    @Override
    public int device_setSource(byte[] data) {
        return getSDKInstance().device_setSource(data);
    }

    @Override
    public int device_getSource(byte[] data) {
        return getSDKInstance().device_getSource(data);
    }

    @Override
    public int device_controlUserInterface(byte[] values) {
        return getSDKInstance().device_controlUserInterface(values);
    }

    @Override
    public int device_calibrateParameters(byte delta) {
        return getSDKInstance().device_calibrateParameters(delta);
    }

    @Override
    public int device_getDriveFreeSpace(StringBuilder freeSpace, StringBuilder usedSpace) {
        return getSDKInstance().device_getDriveFreeSpace( freeSpace,  usedSpace);
    }

    @Override
    public int device_listDirectory(String directoryName, boolean recursive, boolean onSD, StringBuilder directory) {
        return getSDKInstance().device_listDirectory( directoryName,  recursive,  onSD,  directory);
    }

    @Override
    public int device_createDirectory(String directoryName) {
        return getSDKInstance().device_createDirectory( directoryName);
    }

    @Override
    public int device_deleteDirectory(String directoryName) {
        return getSDKInstance().device_deleteDirectory( directoryName);
    }

    @Override
    public int device_deleteFile(String fileName) {
        return getSDKInstance().device_deleteFile( fileName);
    }

    @Override
    public int device_enhancedPassthrough(byte[] data) {
        return getSDKInstance().device_enhancedPassthrough( data);
    }

    @Override
    public int device_controlIndicator(byte indicator, boolean enable) {
        return getSDKInstance().device_controlIndicator( indicator,  enable);
    }

    @Override
    public int device_getKSN(byte keyNameIndex, byte[] keySlot, ResDataStruct resKSN) {
        return getSDKInstance().device_getKSN( keyNameIndex, keySlot,  resKSN);
    }

    @Override
    public int device_enableTDES(ResDataStruct respData) {
        return getSDKInstance().device_enableTDES( respData);
    }

    @Override
    public int device_enableAES(ResDataStruct respData) {
        return getSDKInstance().device_enableAES( respData);
    }

    @Override
    public int device_calibrateReader(ResDataStruct respData) {
        return getSDKInstance().device_calibrateReader( respData);
    }

    @Override
    public int device_enableErrorNotification(ResDataStruct respData, boolean enable) {
        return getSDKInstance().device_enableErrorNotification( respData,  enable);
    }

    @Override
    public int device_enableExpDate(ResDataStruct respData, boolean enable) {
        return getSDKInstance().device_enableExpDate( respData,  enable);
    }

    @Override
    public int device_enableForceEncryption(ResDataStruct respData, boolean enable) {
        return getSDKInstance().device_enableForceEncryption( respData,  enable);
    }

    @Override
    public int device_setDateTime(byte[] mac) {
        return getSDKInstance().device_setDateTime( mac);
    }

    @Override
    public int device_wakeup() {
        return getSDKInstance().device_wakeup();
    }

    @Override
    public int device_getKeyStatus(ResDataStruct respData) {
        return getSDKInstance().device_getKeyStatus( respData);
    }

    @Override
    public int device_getDRS(ResDataStruct respData) {
        return getSDKInstance().device_getDRS(respData);
    }

    @Override
    public int device_verifyBackdoorKey() {
        return getSDKInstance().device_verifyBackdoorKey();
    }

    @Override
    public void setfoundBlockedApplication(boolean found) {
        getSDKInstance().setfoundBlockedApplication( found);
    }

    @Override
    public boolean foundBlockedApplication() {
        return getSDKInstance().foundBlockedApplication();
    }

    @Override
    public int icc_getICCReaderStatus_LEDControl(ICCReaderStatusStruct ICCStatus, Common.EMV_TRANSACTION_STAGE transactionStage) {
        return getSDKInstance().icc_getICCReaderStatus_LEDControl( ICCStatus,  transactionStage);
    }

    @Override
    public int icc_powerOnICC(PowerOnStructure options, ResDataStruct atrPPS) {
        return getSDKInstance().icc_powerOnICC( options,  atrPPS);
    }

    @Override
    public int icc_getATR() {
        return getSDKInstance().icc_getATR();
    }

    @Override
    public int icc_powerOffICC() {
        return getSDKInstance().icc_powerOffICC();
    }

    @Override
    public int icc_passthroughOnICCforAPDU() {
        return getSDKInstance().icc_passthroughOnICCforAPDU();
    }

    @Override
    public int icc_passthroughOffICCforAPDU() {
        return getSDKInstance().icc_passthroughOffICCforAPDU();
    }

    @Override
    public int config_getDateTime(StringBuilder dateTime) {
        return getSDKInstance().config_getDateTime( dateTime);
    }

    @Override
    public int config_setLEDControl(byte MSRLedOption, byte ICCLedOption) {
        return getSDKInstance().config_setLEDControl( MSRLedOption,  ICCLedOption);
    }

    @Override
    public int config_setLEDController(boolean firmwareControlMSRLED, boolean firmwareControlICCLED) {
        return getSDKInstance().config_setLEDController( firmwareControlMSRLED,  firmwareControlICCLED);
    }

    @Override
    public int config_getLEDController(ResDataStruct respData) {
        return getSDKInstance().config_getLEDController( respData);
    }

    @Override
    public int config_setBeeperControl(byte BeeperOption) {
        return getSDKInstance().config_setBeeperControl( BeeperOption);
    }

    @Override
    public int config_setBeeperController(boolean firmwareControlBeeper) {
        return getSDKInstance().config_setBeeperController( firmwareControlBeeper);
    }

    @Override
    public int config_getBeeperControl(ResDataStruct respData) {
        return getSDKInstance().config_getBeeperControl( respData);
    }

    @Override
    public int config_beepersControl(int beeperIndex, int frequency, int duration) {
        return getSDKInstance().config_beepersControl( beeperIndex,  frequency,  duration);
    }

    @Override
    public int config_getEncryptionControl(ResDataStruct respData) {
        return getSDKInstance().config_getEncryptionControl( respData);
    }

    @Override
    public int config_setEncryptionControl(byte Encryption) {
        return getSDKInstance().config_setEncryptionControl( Encryption);
    }

    @Override
    public int config_setEncryptionControl(boolean msr, boolean icc) {
        return getSDKInstance().config_setEncryptionControl( msr,  icc);
    }

    @Override
    public int config_setBLEParameters(String url, String uid, String nameSpaceId, String instanceId) {
        return getSDKInstance().config_setBLEParameters( url,  uid,  nameSpaceId,  instanceId);
    }

    @Override
    public int config_setLongTermPrivateKey(byte[] encryptedKeyData, byte[] plainDataHash) {
        return getSDKInstance().config_setLongTermPrivateKey( encryptedKeyData,  plainDataHash);
    }

    @Override
    public int device_getBatteryVoltage(StringBuilder batteryInfo) {
        return getSDKInstance().device_getBatteryVoltage( batteryInfo);
    }

    @Override
    public int device_rebootDevice() {
        return getSDKInstance().device_rebootDevice();
    }

    @Override
    public int device_sendCommandDirectIO(String hexCommand, ResDataStruct respData) {
        return getSDKInstance().device_sendCommandDirectIO( hexCommand,  respData);
    }

    @Override
    public void cancelCurrentCommand() {
         getSDKInstance().cancelCurrentCommand();
    }

    @Override
    public int icc_exchangeAPDU(byte[] dataAPDU, APDUResponseStruct response) {
        return getSDKInstance().icc_exchangeAPDU(dataAPDU,  response);
    }

    @Override
    public int icc_exchangeEncryptedAPDU(byte[] dataAPDU, byte[] ksn, APDUResponseStruct response) {
        return getSDKInstance().icc_exchangeEncryptedAPDU(dataAPDU, ksn,  response);
    }

    @Override
    public int icc_exchangeMultiAPDU(byte[] dataAPDU, ResDataStruct respData) {
        return getSDKInstance().icc_exchangeMultiAPDU( dataAPDU,  respData);
    }

    @Override
    public int icc_getAPDU_KSN(ResDataStruct resKSN) {
        return getSDKInstance().icc_getAPDU_KSN( resKSN);
    }

    @Override
    public int icc_getAPDU_KSN(byte KeyNameIndex, byte[] KeySlot, ResDataStruct resKSN) {
        return getSDKInstance().icc_getAPDU_KSN( KeyNameIndex,  KeySlot,  resKSN);
    }

    @Override
    public int icc_setKeyTypeForICCDUKPT(byte encryption) {
        return getSDKInstance().icc_setKeyTypeForICCDUKPT( encryption);
    }

    @Override
    public int icc_getKeyTypeForICCDUKPT(ResDataStruct respData) {
        return getSDKInstance().icc_getKeyTypeForICCDUKPT( respData);
    }

    @Override
    public int icc_setKeyFormatForICCDUKPT(byte encryption) {
        return getSDKInstance().icc_setKeyFormatForICCDUKPT( encryption);
    }

    @Override
    public int icc_getKeyFormatForICCDUKPT(ResDataStruct respData) {
        return getSDKInstance().icc_getKeyFormatForICCDUKPT( respData);
    }

    @Override
    public int icc_enable(boolean withNotification) {
        return getSDKInstance().icc_enable( withNotification);
    }

    @Override
    public int icc_disable() {
        return getSDKInstance().icc_disable();
    }

    @Override
    public int icc_getFunctionStatus(ResDataStruct respData) {
        return getSDKInstance().icc_getFunctionStatus( respData);
    }

    @Override
    public int icc_enableNotification(boolean enableNotifyICCStatus) {
        return getSDKInstance().icc_enableNotification( enableNotifyICCStatus);
    }

    @Override
    public int icc_defaultSetting() {
        return getSDKInstance().icc_defaultSetting();
    }

    @Override
    public int icc_reviewAllSetting(ICCSettingStruct iccSetting) {
        return getSDKInstance().icc_reviewAllSetting(iccSetting);
    }

    @Override
    public int emv_removeAllApplicationData() {
        return getSDKInstance().emv_removeAllApplicationData();
    }

    @Override
    public int ctls_setGlobalConfiguration(byte[] TLV, ResDataStruct respData) {
        return getSDKInstance().ctls_setGlobalConfiguration( TLV,  respData);
    }

    @Override
    public int ctls_resetConfigurationGroup(int group) {
        return getSDKInstance().ctls_resetConfigurationGroup( group);
    }

    @Override
    public int ctls_displayOnlineAuthResult(boolean isOK, byte[] tlv) {
        return getSDKInstance().ctls_displayOnlineAuthResult( isOK,  tlv);
    }

    @Override
    public int emv_removeAllCAPK() {
        return getSDKInstance().emv_removeAllCAPK();
    }

    @Override
    public int emv_removeAllCRL() {
        return getSDKInstance().emv_removeAllCRL();
    }

    @Override
    public int emv_retrieveCRLStatus(ResDataStruct respData) {
        return getSDKInstance().emv_retrieveCRLStatus( respData);
    }

    @Override
    public int emv_retrieveExceptionList(ResDataStruct respData) {
        return getSDKInstance().emv_retrieveExceptionList( respData);
    }

    @Override
    public int emv_setException(byte[] exception) {
        return getSDKInstance().emv_setException(exception);
    }

    @Override
    public int emv_removeException(byte[] exception) {
        return getSDKInstance().emv_removeException(exception);
    }

    @Override
    public int emv_removeAllExceptions() {
        return getSDKInstance().emv_removeAllExceptions();
    }

    @Override
    public int emv_retrieveExceptionLogStatus(ResDataStruct respData) {
        return getSDKInstance().emv_retrieveExceptionLogStatus( respData);
    }

    @Override
    public int emv_removeTransactionLog() {
        return getSDKInstance().emv_removeTransactionLog();
    }

    @Override
    public int emv_retrieveTransactionLogStatus(ResDataStruct respData) {
        return getSDKInstance().emv_retrieveTransactionLogStatus( respData);
    }

    @Override
    public int emv_retrieveTransactionLog(ResDataStruct respData) {
        return getSDKInstance().emv_retrieveTransactionLog( respData);
    }

    @Override
    public int icc_removeCRLByRIDIndex(byte[] data, ResDataStruct respData) {
        return getSDKInstance().icc_removeCRLByRIDIndex( data,  respData);
    }

    @Override
    public int icc_getInterfaceDeviceSerialNumber(ResDataStruct respData) {
        return getSDKInstance().icc_getInterfaceDeviceSerialNumber( respData);
    }

    @Override
    public int icc_setInterfaceDeviceSerialNumber(byte[] data, ResDataStruct respData) {
        return getSDKInstance().icc_setInterfaceDeviceSerialNumber( data,  respData);
    }

    @Override
    public int icc_getTerminalId(ResDataStruct respData) {
        return getSDKInstance().icc_getTerminalId(respData);
    }

    @Override
    public int icc_setTerminalId(byte[] data, ResDataStruct respData) {
        return getSDKInstance().icc_setTerminalId(data, respData);
    }

    @Override
    public void waitForOtherCommands() {
        getSDKInstance().waitForOtherCommands();
    }

    @Override
    public void device_enableAutoPollEMV(boolean enabled, double amount, double amtOther, int type, int timeout, byte[] tags) {
        getSDKInstance().device_enableAutoPollEMV( enabled,  amount,  amtOther,  type,  timeout,  tags);
    }

    @Override
    public void emv_secondResponse(int timeout, int mode) {
         getSDKInstance().emv_secondResponse( timeout,  mode);
    }

    @Override
    public int emv_callbackResponseMSR(byte[] MSR) {
        return getSDKInstance().emv_callbackResponseMSR( MSR);
    }

    @Override
    public int device_setPMCStatus(byte[] idleSleepTime, ResDataStruct respData) {
        return getSDKInstance().device_setPMCStatus(idleSleepTime,  respData)  ;

    }

    @Override
    public int device_getPMCStatus(ResDataStruct respData) {
        return getSDKInstance().device_getPMCStatus( respData);

    }

    @Override
    public int device_shutOffPower(ResDataStruct respData) {
        return getSDKInstance().device_shutOffPower( respData  );

    }

    @Override
    public int msr_reviewAllSetting(MSRSettingStruct msrSetting) {
        return getSDKInstance().msr_reviewAllSetting( msrSetting);

    }

    @Override
    public int msr_reviewSecurityLevel(byte[] secSetting) {
        return getSDKInstance().msr_reviewSecurityLevel( secSetting) ;

    }

    @Override
    public int msr_setDecodingMethod(int method) {
        return getSDKInstance().msr_setDecodingMethod( method);

    }

    @Override
    public int msr_setKeyManagement(boolean fixed) {
        return getSDKInstance().msr_setKeyManagement( fixed);
    }

    @Override
    public int msr_selectMagneticTrack(int trackMode) {
        return getSDKInstance().msr_selectMagneticTrack( trackMode) ;
    }

    @Override
    public int msr_setTrackSeparator(int sep, String zz) {
        return getSDKInstance().msr_setTrackSeparator( sep,  zz);
    }

    @Override
    public int msr_setTerminator(int ter, String zz) {
        return getSDKInstance().msr_setTerminator( ter,  zz) ;
    }

    @Override
    public int msr_setTrackPrefix(int track, String zz) {
        return getSDKInstance().msr_setTrackPrefix( track,  zz);
    }

    @Override
    public int msr_setTrackSuffix(int track, String zz) {
        return getSDKInstance().msr_setTrackSuffix( track,  zz);
    }

    @Override
    public int msr_showTrackPrefixMessage(int track) {
        return getSDKInstance().msr_showTrackPrefixMessage( track);
    }

    @Override
    public int msr_showTrackSuffixMessage(int track) {
        return getSDKInstance().msr_showTrackSuffixMessage( track);
    }

    @Override
    public int msr_changeToDefault() {
        return getSDKInstance().msr_changeToDefault();
    }

    @Override
    public int msr_setPreamble(String zz) {
        return getSDKInstance().msr_setPreamble(zz);
    }

    @Override
    public int msr_setPostamble(String zz) {
        return getSDKInstance().msr_setPostamble(zz);
    }

    @Override
    public int ctls_startTransaction() {
        return getSDKInstance().ctls_startTransaction();
    }

    @Override
    public int msr_startTransaction(double amount, double amtOther, int type, int timeout, byte[] tags) {
        return getSDKInstance().msr_startTransaction( amount,  amtOther,  type,  timeout,  tags);
    }

    @Override
    public int msr_startMSRSwipeWithDisplay(String line1, String line2, String line3) {
        return getSDKInstance().msr_startMSRSwipeWithDisplay( line1,  line2,  line3);
    }

    @Override
    public int msr_setExpirationMask(boolean mask) {
        return getSDKInstance().msr_setExpirationMask(mask);
    }

    @Override
    public int msr_getExpirationMask(ResDataStruct respData) {
        return getSDKInstance().msr_getExpirationMask( respData) ;
    }

    @Override
    public int msr_setClearPANID(byte value) {
        return getSDKInstance().msr_setClearPANID( value) ;
    }

    @Override
    public int msr_getSwipeForcedEncryptionOption(ResDataStruct respData) {
        return getSDKInstance().msr_getSwipeForcedEncryptionOption( respData);
    }

    @Override
    public int msr_setSwipeForcedEncryptionOption(boolean track1, boolean track2, boolean track3, boolean track3card0) {
        return getSDKInstance().msr_setSwipeForcedEncryptionOption( track1,  track2,  track3,  track3card0);
    }

    @Override
    public int msr_getSwipeMaskOption(ResDataStruct respData) {
        return getSDKInstance().msr_getSwipeMaskOption( respData);
    }

    @Override
    public int msr_setSwipeMaskOption(boolean track1, boolean track2, boolean track3) {
        return getSDKInstance().msr_setSwipeMaskOption( track1,  track2,  track3);
    }

    @Override
    public int msr_getClearPANID(ResDataStruct respData) {
        return getSDKInstance().msr_getClearPANID( respData);
    }

    @Override
    public int msr_getSetting(byte setting, ResDataStruct respData) {
        return getSDKInstance().msr_getSetting( setting,  respData);
    }

    @Override
    public int msr_setSetting(byte setting, byte val) {
        return getSDKInstance().msr_setSetting( setting,  val);
    }

    @Override
    public int msr_setSetting(byte setting, byte[] val) {
        return getSDKInstance().msr_setSetting( setting, val) ;
    }

    @Override
    public int msr_setSwipeEncryption(byte encryption) {
        return getSDKInstance().msr_setSwipeEncryption( encryption);
    }

    @Override
    public int msr_getSwipeEncryption(ResDataStruct respData) {
        return getSDKInstance().msr_getSwipeEncryption( respData);
    }

    @Override
    public int msr_enableBufferMode(boolean isBufferMode, boolean withNotification) {
        return getSDKInstance().msr_enableBufferMode( isBufferMode,  withNotification);
    }

    @Override
    public int msr_disable() {
        return getSDKInstance().msr_disable();
    }

    @Override
    public int msr_setWhiteList(byte[] val) {
        return getSDKInstance().msr_setWhiteList( val);
    }

    @Override
    public int msr_RetrieveWhiteList(ResDataStruct respData) {
        return getSDKInstance().msr_RetrieveWhiteList( respData) ;
    }

    @Override
    public int msr_getFunctionStatus(ResDataStruct respData) {
        return getSDKInstance().msr_getFunctionStatus( respData) ;
    }

    @Override
    public int msr_flushTrackData() {
        return getSDKInstance().msr_flushTrackData();
    }

    @Override
    public int felica_authentication(byte[] key) {
        return getSDKInstance().felica_authentication( key) ;
    }

    @Override
    public int felica_readWithMac(int blockCnt, byte[] blockList, ResDataStruct respData) {
        return getSDKInstance().felica_readWithMac( blockCnt, blockList,  respData );
    }

    @Override
    public int felica_writeWithMac(byte blockNum, byte[] blockData) {
        return getSDKInstance().felica_writeWithMac( blockNum, blockData);
    }

    @Override
    public int felica_read(byte[] serviceCodeList, int blockCnt, byte[] blockList, ResDataStruct respData) {
        return getSDKInstance().felica_read(serviceCodeList,  blockCnt, blockList,  respData);
    }

    @Override
    public int felica_write(byte[] serviceCodeList, int blockCnt, byte[] blockList, byte[] blockData, ResDataStruct respData) {
        return getSDKInstance().felica_write( serviceCodeList,  blockCnt, blockList, blockData,  respData);
    }

    @Override
    public int felica_poll(byte[] systemCode, ResDataStruct respData) {
        return getSDKInstance().felica_poll( systemCode,  respData);
    }

    @Override
    public int felica_requestService(byte[] nodeCode, ResDataStruct respData) {
        return getSDKInstance().felica_requestService( nodeCode,  respData);
    }

    @Override
    public int pin_getEncryptedOnlinePIN(int keyType, int timeout) {
        return getSDKInstance().pin_getEncryptedOnlinePIN( keyType,  timeout);
    }

    @Override
    public int pin_getPAN(int getCSC, int timeout) {
        return getSDKInstance().pin_getPAN( getCSC,  timeout);
    }

    @Override
    public int pin_promptCreditDebit(byte currencySymbol, String displayAmount, int timeout, ResDataStruct respData) {
        return getSDKInstance().pin_promptCreditDebit( currencySymbol,  displayAmount,  timeout,  respData );
    }

    @Override
    public int ws_requestCSR(ResDataStruct respData) {
        return getSDKInstance().ws_requestCSR( respData);
    }

    @Override
    public int ws_loadSSLCert(String name, String dataDER) {
        return getSDKInstance().ws_loadSSLCert( name,  dataDER) ;
    }

    @Override
    public int ws_revokeSSLCert(String name) {
        return getSDKInstance().ws_revokeSSLCert( name) ;
    }

    @Override
    public int ws_deleteSSLCert(String name) {
        return getSDKInstance().ws_deleteSSLCert( name);
    }

    @Override
    public int ws_getCertChainType(ResDataStruct respData) {
        return getSDKInstance().ws_getCertChainType( respData) ;
    }

    @Override
    public int ws_updateRootCertificate(String name, String dataDER, String signature) {
        return getSDKInstance().ws_updateRootCertificate( name,  dataDER,  signature) ;
    }

    @Override
    public int pin_cancelPINEntry() {
        return getSDKInstance().pin_cancelPINEntry();
    }

    @Override
    public int pin_displayMessageGetEncryptedPIN(byte PANKeyType, byte[] PAN, byte PINMaxLen, byte PINMinLen, byte[] LCDMsg, ResDataStruct respData) {
        return getSDKInstance().pin_displayMessageGetEncryptedPIN( PANKeyType,  PAN,  PINMaxLen,  PINMinLen,  LCDMsg,  respData) ;
    }

    @Override
    public int pin_getFunctionKey(ResDataStruct respData) {
        return getSDKInstance().pin_getFunctionKey( respData) ;
    }

    @Override
    public int pin_displayMessageGetNumericKey(byte DisplayFlag, byte KeyMaxLen, byte KeyMinLen, byte[] TextDisplayMsg, byte[] DisplayMsgSig, ResDataStruct respData) {
        return getSDKInstance().pin_displayMessageGetNumericKey( DisplayFlag,  KeyMaxLen,  KeyMinLen, TextDisplayMsg, DisplayMsgSig,  respData);
    }

    @Override
    public int pin_displayMessageGetAmount(byte DisplayFlag, byte KeyMaxLen, byte KeyMinLen, byte[] TextDisplayMsg, byte[] DisplayMsgSig, ResDataStruct respData) {
        return getSDKInstance().pin_displayMessageGetAmount( DisplayFlag,  KeyMaxLen,  KeyMinLen,  TextDisplayMsg,  DisplayMsgSig,  respData);
    }

    @Override
    public int lcd_resetInitialState() {
        return getSDKInstance().lcd_resetInitialState();
    }

    @Override
    public int lcd_customDisplayMode(boolean enable) {
        return getSDKInstance().lcd_customDisplayMode( enable);
    }

    @Override
    public int lcd_setForeBackColor(byte[] foreRGB, byte[] backRGB) {
        return getSDKInstance().lcd_setForeBackColor( foreRGB,  backRGB);
    }

    @Override
    public int lcd_clearDisplay() {
        return getSDKInstance().lcd_clearDisplay();
    }

    @Override
    public int lcd_captureSignature(int timeout) {
        return getSDKInstance().lcd_captureSignature( timeout) ;
    }

    @Override
    public int lcd_startSlideShow(String files, int posX, int posY, int posMode, boolean touchEnable, boolean recursion, boolean touchTerminate, int delay, int loops, boolean clearScreen) {
        return getSDKInstance().lcd_startSlideShow( files,  posX,  posY,  posMode,  touchEnable,  recursion,  touchTerminate,  delay,  loops,  clearScreen);
    }

    @Override
    public int lcd_cancelSlideShow(ResDataStruct respData) {
        return getSDKInstance().lcd_cancelSlideShow( respData) ;
    }

    @Override
    public int lcd_setDisplayImage(String files, int posX, int posY, int posMode, boolean touchEnable, boolean clearScreen) {
        return getSDKInstance().lcd_setDisplayImage( files,  posX,  posY,  posMode,  touchEnable,  clearScreen) ;
    }

    @Override
    public int lcd_setBackgroundImage(String files, boolean enable) {
        return getSDKInstance().lcd_setBackgroundImage( files,  enable) ;
    }

    @Override
    public int lcd_displayText(int posX, int posY, int displayWidth, int displayHeight, int fontDesignation, int fontID, int screenPosition, String displayText, ResDataStruct respData) {
        return getSDKInstance().lcd_displayText( posX,  posY,  displayWidth,  displayHeight,  fontDesignation,  fontID,  screenPosition,  displayText,  respData);
    }

    @Override
    public int lcd_displayParagraph(int posX, int posY, int displayWidth, int displayHeight, int fontDesignation, int fontID, int displayProperties, String displayText) {
        return getSDKInstance().lcd_displayParagraph( posX,  posY,  displayWidth,  displayHeight,  fontDesignation,  fontID,  displayProperties,  displayText);
    }

    @Override
    public int lcd_displayButton(int posX, int posY, int buttonWidth, int buttonHeight, int fontDesignation, int fontID, int displayPosition, String buttonLabel, int buttonTextColorR, int buttonTextColorG, int buttonTextColorB, int buttonBackgroundColorR, int buttonBackgroundColorG, int buttonBackgroundColorB, ResDataStruct respData) {
        return getSDKInstance().lcd_displayButton( posX,  posY,  buttonWidth,  buttonHeight,  fontDesignation,  fontID,  displayPosition,  buttonLabel,  buttonTextColorR,  buttonTextColorG,  buttonTextColorB,  buttonBackgroundColorR,  buttonBackgroundColorG,  buttonBackgroundColorB,  respData);
    }

    @Override
    public int lcd_createList(int posX, int posY, int numOfColumns, int numOfRows, int fontDesignation, int fontID, boolean verticalScrollArrowsVisible, boolean borderedListItems, boolean borderdScrollArrows, boolean touchSensitive, boolean automaticScrolling, ResDataStruct respData) {
        return getSDKInstance().lcd_createList( posX,  posY,  numOfColumns,  numOfRows,  fontDesignation,  fontID,  verticalScrollArrowsVisible,  borderedListItems,  borderdScrollArrows,  touchSensitive,  automaticScrolling,  respData);
    }

    @Override
    public int lcd_addItemToList(byte[] listGraphicsID, String itemName, String itemID, boolean selected) {
        return getSDKInstance().lcd_addItemToList(listGraphicsID,  itemName,  itemID,  selected) ;
    }

    @Override
    public int lcd_getSelectedListItem(byte[] listGraphicsID, String itemID, ResDataStruct respData) {
        return getSDKInstance().lcd_getSelectedListItem( listGraphicsID,  itemID,  respData);
    }

    @Override
    public int lcd_clearEventQueue() {
        return getSDKInstance().lcd_clearEventQueue();
    }

    @Override
    public int lcd_getInputEvent(int timeout, ResDataStruct respData) {
        return getSDKInstance().lcd_getInputEvent( timeout,  respData) ;
    }

    @Override
    public int lcd_createInputField(byte[] specs, ResDataStruct respData) {
        return getSDKInstance().lcd_createInputField(specs,  respData);
    }

    @Override
    public int lcd_getInputFieldValue(byte[] listGraphicsID, ResDataStruct respData) {
        return getSDKInstance().lcd_getInputFieldValue( listGraphicsID,  respData) ;
    }

    @Override
    public void stopWaitingTask() {
        getSDKInstance().stopWaitingTask();
    }

    @Override
    public boolean setWaitingMSRResponseTimeout(int timeoutValue) {
        return getSDKInstance().setWaitingMSRResponseTimeout( timeoutValue );
    }

    @Override
    public boolean setWaitingExchangeAPDUResponseTimeout(int timeoutValue) {
        return getSDKInstance().setWaitingExchangeAPDUResponseTimeout( timeoutValue);
    }

    @Override
    public boolean setWaitingPINResponseTimeout(int timeoutValue) {
        return getSDKInstance().setWaitingPINResponseTimeout( timeoutValue);
    }

    @Override
    public void setEMVListener(OnReceiverListener callback) {
        getSDKInstance().setEMVListener( callback) ;
    }

    @Override
    public ReaderInfo.SupportStatus getSupportStatus(ReaderInfo.DEVICE_TYPE readerType) {
        return getSDKInstance().getSupportStatus(readerType) ;
    }

    @Override
    public Map<String, byte[]> getUniPayEncryptedTags() {
        return getSDKInstance().getUniPayEncryptedTags();
    }

    @Override
    public Map<String, byte[]> getUniPayMaskedTags() {
        return getSDKInstance().getUniPayMaskedTags();
    }

    @Override
    public void startUniPayEMV() {
        getSDKInstance().startUniPayEMV();
    }

    @Override
    public void completeUniPayEMV() {
        getSDKInstance().completeUniPayEMV();
    }

    @Override
    public void endUniPayEMV() {
        getSDKInstance().endUniPayEMV();
    }

    @Override
    public void setEncryptedUniPay(boolean isEncrypted) {
        getSDKInstance().setEncryptedUniPay(isEncrypted);
    }

    @Override
    public byte[] parseR_APDU(byte[] data) {
        return getSDKInstance().parseR_APDU( data);
    }

}
