package com.securevoting.service;

import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.bouncycastle.pqc.jcajce.spec.DilithiumParameterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.security.*;
import java.util.Base64;

/**
 * Post-Quantum Digital Signature Service using BouncyCastle's Dilithium implementation.
 * Provides quantum-resistant digital signatures for vote authentication.
 *
 * Supports Dilithium algorithms (NIST PQC Standard):
 * - Dilithium2: 128-bit security level
 * - Dilithium3: 192-bit security level (recommended)
 * - Dilithium5: 256-bit security level
 *
 * This service complements the encryption services by providing
 * post-quantum signatures for additional security and authentication.
 */
@Service
public class PostQuantumSignatureService {

    private static final Logger logger = LoggerFactory.getLogger(PostQuantumSignatureService.class);
    private static final String PROVIDER = "BCPQC";
    private static final String SIGNATURE_ALGORITHM = "Dilithium";

    @Value("${quantum.crypto.signature.algorithm:Dilithium3}")
    private String signatureAlgorithm;

    @Value("${quantum.crypto.signature.enabled:true}")
    private boolean signatureEnabled;

    private DilithiumParameterSpec dilithiumSpec;
    private KeyPair signingKeyPair;
    private boolean initialized = false;

    @PostConstruct
    public void init() {
        logger.info("PostQuantumSignatureService initializing - Algorithm: {}, Enabled: {}",
                signatureAlgorithm, signatureEnabled);

        if (!signatureEnabled) {
            logger.info("Post-quantum signatures are disabled");
            return;
        }

        try {
            // Register BouncyCastle PQC Provider if not already registered
            if (Security.getProvider(PROVIDER) == null) {
                Security.addProvider(new BouncyCastlePQCProvider());
                logger.info("BouncyCastle PQC Provider registered for signatures");
            }

            // Set Dilithium parameter spec based on configuration
            switch (signatureAlgorithm) {
                case "Dilithium2":
                    dilithiumSpec = DilithiumParameterSpec.dilithium2;
                    break;
                case "Dilithium5":
                    dilithiumSpec = DilithiumParameterSpec.dilithium5;
                    break;
                case "Dilithium3":
                default:
                    dilithiumSpec = DilithiumParameterSpec.dilithium3;
                    signatureAlgorithm = "Dilithium3";
                    break;
            }

            // Generate a default signing key pair for the service
            generateNewKeyPair();

            initialized = true;
            logger.info("PostQuantumSignatureService initialized successfully with algorithm: {}", signatureAlgorithm);
            logger.info("Post-quantum signatures ready - Security Level: {}", getSecurityLevel());

        } catch (Exception e) {
            logger.error("Failed to initialize PostQuantumSignatureService: {}", e.getMessage());
            logger.error("Post-quantum signatures will not be available");
            initialized = false;
        }
    }

    /**
     * Generate a new Dilithium key pair for signing.
     *
     * @return The generated KeyPair
     * @throws Exception if key generation fails
     */
    public KeyPair generateNewKeyPair() throws Exception {
        logger.info("Generating new Dilithium key pair - Algorithm: {}", signatureAlgorithm);

        KeyPairGenerator kpg = KeyPairGenerator.getInstance(SIGNATURE_ALGORITHM, PROVIDER);
        kpg.initialize(dilithiumSpec, new SecureRandom());
        signingKeyPair = kpg.generateKeyPair();

        logger.info("Dilithium key pair generated - Public key: {} bytes, Private key: {} bytes",
                signingKeyPair.getPublic().getEncoded().length,
                signingKeyPair.getPrivate().getEncoded().length);

        return signingKeyPair;
    }

    /**
     * Check if the service is properly initialized and ready to use.
     */
    public boolean isInitialized() {
        return initialized && signatureEnabled;
    }

    /**
     * Sign data using post-quantum Dilithium signature algorithm.
     *
     * @param data Data to sign
     * @return SignatureResult containing signature and public key
     * @throws Exception if signing fails
     */
    public SignatureResult sign(byte[] data) throws Exception {
        if (!signatureEnabled) {
            logger.debug("Post-quantum signatures disabled, returning null");
            return null;
        }

        if (!initialized) {
            throw new IllegalStateException("PostQuantumSignatureService not initialized. Check logs for errors.");
        }

        logger.info("Signing data using Dilithium - Algorithm: {}, Data size: {} bytes",
                signatureAlgorithm, data.length);

        try {
            // Create signature instance
            Signature signer = Signature.getInstance(SIGNATURE_ALGORITHM, PROVIDER);
            signer.initSign(signingKeyPair.getPrivate(), new SecureRandom());

            // Sign the data
            signer.update(data);
            byte[] signature = signer.sign();

            logger.info("Dilithium signature created - Signature size: {} bytes", signature.length);

            // Create result
            SignatureResult result = new SignatureResult();
            result.setSignature(Base64.getEncoder().encodeToString(signature));
            result.setPublicKey(Base64.getEncoder().encodeToString(signingKeyPair.getPublic().getEncoded()));
            result.setAlgorithm(signatureAlgorithm);

            logger.info("Post-quantum signature completed successfully");
            return result;

        } catch (Exception e) {
            logger.error("Dilithium signing failed: {}", e.getMessage(), e);
            throw new Exception("Failed to sign data with Dilithium: " + e.getMessage(), e);
        }
    }

