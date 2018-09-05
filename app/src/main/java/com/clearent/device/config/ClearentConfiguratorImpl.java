package com.clearent.device.config;

import com.clearent.device.Clearent_VP3300;
import com.idtechproducts.device.ErrorCode;
//TODO singleton ?
public class ClearentConfiguratorImpl implements ClearentConfigurator {

    private static final String RELATIVE_URL = "rest/v2/mobile/devices";

    private boolean configured = false;

    public ClearentConfiguratorImpl() {

    }

    @Override
    public void configure(Clearent_VP3300 clearentVp3300) {
        if(configured) {
            //TODO notify already configured
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
            //notify through listener...
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
            //notify through listener...
            return;
        }

        //TODO init the clock
//    [self initClock];
//
//        ClearentConfigFetcher *clearentConfigFetcher = [[ClearentConfigFetcher alloc] init:[NSURLSession sharedSession] baseUrl:self.baseUrl deviceSerialNumber:deviceSerialNumber kernelVersion:kernelVersion publicKey:self.publicKey];
//
//        ClearentConfigFetcherResponse clearentConfigFetcherResponse = ^(NSDictionary *json) {
//            if(json != nil) {
//            [self configure:json];
//            } else {
//            [self notify:@"VIVOpay failed to retrieve configuration"];
//            }
//        };
//
//    [clearentConfigFetcher fetchConfiguration: clearentConfigFetcherResponse];
        ClearentConfigFetcherResponseHandler clearentConfigFetcherResponseHandler = new ClearentConfigFetcherResponseHandler(clearentVp3300);
        ClearentConfigFetcher clearentConfigFetcher = new ClearentConfigFetcherImpl(clearentVp3300.getPaymentsBaseUrl(),clearentVp3300.getPaymentsPublicKey(),stringBuilderSerialNumber.toString(),stringBuilderKernelVersion.toString());
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
}
