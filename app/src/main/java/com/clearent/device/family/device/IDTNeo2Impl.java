package com.clearent.device.family.device;

import android.content.Context;

import com.clearent.device.Device;
import com.clearent.device.PublicOnReceiverListener;
import com.idtechproducts.device.IDT_Device;
import com.idtechproducts.device.IDT_NEO2;
import com.idtechproducts.device.IDT_VP3300;
import com.idtechproducts.device.OnReceiverListenerPIN;
import com.idtechproducts.device.OnReceiverListenerPINRequest;
import com.idtechproducts.device.ReaderInfo;

public class IDTNeo2Impl extends Device implements IDTNeo2 {

    private static IDT_NEO2 idt_neo2;

    public IDTNeo2Impl(PublicOnReceiverListener publicOnReceiverListener, OnReceiverListenerPIN callback2, Context context, String paymentsBaseUrl, String paymentsPublicKey) {
        super(publicOnReceiverListener, paymentsBaseUrl,paymentsPublicKey);
        this.idt_neo2 = new IDT_NEO2(getClearentOnReceiverListener(), callback2, context);
    }

    @Override
    public IDT_Device getSDKInstance() {
        return idt_neo2.getSDKInstance();
    }

}
