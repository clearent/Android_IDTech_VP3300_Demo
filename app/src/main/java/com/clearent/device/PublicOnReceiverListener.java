package com.clearent.device;

import com.clearent.device.token.domain.TransactionToken;
import com.idtechproducts.device.StructConfigParameters;

public interface PublicOnReceiverListener {

    void isReady();

    void successfulTransactionToken(TransactionToken transactionToken);

    void lcdDisplay(int var1, String[] var2, int var3);

    void lcdDisplay(int var1, String[] var2, int var3, byte[] var4, byte var5);

    void deviceConnected();

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
