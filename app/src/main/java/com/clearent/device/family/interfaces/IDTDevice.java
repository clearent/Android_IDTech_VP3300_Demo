package com.clearent.device.family.interfaces;

import android.annotation.SuppressLint;

import com.idtechproducts.device.APDUResponseStruct;
import com.idtechproducts.device.Common;
import com.idtechproducts.device.ICCReaderStatusStruct;
import com.idtechproducts.device.ICCSettingStruct;
import com.idtechproducts.device.IDTMSRData;
import com.idtechproducts.device.MSRSettingStruct;
import com.idtechproducts.device.OnReceiverListener;
import com.idtechproducts.device.PowerOnStructure;
import com.idtechproducts.device.ReaderInfo;
import com.idtechproducts.device.ResDataStruct;
import com.idtechproducts.device.StructConfigParameters;
import com.idtechproducts.device.USBBypassListener;
import com.idtechproducts.device.audiojack.tasks.TaskManager;
import com.idtechproducts.device.audiojack.tools.FirmwareUpdateTool;

import java.util.Map;

//TODO delete kept for code review
interface IDTDevice {
    void btle_WakeUp();

    void setIDT_Device(FirmwareUpdateTool fwTool);

    boolean device_setDeviceType(ReaderInfo.DEVICE_TYPE deviceType);

    boolean device_setDeviceType(ReaderInfo.DEVICE_TYPE deviceType, int PID);

    boolean device_setDeviceType(ReaderInfo.DEVICE_TYPE deviceType, boolean isSRED);

    boolean device_setDeviceType(ReaderInfo.DEVICE_TYPE deviceType, boolean isTTK, boolean isSRED);

    boolean device_setDeviceType(ReaderInfo.DEVICE_TYPE deviceType, boolean isTTK, boolean isSRED, boolean isThales);

    boolean device_setDeviceType(ReaderInfo.DEVICE_TYPE deviceType, boolean isTTK, boolean isSRED, boolean isThales, int PID);

    ReaderInfo.DEVICE_TYPE device_getDeviceType();

    int device_setDisplayReady();

    void registerListen();

    void unregisterListen();

    void release();

    String config_getXMLVersionInfo();

    String phone_getInfoManufacture();

    String phone_getInfoModel();

    void log_setVerboseLoggingEnable(boolean enableShowLog);

    String log_exportLogs();

    void log_setSaveLogEnable(boolean enable);

    int log_deleteLogs();

    void config_setXMLFileNameWithPath(String xmlFilename);

    boolean config_loadingConfigurationXMLFile(boolean updateAutomatically);

    boolean device_connect();

    void device_ConnectWithoutValidation(boolean noValidate, ReaderInfo.DEVICE_TYPE deviceType);

    void device_disconnect();

    boolean device_isConnected();

    boolean device_connectWithProfile(StructConfigParameters profile);

    boolean emv_getDisplayMessage(int timeout, long startTime, ResDataStruct resData);

    @Deprecated
    boolean msr_isSwipeCardRunning();

    int device_startRKI();

    void setBypassListener(USBBypassListener callback);

    void clearBypassListener();

    com.idtechproducts.device.IDT_Device getSDKInstance();

    void externalConnect();

    void externalDisconnect();

    void externalDeviceNotFound();

    TaskManager.TaskStartRet sendAudioCommand_helper(String cmd, int timeout, ResDataStruct respData);

    String config_getSDKVersion();

    int device_getFirmwareVersion(StringBuilder version);

    byte[] getFirmwareVersionForTTK();

    int device_updateFirmware(String[] commands);

    int device_controlBeep(int index, int frequency, int duration);

    int device_controlLED(byte indexLED, byte control, int intervalOn, int intervalOff);

    int device_controlLED_ICC(int controlMode, int interval);

    int device_getTransactionResults(IDTMSRData cardData);

    int device_selfCheck();

    int device_pingDevice();

    int device_setBurstMode(byte mode);

    int device_setPollMode(byte mode);

    int device_setBluetoothParameters(String name, byte[] oldPassword, byte[] newPassword);

    int device_setSource(byte[] data);

    int device_getSource(byte[] data);

    int device_controlUserInterface(byte[] values);

    int device_startTransaction(double amount, double amtOther, int type, int timeout, byte[] tags);

    int device_cancelTransaction();

    int device_calibrateParameters(byte delta);

    int device_getDriveFreeSpace(StringBuilder freeSpace, StringBuilder usedSpace);