    /**
     * Sign data with a specific private key.
     *
     * @param data       Data to sign
     * @param privateKey Private key to use for signing
     * @return SignatureResult containing signature
     * @throws Exception if signing fails
     */
    public SignatureResult sign(byte[] data, PrivateKey privateKey) throws Exception {
        if (!signatureEnabled) {
            return null;
        }

        if (!initialized) {
            throw new IllegalStateException("PostQuantumSignatureService not initialized.");
        }

        logger.info("Signing data with provided private key - Algorithm: {}", signatureAlgorithm);

        Signature signer = Signature.getInstance(SIGNATURE_ALGORITHM, PROVIDER);
        signer.initSign(privateKey, new SecureRandom());
        signer.update(data);
        byte[] signature = signer.sign();

        SignatureResult result = new SignatureResult();
        result.setSignature(Base64.getEncoder().encodeToString(signature));
        result.setAlgorithm(signatureAlgorithm);

        return result;
    }

    /**
     * Verify post-quantum signature.
     *
     * @param data      Original data
     * @param signature Signature to verify (Base64 encoded)
     * @param publicKey Public key for verification (Base64 encoded)
     * @return true if signature is valid, false otherwise
     * @throws Exception if verification fails
     */
    public boolean verify(byte[] data, String signature, String publicKey) throws Exception {
        return verify(data,
                Base64.getDecoder().decode(signature),
                Base64.getDecoder().decode(publicKey));
    }

    /**
     * Verify post-quantum signature.
     *
     * @param data      Original data
     * @param signature Signature to verify
     * @param publicKey Public key for verification (encoded bytes)
     * @return true if signature is valid, false otherwise
     * @throws Exception if verification fails
     */
    public boolean verify(byte[] data, byte[] signature, byte[] publicKey) throws Exception {
        if (!signatureEnabled) {
            logger.debug("Post-quantum signatures disabled, skipping verification");
            return true; // Skip verification if disabled
        }

        if (!initialized) {
            throw new IllegalStateException("PostQuantumSignatureService not initialized.");
        }

        logger.info("Verifying Dilithium signature - Algorithm: {}, Data size: {} bytes, Signature size: {} bytes",
                signatureAlgorithm, data.length, signature.length);

        try {
            // Reconstruct the public key
            KeyFactory keyFactory = KeyFactory.getInstance(SIGNATURE_ALGORITHM, PROVIDER);
            PublicKey pubKey = keyFactory.generatePublic(
                    new org.bouncycastle.jcajce.spec.RawEncodedKeySpec(publicKey)
            );

            // Verify the signature
            Signature verifier = Signature.getInstance(SIGNATURE_ALGORITHM, PROVIDER);
            verifier.initVerify(pubKey);
            verifier.update(data);
            boolean valid = verifier.verify(signature);

            logger.info("Dilithium signature verification: {}", valid ? "VALID" : "INVALID");
            return valid;

        } catch (Exception e) {
            logger.error("Dilithium verification failed: {}", e.getMessage(), e);
            throw new Exception("Failed to verify Dilithium signature: " + e.getMessage(), e);
        }
    }

    /**
     * Get the current signature algorithm being used.
     */
    public String getCurrentAlgorithm() {
        return signatureAlgorithm;
    }

    /**
     * Get the security level description for the current algorithm.
     */
    public String getSecurityLevel() {
        switch (signatureAlgorithm) {
            case "Dilithium2":
                return "128-bit (AES-128 equivalent)";
            case "Dilithium3":
                return "192-bit (AES-192 equivalent)";
            case "Dilithium5":
                return "256-bit (AES-256 equivalent)";
            default:
                return "Unknown";
        }
    }

    /**
     * Get the current public key (Base64 encoded).
     */
    public String getPublicKeyBase64() {
        if (signingKeyPair == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(signingKeyPair.getPublic().getEncoded());
    }

    /**
     * Get information about the Dilithium algorithm.
     */
    public String getAlgorithmInfo() {
        return String.format(
                "Algorithm: %s | Security: %s | Provider: BouncyCastle PQC | Status: %s",
                signatureAlgorithm,
                getSecurityLevel(),
                initialized ? "Ready" : "Not Initialized"
        );
    }

    /**
     * Result class for signature operations.
     */
    public static class SignatureResult {
        private String signature;
        private String publicKey;
        private String algorithm;

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
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

        @Override
        public String toString() {
            return String.format(
                    "SignatureResult{algorithm='%s', signature=%d chars, publicKey=%d chars}",
                    algorithm,
                    signature != null ? signature.length() : 0,
                    publicKey != null ? publicKey.length() : 0
            );
        }
    }
}
