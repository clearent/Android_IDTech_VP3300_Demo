package com.clearent.device.family.device;

import com.idtechproducts.device.ICCReaderStatusStruct;
import com.idtechproducts.device.ReaderInfo;
import com.idtechproducts.device.ResDataStruct;
import com.idtechproducts.device.StructConfigParameters;

/**
 * VP3300
 */
//TODO add swagger
public interface VP3300 {

    /**
     * Set the device type :
     *
     * Device Types
     * DEVICE_TYPE.DEVICE_VP3300_AJ,
     * DEVICE_TYPE.DEVICE_VP3300_AJ_USB,
     * DEVICE_TYPE.DEVICE_VP3300_USB,
     * DEVICE_TYPE.DEVICE_VP3300_BT,
     * DEVICE_TYPE.DEVICE_VP3300_BT_USB
     *
     * @param deviceType enum
     * @return
     */
    boolean device_setDeviceType(ReaderInfo.DEVICE_TYPE deviceType);

   // void setIDT_Device(FirmwareUpdateTool fwTool);

    /**
     * Gets type of device
     * @return ReaderInfo.DEVICE_TYPE
     */
    ReaderInfo.DEVICE_TYPE device_getDeviceType();

    /**
     * to enable SDK detect the phone jack plug in/off notification
     */
    void registerListen();

    /**
     * to disable the detect of the phone jack plug in/off notification
     */
    void unregisterListen();

    /**
     * make the SDK in the idle status.
     */
    void release();

    /**
     * Get the version of SDK.
     * @return String
     */
    String config_getSDKVersion();

    //String config_getXMLVersionInfo();

   // String phone_getInfoManufacture();

   // String phone_getInfoModel();

   // void log_setVerboseLoggingEnable(boolean enable);

  //  void log_setSaveLogEnable(boolean enable);

    //int log_deleteLogs();

    /**
     * Callback Response PIN Request Provides PIN data to the kernel after a callback was received pinRequest delegate.
     * @param mode
     * PIN Mode:
     * EMV_PIN_MODE_CANCEL = 0X00,
     * EMV_PIN_MODE_ONLINE_PIN_DUKPT = 0X01,
     * EMV_PIN_MODE_ONLINE_PIN_MKSK = 0X02,
     * EMV_PIN_MODE_OFFLINE_PIN = 0X03
     * @param KSN Key Serial Number. If no pairing and PIN is plaintext, value is nil
     * @param PIN PIN data, encrypted. If no pairing, PIN will be sent plaintext
     * @return success or error code. Values can be parsed with device_getResponseCodeString
     */
    int emv_callbackResponsePIN(int mode, byte[] KSN, byte[] PIN);

    //TODO we will handle this comment these out when android device process is completed
    void config_setXMLFileNameWithPath(String path);

    //TODO we will handle this comment these out when android device process is completed
    boolean config_loadingConfigurationXMLFile(boolean updateAutomatically);

    boolean device_connectWithProfile(StructConfigParameters profile);

   //void device_ConnectWithoutValidation(boolean noValidate);

    //boolean device_connect();

    /**
     * get the status if the device connected.
     * @return
     */
    boolean device_isConnected();

    //int device_startRKI();

    //int autoConfig_start(String strXMLFilename);

    //void autoConfig_stop();

    /**
     * Start Device Transaction Request - Authorizes the MSR (or CTLS) or EMV transaction for an ICC card
     * @param amount  Transaction amount value (tag value 9F02)
     * @param amtOther Other amount value, if any (tag value 9F03)
     * @param type Transaction type (tag value 9C).
     * @param timeout Timeout value in seconds.
     * @param tags Any other tags to be included in the request. Passed as a string. Example, tag 9F0C with amount 0x000000000100 would be "9F0C06000000000100" If tags 9F02 (amount),9F03 (other amount), or 9C (transaction type) are included, they will take priority over these values supplied as individual parameters to this method. Note: To request tags to be included in default response, use tag DFEE1A, and specify tag list. Example four tags 9F02, 9F36, 95, 9F37 to be included in response = DFEE1A079F029F369f9F37
     * @return success or error code. Values can be parsed with device_getResponseCodeString
     */
    int device_startTransaction(double amount, double amtOther, int type, int timeout, byte[] tags);

