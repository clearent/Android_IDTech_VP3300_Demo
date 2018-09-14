package com.clearent.device.family.vivopay.vp3300;

import com.clearent.device.Configurable;
import com.clearent.device.HasDeviceMetadata;
import com.clearent.device.HasServerCommunication;
import com.clearent.device.HasSwipeSupport;
import com.clearent.device.HasTokenizingSupport;
import com.clearent.device.TransactionTokenNotifier;
import com.idtechproducts.device.ICCReaderStatusStruct;
import com.idtechproducts.device.IDTMSRData;
import com.idtechproducts.device.ReaderInfo;
import com.idtechproducts.device.ResDataStruct;
import com.idtechproducts.device.StructConfigParameters;
import com.idtechproducts.device.audiojack.tools.FirmwareUpdateTool;

import java.util.Map;

//TODO look in idtech docs and see if there is anything different between a 3300 and say a 3310.
// or did we just make a universal interface ?
public interface VP3300Device extends TransactionTokenNotifier, Configurable, HasServerCommunication, HasDeviceMetadata, HasTokenizingSupport, HasSwipeSupport {

    boolean device_setDeviceType(ReaderInfo.DEVICE_TYPE deviceType);

    void setIDT_Device(FirmwareUpdateTool fwTool);

    ReaderInfo.DEVICE_TYPE device_getDeviceType();

    void registerListen();

    void unregisterListen();

    void release();

    String config_getSDKVersion();

    String config_getXMLVersionInfo();

    String phone_getInfoManufacture();

    String phone_getInfoModel();

    void log_setVerboseLoggingEnable(boolean enable);

    void log_setSaveLogEnable(boolean enable);

    int log_deleteLogs();

    int emv_callbackResponsePIN(int mode, byte[] KSN, byte[] PIN);

    void config_setXMLFileNameWithPath(String path);

    boolean config_loadingConfigurationXMLFile(boolean updateAutomatically);

    boolean device_connectWithProfile(StructConfigParameters profile);

    void device_ConnectWithoutValidation(boolean noValidate);

    boolean device_connect();

    boolean device_isConnected();

    int device_startRKI();

    int autoConfig_start(String strXMLFilename);

    void autoConfig_stop();

    int device_startTransaction(double amount, double amtOther, int type, int timeout, byte[] tags);

    int device_cancelTransaction();

    int device_getFirmwareVersion(StringBuilder version);

    int device_pingDevice();

    int device_ReviewAudioJackSetting(ResDataStruct respData);

    int config_getSerialNumber(StringBuilder serialNumber);

    int config_getModelNumber(StringBuilder modNumber);

    int device_getKSN(ResDataStruct ksn);

    int device_setMerchantRecord(int index, boolean enabled, String merchantID, String merchantURL);

    int device_getMerchantRecord(int index, ResDataStruct respData);

    String device_getResponseCodeString(int errorCode);

    int device_sendDataCommand(String cmd, boolean calcLRC, String data, ResDataStruct respData);

    int device_sendDataCommand(String cmd, boolean calcLRC, String data, ResDataStruct respData, int timeout);

    int device_updateFirmware(String[] commands);

    int device_getTransactionResults(IDTMSRData cardData);

    int device_setBurstMode(byte mode);

    int device_setPollMode(byte mode);

    int device_getRTCDateTime(byte[] dateTime);

    int device_setRTCDateTime(byte[] dateTime);

    int icc_getICCReaderStatus(ICCReaderStatusStruct ICCStatus);

    int icc_powerOnICC(ResDataStruct atrPPS);

    int icc_passthroughOnICC();

    int icc_passthroughOffICC();

    int icc_powerOffICC(ResDataStruct respData);

    int icc_exchangeAPDU(byte[] dataAPDU, ResDataStruct response);

    int emv_getEMVKernelVersion(StringBuilder version);

    int emv_getEMVKernelCheckValue(ResDataStruct respData);

    int emv_getEMVConfigurationCheckValue(ResDataStruct respData);

    int emv_retrieveApplicationData(String aid, ResDataStruct respData);

    int emv_removeApplicationData(String aid, ResDataStruct respData);

    int emv_setApplicationData(String aid, byte[] TLV, ResDataStruct respData);

    int emv_retrieveTerminalData(ResDataStruct respData);

    int emv_removeTerminalData(ResDataStruct respData);

    int emv_setTerminalData(byte[] TLV, ResDataStruct respData);

    int emv_retrieveAidList(ResDataStruct respData);

    int emv_retrieveCAPK(byte[] data, ResDataStruct respData);

    int emv_removeCAPK(byte[] capk, ResDataStruct respData);

    int emv_setCAPK(byte[] key, ResDataStruct respData);

    int emv_retrieveCAPKList(ResDataStruct respData);

    int emv_retrieveCRL(ResDataStruct respData);

    int emv_removeCRL(byte[] crlList, ResDataStruct respData);

    int emv_setCRL(byte[] crlList, ResDataStruct respData);

    int emv_startTransaction(double amount, double amtOther, int type, int timeout, byte[] tags, boolean forceOnline);

    int emv_cancelTransaction(ResDataStruct respData);

    void emv_setTransactionParameters(double amount, double amtOther, int type, int timeout, byte[] tags);

    void emv_lcdControlResponse(byte mode, byte data);

    int emv_authenticateTransaction(byte[] tags);

    int emv_completeTransaction(boolean commError, byte[] authCode, byte[] iad, byte[] tlvScripts, byte[] tags);

    int emv_retrieveTransactionResult(byte[] tags, Map<String, Map<String, byte[]>> retrievedTags);

    int device_reviewAllSetting(ResDataStruct respData);

    int msr_defaultAllSetting();

    int msr_getSingleSetting(byte funcID, byte[] response);

    int msr_setSingleSetting(byte funcID, byte setData);

    int msr_cancelMSRSwipe();

    int msr_startMSRSwipe();

    int msr_startMSRSwipe(int timeout);

    int ctls_retrieveApplicationData(String aid, ResDataStruct respData);

    int ctls_removeApplicationData(String aid, ResDataStruct respData);

    int ctls_setApplicationData(byte[] TLV, ResDataStruct respData);

    int ctls_setConfigurationGroup(byte[] TLV, ResDataStruct respData);

    int ctls_getConfigurationGroup(int group, ResDataStruct respData);

    int ctls_getAllConfigurationGroups(ResDataStruct respData);

    int ctls_removeConfigurationGroup(int group);

    int ctls_retrieveTerminalData(ResDataStruct respData);

    int ctls_setTerminalData(byte[] TLV, ResDataStruct respData);

    int ctls_retrieveAidList(ResDataStruct respData);

    int ctls_retrieveCAPK(byte[] data, ResDataStruct respData);

    int ctls_removeCAPK(byte[] capk, ResDataStruct respData);

    int ctls_setCAPK(byte[] key, ResDataStruct respData);

    int ctls_retrieveCAPKList(ResDataStruct respData);

    int ctls_removeAllApplicationData();

    int ctls_removeAllCAPK();

    int ctls_startTransaction(double amount, double amtOther, int type, int timeout, byte[] tags);

    int ctls_cancelTransaction();

    void notifyFailure(String message);

    void setDeviceSerialNumber();

    void setKernelVersion();

    void setFirmwareVersion();

    boolean isConfigured();

    void setConfigured(boolean configured);
}
