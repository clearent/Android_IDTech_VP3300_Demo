package com.clearent.device.config;

import android.util.Log;

import com.clearent.device.DeviceConfigurable;
import com.clearent.device.domain.CommunicationRequest;
import com.idtechproducts.device.Common;
import com.idtechproducts.device.ErrorCode;
import com.idtechproducts.device.ResDataStruct;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ClearentConfiguratorImpl implements ClearentConfigurator {

    private DeviceConfigurable deviceConfigurable;

    public ClearentConfiguratorImpl(DeviceConfigurable deviceConfigurable) {
        this.deviceConfigurable = deviceConfigurable;
    }

    @Override
    public void configure(CommunicationRequest communicationRequest) {
        setTerminalMajorConfiguration();
        setDefaultTerminalTags();
        initClock();
        configureReader(communicationRequest);
    }

    private void setDefaultTerminalTags() {
        String defaultTerminalTags = "5f3601029f1a0208409f3501219f33036028c89f4005f000f0a0019f1e085465726d696e616c9f150212349f160f3030303030303030303030303030309f1c0838373635343332319f4e2231303732312057616c6b65722053742e20437970726573732c204341202c5553412edf260101df1008656e667265737a68df110100df270100dfee150101dfee160100dfee170105dfee180180dfee1e08d09c20d0c41e1400dfee1f0180dfee1b083030303130353030dfee20013cdfee21010adfee2203323c3c";
        String upperCaseTlv = defaultTerminalTags.toUpperCase();
        ResDataStruct resDatStruct = new ResDataStruct();
        byte[] tlvBytes = Common.getBytesFromHexString(upperCaseTlv);
        int setTerminalDataRt = deviceConfigurable.emv_setTerminalData(tlvBytes,resDatStruct);
        if (ErrorCode.SUCCESS == setTerminalDataRt) {
            Log.i("INFO","Emv Entry mode changed from 07 to 05");
        } else{
            String error = "Reader failed to configure default terminal tags. ";
            deviceConfigurable.notifyCommandFailure(setTerminalDataRt, error);
        }
    }

    private void configureReader(CommunicationRequest communicationRequest) {
        ClearentConfigFetcherResponseHandler clearentConfigFetcherResponseHandler = new ClearentConfigFetcherResponseHandler(deviceConfigurable);
        ClearentConfigFetcher clearentConfigFetcher = new ClearentConfigFetcherImpl(communicationRequest);
        clearentConfigFetcher.fetchConfiguration(clearentConfigFetcherResponseHandler);
    }

    private void setTerminalMajorConfiguration() {
        ResDataStruct resDataStruct = new ResDataStruct();
        int commandRt = deviceConfigurable.device_sendDataCommand("6016", false, "05", resDataStruct);
        if (commandRt == ErrorCode.SUCCESS) {
            String info = "Terminal Major Configuration Succeeded ";
            Log.i("INFO", info);
        } else {
            String error = "Reader failed to configure (terminal major). ";
            deviceConfigurable.notifyCommandFailure(commandRt, error);
        }
    }

    private int initClock() {
        int dateRt = initClockDate();
        int timeRt = initClockTime();
        if (0 == dateRt && 0 == timeRt) {
            Log.i("INFO", "Clock Initialized");
        } else {
            return 1;
        }
        return 0;
    }

    private int initClockDate() {
        String clockDate = getClockDateAsYYYYMMDD();
        ResDataStruct resDataStruct = new ResDataStruct();
        return deviceConfigurable.device_sendDataCommand("2503", false, clockDate, resDataStruct);
    }

    private String getClockDateAsYYYYMMDD() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        String date = simpleDateFormat.format(new Date());
        return date;
    }

    private int initClockTime() {
        String clockTime = getClockTimeAsHHMM();
        ResDataStruct resDataStruct = new ResDataStruct();
        return deviceConfigurable.device_sendDataCommand("2501", false, clockTime, resDataStruct);
    }

    private String getClockTimeAsHHMM() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HHmm");
        String time = simpleDateFormat.format(new Date());
        return time;
    }
}
