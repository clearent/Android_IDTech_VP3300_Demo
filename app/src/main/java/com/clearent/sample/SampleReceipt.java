package com.clearent.sample;

import com.clearent.idtech.android.PublicOnReceiverListener;

public interface SampleReceipt {
    void doReceipt(ReceiptRequest receiptRequest, PublicOnReceiverListener publicOnReceiverListener);
}