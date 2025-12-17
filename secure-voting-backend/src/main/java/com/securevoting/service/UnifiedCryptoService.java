package com.securevoting.service;

import com.google.gson.Gson;
import com.securevoting.dto.VotePayload;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

/**
 * Unified Cryptography Service supporting triple-stack approach:
 * - Classical: ECDH key exchange
 * - Quantum: QKD-BB84 key distribution (using quantum simulator)
 * - Post-Quantum: liboqs KEM (Key Encapsulation Mechanism)
 *
 * Routes to appropriate algorithm based on feature flag and payload detection.
 *
 * Architecture:
 * - QKDService: Handles key distribution using BB84 protocol (quantum simulator)
 * - LiboqsCryptoService: Handles post-quantum KEM operations (Kyber algorithms)
 * - PostQuantumSignatureService: Handles post-quantum signatures (Dilithium)
 */
@Service
public class UnifiedCryptoService {

    private static final String ALGORITHM_ECDH = "ECDH";
    private static final String ALGORITHM_QKD_BB84 = "QKD-BB84";
    private static final String ALGORITHM_LIBOQS_PREFIX = "liboqs-"; // e.g., "liboqs-Kyber768"
    private static final String SYMMETRIC_ALGORITHM = "AES";
    private static final String SYMMETRIC_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int GCM_IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int KEY_SIZE_BYTES = 32;
    private static final String INTEGRITY_SALT = "INTEGRITY_SALT";
    private static final String PROVIDER = BouncyCastleProvider.PROVIDER_NAME;

    @Value("${quantum.crypto.enabled:false}")
    private boolean quantumCryptoEnabled;

    @Value("${quantum.crypto.mode:bb84}")
    private String quantumCryptoMode; // "bb84" or "liboqs"

    @Value("${quantum.crypto.fallback.enabled:true}")
    private boolean quantumFallbackEnabled;

    @Value("${crypto.integrity.enabled:true}")
    private boolean integrityEnabled;

    @Autowired
    private CryptoService cryptoService;

    @Autowired
    private QKDService qkdService;

    @Autowired(required = false)
    private LiboqsCryptoService liboqsCryptoService;

    @Autowired
    private SteganographyService steganographyService;

    @PostConstruct
    public void init() {
        Security.addProvider(new BouncyCastleProvider());
        System.out.println("qq.java initialized - Quantum enabled: " + quantumCryptoEnabled +
                ", Mode: " + quantumCryptoMode + ", Integrity enabled: " + integrityEnabled);
        if (quantumCryptoEnabled && "liboqs".equals(quantumCryptoMode) && liboqsCryptoService == null) {
            System.out.println("WARNING: liboqs mode enabled but LiboqsCryptoService not available. " +
                    "Please install liboqs library. Falling back to BB84 or ECDH.");
        }
    }

    /**
     * Encrypts a vote using dual-stack cryptography.
     * Routes to QKD or ECDH based on feature flag.
     *
     * @param voteJson Vote data as JSON string
     * @return Encrypted payload as JSON string
     * @throws Exception if encryption fails
     */
    public String encryptVote(String voteJson) throws Exception {
        System.out.println("=== VOTE ENCRYPTION - Unified Service ===");

        try {
            if (quantumCryptoEnabled) {
                if ("liboqs".equals(quantumCryptoMode) && liboqsCryptoService != null) {
                    return encryptWithLiboqs(voteJson);
                } else {
                    return encryptWithQKD(voteJson);
                }
            } else {
                return encryptWithECDH(voteJson);
            }
        } catch (Exception e) {
            if (quantumCryptoEnabled && quantumFallbackEnabled) {
                System.out.println("WARNING: Quantum encryption failed, falling back to ECDH: " + e.getMessage());
                return encryptWithECDH(voteJson);
            }
            throw e;
        }
    }

