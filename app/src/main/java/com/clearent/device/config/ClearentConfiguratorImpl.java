package com.clearent.device.config;

import android.util.Log;

import com.clearent.device.DeviceConfigurable;
import com.clearent.device.domain.CommunicationRequest;
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
        initClock();
        configureReader(communicationRequest);
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
