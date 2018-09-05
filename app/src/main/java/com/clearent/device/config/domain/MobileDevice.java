package com.clearent.device.config.domain;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class MobileDevice {

    @SerializedName("contact-aids")
    List<MobileContactAid> contactAids;

    @SerializedName("contactless-aids")
    List<MobileContactAid> contactlessAids;

    @SerializedName("ca-public-keys")
    List<CaPublicKey> caPublicKeys;

    public MobileDevice() {
    }

    public List<MobileContactAid> getContactAids() {
        return contactAids;
    }

    public void setContactAids(List<MobileContactAid> contactAids) {
        this.contactAids = contactAids;
    }

    public List<MobileContactAid> getContactlessAids() {
        return contactlessAids;
    }

    public void setContactlessAids(List<MobileContactAid> contactlessAids) {
        this.contactlessAids = contactlessAids;
    }

    public List<CaPublicKey> getCaPublicKeys() {
        return caPublicKeys;
    }

    public void setCaPublicKeys(List<CaPublicKey> caPublicKeys) {
        this.caPublicKeys = caPublicKeys;
    }
}
