package com.securevoting.dto;

/**
 * Represents the structured payload for an encrypted vote.
 * This object is typically serialized to JSON before being embedded in the steganographic image.
 * Supports both classical (ECDH) and quantum (QKD-BB84) key distribution methods.
 */
public class VotePayload {

    /**
     * Algorithm used for key exchange: "ECDH" or "QKD-BB84"
     */
    private String algorithm;
    
    /**
     * Ephemeral public key (for ECDH algorithm).
     * Base64 encoded ECC public key.
     */
    private String ephemeralPublicKey;
    
    /**
     * QKD metadata (for QKD-BB84 algorithm).
     * Contains bases, error rate, and other QKD protocol parameters.
     * JSON string containing QKD session information.
     */
    private String qkdMetadata;
    
    /**
     * Initialization vector for AES-GCM encryption.
     * Base64 encoded IV (12 bytes).
     */
    private String iv;
    
    /**
     * Encrypted ciphertext.
     * Base64 encoded AES-GCM ciphertext.
     */
    private String cipherText;
    
    /**
     * HMAC-SHA256 tag for integrity verification.
     * Base64 encoded HMAC (32 bytes).
     */
    private String hmac;
    
    /**
     * Version number for future algorithm migrations.
     * Optional field for backward compatibility.
     */
    private Integer version;

    /**
     * No-argument constructor for deserialization.
     */
    public VotePayload() {
    }

    /**
     * Constructor for ECDH algorithm.
     * @param algorithm Algorithm identifier ("ECDH")
     * @param ephemeralPublicKey Base64 encoded ephemeral public key.
     * @param iv Base64 encoded initialization vector.
     * @param cipherText Base64 encoded ciphertext.
     * @param hmac Base64 encoded HMAC-SHA256 tag.
     */
    public VotePayload(String algorithm, String ephemeralPublicKey, String iv, String cipherText, String hmac) {
        this.algorithm = algorithm;
        this.ephemeralPublicKey = ephemeralPublicKey;
        this.iv = iv;
        this.cipherText = cipherText;
        this.hmac = hmac;
    }

    /**
     * Constructor for QKD-BB84 algorithm.
     * @param algorithm Algorithm identifier ("QKD-BB84")
     * @param qkdMetadata QKD protocol metadata (JSON string).
     * @param iv Base64 encoded initialization vector.
     * @param cipherText Base64 encoded ciphertext.
     * @param hmac Base64 encoded HMAC-SHA256 tag.
     */
    public VotePayload(String algorithm, String qkdMetadata, String iv, String cipherText, String hmac, boolean isQKD) {
        this.algorithm = algorithm;
        this.qkdMetadata = qkdMetadata;
        this.iv = iv;
        this.cipherText = cipherText;
        this.hmac = hmac;
    }

    // --- Getters and Setters ---

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getEphemeralPublicKey() {
        return ephemeralPublicKey;
    }

    public void setEphemeralPublicKey(String ephemeralPublicKey) {
        this.ephemeralPublicKey = ephemeralPublicKey;
    }

    public String getQkdMetadata() {
        return qkdMetadata;
    }

    public void setQkdMetadata(String qkdMetadata) {
        this.qkdMetadata = qkdMetadata;
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

    public String getHmac() {
        return hmac;
    }

    public void setHmac(String hmac) {
        this.hmac = hmac;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}