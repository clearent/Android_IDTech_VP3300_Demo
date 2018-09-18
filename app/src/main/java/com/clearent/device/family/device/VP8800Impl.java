package com.clearent.device.family.device;

import android.content.Context;

import com.clearent.device.Device;
import com.clearent.device.PublicOnReceiverListener;
import com.idtechproducts.device.IDT_Device;
import com.idtechproducts.device.IDT_VP8800;

public class VP8800Impl extends Device implements VP8800 {

    private static IDT_VP8800 idt_vp8800;

    public VP8800Impl(PublicOnReceiverListener publicOnReceiverListener, Context context, String paymentsBaseUrl, String paymentsPublicKey) {
        super(publicOnReceiverListener,paymentsBaseUrl,paymentsPublicKey);
        this.idt_vp8800 = new IDT_VP8800(getClearentOnReceiverListener(), context);
    }

    @Override
    public IDT_Device getSDKInstance() {
        return idt_vp8800.getSDKInstance();
    }

}
