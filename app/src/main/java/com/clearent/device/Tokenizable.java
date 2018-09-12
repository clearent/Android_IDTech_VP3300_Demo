package com.clearent.device;

public interface Tokenizable extends HasDeviceMetadata, TransactionAware, TransactionTokenNotifier {

    String getPaymentsBaseUrl();
    String getPaymentsPublicKey();
}
