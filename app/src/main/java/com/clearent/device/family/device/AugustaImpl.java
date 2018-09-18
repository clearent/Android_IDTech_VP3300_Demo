package com.clearent.device.family.device;

import android.content.Context;

import com.clearent.device.Device;
import com.clearent.device.PublicOnReceiverListener;
import com.clearent.device.family.device.Augusta;
import com.idtechproducts.device.APDUResponseStruct;
import com.idtechproducts.device.ICCSettingStruct;
import com.idtechproducts.device.IDT_Augusta;
import com.idtechproducts.device.IDT_Device;
import com.idtechproducts.device.MSRSettingStruct;
import com.idtechproducts.device.ReaderInfo;
import com.idtechproducts.device.ResDataStruct;

import static com.idtechproducts.device.ReaderInfo.DEVICE_TYPE.DEVICE_AUGUSTA;

public class AugustaImpl extends Device implements Augusta {

    private static IDT_Augusta idt_augusta;

    public AugustaImpl(PublicOnReceiverListener publicOnReceiverListener, Context context, String paymentsBaseUrl, String paymentsPublicKey) {
        super(publicOnReceiverListener,paymentsBaseUrl,paymentsPublicKey);
        this.idt_augusta = new IDT_Augusta(getClearentOnReceiverListener(), context);
        this.idt_augusta.device_setDeviceType(ReaderInfo.DEVICE_TYPE.DEVICE_AUGUSTA);
    }

    public AugustaImpl(PublicOnReceiverListener publicOnReceiverListener, Context context, boolean isTTK, boolean isSRED, boolean isThales, String paymentsBaseUrl, String paymentsPublicKey) {
        super(publicOnReceiverListener,paymentsBaseUrl,paymentsPublicKey);
        this.idt_augusta = new IDT_Augusta(getClearentOnReceiverListener(), context, isTTK, isSRED, isThales);
        this.idt_augusta.device_setDeviceType(DEVICE_AUGUSTA, isTTK,isSRED,isThales);
    }

    @Override
    public IDT_Device getSDKInstance() {
        return idt_augusta.getSDKInstance();
    }

    @Override
    public boolean device_setDeviceType(ReaderInfo.DEVICE_TYPE deviceType, boolean isTTK, boolean isSRED, boolean isThales) {
        return getSDKInstance().device_setDeviceType(deviceType, isTTK, isSRED, isThales);
    }

    @Override
    public boolean device_isTTK() {
        return IDT_Device._isTTK;
    }

    @Override
    public boolean device_isThales() {
        return IDT_Device._isThales;
    }

    @Override
    public boolean device_isSRED() {
        return IDT_Device._isSRED;
    }

}