    /**
     * Cancels the currently executing Device transaction
     * @return success or error code. Values can be parsed with device_getResponseCodeString
     */
    int device_cancelTransaction();

    /**
     * Gets the firmware version
     * @param version
     * @return success or error code. Values can be parsed with device_getResponseCodeString
     */
    int device_getFirmwareVersion(StringBuilder version);

    /**
     * Pings the reader. If connected, returns success. Otherwise, returns timeout.
     * @return success or error code. Values can be parsed with device_getResponseCodeString
     */
    int device_pingDevice();

    //int device_ReviewAudioJackSetting(ResDataStruct respData);

    /**
     * Gets the device serial number.
     * @return success or error code. Values can be parsed with device_getResponseCodeString
     */
    int config_getSerialNumber(StringBuilder serialNumber);

    /**
     * Gets the model number
     * @return success or error code. Values can be parsed with device_getResponseCodeString
     */
    int config_getModelNumber(StringBuilder modNumber);

    //int device_getKSN(ResDataStruct ksn);

   // int device_setMerchantRecord(int index, boolean enabled, String merchantID, String merchantURL);

   // int device_getMerchantRecord(int index, ResDataStruct respData);

    String device_getResponseCodeString(int errorCode);

    /**
     * Sends a Direct Command Sends a NEO IDG ViVOtech 2.0 command
     * @param cmd Two bytes command (including subCommand) as per NEO IDG Reference Guide (UniPayIII)
     * @param calcLRC Not used for IDG devices
     * @param data Command data (if applicable) for IDG devices
     * @param respData Returns response ResDataStruct.resData. Status Code in ResDataStruct.statusCode
     * @return success or error code. Values can be parsed with device_getResponseCodeString
     */
    int device_sendDataCommand(String cmd, boolean calcLRC, String data, ResDataStruct respData);

    /**
     * Sends a Direct Command Sends a NEO IDG ViVOtech 2.0 command
     * @param cmd Two bytes command (including subCommand) as per NEO IDG Reference Guide (UniPayIII)
     * @param calcLRC Not used for IDG devices
     * @param data Command data (if applicable) for IDG devices
     * @param respData Returns response ResDataStruct.resData. Status Code in ResDataStruct.statusCode
     * @param timeout Command timeout
     * @return success or error code. Values can be parsed with device_getResponseCodeString
     */
    int device_sendDataCommand(String cmd, boolean calcLRC, String data, ResDataStruct respData, int timeout);

   // int device_updateFirmware(String[] commands);

  //  int device_getTransactionResults(IDTMSRData cardData);

    /**
     * mode	0 = OFF, 1 = Always On, 2 = Auto Exit
     * @param mode
     * @return success or error code. Values can be parsed with device_getResponseCodeString
     */
    int device_setBurstMode(byte mode);

    /**
     * Sets the poll mode for the device. Auto Poll keeps reader active, Poll On Demand only polls when requested by terminal
     * @param mode
     * @return success or error code. Values can be parsed with device_getResponseCodeString
     */
    int device_setPollMode(byte mode);

    //int device_getRTCDateTime(byte[] dateTime);

    //int device_setRTCDateTime(byte[] dateTime);

    /**
     * Pointer that will return with the ICCReaderStatus results. bit 0: 0 = ICC Power Not Ready, 1 = ICC Powered bit 1: 0 = Card not seated, 1 = card seated
     * @param ICCStatus
     * @return success or error code. Values can be parsed with device_getResponseCodeString
     */
    int icc_getICCReaderStatus(ICCReaderStatusStruct ICCStatus);

    /**
     * Power up the currently selected microprocessor card in the ICC reader. It follows the ISO7816-3 power up sequence and returns the ATR as its response.
     * @param atrPPS please see PowerOnStructure class for more information.
     * @return success or error code. Values can be parsed with device_getResponseCodeString
     */
    int icc_powerOnICC(ResDataStruct atrPPS);

    /**
     * Enables pass through mode for ICC. Required when direct ICC commands are required (power on/off ICC, exchange APDU)
     * @return success or error code. Values can be parsed with device_getResponseCodeString
     */
    int icc_passthroughOnICC();

    /**
     * Disables pass through mode for ICC. Required when executing transactions (start EMV, start MSR, authenticate transaction)
     * @return success or error code. Values can be parsed with device_getResponseCodeString
     */
    int icc_passthroughOffICC();

