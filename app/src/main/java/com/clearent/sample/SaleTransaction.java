package com.clearent.sample;

import com.google.gson.annotations.SerializedName;

public class SaleTransaction {

    @SerializedName("software-type")
    private String softwareType;

    @SerializedName("software-type-version")
    private String softwareTypeVersion;

    @SerializedName("type")
    private String type = "SALE";

    @SerializedName("amount")
    private String amount;

    @SerializedName("create-token")
    private String createToken;

    public SaleTransaction(String amount) {
        this.amount = amount;
    }

    public String getSoftwareType() {
        return softwareType;
    }

    public void setSoftwareType(String softwareType) {
        this.softwareType = softwareType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCreateToken() {
        return createToken;
    }

    public void setCreateToken(String createToken) {
        this.createToken = createToken;
    }

    public String getSoftwareTypeVersion() {
        return softwareTypeVersion;
    }

    public void setSoftwareTypeVersion(String softwareTypeVersion) {
        this.softwareTypeVersion = softwareTypeVersion;
    }
}
