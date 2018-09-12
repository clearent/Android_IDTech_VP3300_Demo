package com.clearent.device.token.services;

import com.idtechproducts.device.IDTEMVData;
import com.idtechproducts.device.IDTMSRData;

public interface CardTokenizer {

    void createTransactionToken(IDTMSRData cardData);
    void createTransactionToken(IDTEMVData idtemvData);
    //TODO do we need this
    void createTransactionTokenForFallback(IDTMSRData cardData);
}
