package com.clearent.device;

import com.clearent.device.token.domain.TransactionToken;
import com.idtechproducts.device.StructConfigParameters;

/**
 * PublicOnReceiverListener
 *
 * Implement this interface so the device can communicate with you.
 */
public interface PublicOnReceiverListener {

    /**
     * This method will be called when the device is ready to be used.
     */
    void isReady();

    /**
     * This method will be called when a card has been successfully read and translated to a Clearent Transaction Token.
     * This token can be used later to perform a payment.
     *
     * @param transactionToken contains the token and associated metadata.
     */
    void successfulTransactionToken(TransactionToken transactionToken);

    /**
     * lcdDisplay - This method sends back information about the process.
     * @param mode
     * LCD Display Mode:
     * 0x01: Menu Display. A selection must be made to resume the transaction
     * 0x02: Normal Display get function key. A function must be selected to resume the transaction
     * 0x03: Display without input. Message is displayed without pausing the transaction
     * 0x04: List of languages are presented for selection. A selection must be made to resume the transaction
     * 0x10: Clear Screen. Command to clear the LCD screen
     * @param lines Line(s) of data to display
     * @param timeout Timeout value when displaying dialog box
     */
    void lcdDisplay(int mode, String[] lines, int timeout);

    /**
     * lcdDisplay - This method sends back information that can be used to prompt the user for an answer.
     * @param mode
     * LCD Display Mode:
     * 0x01: Menu Display. A selection must be made to resume the transaction
     * 0x02: Normal Display get function key. A function must be selected to resume the transaction
     * 0x03: Display without input. Message is displayed without pausing the transaction
     * 0x04: List of languages are presented for selection. A selection must be made to resume the transaction
     * 0x10: Clear Screen. Command to clear the LCD screen
     * @param lines Line(s) of data to display
     * @param timeout Timeout value when displaying dialog box
     * @param languageCode 2 bytes language code ("EN", "ES", "FR", or "ZH") of the LCD message.
     * @param messageId 1 byte id (from 1 to 34) for a LCD message string.
     */
    void lcdDisplay(int mode, String[] lines, int timeout, byte[] languageCode, byte messageId);

    /**
     * deviceConnected
     * Called when the device is connected (but not necessarily ready for use).
     */
    void deviceConnected();

    /**
     * deviceDisconnected
     * Called when the device is disconnected.
     */
    void deviceDisconnected();

    /**
     * Notify the plug status of phone jack. Timeout when wait for the response.
     * This happens in the process of get PINpad, swipe MSR, EMV Level 2 transaction
     * @param errorCode
     */
    void timeout(int errorCode);

    //TODO this will be removed and will be managed by clearent
    //void autoConfigCompleted(StructConfigParameters var1);

    //TODO this will be removed and will be managed by clearent
    //void autoConfigProgress(int progressValue);

    //TODO hide this ...disabling remote key injection until we know it is needed.
    //void msgRKICompleted(String var1);

    /**
     * The ICC Card seated status notification
     * @param dataNotify the response data.
     * @param strMessage the ICC notification message information
     */
    void ICCNotifyInfo(byte[] dataNotify, String strMessage);

    /**
     * Battery low status notification.
     */
    void msgBatteryLow();

    //TODO hide/remove when android device config is done
    void LoadXMLConfigFailureInfo(int var1, String var2);

    /**
     * The message notify the application to connect the device.
     */
    void msgToConnectDevice();

    /**
     * The message notify the application failed to adjust the audio volume.
     */
    void msgAudioVolumeAjustFailed();

    /**
     * The input/output data notification. Used for debugging and providing support data back to Clearent/IDTech.
     * @param data the input/output data.
     * @param isIncoming true if is incoming data, false if it is out going data.
     */
    void dataInOutMonitor(byte[] data, boolean isIncoming);

    //TODO is this used ?
    public static enum EMV_RESULT_CODE_Types {
        EMV_RESULT_CODE_OFFLINE_APPROVED,
        EMV_RESULT_CODE_OFFLINE_DECLINED,
        EMV_RESULT_CODE_APPROVED,
        EMV_RESULT_CODE_DECLINED,
        EMV_RESULT_CODE_GO_ONLINE,
        EMV_RESULT_CODE_CALL_YOUR_BANK,
        EMV_RESULT_CODE_NOT_ACCEPTED,
        EMV_RESULT_CODE_USE_MAGSTRIPE,
        EMV_RESULT_CODE_TIME_OUT,
        EMV_RESULT_CODE_TRANSACTION_SUCCESS,
        EMV_RESULT_CODE_TERMINATE;

        private EMV_RESULT_CODE_Types() {
        }
    }

}