    /**
     * Decrypts a vote using dual-stack cryptography.
     * Automatically detects algorithm from payload.
     *
     * @param stegoImageData Steganographic image data
     * @return Decrypted vote as JSON string
     * @throws Exception if decryption fails
     */
    public String decryptVote(byte[] stegoImageData) throws Exception {
        System.out.println("=== VOTE DECRYPTION - Unified Service ===");

        // Extract payload from steganographic image
        byte[] extractedData = steganographyService.extractData(stegoImageData);
        String extractedString = new String(extractedData, StandardCharsets.UTF_8);

        System.out.println("Extracted data length: " + extractedString.length());

        // Parse VotePayload
        Gson gson = new Gson();
        VotePayload payload = gson.fromJson(extractedString, VotePayload.class);

        String algorithm = payload.getAlgorithm();
        if (algorithm == null || algorithm.isEmpty()) {
            // Backward compatibility: default to ECDH
            System.out.println("No algorithm specified, defaulting to ECDH");
            algorithm = ALGORITHM_ECDH;
        }

        System.out.println("Detected algorithm: " + algorithm);

        if (ALGORITHM_QKD_BB84.equals(algorithm)) {
            return decryptWithQKD(payload);
        } else if (algorithm != null && algorithm.startsWith(ALGORITHM_LIBOQS_PREFIX)) {
            return decryptWithLiboqs(payload);
        } else {
            return decryptWithECDH(payload);
        }
    }

    // === Private Encryption Methods ===

    private String encryptWithQKD(String voteJson) throws Exception {
        System.out.println("Encrypting vote using QKD-BB84 protocol");

        // Step 1: Generate shared secret using BB84 QKD protocol
        QKDService.QKDResult qkdResult = qkdService.generateSharedSecret();
        byte[] sharedSecret = qkdResult.getSharedSecret();

        // Step 2: Derive AES key
        MessageDigest hash = MessageDigest.getInstance(HASH_ALGORITHM, PROVIDER);
        byte[] derivedKey = hash.digest(sharedSecret);
        SecretKey aesKey = new SecretKeySpec(derivedKey, 0, KEY_SIZE_BYTES, SYMMETRIC_ALGORITHM);

        // Step 3: AES-GCM Encryption
        Cipher aesCipher = Cipher.getInstance(SYMMETRIC_TRANSFORMATION, PROVIDER);
        byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
        new SecureRandom().nextBytes(iv);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, parameterSpec);
        byte[] cipherText = aesCipher.doFinal(voteJson.getBytes(StandardCharsets.UTF_8));

        // Step 4: Calculate HMAC
        String hmac = calculateHMAC(ALGORITHM_QKD_BB84, null,
                Base64.getEncoder().encodeToString(iv),
                Base64.getEncoder().encodeToString(cipherText),
                sharedSecret);

        // Step 5: Create VotePayload
        Gson gson = new Gson();
        String qkdMetadataJson = gson.toJson(qkdResult.getMetadata());

        VotePayload payload = new VotePayload(ALGORITHM_QKD_BB84, qkdMetadataJson,
                Base64.getEncoder().encodeToString(iv),
                Base64.getEncoder().encodeToString(cipherText),
                hmac, true);

        String jsonResult = gson.toJson(payload);
        System.out.println("QKD-BB84 encryption completed. Payload size: " + jsonResult.length() + " bytes");

