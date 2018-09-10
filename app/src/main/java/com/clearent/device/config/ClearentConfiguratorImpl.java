package com.clearent.device.config;

import com.clearent.device.ClearentOnReceiverListener;
import com.clearent.device.Clearent_VP3300;
import com.clearent.device.config.domain.ConfigFetchRequest;
import com.idtechproducts.device.ErrorCode;

public class ClearentConfiguratorImpl implements ClearentConfigurator {

    private static final String RELATIVE_URL = "rest/v2/mobile/devices";

    private boolean configured = false;
    private Clearent_VP3300 clearentVp3300;
    private ClearentOnReceiverListener clearentOnReceiverListener;

    public ClearentConfiguratorImpl(Clearent_VP3300 clearentVp3300, ClearentOnReceiverListener clearentOnReceiverListener) {
       this.clearentVp3300 = clearentVp3300;
       this.clearentOnReceiverListener = clearentOnReceiverListener;
    }

    @Override
    public void configure() {
        if(configured) {
            String[] message = {"VIVOpay configured and ready"};
            clearentOnReceiverListener.lcdDisplay(0,message,0);
            return;
        }

        StringBuilder stringBuilderSerialNumber = new StringBuilder();
        int serialNumberRt = clearentVp3300.config_getSerialNumber(stringBuilderSerialNumber);
        if (serialNumberRt == ErrorCode.SUCCESS) {
            String info = "Serial Number: " + stringBuilderSerialNumber.toString();
            System.out.println("serial number is " + info);
        } else {
            String info = "GetSerialNumber: Failed\n";
            info += "Status: " + clearentVp3300.device_getResponseCodeString(serialNumberRt) + "";
            System.out.println(info);
            String[] message = {info};
            clearentOnReceiverListener.lcdDisplay(0,message,0);
            return;
        }

        StringBuilder stringBuilderKernelVersion = new StringBuilder();
        int kernelVersionRt = clearentVp3300.emv_getEMVKernelVersion(stringBuilderKernelVersion);
        if (kernelVersionRt == ErrorCode.SUCCESS) {
            String info = "Kernel Version: " + stringBuilderKernelVersion.toString();
            System.out.println("kernel version is " + info);
        } else {
            String info = "Kernel version: Failed\n";
            info += "Status: " + clearentVp3300.device_getResponseCodeString(kernelVersionRt) + "";
            System.out.println(info);
            String[] message = {info};
            clearentOnReceiverListener.lcdDisplay(0,message,0);
            return;
        }

        //TODO init the clock
//    [self initClock];
//
        ConfigFetchRequest configFetchRequest = new ConfigFetchRequest(clearentVp3300.getPaymentsBaseUrl(),clearentVp3300.getPaymentsPublicKey(),stringBuilderSerialNumber.toString(),stringBuilderKernelVersion.toString());
        ClearentConfigFetcherResponseHandler clearentConfigFetcherResponseHandler = new ClearentConfigFetcherResponseHandler(clearentVp3300, this);
        ClearentConfigFetcher clearentConfigFetcher = new ClearentConfigFetcherImpl(configFetchRequest);
        clearentConfigFetcher.fetchConfiguration(clearentConfigFetcherResponseHandler);
    }

//
//    - (int) initClock {
//        RETURN_CODE dateRt = [self initClockDate];
//        RETURN_CODE timeRt = [self initClockTime];
//        if (RETURN_CODE_DO_SUCCESS == dateRt && RETURN_CODE_DO_SUCCESS == timeRt) {
//            NSLog(@"Clock Initialized");
//        } else {
//            return CLOCK_FAILED;
//        }
//        return CLOCK_CONFIGURATION_SUCCESS;
//    }
//
//- (int) initClockDate {
//        NSData *clockDate = [self getClockDateAsYYYYMMDD];
//        NSData *result;
//        return [_sharedController device_sendIDGCommand:0x25 subCommand:0x03 data:clockDate response:&result];
//    }
//
//- (NSData*) getClockDateAsYYYYMMDD {
//        NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
//        dateFormatter.dateFormat = @"yyyyMMdd";
//        NSString *dateString = [dateFormatter stringFromDate:[NSDate date]];
//        return [IDTUtility hexToData:dateString];
//    }
//
//- (int) initClockTime {
//        NSData *timeDate = [self getClockTimeAsHHMM];
//        NSData *result;
//        return [_sharedController device_sendIDGCommand:0x25 subCommand:0x01 data:timeDate response:&result];
//    }
//
//- (NSData*) getClockTimeAsHHMM {
//        NSDateFormatter *timeFormatter = [[NSDateFormatter alloc] init];
//        timeFormatter.dateFormat = @"HHMM";
//        NSString *timeString = [timeFormatter stringFromDate:[NSDate date]];
//        return [IDTUtility hexToData:timeString];
//    }
//


    @Override
    public boolean isConfigured() {
        return configured;
    }

    @Override
    public void notifyReady() {
        this.configured = true;
        String[] message = {"VIVOpay configured and ready"};
        clearentOnReceiverListener.lcdDisplay(0,message,0);
    }

    @Override
    public void notifyFailure(String message) {
        String[] messageArray = {message};
        clearentOnReceiverListener.lcdDisplay(0,messageArray,0);
    }
}
