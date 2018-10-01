package com.clearent.sample;

import com.clearent.idtech.android.PublicOnReceiverListener;

public interface SampleTransaction {
    void doSale(PostTransactionRequest postTransactionRequest, PublicOnReceiverListener publicOnReceiverListener);
}