package com.clearent.sample;

import com.clearent.device.PublicOnReceiverListener;

public interface SampleTransaction {
    void doSale(PostTransactionRequest postTransactionRequest, PublicOnReceiverListener publicOnReceiverListener);
}