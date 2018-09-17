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
     * @param var1
     * @param var2
     * @param var3
     */
    void lcdDisplay(int var1, String[] var2, int var3);

    /**
     * lcdDisplay - This method sends back information that can be used to prompt the user for an answer.
     * @param var1
     * @param var2
     * @param var3
     * @param var4
     * @param var5
     */
    void lcdDisplay(int var1, String[] var2, int var3, byte[] var4, byte var5);

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

    void timeout(int var1);

    void autoConfigCompleted(StructConfigParameters var1);

    void autoConfigProgress(int var1);

    void msgRKICompleted(String var1);

    void ICCNotifyInfo(byte[] var1, String var2);

    void msgBatteryLow();

    void LoadXMLConfigFailureInfo(int var1, String var2);

    void msgToConnectDevice();

    void msgAudioVolumeAjustFailed();

    void dataInOutMonitor(byte[] var1, boolean var2);

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
