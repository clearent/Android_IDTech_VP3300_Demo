package com.clearent.device.config.domain;


import com.google.gson.annotations.SerializedName;

public class CaPublicKey {

    @SerializedName("name")
    private String name;

    @SerializedName("rid")
    private String rid;

    @SerializedName("key-index")
    private String keyIndex;

    @SerializedName("hash-algorithm")
    private String hashAlgorithm;

    @SerializedName("encryption-algorithm")
    private String encryptionAlgorithm;

    @SerializedName("hash-value")
    private String hashValue;

    @SerializedName("key-exponent")
    private String keyExponent;

    @SerializedName("modulus")
    private String modulus;

    @SerializedName("big-endian-modulus-length")
    private String bigEndianModulusLength;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRid() {
        return rid;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }

    public String getKeyIndex() {
        return keyIndex;
    }

    public void setKeyIndex(String keyIndex) {
        this.keyIndex = keyIndex;
    }

    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    public void setHashAlgorithm(String hashAlgorithm) {
        this.hashAlgorithm = hashAlgorithm;
    }

    public String getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }

    public void setEncryptionAlgorithm(String encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
    }

    public String getHashValue() {
        return hashValue;
    }

    public void setHashValue(String hashValue) {
        this.hashValue = hashValue;
    }

    public String getKeyExponent() {
        return keyExponent;
    }

    public void setKeyExponent(String keyExponent) {
        this.keyExponent = keyExponent;
    }

    public String getModulus() {
        return modulus;
    }

    public void setModulus(String modulus) {
        this.modulus = modulus;
    }

    public String getBigEndianModulusLength() {
        return bigEndianModulusLength;
    }

    public void setBigEndianModulusLength(String bigEndianModulusLength) {
        this.bigEndianModulusLength = bigEndianModulusLength;
    }

    public String getOrderedValues() {
        return rid + keyIndex + hashAlgorithm + encryptionAlgorithm + hashValue + keyExponent + bigEndianModulusLength + modulus;
    }
}

