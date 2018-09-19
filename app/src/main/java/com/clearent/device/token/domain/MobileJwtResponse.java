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

    public void setMobileJwtErrorResponse(MobileJwtErrorResponse mobileJwtErrorResponse) {
        this.mobileJwtErrorResponse = mobileJwtErrorResponse;
    }

    public MobileJwtSuccessResponse getMobileJwtSuccessResponse() {
        return mobileJwtSuccessResponse;
    }

    public void setMobileJwtSuccessResponse(MobileJwtSuccessResponse mobileJwtSuccessResponse) {
        this.mobileJwtSuccessResponse = mobileJwtSuccessResponse;
    }
}
