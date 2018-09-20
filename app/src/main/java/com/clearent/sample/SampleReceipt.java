package com.clearent.sample;

import com.clearent.device.PublicOnReceiverListener;

public interface SampleReceipt {
    void doReceipt(ReceiptRequest receiptRequest, PublicOnReceiverListener publicOnReceiverListener);
}