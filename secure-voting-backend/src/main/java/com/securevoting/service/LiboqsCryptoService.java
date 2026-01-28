package com.securevoting.service;

import org.bouncycastle.jcajce.SecretKeyWithEncapsulation;
import org.bouncycastle.jcajce.spec.KEMExtractSpec;
import org.bouncycastle.jcajce.spec.KEMGenerateSpec;
import org.bouncycastle.pqc.jcajce.provider.BouncyCastlePQCProvider;
import org.bouncycastle.pqc.jcajce.spec.KyberParameterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.crypto.KeyGenerator;
import java.security.*;

/**
 * Post-Quantum Cryptography Service using BouncyCastle's Kyber implementation.
 * Provides post-quantum Key Encapsulation Mechanism (KEM) for secure key exchange.
 *
 * Supports Kyber algorithms (NIST PQC Standard):
 * - Kyber512: 128-bit security level
 * - Kyber768: 192-bit security level (recommended)
 * - Kyber1024: 256-bit security level
 *
 * This service complements QKDService by providing an alternative post-quantum
 * key exchange mechanism that works on classical hardware.
 */
@Service
public class LiboqsCryptoService {

    private static final Logger logger = LoggerFactory.getLogger(LiboqsCryptoService.class);
    private static final String PROVIDER = "BCPQC";
    private static final String KEM_ALGORITHM = "Kyber";

    @Value("${quantum.crypto.algorithm:Kyber768}")
    private String kemAlgorithm;

    private KyberParameterSpec kyberSpec;
    private boolean initialized = false;

    @PostConstruct
    public void init() {
        try {
            // Register BouncyCastle PQC Provider
            if (Security.getProvider(PROVIDER) == null) {
                Security.addProvider(new BouncyCastlePQCProvider());
                logger.info("BouncyCastle PQC Provider registered successfully");
            }

            // Set Kyber parameter spec based on configuration
            switch (kemAlgorithm) {
                case "Kyber512":
                    kyberSpec = KyberParameterSpec.kyber512;
                    break;
                case "Kyber1024":
                    kyberSpec = KyberParameterSpec.kyber1024;
                    break;
                case "Kyber768":
                default:
                    kyberSpec = KyberParameterSpec.kyber768;
                    kemAlgorithm = "Kyber768";
                    break;
            }

            // Test that Kyber is available
            KeyPairGenerator testKpg = KeyPairGenerator.getInstance(KEM_ALGORITHM, PROVIDER);
            testKpg.initialize(kyberSpec, new SecureRandom());

            initialized = true;
            logger.info("LiboqsCryptoService initialized successfully with algorithm: {}", kemAlgorithm);
            logger.info("Post-quantum Liboqs KEM ready for use - Security Level: {}", getSecurityLevel());

        } catch (Exception e) {
            logger.error("Failed to initialize LiboqsCryptoService: {}", e.getMessage());
            logger.error("Post-quantum Liboqs cryptography will not be available");
            initialized = false;
        }
    }

    /**
     * Check if the service is properly initialized and ready to use.
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Generate key pair and encapsulate to get shared secret.
     * This is the "sender" side of the KEM operation.
     *
     * @return LiboqsResult containing shared secret, public key, secret key, and ciphertext
     * @throws Exception if KEM operations fail
     */
    public LiboqsResult encapsulate() throws Exception {
        if (!initialized) {
            throw new IllegalStateException("LiboqsCryptoService not initialized. Check logs for errors.");
        }

        logger.info("=== Liboqs - Kyber KEM Encapsulation - Algorithm: {} ===", kemAlgorithm);

        try {
            // Step 1: Generate Kyber key pair
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(KEM_ALGORITHM, PROVIDER);
            kpg.initialize(kyberSpec, new SecureRandom());
            KeyPair keyPair = kpg.generateKeyPair();

            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            logger.debug("Generated Liboqs Kyber key pair - Public key: {} bytes, Private key: {} bytes",
                    publicKey.getEncoded().length, privateKey.getEncoded().length);

            // Step 2: Encapsulate - Generate shared secret and ciphertext
            KeyGenerator keyGen = KeyGenerator.getInstance(KEM_ALGORITHM, PROVIDER);
            keyGen.init(new KEMGenerateSpec(publicKey, "AES"), new SecureRandom());
            SecretKeyWithEncapsulation secretKeyWithEnc = (SecretKeyWithEncapsulation) keyGen.generateKey();

            byte[] sharedSecret = secretKeyWithEnc.getEncoded();
            byte[] encapsulation = secretKeyWithEnc.getEncapsulation();

            logger.info("Liboqs - KEM encapsulation successful - Shared secret: {} bytes, Ciphertext: {} bytes",
                    sharedSecret.length, encapsulation.length);

            // Step 3: Create result object
            LiboqsResult result = new LiboqsResult();
            result.setSharedSecret(sharedSecret);
            result.setPublicKey(publicKey.getEncoded());
            result.setSecretKey(privateKey.getEncoded());
            result.setCiphertext(encapsulation);
            result.setAlgorithm(kemAlgorithm);

            logger.info("Liboqs - Kyber KEM encapsulation completed successfully");
            return result;

        } catch (Exception e) {
            logger.error("Liboqs - KEM encapsulation failed: {}", e.getMessage(), e);
            throw new Exception("Liboqs - Failed to perform Kyber KEM encapsulation: " + e.getMessage(), e);
        }
    }