    /**
     * Powers down the ICC
     * @param respData
     * @return success or error code. Values can be parsed with device_getResponseCodeString
     */
    int icc_powerOffICC(ResDataStruct respData);

    //int icc_exchangeAPDU(byte[] dataAPDU, ResDataStruct response);

    /**
     * Polls the device for the kernel version
     * @param version
     * @return success or error code. Values can be parsed with device_getResponseCodeString
     */
    int emv_getEMVKernelVersion(StringBuilder version);

//    int emv_getEMVKernelCheckValue(ResDataStruct respData);
//
//    int emv_getEMVConfigurationCheckValue(ResDataStruct respData);
//
//    int emv_retrieveApplicationData(String aid, ResDataStruct respData);
//
//    int emv_removeApplicationData(String aid, ResDataStruct respData);
//
//    int emv_setApplicationData(String aid, byte[] TLV, ResDataStruct respData);
//
//    int emv_retrieveTerminalData(ResDataStruct respData);
//
//    int emv_removeTerminalData(ResDataStruct respData);
//
//    int emv_setTerminalData(byte[] TLV, ResDataStruct respData);
//
//    int emv_retrieveAidList(ResDataStruct respData);
//
//    int emv_retrieveCAPK(byte[] data, ResDataStruct respData);
//
//    int emv_removeCAPK(byte[] capk, ResDataStruct respData);
//
//    int emv_setCAPK(byte[] key, ResDataStruct respData);
//
//    int emv_retrieveCAPKList(ResDataStruct respData);
//
//    int emv_retrieveCRL(ResDataStruct respData);
//
//    int emv_removeCRL(byte[] crlList, ResDataStruct respData);
//
//    int emv_setCRL(byte[] crlList, ResDataStruct respData);

    /**
     * Start EMV Transaction Request
     * @param amount Transaction amount value (tag value 9F02)
     * @param amtOther Other amount value, if any (tag value 9F03)
     * @param type Transaction type (tag value 9C).
     * @param timeout Timeout value in seconds.
     * @param tags Any other tags to be included in the request. Passed as a string. Example, tag 9F0C with amount 0x000000000100 would be "9F0C06000000000100" If tags 9F02 (amount),9F03 (other amount), or 9C (transaction type) are included, they will take priority over these values supplied as individual parameters to this method.
     * @param forceOnline TRUE = do not allow offline approval, FALSE = allow ICC to approve offline if terminal capable Note: To request tags to be included in default response, use tag DFEE1A, and specify tag list. Example four tags 9F02, 9F36, 95, 9F37 to be included in response = DFEE1A079F029F369f9F37
     * @return success or error code. Values can be parsed with device_getResponseCodeString
     */
    int emv_startTransaction(double amount, double amtOther, int type, int timeout, byte[] tags, boolean forceOnline);

    /**
     * Cancels the currently executing EMV transaction.
     * @param respData
     * @return success or error code. Values can be parsed with device_getResponseCodeString
     */
    int emv_cancelTransaction(ResDataStruct respData);

  //  void emv_setTransactionParameters(double amount, double amtOther, int type, int timeout, byte[] tags);

    /**
     * Callback Response LCD Display
     * Provides menu selection responses to the kernel after a callback was received lcdDisplay delegate.
     * @param mode
     *
     * The choices are as follows
     * 0x00 Cancel
     * 0x01 Menu Display
     * 0x02 Normal Display get Function Key supply either 0x43 ('C') for Cancel, or 0x45 ('E') for Enter/accept
     * 0x08 Language Menu Display
     * @param data
     */
    void emv_lcdControlResponse(byte mode, byte data);

    //int emv_authenticateTransaction(byte[] tags);

    /**
     * Complete EMV Transaction Request
     * @param commError Communication error with host. Set to TRUE if host was unreachable, or FALSE if host response received. If Communication error, authCode, iad, tlvScripts can be null.
     * @param authCode Authorization code from host. Two bytes. Example 0x3030. (Tag value 8A). Required
     * @param iad Issuer Authentication Data, if any. Example 0x11223344556677883030 (tag value 91).
     * @param tlvScripts 71/72 scripts, if any
     * @param tags Additional TVL data to return with transaction results (if any)
     * @return success or error code. Values can be parsed with device_getResponseCodeString
     */
    int emv_completeTransaction(boolean commError, byte[] authCode, byte[] iad, byte[] tlvScripts, byte[] tags);

