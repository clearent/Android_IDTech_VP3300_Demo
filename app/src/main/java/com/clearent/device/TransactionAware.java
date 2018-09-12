package com.clearent.device;

import java.util.Map;

public interface TransactionAware {
    int emv_retrieveTransactionResult(byte[] tags, Map<String, Map<String, byte[]>> retrievedTags);
}