    int device_listDirectory(String directoryName, boolean recursive, boolean onSD, StringBuilder directory);

    int device_createDirectory(String directoryName);

    int device_deleteDirectory(String directoryName);

    int device_deleteFile(String fileName);

    int device_enhancedPassthrough(byte[] data);

    int device_controlIndicator(byte indicator, boolean enable);

    int config_getSerialNumber(StringBuilder serialNumber);

    int config_getModelNumber(StringBuilder modNumber);

    int device_getKSN(ResDataStruct resKSN);

    int device_getKSN(byte keyNameIndex, byte[] keySlot, ResDataStruct resKSN);

    int device_ReviewAudioJackSetting(ResDataStruct respData);

    int device_enableTDES(ResDataStruct respData);

    int device_enableAES(ResDataStruct respData);

    int device_calibrateReader(ResDataStruct respData);

    int device_enableErrorNotification(ResDataStruct respData, boolean enable);

    int device_enableExpDate(ResDataStruct respData, boolean enable);

    int device_enableForceEncryption(ResDataStruct respData, boolean enable);

    @SuppressLint({"SimpleDateFormat"})
    int device_setDateTime(byte[] mac);

    int device_setRTCDateTime(byte[] dateTime);

    int device_getRTCDateTime(byte[] dateTime);

    @SuppressLint({"SimpleDateFormat"})
    int device_wakeup();

    int device_setMerchantRecord(int index, boolean enabled, String merchantID, String merchantURL);

    int device_getMerchantRecord(int index, ResDataStruct respData);

    int device_getKeyStatus(ResDataStruct respData);

    int device_getDRS(ResDataStruct respData);

    int device_verifyBackdoorKey();

    void setfoundBlockedApplication(boolean found);

    boolean foundBlockedApplication();

    int icc_getICCReaderStatus(ICCReaderStatusStruct ICCStatus);

    int icc_getICCReaderStatus_LEDControl(ICCReaderStatusStruct ICCStatus, Common.EMV_TRANSACTION_STAGE transactionStage);

    int icc_powerOnICC(PowerOnStructure options, ResDataStruct atrPPS);

    int icc_powerOnICC(ResDataStruct atrPPS);

    int icc_getATR();

    int icc_powerOffICC();

    int icc_powerOffICC(ResDataStruct respData);

    int icc_passthroughOnICC();

    int icc_passthroughOnICCforAPDU();

    int icc_passthroughOffICC();

    int icc_passthroughOffICCforAPDU();

    int config_getDateTime(StringBuilder dateTime);

    int config_setLEDControl(byte MSRLedOption, byte ICCLedOption);

    int config_setLEDController(boolean firmwareControlMSRLED, boolean firmwareControlICCLED);

    int config_getLEDController(ResDataStruct respData);

    int config_setBeeperControl(byte BeeperOption);

    int config_setBeeperController(boolean firmwareControlBeeper);

    int config_getBeeperControl(ResDataStruct respData);

    int config_beepersControl(int beeperIndex, int frequency, int duration);

    int config_getEncryptionControl(ResDataStruct respData);

    int config_setEncryptionControl(byte Encryption);

    int config_setEncryptionControl(boolean msr, boolean icc);

    int config_setBLEParameters(String url, String uid, String nameSpaceId, String instanceId);

    int config_setLongTermPrivateKey(byte[] encryptedKeyData, byte[] plainDataHash);

    int device_getBatteryVoltage(StringBuilder batteryInfo);

    int device_rebootDevice();

    int device_sendCommandDirectIO(String hexCommand, ResDataStruct respData);

    int device_sendDataCommand(String cmd, boolean calcLRC, String data, ResDataStruct respData);

    int device_sendDataCommand(String cmd, boolean calcLRC, String data, ResDataStruct respData, int timeout);

    String device_getResponseCodeString(int errorCode);

    void cancelCurrentCommand();

    int icc_exchangeAPDU(byte[] dataAPDU, APDUResponseStruct response);

    int icc_exchangeAPDU(byte[] dataAPDU, ResDataStruct response);

    int icc_exchangeEncryptedAPDU(byte[] dataAPDU, byte[] ksn, APDUResponseStruct response);

    int icc_exchangeMultiAPDU(byte[] dataAPDU, ResDataStruct respData);

    int icc_getAPDU_KSN(ResDataStruct resKSN);