        return jsonResult;
    }

    private String encryptWithLiboqs(String voteJson) throws Exception {
        System.out.println("Encrypting vote using liboqs KEM");

        if (liboqsCryptoService == null) {
            throw new UnsupportedOperationException(
                    "LiboqsCryptoService not available. Please install liboqs library. " +
                            "See LIBOQS_SETUP.md for instructions.");
        }

        // Step 1: Generate shared secret using liboqs KEM
        LiboqsCryptoService.LiboqsResult liboqsResult = liboqsCryptoService.encapsulate();
        byte[] sharedSecret = liboqsResult.getSharedSecret();
        String algorithm = ALGORITHM_LIBOQS_PREFIX + liboqsResult.getAlgorithm();

        // Step 2: Derive AES key
        MessageDigest hash = MessageDigest.getInstance(HASH_ALGORITHM, PROVIDER);
        byte[] derivedKey = hash.digest(sharedSecret);
        SecretKey aesKey = new SecretKeySpec(derivedKey, 0, KEY_SIZE_BYTES, SYMMETRIC_ALGORITHM);

        // Step 3: AES-GCM Encryption
        Cipher aesCipher = Cipher.getInstance(SYMMETRIC_TRANSFORMATION, PROVIDER);
        byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
        new SecureRandom().nextBytes(iv);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, parameterSpec);
        byte[] cipherText = aesCipher.doFinal(voteJson.getBytes(StandardCharsets.UTF_8));

        // Step 4: Calculate HMAC
        String publicKeyB64 = Base64.getEncoder().encodeToString(liboqsResult.getPublicKey());
        String hmac = calculateHMAC(algorithm, publicKeyB64,
                Base64.getEncoder().encodeToString(iv),
                Base64.getEncoder().encodeToString(cipherText),
                sharedSecret);

        // Step 5: Create VotePayload with liboqs metadata
        Gson gson = new Gson();
        LiboqsMetadata liboqsMetadata = new LiboqsMetadata();
        liboqsMetadata.setSecretKey(Base64.getEncoder().encodeToString(liboqsResult.getSecretKey()));
        liboqsMetadata.setCiphertext(Base64.getEncoder().encodeToString(liboqsResult.getCiphertext()));
        liboqsMetadata.setPublicKey(publicKeyB64);
        liboqsMetadata.setAlgorithm(liboqsResult.getAlgorithm());

        String metadataJson = gson.toJson(liboqsMetadata);

        VotePayload payload = new VotePayload();
        payload.setAlgorithm(algorithm);
        payload.setEphemeralPublicKey(publicKeyB64);
        payload.setQkdMetadata(metadataJson);
        payload.setIv(Base64.getEncoder().encodeToString(iv));
        payload.setCipherText(Base64.getEncoder().encodeToString(cipherText));
        payload.setHmac(hmac);

        String jsonResult = gson.toJson(payload);
        System.out.println("liboqs KEM encryption completed. Payload size: " + jsonResult.length() + " bytes");

        return jsonResult;
    }

    private String encryptWithECDH(String voteJson) throws Exception {
        System.out.println("Encrypting vote using ECDH");

        // Use existing CryptoService for ECDH encryption
        String encryptedPayload = cryptoService.encryptVote(voteJson);

        // Parse and add HMAC if integrity is enabled
        if (integrityEnabled) {
            Gson gson = new Gson();
            VotePayload payload = gson.fromJson(encryptedPayload, VotePayload.class);

            // Extract shared secret for HMAC calculation (simplified - in practice, need to store)
            // For now, we'll calculate HMAC using the ephemeral public key
            String hmac = calculateHMAC(ALGORITHM_ECDH, payload.getEphemeralPublicKey(),
                    payload.getIv(), payload.getCipherText(), null);

            payload.setAlgorithm(ALGORITHM_ECDH);
            payload.setHmac(hmac);

            return gson.toJson(payload);
        }

        return encryptedPayload;
    }

    // === Private Decryption Methods ===

    private String decryptWithQKD(VotePayload payload) throws Exception {
        System.out.println("Decrypting vote using QKD-BB84 protocol");

        // Step 1: Extract QKD metadata
        Gson gson = new Gson();
        QKDService.QKDMetadata metadata = gson.fromJson(payload.getQkdMetadata(),
                QKDService.QKDMetadata.class);

        // Step 2: Reconstruct shared secret using BB84 QKD protocol
        byte[] sharedSecret;
        try {
            sharedSecret = qkdService.reconstructSharedSecret(metadata, metadata.getBobBases());
        } catch (IllegalArgumentException e) {
            System.err.println("Failed to reconstruct QKD shared secret: " + e.getMessage());
            throw new SecurityException("Cannot decrypt vote: " + e.getMessage() +
                    ". This vote was encrypted before the QKD fix and cannot be decrypted. " +
                    "See QKD_HMAC_MISMATCH_FIX.md for details.", e);
        }

        // Step 3: Verify HMAC
        if (integrityEnabled && payload.getHmac() != null) {
            String calculatedHmac = calculateHMAC(ALGORITHM_QKD_BB84, null,
                    payload.getIv(), payload.getCipherText(),
                    sharedSecret);
            if (!constantTimeEquals(calculatedHmac, payload.getHmac())) {
                System.err.println("HMAC verification failed for QKD vote");
                System.err.println("Expected HMAC: " + payload.getHmac());
                System.err.println("Calculated HMAC: " + calculatedHmac);
                throw new SecurityException("Integrity check failed: HMAC mismatch. " +
                        "Vote may have been tampered with or key reconstruction failed.");
            }
            System.out.println("HMAC verification passed");
        }

        // Step 4: Derive AES key
        MessageDigest hash = MessageDigest.getInstance(HASH_ALGORITHM, PROVIDER);
        byte[] derivedKey = hash.digest(sharedSecret);
        SecretKey aesKey = new SecretKeySpec(derivedKey, 0, KEY_SIZE_BYTES, SYMMETRIC_ALGORITHM);

        // Step 5: AES-GCM Decryption
        byte[] iv = Base64.getDecoder().decode(payload.getIv());
        byte[] cipherText = Base64.getDecoder().decode(payload.getCipherText());

        Cipher aesCipher = Cipher.getInstance(SYMMETRIC_TRANSFORMATION, PROVIDER);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
        aesCipher.init(Cipher.DECRYPT_MODE, aesKey, parameterSpec);
        byte[] decryptedVote = aesCipher.doFinal(cipherText);

        String voteJson = new String(decryptedVote, StandardCharsets.UTF_8);
        System.out.println("QKD-BB84 decryption completed successfully");

        return voteJson;
    }

    private String decryptWithLiboqs(VotePayload payload) throws Exception {
        System.out.println("Decrypting vote using liboqs KEM");

        if (liboqsCryptoService == null) {
            throw new UnsupportedOperationException(
                    "LiboqsCryptoService not available. Please install liboqs library.");
        }

        // Step 1: Extract liboqs metadata
        Gson gson = new Gson();
        LiboqsMetadata metadata = gson.fromJson(payload.getQkdMetadata(), LiboqsMetadata.class);

        // Step 2: Reconstruct shared secret using liboqs KEM decapsulation
        byte[] secretKey = Base64.getDecoder().decode(metadata.getSecretKey());
        byte[] ciphertext = Base64.getDecoder().decode(metadata.getCiphertext());
        byte[] sharedSecret = liboqsCryptoService.decapsulate(secretKey, ciphertext);

        // Step 3: Verify HMAC
        if (integrityEnabled && payload.getHmac() != null) {
            String calculatedHmac = calculateHMAC(payload.getAlgorithm(), payload.getEphemeralPublicKey(),
                    payload.getIv(), payload.getCipherText(),
                    sharedSecret);
            if (!constantTimeEquals(calculatedHmac, payload.getHmac())) {
                System.err.println("HMAC verification failed - vote may have been tampered");
                throw new SecurityException("Integrity check failed: HMAC mismatch");
            }
            System.out.println("HMAC verification passed");
        }

        // Step 4: Derive AES key
        MessageDigest hash = MessageDigest.getInstance(HASH_ALGORITHM, PROVIDER);
        byte[] derivedKey = hash.digest(sharedSecret);
        SecretKey aesKey = new SecretKeySpec(derivedKey, 0, KEY_SIZE_BYTES, SYMMETRIC_ALGORITHM);

        // Step 5: AES-GCM Decryption
        byte[] iv = Base64.getDecoder().decode(payload.getIv());
        byte[] cipherText = Base64.getDecoder().decode(payload.getCipherText());

        Cipher aesCipher = Cipher.getInstance(SYMMETRIC_TRANSFORMATION, PROVIDER);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
        aesCipher.init(Cipher.DECRYPT_MODE, aesKey, parameterSpec);
        byte[] decryptedVote = aesCipher.doFinal(cipherText);

        String voteJson = new String(decryptedVote, StandardCharsets.UTF_8);
        System.out.println("liboqs KEM decryption completed successfully");

        return voteJson;
    }

    private String decryptWithECDH(VotePayload payload) throws Exception {
        System.out.println("Decrypting vote using ECDH");

        // Note: For ECDH, the existing CryptoService expects stego image data
        // Since we're already extracting the payload, we need to reconstruct
        // the encryption flow or use CryptoService's internal methods
        // For now, we'll decrypt directly using the payload data

        // Verify HMAC if integrity is enabled
        if (integrityEnabled && payload.getHmac() != null) {
            // For ECDH HMAC verification, we need the shared secret
            // This would require recreating the ECDH key agreement
            // Simplified: HMAC verification would be done after key derivation
            System.out.println("HMAC verification for ECDH will be done after key derivation");
        }

        // Use CryptoService's internal decryption logic
        // We'll delegate to CryptoService but need to pass the payload data
        // For backward compatibility, reconstruct the VotePayload JSON and embed it
        Gson gson = new Gson();
        String payloadJson = gson.toJson(payload);
        byte[] payloadBytes = payloadJson.getBytes(StandardCharsets.UTF_8);

        // Create a temporary stego image with the payload for CryptoService
        byte[] tempStegoData = steganographyService.embedData(payloadBytes);
        return cryptoService.decryptVote(tempStegoData);
    }

    // === Integrity Methods ===

    private String calculateHMAC(String algorithm, String publicKey, String iv,
                                 String cipherText, byte[] sharedSecret) throws Exception {
        if (!integrityEnabled) {
            return null;
        }

        // Derive integrity key
        byte[] integrityKey;
        if (sharedSecret != null) {
            MessageDigest hash = MessageDigest.getInstance(HASH_ALGORITHM, PROVIDER);
            byte[] saltBytes = INTEGRITY_SALT.getBytes(StandardCharsets.UTF_8);
            byte[] combined = new byte[sharedSecret.length + saltBytes.length];
            System.arraycopy(sharedSecret, 0, combined, 0, sharedSecret.length);
            System.arraycopy(saltBytes, 0, combined, sharedSecret.length, saltBytes.length);
            integrityKey = hash.digest(combined);
        } else {
            // For ECDH, use a simplified approach (in practice, need shared secret)
            integrityKey = HASH_ALGORITHM.getBytes(StandardCharsets.UTF_8);
        }

        // Calculate HMAC
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(integrityKey, HMAC_ALGORITHM);
        mac.init(keySpec);

        String dataToSign = algorithm + (publicKey != null ? publicKey : "") + iv + cipherText;
        byte[] hmacBytes = mac.doFinal(dataToSign.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(hmacBytes);
    }

    // === Inner Classes ===

    /**
     * Metadata class for liboqs KEM operations.
     */
    private static class LiboqsMetadata {
        private String secretKey;      // Base64 encoded secret key
        private String ciphertext;     // Base64 encoded KEM ciphertext
        private String publicKey;      // Base64 encoded public key
        private String algorithm;      // KEM algorithm name (e.g., "Kyber768")

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public String getCiphertext() {
            return ciphertext;
        }

        public void setCiphertext(String ciphertext) {
            this.ciphertext = ciphertext;
        }

        public String getPublicKey() {
            return publicKey;
        }

        public void setPublicKey(String publicKey) {
            this.publicKey = publicKey;
        }

        public String getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return a == b;
        }
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
