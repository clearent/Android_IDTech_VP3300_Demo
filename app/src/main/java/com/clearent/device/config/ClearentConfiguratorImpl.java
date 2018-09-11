package com.clearent.device.config;

import android.util.Log;

import com.clearent.device.ClearentOnReceiverListener;
import com.clearent.device.Clearent_VP3300;
import com.clearent.device.config.domain.ConfigFetchRequest;
import com.idtechproducts.device.ErrorCode;
import com.idtechproducts.device.ResDataStruct;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ClearentConfiguratorImpl implements ClearentConfigurator {

    private static final String RELATIVE_URL = "rest/v2/mobile/devices";

    private Clearent_VP3300 clearentVp3300;
    private ClearentOnReceiverListener clearentOnReceiverListener;

    public ClearentConfiguratorImpl(Clearent_VP3300 clearentVp3300, ClearentOnReceiverListener clearentOnReceiverListener) {
        this.clearentVp3300 = clearentVp3300;
        this.clearentOnReceiverListener = clearentOnReceiverListener;
    }

    @Override
    public void configure() {
        setTerminalMajorConfiguration();
        initClock();
        configureReader();
    }

    private void configureReader() {
        ConfigFetchRequest configFetchRequest = createConfigFetchRequest();
        ClearentConfigFetcherResponseHandler clearentConfigFetcherResponseHandler = new ClearentConfigFetcherResponseHandler(clearentVp3300);
        ClearentConfigFetcher clearentConfigFetcher = new ClearentConfigFetcherImpl(configFetchRequest);
        clearentConfigFetcher.fetchConfiguration(clearentConfigFetcherResponseHandler);
    }

    private ConfigFetchRequest createConfigFetchRequest() {
        String kernelVersion = clearentVp3300.getKernelVersion();
        String deviceSerialNumber = clearentVp3300.getDeviceSerialNumber();
        ConfigFetchRequest configFetchRequest = new ConfigFetchRequest(clearentVp3300.getPaymentsBaseUrl(), clearentVp3300.getPaymentsPublicKey(), deviceSerialNumber, kernelVersion);
        return configFetchRequest;

    }
    private void setTerminalMajorConfiguration() {
        ResDataStruct resDataStruct = new ResDataStruct();
        int commandRt = clearentVp3300.device_sendDataCommand("6016", false, "05", resDataStruct);
        if (commandRt == ErrorCode.SUCCESS) {
            String info = "Terminal Major Configuration Succeeded ";
            Log.i("INFO", info);
        } else {
            String info = "Reader failed to configure (terminal major). ";
            info += "Status: " + clearentVp3300.device_getResponseCodeString(commandRt) + "";
            String[] message = {info};
            clearentOnReceiverListener.lcdDisplay(0, message, 0);
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
        return clearentVp3300.device_sendDataCommand("2503", false, clockDate, resDataStruct);
    }

    private String getClockDateAsYYYYMMDD() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        String date = simpleDateFormat.format(new Date());
        return date;
    }

    private int initClockTime() {
        String clockTime = getClockTimeAsHHMM();
        ResDataStruct resDataStruct = new ResDataStruct();
        return clearentVp3300.device_sendDataCommand("2501", false, clockTime, resDataStruct);
    }

    private String getClockTimeAsHHMM() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HHmm");
        String time = simpleDateFormat.format(new Date());
        return time;
    }
}