    int icc_getAPDU_KSN(byte KeyNameIndex, byte[] KeySlot, ResDataStruct resKSN);

    int icc_setKeyTypeForICCDUKPT(byte encryption);

    int icc_getKeyTypeForICCDUKPT(ResDataStruct respData);

    int icc_setKeyFormatForICCDUKPT(byte encryption);

    int icc_getKeyFormatForICCDUKPT(ResDataStruct respData);

    int icc_enable(boolean withNotification);

    int icc_disable();

    int icc_getFunctionStatus(ResDataStruct respData);

    int icc_enableNotification(boolean enableNotifyICCStatus);

    int icc_defaultSetting();

    int icc_reviewAllSetting(ICCSettingStruct iccSetting);

    int ctls_retrieveApplicationData(String aid, ResDataStruct respData);

    int emv_retrieveApplicationData(String aid, ResDataStruct respData);

    int ctls_removeApplicationData(String aidFile, ResDataStruct respData);

    int emv_removeApplicationData(String aidFile, ResDataStruct respData);

    int ctls_removeAllApplicationData();

    int emv_removeAllApplicationData();

    int ctls_setApplicationData(byte[] tlv, ResDataStruct respData);

    int ctls_setConfigurationGroup(byte[] TLV, ResDataStruct respData);

    int ctls_setGlobalConfiguration(byte[] TLV, ResDataStruct respData);

    int ctls_getConfigurationGroup(int group, ResDataStruct respData);

    int ctls_getAllConfigurationGroups(ResDataStruct respData);

    int ctls_removeConfigurationGroup(int group);

    int ctls_resetConfigurationGroup(int group);

    int ctls_displayOnlineAuthResult(boolean isOK, byte[] tlv);

    int emv_setApplicationData(String name, byte[] tlv, ResDataStruct respData);

    int ctls_retrieveAidList(ResDataStruct respData);

    int emv_retrieveAidList(ResDataStruct respData);

    int ctls_retrieveTerminalData(ResDataStruct respData);

    int emv_retrieveTerminalData(ResDataStruct respData);

    int emv_removeTerminalData(ResDataStruct respData);

    int ctls_setTerminalData(byte[] TLV, ResDataStruct respData);

    int emv_setTerminalData(byte[] TLV, ResDataStruct respData);

    int ctls_retrieveCAPK(byte[] capk, ResDataStruct respData);

    int emv_retrieveCAPK(byte[] capk, ResDataStruct respData);

    int ctls_removeCAPK(byte[] capk, ResDataStruct respData);

    int emv_removeCAPK(byte[] capk, ResDataStruct respData);

    int ctls_setCAPK(byte[] data, ResDataStruct respData);

    int emv_setCAPK(byte[] data, ResDataStruct respData);

    int ctls_retrieveCAPKList(ResDataStruct respData);

    int emv_retrieveCAPKList(ResDataStruct respData);

    int ctls_removeAllCAPK();

    int emv_removeAllCAPK();

    int emv_retrieveCRL(ResDataStruct respData);

    int emv_removeCRL(byte[] crlList, ResDataStruct respData);

    int emv_removeAllCRL();

    int emv_retrieveCRLStatus(ResDataStruct respData);

    int emv_retrieveExceptionList(ResDataStruct respData);

    int emv_setException(byte[] exception);

    int emv_removeException(byte[] exception);

    int emv_removeAllExceptions();

    int emv_retrieveExceptionLogStatus(ResDataStruct respData);

    int emv_removeTransactionLog();

    int emv_retrieveTransactionLogStatus(ResDataStruct respData);

    int emv_retrieveTransactionLog(ResDataStruct respData);

    int icc_removeCRLByRIDIndex(byte[] data, ResDataStruct respData);

    int emv_setCRL(byte[] crlList, ResDataStruct respData);

    int icc_getInterfaceDeviceSerialNumber(ResDataStruct respData);

    int icc_setInterfaceDeviceSerialNumber(byte[] data, ResDataStruct respData);

    int icc_getTerminalId(ResDataStruct respData);

    int icc_setTerminalId(byte[] data, ResDataStruct respData);

    void waitForOtherCommands();

    @SuppressLint({"SimpleDateFormat"})
    int emv_startTransaction(double amount, double amtOther, int type, int timeout, byte[] tags, boolean forceOnline);

    void device_enableAutoPollEMV(boolean enabled, double amount, double amtOther, int type, int timeout, byte[] tags);

