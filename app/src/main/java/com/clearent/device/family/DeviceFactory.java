package com.clearent.device.family;

import android.content.Context;

import com.clearent.device.PublicOnReceiverListener;
import com.clearent.device.family.device.Augusta;
import com.clearent.device.family.device.AugustaImpl;
import com.clearent.device.family.device.VP3300;
import com.clearent.device.family.device.VP3300Impl;
import com.clearent.device.family.device.VP8800;
import com.clearent.device.family.device.VP8800Impl;
import com.idtechproducts.device.OnReceiverListenerPINRequest;
import com.idtechproducts.device.ReaderInfo;

import static com.idtechproducts.device.ReaderInfo.DEVICE_TYPE.DEVICE_AUGUSTA;

/**
 * This factory returns wrappers of IDTech concrete implementations per device.
 */
public class DeviceFactory {

    /**
     * getVP3300 Use this method to get a VP3300 object for interacting with the VivoPay series. Devices support audio jack, audio jack + usb, usb only, and bluetooth.
     *
     * @param deviceType               select a device type based on the reader you want to use.
     * @param publicOnReceiverListener implement this interface allowing the device to communicate back to you.
     * @param context                  Provide the android context.
     * @param paymentsBaseUrl          Clearent requires a base url so the library can communicate with its servers ex- https://gateway.clearent.net
     * @param paymentsPublicKey        Clearent requires a public key to communicate with its servers.
     * @return VP3300 Object
     */
    public static VP3300 getVP3300(ReaderInfo.DEVICE_TYPE deviceType, PublicOnReceiverListener publicOnReceiverListener, Context context, String paymentsBaseUrl, String paymentsPublicKey) {
        switch (deviceType) {
            case DEVICE_VP3300_AJ:
            case DEVICE_VP3300_AJ_USB:
            case DEVICE_VP3300_USB:
            case DEVICE_VP3300_BT:
                VP3300 vP3300 = new VP3300Impl(publicOnReceiverListener, context, paymentsBaseUrl, paymentsPublicKey);
                vP3300.device_setDeviceType(deviceType);
                vP3300.emv_allowFallback(true);
                return vP3300;
            default:
                throw new RuntimeException("No VP3300 device found");
        }
    }

    /**
     * getVP3300 Use this method to get a VP3300 object for interacting with the VivoPay series. Devices support audio jack, audio jack + usb, usb only, and bluetooth.
     *
     * @param deviceType               select a device type based on the reader you want to use.
     * @param publicOnReceiverListener implement this interface allowing the device to communicate back to you.
     * @param callback2                implement this interface allowing the device to communicate back to you for pin requests.
     * @param context                  Provide the android context.
     * @param paymentsBaseUrl          Clearent requires a base url so the library can communicate with its servers ex- https://gateway.clearent.net
     * @param paymentsPublicKey        Clearent requires a public key to communicate with its servers.
     * @return VP3300 Object
     */
    public static VP3300 getVP3300(ReaderInfo.DEVICE_TYPE deviceType, PublicOnReceiverListener publicOnReceiverListener, OnReceiverListenerPINRequest callback2, Context context, String paymentsBaseUrl, String paymentsPublicKey) {
        switch (deviceType) {
            case DEVICE_VP3300_AJ:
            case DEVICE_VP3300_AJ_USB:
            case DEVICE_VP3300_USB:
            case DEVICE_VP3300_BT:
                VP3300 vP3300 = new VP3300Impl(publicOnReceiverListener, callback2, context, paymentsBaseUrl, paymentsPublicKey);
                vP3300.device_setDeviceType(deviceType);
                vP3300.emv_allowFallback(true);
                return vP3300;
            default:
                throw new RuntimeException("No VP3300 device found");
        }
    }

    /**
     * getAugusta Use this method to get an object to interact with an Augusta device.
     *
     * @param publicOnReceiverListener implement this interface allowing the device to communicate back.
     * @param context                  Provide the android context.
     * @param isTTK
     * @param isSRED
     * @param isThales
     * @param paymentsBaseUrl          Clearent requires a base url so the library can communicate with its servers ex- https://gateway.clearent.net
     * @param paymentsPublicKey        Clearent requires a public key to communicate with its servers.
     * @return Augusta object
     */
    public static Augusta getAugusta(PublicOnReceiverListener publicOnReceiverListener, Context context, boolean isTTK, boolean isSRED, boolean isThales, String paymentsBaseUrl, String paymentsPublicKey) {
        Augusta augusta = new AugustaImpl(publicOnReceiverListener, context, isTTK, isSRED, isThales, paymentsBaseUrl, paymentsPublicKey);
        augusta.device_setDeviceType(DEVICE_AUGUSTA, isTTK,isSRED,isThales);
        return augusta;
    }

    /**
     * getAugusta Use this method to get an object to interact with an Augusta device.
     *
     * @param publicOnReceiverListener implement this interface allowing the device to communicate back.
     * @param context                  Provide the android context.
     * @param paymentsBaseUrl          Clearent requires a base url so the library can communicate with its servers ex- https://gateway.clearent.net
     * @param paymentsPublicKey        Clearent requires a public key to communicate with its servers.
     * @return Augusta object
     */
    public static Augusta getAugusta(PublicOnReceiverListener publicOnReceiverListener, Context context, String paymentsBaseUrl, String paymentsPublicKey) {
        return new AugustaImpl(publicOnReceiverListener, context, paymentsBaseUrl, paymentsPublicKey);
    }

    /**
     * getVP8800 Use this method to get an object to interact with a VP8800 device.
     *
     * @param publicOnReceiverListener implement this interface allowing the device to communicate back.
     * @param context                  Provide the android context.
     * @param paymentsBaseUrl          Clearent requires a base url so the library can communicate with its servers ex- https://gateway.clearent.net
     * @param paymentsPublicKey        Clearent requires a public key to communicate with its servers.
     * @return Augusta object
     */
    public static VP8800 getVP8800(PublicOnReceiverListener publicOnReceiverListener, Context context, String paymentsBaseUrl, String paymentsPublicKey) {
        return new VP8800Impl(publicOnReceiverListener, context, paymentsBaseUrl, paymentsPublicKey);
    }
}