    /**
     * Decapsulate to recover shared secret.
     * This is the "receiver" side of the KEM operation.
     *
     * @param secretKeyBytes Encoded secret (private) key from key pair generation
     * @param ciphertext     Ciphertext (encapsulation) from encapsulate()
     * @return Shared secret byte array (same as the one from encapsulate)
     * @throws Exception if decapsulation fails
     */
    public byte[] decapsulate(byte[] secretKeyBytes, byte[] ciphertext) throws Exception {
        if (!initialized) {
            throw new IllegalStateException("LiboqsCryptoService not initialized. Check logs for errors.");
        }

        logger.info("=== Liboqs - Kyber KEM Decapsulation - Algorithm: {} ===", kemAlgorithm);

        try {
            // Step 1: Reconstruct the private key from PKCS8 encoded bytes
            KeyFactory keyFactory = KeyFactory.getInstance(KEM_ALGORITHM, PROVIDER);
            java.security.spec.PKCS8EncodedKeySpec keySpec = new java.security.spec.PKCS8EncodedKeySpec(secretKeyBytes);
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            logger.debug("Liboqs - Reconstructed private key: {} bytes", privateKey.getEncoded().length);

            // Step 2: Decapsulate - Recover shared secret from ciphertext
            KeyGenerator keyGen = KeyGenerator.getInstance(KEM_ALGORITHM, PROVIDER);
            keyGen.init(new KEMExtractSpec(privateKey, ciphertext, "AES"));
            SecretKeyWithEncapsulation secretKey = (SecretKeyWithEncapsulation) keyGen.generateKey();

            byte[] sharedSecret = secretKey.getEncoded();

            logger.info("Liboqs - KEM decapsulation successful - Shared secret: {} bytes", sharedSecret.length);
            logger.info("Liboqs - Kyber KEM decapsulation completed successfully");

            return sharedSecret;

        } catch (Exception e) {
            logger.error("Liboqs - KEM decapsulation failed: {}", e.getMessage(), e);
            throw new Exception("Liboqs - Failed to perform Kyber KEM decapsulation: " + e.getMessage(), e);
        }
    }

    /**
     * Decapsulate using public key, secret key, and ciphertext.
     * Public key is not needed for decapsulation but kept for API compatibility.
     *
     * @param publicKey  Public key (not used, for API compatibility)
     * @param secretKey  Secret key
     * @param ciphertext Ciphertext from encapsulation
     * @return Shared secret byte array
     * @throws Exception if decapsulation fails
     */
    public byte[] decapsulate(byte[] publicKey, byte[] secretKey, byte[] ciphertext) throws Exception {
        // Public key is not needed for decapsulation
        return decapsulate(secretKey, ciphertext);
    }

    /**
     * Get the current KEM algorithm being used.
     *
     * @return Algorithm name (e.g., "Kyber768")
     */
    public String getCurrentAlgorithm() {
        return kemAlgorithm;
    }

    /**
     * Get the security level description for the current algorithm.
     */
    public String getSecurityLevel() {
        switch (kemAlgorithm) {
            case "Kyber512":
                return "128-bit (AES-128 equivalent)";
            case "Kyber768":
                return "192-bit (AES-192 equivalent)";
            case "Kyber1024":
                return "256-bit (AES-256 equivalent)";
            default:
                return "Unknown";
        }
    }

    /**
     * Get information about the Kyber algorithm.
     */
    public String getAlgorithmInfo() {
        return String.format(
                "Algorithm: %s | Security: %s | Provider: BouncyCastle PQC | Status: %s",
                kemAlgorithm,
                getSecurityLevel(),
                initialized ? "Ready" : "Not Initialized"
        );
    }

    /**
     * Result class for KEM operations.
     */
    public static class LiboqsResult {
        private byte[] sharedSecret;
        private byte[] publicKey;
        private byte[] secretKey;
        private byte[] ciphertext;
        private String algorithm;

        public byte[] getSharedSecret() {
            return sharedSecret;
        }

        public void setSharedSecret(byte[] sharedSecret) {
            this.sharedSecret = sharedSecret;
        }

        public byte[] getPublicKey() {
            return publicKey;
        }

        public void setPublicKey(byte[] publicKey) {
            this.publicKey = publicKey;
        }

        public byte[] getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(byte[] secretKey) {
            this.secretKey = secretKey;
        }

        public byte[] getCiphertext() {
            return ciphertext;
        }

        public void setCiphertext(byte[] ciphertext) {
            this.ciphertext = ciphertext;
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
                    "LiboqsResult{algorithm='%s', sharedSecret=%d bytes, publicKey=%d bytes, secretKey=%d bytes, ciphertext=%d bytes}",
                    algorithm,
                    sharedSecret != null ? sharedSecret.length : 0,
                    publicKey != null ? publicKey.length : 0,
                    secretKey != null ? secretKey.length : 0,
                    ciphertext != null ? ciphertext.length : 0
            );
        }
    }
}
