package com.clearent.sample;

import com.clearent.idtech.android.token.manual.ManualEntry;

public class CreditCard implements ManualEntry {

    private String card;
    private String expirationDateMMYY;
    private String csc;
    private String softwareType;
    private String softwareTypeVersion;

    @Override
    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
    }

    @Override
    public String getExpirationDateMMYY() {
        return expirationDateMMYY;
    }

    public void setExpirationDateMMYY(String expirationDateMMYY) {
        this.expirationDateMMYY = expirationDateMMYY;
    }

    @Override
    public String getCsc() {
        return csc;
    }

    public void setCsc(String csc) {
        this.csc = csc;
    }

    @Override
    public String getSoftwareType() {
        return softwareType;
    }

    public void setSoftwareType(String softwareType) {
        this.softwareType = softwareType;
    }

    @Override
    public String getSoftwareTypeVersion() {
        return softwareTypeVersion;
    }

    public void setSoftwareTypeVersion(String softwareTypeVersion) {
        this.softwareTypeVersion = softwareTypeVersion;
    }
}
