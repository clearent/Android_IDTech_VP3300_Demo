package com.clearent.device.config;

//TODO new object needed ?
public class ClearentConfigFetcherImpl implements ClearentConfigFetcher {

    private String baseUrl;
    private String publicKey;
    private String deviceSerialNumber;
    private String kernelVersion;

    public ClearentConfigFetcherImpl(String baseUrl, String publicKey, String deviceSerialNumber, String kernelVersion) {
        this.baseUrl = baseUrl;
        this.publicKey = publicKey;
        this.deviceSerialNumber = deviceSerialNumber;
        this.kernelVersion = kernelVersion;
    }

    @Override
    public void fetchConfiguration(ClearentConfigFetcherResponseHandler clearentConfigFetcherResponseHandler) {
             //TODO rest call to mobile-devices
        //call handler method onsuccess. otherwise send null back. the handler will communicate the results
        clearentConfigFetcherResponseHandler.handleResponse("this is the mobile device response");
    }


//
//- (NSMutableURLRequest*) createNSMutableURLRequest {
//        NSMutableURLRequest *nSMutableURLRequest = [[NSMutableURLRequest alloc] init];
//    [nSMutableURLRequest setHTTPMethod:@"GET"];
//    [nSMutableURLRequest setValue:@"application/json" forHTTPHeaderField:@"Accept"];
//    [nSMutableURLRequest setValue:_publicKey  forHTTPHeaderField:@"public-key"];
//    [nSMutableURLRequest setURL:[NSURL URLWithString:[self createTargetUrl]]];
//        return nSMutableURLRequest;
//    }
//
//- (NSString*) createTargetUrl {
//        NSString *trimmedDeviceSerialNumber = [_deviceSerialNumber substringToIndex:10];
//        NSString *urlEncodedKernelVersion = [_kernelVersion stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLHostAllowedCharacterSet]];
//        return [NSString stringWithFormat:@"%@/%@/%@/%@", _baseUrl, RELATIVE_URL,  trimmedDeviceSerialNumber, urlEncodedKernelVersion];
//    }
}