    void emv_setTransactionParameters(double amount, double amtOther, int type, int timeout, byte[] tags);

    void emv_secondResponse(int timeout, int mode);

    int emv_callbackResponseMSR(byte[] MSR);

    int emv_callbackResponsePIN(int mode, byte[] KSN, byte[] PIN);

    int emv_completeTransaction(boolean commError, byte[] authCode, byte[] iad, byte[] tlvScripts, byte[] tags);

    int emv_retrieveTransactionResult(byte[] tags, Map<String, Map<String, byte[]>> retrievedTags);

    int emv_cancelTransaction(ResDataStruct respData);

    void emv_lcdControlResponse(byte mode, byte selection);

    int emv_getEMVKernelVersion(StringBuilder version);

    int emv_getEMVKernelCheckValue(ResDataStruct respData);

    int emv_getEMVConfigurationCheckValue(ResDataStruct respData);

    int emv_authenticateTransaction(byte[] tags);

    int device_setPMCStatus(byte[] idleSleepTime, ResDataStruct respData);

    int device_getPMCStatus(ResDataStruct respData);

    int device_shutOffPower(ResDataStruct respData);

    int msr_reviewAllSetting(MSRSettingStruct msrSetting);

    int device_reviewAllSetting(ResDataStruct respData);

    int msr_reviewSecurityLevel(byte[] secSetting);

    int msr_setDecodingMethod(int method);

    int msr_setKeyManagement(boolean fixed);

    int msr_selectMagneticTrack(int trackMode);

    int msr_setTrackSeparator(int sep, String zz);

    int msr_setTerminator(int ter, String zz);

    int msr_setTrackPrefix(int track, String zz);

    int msr_setTrackSuffix(int track, String zz);

    int msr_showTrackPrefixMessage(int track);

    int msr_showTrackSuffixMessage(int track);

    int msr_changeToDefault();

    int msr_setPreamble(String zz);

    int msr_setPostamble(String zz);

    int msr_defaultAllSetting();

    int msr_getSingleSetting(byte funcID, byte[] response);

    int msr_setSingleSetting(byte funcID, byte setData);

    int ctls_startTransaction();

    int msr_startTransaction(double amount, double amtOther, int type, int timeout, byte[] tags);

    @SuppressLint({"SimpleDateFormat"})
    int ctls_startTransaction(double amount, double amtOther, int type, int timeout, byte[] tags);

    @SuppressLint({"SimpleDateFormat"})
    int msr_startMSRSwipe();

    @SuppressLint({"SimpleDateFormat"})
    int msr_startMSRSwipe(int timeout);

    int msr_startMSRSwipeWithDisplay(String line1, String line2, String line3);

    int msr_setExpirationMask(boolean mask);

    int msr_getExpirationMask(ResDataStruct respData);

    int msr_setClearPANID(byte value);

    int msr_getSwipeForcedEncryptionOption(ResDataStruct respData);

    int msr_setSwipeForcedEncryptionOption(boolean track1, boolean track2, boolean track3, boolean track3card0);

    int msr_getSwipeMaskOption(ResDataStruct respData);

    int msr_setSwipeMaskOption(boolean track1, boolean track2, boolean track3);

    int msr_getClearPANID(ResDataStruct respData);

    int msr_getSetting(byte setting, ResDataStruct respData);

    int msr_setSetting(byte setting, byte val);

    int msr_setSetting(byte setting, byte[] val);

    int msr_setSwipeEncryption(byte encryption);

    int msr_getSwipeEncryption(ResDataStruct respData);

    int msr_enableBufferMode(boolean isBufferMode, boolean withNotification);

    int msr_disable();

    int msr_setWhiteList(byte[] val);

    int msr_RetrieveWhiteList(ResDataStruct respData);

    int msr_getFunctionStatus(ResDataStruct respData);

    int msr_flushTrackData();

    int ctls_cancelTransaction();

    int msr_cancelMSRSwipe();

    int felica_authentication(byte[] key);

    int felica_readWithMac(int blockCnt, byte[] blockList, ResDataStruct respData);

    int felica_writeWithMac(byte blockNum, byte[] blockData);

    int felica_read(byte[] serviceCodeList, int blockCnt, byte[] blockList, ResDataStruct respData);

    int felica_write(byte[] serviceCodeList, int blockCnt, byte[] blockList, byte[] blockData, ResDataStruct respData);

    int felica_poll(byte[] systemCode, ResDataStruct respData);

