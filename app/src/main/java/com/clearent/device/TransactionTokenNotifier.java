package com.clearent.device;

import com.clearent.device.token.domain.TransactionToken;

public interface TransactionTokenNotifier {
    void notifyNewTransactionToken(TransactionToken transactionToken);
    void notifyTransactionTokenFailure(String message);
}
