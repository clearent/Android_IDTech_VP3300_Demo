package com.clearent.device.token.services;

import com.idtechproducts.device.IDTEMVData;
import com.idtechproducts.device.IDTMSRData;

public interface CardTokenizer {
    void createTransactionToken(IDTMSRData cardData);
    void createTransactionToken(IDTEMVData idtemvData);
    void createTransactionTokenForFallback(IDTMSRData cardData);
}
