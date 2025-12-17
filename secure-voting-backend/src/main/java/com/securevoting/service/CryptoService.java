package com.securevoting.service;

import com.google.gson.Gson;
import com.securevoting.dto.VotePayload;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class CryptoService {

    private static final String ELLIPTIC_CURVE_ALGORITHM = "EC";
    private static final String KEY_AGREEMENT_ALGORITHM = "ECDH";
    private static final String SYMMETRIC_ALGORITHM = "AES";
    private static final String SYMMETRIC_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final String CURVE_NAME = "secp256r1";
    private static final int GCM_IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final String PROVIDER = BouncyCastleProvider.PROVIDER_NAME;

    private static final String PUBLIC_KEY_FILE = "authority_public.key";
    private static final String PRIVATE_KEY_FILE = "authority_private.key";

    private KeyPair authorityKeyPair;
    
    @Autowired
    private SteganographyService steganographyService;

    @PostConstruct
    public void init() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        File privateKeyFile = new File(PRIVATE_KEY_FILE);
        File publicKeyFile = new File(PUBLIC_KEY_FILE);

        if (privateKeyFile.exists() && publicKeyFile.exists()) {
            // Load existing keys
            byte[] privateKeyBytes = Files.readAllBytes(Paths.get(PRIVATE_KEY_FILE));
            byte[] publicKeyBytes = Files.readAllBytes(Paths.get(PUBLIC_KEY_FILE));

            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);

            KeyFactory keyFactory = KeyFactory.getInstance(ELLIPTIC_CURVE_ALGORITHM, PROVIDER);
            PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

            authorityKeyPair = new KeyPair(publicKey, privateKey);
        } else {
            // Generate new keys
            authorityKeyPair = generateEccKeyPair();
            saveKeys();
        }
    }

    private void saveKeys() throws IOException {
        try (FileOutputStream privateKeyOut = new FileOutputStream(PRIVATE_KEY_FILE);
             FileOutputStream publicKeyOut = new FileOutputStream(PUBLIC_KEY_FILE)) {
            
            privateKeyOut.write(authorityKeyPair.getPrivate().getEncoded());
            publicKeyOut.write(authorityKeyPair.getPublic().getEncoded());
        }
    }

    /**
     * Encrypts a vote using hybrid cryptography:
     * 1. ECC Key Agreement (ECDH) to create shared secret
     * 2. AES-GCM Key Derivation using SHA-256
     * 3. AES-GCM encryption of vote data
     */
    public String encryptVote(String voteJson) throws Exception {
        System.out.println("=== VOTE ENCRYPTION PROCESS ===");
        
        // Step 1: ECC Key Agreement (ECDH)
        PublicKey authorityPublicKey = authorityKeyPair.getPublic();
        KeyPair ephemeralKeyPair = generateEccKeyPair();
        PublicKey ephemeralPublicKey = ephemeralKeyPair.getPublic();
        PrivateKey ephemeralPrivateKey = ephemeralKeyPair.getPrivate();
        System.out.println("ECC - Temperory Key pair has been generated");

        KeyAgreement keyAgreement = KeyAgreement.getInstance(KEY_AGREEMENT_ALGORITHM, PROVIDER);
        keyAgreement.init(ephemeralPrivateKey);
        keyAgreement.doPhase(authorityPublicKey, true);
        byte[] sharedSecret = keyAgreement.generateSecret();
        
        System.out.println("Successfully created shared secret via ECDH");

        // Step 2: AES-GCM Key Derivation (SHA-256)
        MessageDigest hash = MessageDigest.getInstance(HASH_ALGORITHM, PROVIDER);
        byte[] derivedKey = hash.digest(sharedSecret);
        SecretKey aesKey = new SecretKeySpec(derivedKey, 0, 32, SYMMETRIC_ALGORITHM);
        
        System.out.println("Successfully converted secure_secret_key into AES key using SHA-256");

        // Step 3: AES-GCM Encryption
        Cipher aesCipher = Cipher.getInstance(SYMMETRIC_TRANSFORMATION, PROVIDER);
        byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
        new SecureRandom().nextBytes(iv);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, parameterSpec);
        byte[] cipherText = aesCipher.doFinal(voteJson.getBytes());
        
        System.out.println("Successfully encrypted vote using AES-GCM by AES key");

        // Create VotePayload with X.509 encoded ephemeral public key
        String ephemeralPublicKeyB64 = Base64.getEncoder().encodeToString(ephemeralPublicKey.getEncoded());
        String ivB64 = Base64.getEncoder().encodeToString(iv);
        String cipherTextB64 = Base64.getEncoder().encodeToString(cipherText);

        // Use new constructor with algorithm and hmac (hmac will be added by UnifiedCryptoService if needed)
        VotePayload payload = new VotePayload();
        payload.setAlgorithm("ECDH");
        payload.setEphemeralPublicKey(ephemeralPublicKeyB64);
        payload.setIv(ivB64);
        payload.setCipherText(cipherTextB64);
        payload.setHmac(null); // HMAC will be added by UnifiedCryptoService wrapper
        
        Gson gson = new Gson();
        String jsonResult = gson.toJson(payload);
        
        System.out.println("EncryptVote - JSON Result: " + jsonResult);
        System.out.println("EncryptVote - JSON Length: " + jsonResult.length());
        
        return jsonResult;
    }

    /**
     * Decrypts a vote using hybrid cryptography:
     * 1. Extract data from steganographic image
     * 2. Parse JSON VotePayload
     * 3. Reconstruct ephemeral public key
     * 4. ECC Key Agreement (ECDH) to recreate shared secret
     * 5. AES-GCM Key Derivation using SHA-256
     * 6. AES-GCM decryption of vote data
     */
    public String decryptVote(byte[] stegoImageData) throws Exception {
        System.out.println("=== VOTE DECRYPTION PROCESS ===");
        
        // Step 1: Extract the encrypted payload from the steganographic image
        byte[] extractedData = steganographyService.extractData(stegoImageData);
        String extractedString = new String(extractedData);
        
        System.out.println("Extracted data length: " + extractedString.length());
        System.out.println("First 100 chars: " + extractedString.substring(0, Math.min(100, extractedString.length())));
        
        // Step 2: Parse the JSON VotePayload
        Gson gson = new Gson();
        VotePayload payload = gson.fromJson(extractedString, VotePayload.class);
        
        System.out.println("Successfully parsed VotePayload JSON");
        
        // Step 3: Decode Base64 components
        byte[] ephemeralPublicKeyBytes = Base64.getDecoder().decode(payload.getEphemeralPublicKey());
        byte[] iv = Base64.getDecoder().decode(payload.getIv());
        byte[] cipherText = Base64.getDecoder().decode(payload.getCipherText());
        
        System.out.println("Decoded ephemeral key length: " + ephemeralPublicKeyBytes.length);
        System.out.println("Decoded IV length: " + iv.length);
        System.out.println("Decoded ciphertext length: " + cipherText.length);
        
        // Step 4: Reconstruct the ephemeral public key (X.509 format)
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(ephemeralPublicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(ELLIPTIC_CURVE_ALGORITHM, PROVIDER);
        PublicKey ephemeralPublicKey = keyFactory.generatePublic(keySpec);
        
        System.out.println("Successfully reconstructed ephemeral public key");
        
        // Step 5: ECC Key Agreement (ECDH) - Recreate shared secret
        PrivateKey authorityPrivateKey = authorityKeyPair.getPrivate();
        KeyAgreement keyAgreement = KeyAgreement.getInstance(KEY_AGREEMENT_ALGORITHM, PROVIDER);
        keyAgreement.init(authorityPrivateKey);
        keyAgreement.doPhase(ephemeralPublicKey, true);
        byte[] sharedSecret = keyAgreement.generateSecret();
        
        System.out.println("Successfully recreated shared secret");
        
        // Step 6: AES-GCM Key Derivation (SHA-256)
        MessageDigest hash = MessageDigest.getInstance(HASH_ALGORITHM, PROVIDER);
        byte[] derivedKey = hash.digest(sharedSecret);
        SecretKey aesKey = new SecretKeySpec(derivedKey, 0, 32, SYMMETRIC_ALGORITHM);
        
        System.out.println("Successfully derived AES key");
        
        // Step 7: AES-GCM Decryption
        Cipher aesCipher = Cipher.getInstance(SYMMETRIC_TRANSFORMATION, PROVIDER);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
        aesCipher.init(Cipher.DECRYPT_MODE, aesKey, parameterSpec);
        byte[] decryptedVote = aesCipher.doFinal(cipherText);
        
        String voteJson = new String(decryptedVote);
        System.out.println("Successfully decrypted vote: " + voteJson);
        
        return voteJson;
    }

    private KeyPair generateEccKeyPair() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchProviderException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ELLIPTIC_CURVE_ALGORITHM, PROVIDER);
        ECGenParameterSpec ecSpec = new ECGenParameterSpec(CURVE_NAME);
        keyGen.initialize(ecSpec, new SecureRandom());
        return keyGen.generateKeyPair();
    }
}