    int felica_requestService(byte[] nodeCode, ResDataStruct respData);

    int pin_getEncryptedOnlinePIN(int keyType, int timeout);

    int pin_getPAN(int getCSC, int timeout);

    int pin_promptCreditDebit(byte currencySymbol, String displayAmount, int timeout, ResDataStruct respData);

    int ws_requestCSR(ResDataStruct respData);

    int ws_loadSSLCert(String name, String dataDER);

    int ws_revokeSSLCert(String name);

    int ws_deleteSSLCert(String name);

    int ws_getCertChainType(ResDataStruct respData);

    int ws_updateRootCertificate(String name, String dataDER, String signature);

    int pin_cancelPINEntry();

    int pin_displayMessageGetEncryptedPIN(byte PANKeyType, byte[] PAN, byte PINMaxLen, byte PINMinLen, byte[] LCDMsg, ResDataStruct respData);

    int pin_getFunctionKey(ResDataStruct respData);

    int pin_displayMessageGetNumericKey(byte DisplayFlag, byte KeyMaxLen, byte KeyMinLen, byte[] TextDisplayMsg, byte[] DisplayMsgSig, ResDataStruct respData);

    int pin_displayMessageGetAmount(byte DisplayFlag, byte KeyMaxLen, byte KeyMinLen, byte[] TextDisplayMsg, byte[] DisplayMsgSig, ResDataStruct respData);

    int lcd_resetInitialState();

    int lcd_customDisplayMode(boolean enable);

    int lcd_setForeBackColor(byte[] foreRGB, byte[] backRGB);

    int lcd_clearDisplay();

    int lcd_captureSignature(int timeout);

    int lcd_startSlideShow(String files, int posX, int posY, int posMode, boolean touchEnable, boolean recursion, boolean touchTerminate, int delay, int loops, boolean clearScreen);

    int lcd_cancelSlideShow(ResDataStruct respData);

    int lcd_setDisplayImage(String files, int posX, int posY, int posMode, boolean touchEnable, boolean clearScreen);

    int lcd_setBackgroundImage(String files, boolean enable);

    int lcd_displayText(int posX, int posY, int displayWidth, int displayHeight, int fontDesignation, int fontID, int screenPosition, String displayText, ResDataStruct respData);

    int lcd_displayParagraph(int posX, int posY, int displayWidth, int displayHeight, int fontDesignation, int fontID, int displayProperties, String displayText);

    int lcd_displayButton(int posX, int posY, int buttonWidth, int buttonHeight, int fontDesignation, int fontID, int displayPosition, String buttonLabel, int buttonTextColorR, int buttonTextColorG, int buttonTextColorB, int buttonBackgroundColorR, int buttonBackgroundColorG, int buttonBackgroundColorB, ResDataStruct respData);

    int lcd_createList(int posX, int posY, int numOfColumns, int numOfRows, int fontDesignation, int fontID, boolean verticalScrollArrowsVisible, boolean borderedListItems, boolean borderdScrollArrows, boolean touchSensitive, boolean automaticScrolling, ResDataStruct respData);

    int lcd_addItemToList(byte[] listGraphicsID, String itemName, String itemID, boolean selected);

    int lcd_getSelectedListItem(byte[] listGraphicsID, String itemID, ResDataStruct respData);

    int lcd_clearEventQueue();

    int lcd_getInputEvent(int timeout, ResDataStruct respData);

    int lcd_createInputField(byte[] specs, ResDataStruct respData);

    int lcd_getInputFieldValue(byte[] listGraphicsID, ResDataStruct respData);

    int autoConfig_start(String strXMLFilename);

    void autoConfig_stop();

    void stopWaitingTask();

    boolean setWaitingMSRResponseTimeout(int timeoutValue);

    boolean setWaitingExchangeAPDUResponseTimeout(int timeoutValue);

    boolean setWaitingPINResponseTimeout(int timeoutValue);

    void setEMVListener(OnReceiverListener callback);

    ReaderInfo.SupportStatus getSupportStatus(ReaderInfo.DEVICE_TYPE readerType);

    Map<String, byte[]> getUniPayEncryptedTags();

    Map<String, byte[]> getUniPayMaskedTags();

    void startUniPayEMV();

    void completeUniPayEMV();

    void endUniPayEMV();

    void setEncryptedUniPay(boolean isEncrypted);

    byte[] parseR_APDU(byte[] data);
}
