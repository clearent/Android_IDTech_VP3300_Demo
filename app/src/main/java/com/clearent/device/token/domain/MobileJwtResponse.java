package com.clearent.device.token.domain;

public class MobileJwtResponse {

    private MobileJwtErrorResponse mobileJwtErrorResponse;
    private MobileJwtSuccessResponse mobileJwtSuccessResponse;

    public MobileJwtResponse() {
    }

    public MobileJwtResponse(MobileJwtSuccessResponse mobileJwtSuccessResponse) {
        this.mobileJwtSuccessResponse = mobileJwtSuccessResponse;
    }

    public MobileJwtResponse(MobileJwtErrorResponse mobileJwtErrorResponse) {
        this.mobileJwtErrorResponse = mobileJwtErrorResponse;
    }

    public MobileJwtErrorResponse getMobileJwtErrorResponse() {
        return mobileJwtErrorResponse;
    }

    public MobileJwtSuccessResponse getMobileJwtSuccessResponse() {
        return mobileJwtSuccessResponse;
    }
}
