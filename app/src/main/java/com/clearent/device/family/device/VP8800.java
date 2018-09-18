package com.clearent.device.family.device;

import com.idtechproducts.device.ICCReaderStatusStruct;
import com.idtechproducts.device.IDTMSRData;
import com.idtechproducts.device.ReaderInfo;
import com.idtechproducts.device.ResDataStruct;
import com.idtechproducts.device.StructConfigParameters;
import com.idtechproducts.device.audiojack.tools.FirmwareUpdateTool;

import java.util.Map;

//TODO Add Swagger
public interface VP8800 {
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

    int device_calibrateParameters(byte delta);

    int device_getDriveFreeSpace(StringBuilder freeSpace, StringBuilder usedSpace);

    int device_listDirectory(String directoryName, boolean recursive, boolean onSD, StringBuilder directory);

    int device_createDirectory(String directoryName);

    int device_deleteDirectory(String directoryName);

    int device_deleteFile(String fileName);

    int device_enhancedPassthrough(byte[] data);

    int device_controlIndicator(byte indicator, boolean enable);

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

    int emv_removeAllCRL();

    int emv_retrieveExceptionList(ResDataStruct respData);

    int emv_setException(byte[] exception);

    int emv_removeException(byte[] exception);

    int emv_removeAllExceptions();

    int emv_retrieveExceptionLogStatus(ResDataStruct respData);

    int emv_retrieveTransactionLogStatus(ResDataStruct respData);

    int emv_retrieveTransactionLog(ResDataStruct respData);

    int emv_removeTransactionLog();

    int emv_retrieveCRLStatus(ResDataStruct respData);

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

    int ctls_resetConfigurationGroup(int group);

    int ctls_displayOnlineAuthResult(boolean isOK, byte[] tlv);

    int pin_getEncryptedOnlinePIN(int keyType, int timeout);

    int pin_getPAN(int getCSC, int timeout);

    int pin_promptCreditDebit(byte currencySymbol, String displayAmount, int timeout, ResDataStruct respData);

    int ws_requestCSR(ResDataStruct respData);

    int ws_loadSSLCert(String name, String dataDER);

    int ws_revokeSSLCert(String name);

    int ws_deleteSSLCert(String name);

    int ws_getCertChainType(ResDataStruct respData);

    int ws_updateRootCertificate(String name, String dataDER, String signature);

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
}
