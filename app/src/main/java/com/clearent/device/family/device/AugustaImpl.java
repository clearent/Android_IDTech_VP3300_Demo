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

    @Override
    public boolean device_setDeviceType(ReaderInfo.DEVICE_TYPE deviceType) {
        return getSDKInstance().device_setDeviceType(deviceType);
    }

    @Override
    public int device_getDRS(ResDataStruct respData) {
        return getSDKInstance().device_getDRS(respData);
    }

    @Override
    public int device_verifyBackdoorKey() {
        return getSDKInstance().device_verifyBackdoorKey();
    }

    @Override
    public int device_selfCheck() {
        return getSDKInstance().device_selfCheck();
    }

    @Override
    public int device_rebootDevice() {
        return getSDKInstance().device_rebootDevice();
    }

    @Override
    public int device_controlBeep(int index, int frequency, int duration) {
        return getSDKInstance().device_controlBeep(index, frequency, duration);
    }

    @Override
    public int device_controlLED(byte indexLED, byte control, int intervalOn, int intervalOff) {
        return getSDKInstance().device_controlLED(indexLED, control, intervalOn, intervalOff);
    }

    @Override
    public int device_controlLED_ICC(int controlMode, int interval) {
        return getSDKInstance().device_controlLED_ICC(controlMode, interval);
    }

    @Override
    public int device_setDateTime(byte[] mac) {
        return getSDKInstance().device_setDateTime(mac);
    }

    @Override
    public int device_getKeyStatus(ResDataStruct respData) {
        return getSDKInstance().device_getKeyStatus(respData);
    }

    @Override
    public int config_setBeeperController(boolean firmwareControlBeeper) {
        return getSDKInstance().config_setBeeperController(firmwareControlBeeper);
    }

    @Override
    public int config_setLEDController(boolean firmwareControlMSRLED, boolean firmwareControlICCLED) {
        return getSDKInstance().config_setLEDController(firmwareControlMSRLED, firmwareControlICCLED);
    }

    @Override
    public int config_setEncryptionControl(byte Encryption) {
        return getSDKInstance().config_setEncryptionControl(Encryption);
    }

    @Override
    public int config_setEncryptionControl(boolean msr, boolean icc) {
        return getSDKInstance().config_setEncryptionControl(msr, icc);
    }

    @Override
    public int config_getEncryptionControl(ResDataStruct respData) {
        return getSDKInstance().config_getEncryptionControl(respData);
    }

    @Override
    public int icc_setKeyTypeForICCDUKPT(byte encryption) {
        return getSDKInstance().icc_setKeyTypeForICCDUKPT(encryption);
    }

    @Override
    public int icc_getKeyTypeForICCDUKPT(ResDataStruct respData) {
        return getSDKInstance().icc_getKeyTypeForICCDUKPT(respData);
    }

    @Override
    public int icc_setKeyFormatForICCDUKPT(byte encryption) {
        return getSDKInstance().icc_setKeyFormatForICCDUKPT(encryption);
    }

    @Override
    public int icc_getKeyFormatForICCDUKPT(ResDataStruct respData) {
        return getSDKInstance().icc_getKeyFormatForICCDUKPT(respData);
    }

    @Override
    public int icc_enable(boolean withNotification) {
        return getSDKInstance().icc_enable(withNotification);
    }

    @Override
    public int icc_disable() {
        return getSDKInstance().icc_disable();
    }

    @Override
    public int icc_getFunctionStatus(ResDataStruct respData) {
        return getSDKInstance().icc_getFunctionStatus(respData);
    }

    @Override
    public int emv_removeAllApplicationData() {
        return getSDKInstance().emv_removeAllApplicationData();
    }

    @Override
    public int emv_removeAllCAPK() {
        return getSDKInstance().emv_removeAllCAPK();
    }

    @Override
    public int emv_removeAllCRL() {
        return getSDKInstance().emv_removeAllCRL();
    }

    @Override
    public int icc_getAPDU_KSN(byte KeyNameIndex, byte[] KeySlot, ResDataStruct resKSN) {
        return getSDKInstance().icc_getAPDU_KSN(KeyNameIndex,KeySlot, resKSN);
    }

    @Override
    public int icc_reviewAllSetting(ICCSettingStruct iccSetting) {
        return getSDKInstance().icc_reviewAllSetting(iccSetting);
    }

    @Override
    public int icc_exchangeAPDU(byte[] dataAPDU, APDUResponseStruct response) {
        return getSDKInstance().icc_exchangeAPDU(dataAPDU,response);
    }

    @Override
    public int msr_reviewAllSetting(MSRSettingStruct msrSetting) {
        return getSDKInstance().msr_reviewAllSetting(msrSetting);
    }

    @Override
    public int msr_setExpirationMask(boolean mask) {
        return getSDKInstance().msr_setExpirationMask(mask);
    }

    @Override
    public int msr_getExpirationMask(ResDataStruct respData) {
        return getSDKInstance().msr_getExpirationMask(respData);
    }

    @Override
    public int msr_setClearPANID(byte value) {
        return getSDKInstance().msr_setClearPANID(value);
    }

    @Override
    public int msr_getClearPANID(ResDataStruct respData) {
        return getSDKInstance().msr_getClearPANID(respData);
    }

    @Override
    public int msr_getSwipeForcedEncryptionOption(ResDataStruct respData) {
        return getSDKInstance().msr_getSwipeForcedEncryptionOption(respData);
    }

    @Override
    public int msr_setSwipeForcedEncryptionOption(boolean track1, boolean track2, boolean track3, boolean track3card0) {
        return getSDKInstance().msr_setSwipeForcedEncryptionOption(track1, track2, track3, track3card0);
    }

    @Override
    public int msr_getSwipeMaskOption(ResDataStruct respData) {
        return getSDKInstance().msr_getSwipeMaskOption(respData);
    }

    @Override
    public int msr_setSwipeMaskOption(boolean track1, boolean track2, boolean track3) {
        return getSDKInstance().msr_setSwipeMaskOption(track1, track2, track3);
    }

    @Override
    public int msr_getSetting(byte setting, ResDataStruct respData) {
        return getSDKInstance().msr_getSetting(setting,respData);
    }

    @Override
    public int msr_setSetting(byte setting, byte val) {
        return getSDKInstance().msr_setSetting(setting, val);
    }

    @Override
    public int msr_setSwipeEncryption(byte encryption) {
        return getSDKInstance().msr_setSwipeEncryption(encryption);
    }

    @Override
    public int msr_getSwipeEncryption(ResDataStruct respData) {
        return getSDKInstance().msr_getSwipeEncryption(respData);
    }

    @Override
    public int msr_enableBufferMode(boolean isBufferMode, boolean withNotification) {
        return getSDKInstance().msr_enableBufferMode(isBufferMode, withNotification);
    }

    @Override
    public int msr_disable() {
        return getSDKInstance().msr_disable();
    }

    @Override
    public int msr_setWhiteList(byte[] val) {
        return getSDKInstance().msr_setWhiteList(val);
    }

    @Override
    public int msr_RetrieveWhiteList(ResDataStruct respData) {
        return getSDKInstance().msr_RetrieveWhiteList(respData);
    }

    @Override
    public int msr_getFunctionStatus(ResDataStruct respData) {
        return getSDKInstance().msr_getFunctionStatus(respData);
    }

    @Override
    public int ctls_startTransaction() {
        return getSDKInstance().ctls_startTransaction();
    }
}
