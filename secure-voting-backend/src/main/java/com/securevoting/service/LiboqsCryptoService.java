package com.securevoting.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.security.SecureRandom;
import java.util.Base64;

// Note: liboqs-java imports - uncomment when library is properly installed
//import org.openquantumsafe.KEM;
//import org.openquantumsafe.KeyEncapsulation;
//import org.openquantumsafe.kyber.Kyber;

/**
 * Post-Quantum Cryptography Service using liboqs library.
 * Provides post-quantum cryptographic operations OTHER than key distribution.
 *
 * Note: Key distribution is handled by QKDService using BB84 protocol.
 * This service provides:
 * - Digital signatures (Dilithium)
 * - Additional KEM operations (if needed for other purposes)
 * - Other post-quantum cryptographic primitives
 *
 * Supports algorithms:
 * - KEM: Kyber512, Kyber768, Kyber1024
 * - Signatures: Dilithium2, Dilithium3, Dilithium5
 */
@Service
public class LiboqsCryptoService {

    private static final Logger logger = LoggerFactory.getLogger(LiboqsCryptoService.class);
    private static final int SHARED_SECRET_SIZE = 32; // 32 bytes for AES-256

    @Value("${quantum.crypto.algorithm:Kyber768}")
    private String kemAlgorithm;

    @Value("${quantum.crypto.native.library.path:./lib}")
    private String nativeLibraryPath;

    private String currentAlgorithm;

    @PostConstruct
    public void init() {
        this.currentAlgorithm = kemAlgorithm;

        // Set native library path if specified
        if (nativeLibraryPath != null && !nativeLibraryPath.isEmpty()) {
            String currentPath = System.getProperty("java.library.path", "");
            String separator = System.getProperty("os.name").toLowerCase().contains("win") ? ";" : ":";
            if (!currentPath.isEmpty() && !currentPath.endsWith(separator)) {
                currentPath += separator;
            }
            System.setProperty("java.library.path", currentPath + nativeLibraryPath);
        }

        logger.info("LiboqsCryptoService initialized with algorithm: {}", currentAlgorithm);

        // Test if liboqs is available
        try {
            // Uncomment when liboqs-java is properly installed:
            // String[] availableAlgorithms = KEM.get_supported_KEMs();
            // logger.info("Available liboqs KEM algorithms: {}", String.join(", ", availableAlgorithms));
            // if (!isAlgorithmSupported(currentAlgorithm)) {
            //     logger.warn("Algorithm {} not supported, falling back to Kyber768", currentAlgorithm);
            //     currentAlgorithm = "Kyber768";
            // }
            logger.info("liboqs-java library integration - ensure native library is in path: {}", nativeLibraryPath);
        } catch (Exception e) {
            logger.error("Failed to initialize liboqs: {}", e.getMessage());
            logger.error("Make sure liboqs native library is available in the library path");
        }
    }

    /**
     * Generate key pair and encapsulate to get shared secret.
     *
     * @return LiboqsResult containing shared secret, public key, and ciphertext
     * @throws Exception if KEM operations fail
     */
    public LiboqsResult encapsulate() throws Exception {
        logger.info("=== liboqs KEM Encapsulation - Algorithm: {} ===", currentAlgorithm);

        try {
            // TODO: Uncomment and use when liboqs-java is properly installed
            // Create KEM instance
            // KeyEncapsulation kem = new KeyEncapsulation(currentAlgorithm, null);
            //
            // // Generate key pair (public key is generated automatically)
            // byte[] publicKey = kem.generate_keypair();
            // logger.debug("Generated public key: {} bytes", publicKey.length);
            //
            // // Encapsulate: create shared secret and ciphertext
            // KeyEncapsulation.EncapsulationResult encapResult = kem.encapsulate(publicKey);
            // byte[] sharedSecret = encapResult.getSharedSecret();
            // byte[] ciphertext = encapResult.getCiphertext();
            //
            // logger.info("KEM encapsulation successful. Shared secret: {} bytes, Ciphertext: {} bytes",
            //            sharedSecret.length, ciphertext.length);
            //
            // // Store private key for later decapsulation
            // byte[] secretKey = kem.export_secret_key();
            //
            // LiboqsResult result = new LiboqsResult();
            // result.setSharedSecret(sharedSecret);
            // result.setPublicKey(publicKey);
            // result.setSecretKey(secretKey);
            // result.setCiphertext(ciphertext);
            // result.setAlgorithm(currentAlgorithm);
            //
            // kem.dispose();
            //
            // return result;

            // Temporary implementation until liboqs-java is properly installed
            throw new UnsupportedOperationException(
                    "liboqs-java library not yet configured. " +
                            "Please install liboqs native library and ensure liboqs-java JAR is in classpath. " +
                            "See: https://github.com/open-quantum-safe/liboqs-java");
        } catch (UnsupportedOperationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("KEM encapsulation failed: {}", e.getMessage(), e);
            throw new Exception("Failed to perform KEM encapsulation: " + e.getMessage(), e);
        }
    }

    /**
     * Decapsulate to recover shared secret.
     *
     * @param secretKey Secret key from key pair generation
     * @param ciphertext Ciphertext from encapsulation
     * @return Shared secret byte array
     * @throws Exception if decapsulation fails
     */
    public byte[] decapsulate(byte[] secretKey, byte[] ciphertext) throws Exception {
        logger.info("=== liboqs KEM Decapsulation - Algorithm: {} ===", currentAlgorithm);

        try {
            // TODO: Uncomment when liboqs-java is properly installed
            // Create KEM instance with secret key
            // KeyEncapsulation kem = new KeyEncapsulation(currentAlgorithm, secretKey);
            //
            // // Decapsulate: recover shared secret from ciphertext
            // byte[] sharedSecret = kem.decapsulate(ciphertext);
            //
            // logger.info("KEM decapsulation successful. Shared secret: {} bytes", sharedSecret.length);
            //
            // kem.dispose();
            //
            // return sharedSecret;

            // Temporary implementation until liboqs-java is properly installed
            throw new UnsupportedOperationException(
                    "liboqs-java library not yet configured. " +
                            "Please install liboqs native library and ensure liboqs-java JAR is in classpath.");
        } catch (UnsupportedOperationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("KEM decapsulation failed: {}", e.getMessage(), e);
            throw new Exception("Failed to perform KEM decapsulation: " + e.getMessage(), e);
        }
    }

    /**
     * Decapsulate using public key and ciphertext (alternative method).
     *
     * @param publicKey Public key
     * @param secretKey Secret key
     * @param ciphertext Ciphertext from encapsulation
     * @return Shared secret byte array
     * @throws Exception if decapsulation fails
     */
    public byte[] decapsulate(byte[] publicKey, byte[] secretKey, byte[] ciphertext) throws Exception {
        // Same as decapsulate(secretKey, ciphertext) - publicKey is not needed for decapsulation
        return decapsulate(secretKey, ciphertext);
    }

    /**
     * Get the current KEM algorithm being used.
     *
     * @return Algorithm name
     */
    public String getCurrentAlgorithm() {
        return currentAlgorithm;
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
    }
}