 //   int emv_retrieveTransactionResult(byte[] tags, Map<String, Map<String, byte[]>> retrievedTags);

//    int device_reviewAllSetting(ResDataStruct respData);

//    int msr_defaultAllSetting();

//    int msr_getSingleSetting(byte funcID, byte[] response);
//
//    int msr_setSingleSetting(byte funcID, byte setData);

    /**
     * Cancels MSR swipe request.
     *
     * @return success or error code. Values can be parsed with device_getResponseCodeString
     */
    int msr_cancelMSRSwipe();

    /**
     * Enable MSR swipe card.
     * Returns encrypted MSR data or function key value by call back function. The function swipeMSRData in interface
     * @return success or error code. Values can be parsed with device_getResponseCodeString
     */
    int msr_startMSRSwipe();

    /**
     * Enable MSR swipe card.
     * Returns encrypted MSR data or function key value by call back function. The function swipeMSRData in interface
     * @param timeout Swipe Timeout Value timeout value in seconds; maximum value is 255 seconds. If it is 0, it will be set to 5 seconds.
     * @return success or error code. Values can be parsed with device_getResponseCodeString
     */
    int msr_startMSRSwipe(int timeout);

//    int ctls_retrieveApplicationData(String aid, ResDataStruct respData);
//
//    int ctls_removeApplicationData(String aid, ResDataStruct respData);
//
//    int ctls_setApplicationData(byte[] TLV, ResDataStruct respData);

//    int ctls_setConfigurationGroup(byte[] TLV, ResDataStruct respData);
//
//    int ctls_getConfigurationGroup(int group, ResDataStruct respData);
//
//    int ctls_getAllConfigurationGroups(ResDataStruct respData);
//
//    int ctls_removeConfigurationGroup(int group);
//
//    int ctls_retrieveTerminalData(ResDataStruct respData);
//
//    int ctls_setTerminalData(byte[] TLV, ResDataStruct respData);
//
//    int ctls_retrieveAidList(ResDataStruct respData);
//
//    int ctls_retrieveCAPK(byte[] data, ResDataStruct respData);
//
//    int ctls_removeCAPK(byte[] capk, ResDataStruct respData);
//
//    int ctls_setCAPK(byte[] key, ResDataStruct respData);
//
//    int ctls_retrieveCAPKList(ResDataStruct respData);

//    int ctls_removeAllApplicationData();

//    int ctls_removeAllCAPK();

    /**
     * Start CTLS (or MSR) Transaction Request
     * @param amount Transaction amount value (tag value 9F02)
     * @param amtOther Other amount value, if any (tag value 9F03)
     * @param type Transaction type (tag value 9C).
     * @param timeout Timeout value in seconds.
     * @param tags Any other tags to be included in the request. Passed as a string. Example, tag 9F0C with amount 0x000000000100 would be "9F0C06000000000100" If tags 9F02 (amount),9F03 (other amount), or 9C (transaction type) are included, they will take priority over these values supplied as individual parameters to this method. Note: To request tags to be included in default response, use tag DFEE1A, and specify tag list. Example four tags 9F02, 9F36, 95, 9F37 to be included in response = DFEE1A079F029F369f9F37 For SmartTap, pass the tag FFEE08 with the value 0200 For Apple VAS, pass the tag FFEE06 with the value 9F220201009F2604000000009F2B050100000000DF010101
     * @return success or error code. Values can be parsed with device_getResponseCodeString
     */
    int ctls_startTransaction(double amount, double amtOther, int type, int timeout, byte[] tags);

    /**
     * Cancels the currently executing CTLS transaction (or MSR swipe request).
     * @return success or error code. Values can be parsed with device_getResponseCodeString
     */
    int ctls_cancelTransaction();

    /**
     * Allow fallback for EMV transactions. Default is TRUE
     * @param allow TRUE = allow fallback, FALSE = don't allow fallback
     */
    void emv_allowFallback(boolean allow);

    //void emv_setAutoAuthenticateTransaction(boolean auto);

    //boolean emv_getAutoAuthenticateTransaction();

    //void emv_setAutoCompleteTransaction(boolean auto);

   // boolean emv_getAutoCompleteTransaction();

}
