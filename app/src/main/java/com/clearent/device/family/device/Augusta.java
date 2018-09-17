package com.clearent.device.family.device;

import com.idtechproducts.device.APDUResponseStruct;
import com.idtechproducts.device.ICCReaderStatusStruct;
import com.idtechproducts.device.ICCSettingStruct;
import com.idtechproducts.device.IDT_Device;
import com.idtechproducts.device.MSRSettingStruct;
import com.idtechproducts.device.ReaderInfo;
import com.idtechproducts.device.ResDataStruct;
import com.idtechproducts.device.StructConfigParameters;
import com.idtechproducts.device.audiojack.tools.FirmwareUpdateTool;

import java.util.Map;

public interface Augusta {

    boolean device_setDeviceType(ReaderInfo.DEVICE_TYPE deviceType, boolean isTTK, boolean isSRED, boolean isThales);

    boolean device_isTTK();

    boolean device_isThales();

    boolean device_isSRED();

    boolean device_setDeviceType(ReaderInfo.DEVICE_TYPE deviceType);

    int device_getDRS(ResDataStruct respData);

    int device_verifyBackdoorKey();

    int device_selfCheck();

    int device_rebootDevice();

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

    void config_setXMLFileNameWithPath(String path);

    boolean config_loadingConfigurationXMLFile(boolean updateAutomatically);

    boolean device_connectWithProfile(StructConfigParameters profile);

    boolean device_isConnected();

    int device_startRKI();

    int autoConfig_start(String strXMLFilename);

    void autoConfig_stop();

    int device_getFirmwareVersion(StringBuilder version);

    int device_controlBeep(int index, int frequency, int duration);

    int device_controlLED(byte indexLED, byte control, int intervalOn, int intervalOff);

    int device_controlLED_ICC(int controlMode, int interval);

    int device_setDateTime(byte[] mac);

    int device_getKeyStatus(ResDataStruct respData);

    int config_setBeeperController(boolean firmwareControlBeeper);

    int config_setLEDController(boolean firmwareControlMSRLED, boolean firmwareControlICCLED);

    int config_setEncryptionControl(byte Encryption);

    int config_setEncryptionControl(boolean msr, boolean icc);

    int config_getEncryptionControl(ResDataStruct respData);

    int icc_setKeyTypeForICCDUKPT(byte encryption);

    int icc_getKeyTypeForICCDUKPT(ResDataStruct respData);

    int icc_setKeyFormatForICCDUKPT(byte encryption);

    int icc_getKeyFormatForICCDUKPT(ResDataStruct respData);

    int icc_enable(boolean withNotification);

    int icc_disable();

    int icc_getFunctionStatus(ResDataStruct respData);

    int emv_getEMVKernelVersion(StringBuilder version);

    int emv_getEMVKernelCheckValue(ResDataStruct respData);

    int emv_getEMVConfigurationCheckValue(ResDataStruct respData);

    int emv_removeAllApplicationData();

    int emv_removeAllCAPK();

    int emv_removeAllCRL();

    int emv_retrieveTransactionResult(byte[] tags, Map<String, Map<String, byte[]>> retrievedTags);

    int config_getSerialNumber(StringBuilder serialNumber);

    int config_getModelNumber(StringBuilder modNumber);

    int icc_getAPDU_KSN(byte KeyNameIndex, byte[] KeySlot, ResDataStruct resKSN);

    String device_getResponseCodeString(int errorCode);

    int device_sendDataCommand(String cmd, boolean calcLRC, String data, ResDataStruct respData, int timeout);

    int device_sendDataCommand(String cmd, boolean calcLRC, String data, ResDataStruct respData);

    int icc_getICCReaderStatus(ICCReaderStatusStruct ICCStatus);

    int icc_powerOnICC(ResDataStruct atrPPS);

    int icc_reviewAllSetting(ICCSettingStruct iccSetting);

    int icc_passthroughOnICC();

    int icc_passthroughOffICC();

    int icc_powerOffICC(ResDataStruct respData);

    int icc_exchangeAPDU(byte[] dataAPDU, APDUResponseStruct response);

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

    void emv_lcdControlResponse(byte mode, byte data);

    int emv_authenticateTransaction(byte[] tags);

    int emv_completeTransaction(boolean commError, byte[] authCode, byte[] iad, byte[] tlvScripts, byte[] tags);

    int msr_reviewAllSetting(MSRSettingStruct msrSetting);

    int msr_defaultAllSetting();

    int msr_getSingleSetting(byte funcID, byte[] response);

    int msr_setSingleSetting(byte funcID, byte setData);

    int msr_cancelMSRSwipe();

    int msr_startMSRSwipe();

    int msr_startMSRSwipe(int timeout);

    int msr_setExpirationMask(boolean mask);

    int msr_getExpirationMask(ResDataStruct respData);

    int msr_setClearPANID(byte value);

    int msr_getClearPANID(ResDataStruct respData);

    int msr_getSwipeForcedEncryptionOption(ResDataStruct respData);

    int msr_setSwipeForcedEncryptionOption(boolean track1, boolean track2, boolean track3, boolean track3card0);

    int msr_getSwipeMaskOption(ResDataStruct respData);

    int msr_setSwipeMaskOption(boolean track1, boolean track2, boolean track3);

    int msr_getSetting(byte setting, ResDataStruct respData);

    int msr_setSetting(byte setting, byte val);

    int msr_setSwipeEncryption(byte encryption);

    int msr_getSwipeEncryption(ResDataStruct respData);

    int msr_enableBufferMode(boolean isBufferMode, boolean withNotification);

    int msr_disable();

    int msr_setWhiteList(byte[] val);

    int msr_RetrieveWhiteList(ResDataStruct respData);

    int msr_getFunctionStatus(ResDataStruct respData);

    int ctls_startTransaction();

    int ctls_cancelTransaction();
}
