package com.securevoting.dto;

/**
 * Represents the structured payload for an encrypted vote.
 * This object is typically serialized to JSON before being embedded in the steganographic image.
 */
public class VotePayload {

    private String ephemeralPublicKey;
    private String iv;
    private String cipherText;

    /**
     * No-argument constructor for deserialization.
     */
    public VotePayload() {
    }

    /**
     * Constructor to create a new payload.
     * @param ephemeralPublicKey Base64 encoded ephemeral public key.
     * @param iv Base64 encoded initialization vector.
     * @param cipherText Base64 encoded ciphertext.
     */
    public VotePayload(String ephemeralPublicKey, String iv, String cipherText) {
        this.ephemeralPublicKey = ephemeralPublicKey;
        this.iv = iv;
        this.cipherText = cipherText;
    }

    // --- Getters and Setters ---

    public String getEphemeralPublicKey() {
        return ephemeralPublicKey;
    }

    public void setEphemeralPublicKey(String ephemeralPublicKey) {
        this.ephemeralPublicKey = ephemeralPublicKey;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public String getCipherText() {
        return cipherText;
    }

    public void setCipherText(String cipherText) {
        this.cipherText = cipherText;
    }
}