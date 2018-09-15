package com.clearent.device.family.vivopay.vp3300;

import android.content.Context;

import com.clearent.device.Device;
import com.clearent.device.PublicOnReceiverListener;
import com.idtechproducts.device.IDT_Device;
import com.idtechproducts.device.IDT_VP3300;
import com.idtechproducts.device.OnReceiverListenerPINRequest;
import com.idtechproducts.device.ReaderInfo;

public class VP3300Impl extends Device implements VP3300 {

    private static IDT_VP3300 idt_vp3300;

    public VP3300Impl(PublicOnReceiverListener publicOnReceiverListener, Context context, String paymentsBaseUrl, String paymentsPublicKey) {
        super(publicOnReceiverListener,paymentsBaseUrl,paymentsPublicKey);
        this.idt_vp3300 = new IDT_VP3300(getClearentOnReceiverListener(), context);
    }

    public VP3300Impl(PublicOnReceiverListener publicOnReceiverListener, OnReceiverListenerPINRequest callback2, Context context, String paymentsBaseUrl, String paymentsPublicKey) {
        super(publicOnReceiverListener, paymentsBaseUrl,paymentsPublicKey);
        this.idt_vp3300 = new IDT_VP3300(getClearentOnReceiverListener(), callback2, context);
    }

    @Override
    public IDT_Device getSDKInstance() {
        return idt_vp3300.getSDKInstance();
    }

    @Override
    public boolean device_setDeviceType(ReaderInfo.DEVICE_TYPE deviceType) {
        if (deviceType == ReaderInfo.DEVICE_TYPE.DEVICE_VP3300_AJ) {
            return getSDKInstance().device_setDeviceType(ReaderInfo.DEVICE_TYPE.DEVICE_VP3300_AJ);
        } else if (deviceType == ReaderInfo.DEVICE_TYPE.DEVICE_VP3300_AJ_USB) {
            return getSDKInstance().device_setDeviceType(ReaderInfo.DEVICE_TYPE.DEVICE_VP3300_AJ_USB);
        } else if (deviceType == ReaderInfo.DEVICE_TYPE.DEVICE_VP3300_USB) {
            return getSDKInstance().device_setDeviceType(ReaderInfo.DEVICE_TYPE.DEVICE_VP3300_USB);
        } else if (deviceType == ReaderInfo.DEVICE_TYPE.DEVICE_VP3300_BT) {
            return getSDKInstance().device_setDeviceType(ReaderInfo.DEVICE_TYPE.DEVICE_VP3300_BT);
        } else {
            return deviceType == ReaderInfo.DEVICE_TYPE.DEVICE_VP3300_BT_USB ? getSDKInstance().device_setDeviceType(ReaderInfo.DEVICE_TYPE.DEVICE_VP3300_BT_USB) : false;
        }
    }

}
