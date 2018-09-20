package com.clearent.sample;

import com.google.gson.annotations.SerializedName;

public class SaleTransaction {

    @SerializedName("software-type")
    private String softwareType = "IDTECH";

    @SerializedName("software-version")
    private String softwareVersion = "android-idtech-vp3300-demo-1.0";

    @SerializedName("type")
    private String type = "SALE";

    @SerializedName("amount")
    private String amount;

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
}
