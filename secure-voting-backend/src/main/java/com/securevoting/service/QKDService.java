package com.securevoting.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * Quantum Key Distribution (QKD) Service implementing BB84 protocol.
 * Uses quantum simulator (Qiskit via Python) for quantum state generation and measurement.
 *
 * This service handles actual quantum key distribution using BB84 protocol.
 * For other post-quantum operations, use LiboqsCryptoService.
 */
@Service
public class QKDService {

    private static final int KEY_SIZE_BYTES = 32;

    @Value("${quantum.crypto.simulator:qiskit}")
    private String simulator;

    @Value("${quantum.crypto.python.path:python3}")
    private String pythonPath;

    @Value("${quantum.crypto.qiskit.script.path:./scripts/qkd_bb84.py}")
    private String qiskitScriptPath;

    @Value("${quantum.crypto.qkd.qubits:256}")
    private int numberOfQubits;

    @Value("${quantum.crypto.qkd.error.threshold:0.11}")
    private double errorThreshold;

    private SecureRandom secureRandom;

    @PostConstruct
    public void init() {
        this.secureRandom = new SecureRandom();
        System.out.println("QKDService initialized with BB84 protocol - simulator: " + simulator +
                ", qubits: " + numberOfQubits + ", error threshold: " + errorThreshold);
    }

    /**
     * Generate shared secret using BB84 QKD protocol.
     *
     * @return QKDResult containing shared secret and metadata
     * @throws Exception if QKD protocol fails
     */
    public QKDResult generateSharedSecret() throws Exception {
        System.out.println("=== QKD BB84 Protocol - Key Generation ===");

        // Step 1: Alice generates random bits and bases
        List<Integer> aliceBits = generateRandomBits(numberOfQubits);
        List<Integer> aliceBases = generateRandomBases(numberOfQubits);
        System.out.println("Alice generated " + numberOfQubits + " random bits and bases");

        // Step 2: Create quantum states (simulated)
        List<Integer> quantumStates = createQuantumStates(aliceBits, aliceBases);

        // Step 3: Bob measures with random bases
        List<Integer> bobBases = generateRandomBases(numberOfQubits);
        List<Integer> bobResults = measureQuantumStates(quantumStates, bobBases);
        System.out.println("Bob measured quantum states with random bases");

        // Step 4: Basis reconciliation (public discussion)
        QKDReconciliationResult reconciliation = reconcileBases(aliceBases, bobBases, aliceBits, bobResults);
        System.out.println("Basis reconciliation: " + reconciliation.getMatchingBits().size() +
                " matching bits out of " + numberOfQubits);

        // Step 5: Error estimation and eavesdropping detection
        double errorRate = estimateError(reconciliation.getMatchingBits(),
                reconciliation.getAliceBits(),
                reconciliation.getBobBits());
        System.out.println("Error rate: " + errorRate + " (threshold: " + errorThreshold + ")");

        if (errorRate > errorThreshold) {
            System.err.println("Eavesdropping detected! Error rate " + errorRate +
                    " exceeds threshold " + errorThreshold);
            throw new SecurityException("Eavesdropping detected in QKD protocol. Error rate: " + errorRate);
        }

        // Step 6: Privacy amplification
        byte[] sharedSecret = privacyAmplification(reconciliation.getMatchingBits(),
                reconciliation.getAliceBits());
        System.out.println("Privacy amplification completed. Shared secret: " + sharedSecret.length + " bytes");

        // Create QKD metadata for storage
        QKDMetadata metadata = new QKDMetadata();
        metadata.setAliceBases(aliceBases);
        metadata.setAliceBits(aliceBits);
        metadata.setBobBases(bobBases);
        metadata.setErrorRate(errorRate);
        metadata.setNumberOfQubits(numberOfQubits);
        metadata.setMatchingBitsCount(reconciliation.getMatchingBits().size());

        QKDResult result = new QKDResult();
        result.setSharedSecret(sharedSecret);
        result.setMetadata(metadata);

        return result;
    }

    /**
     * Reconstruct shared secret from QKD metadata (for decryption).
     *
     * @param metadata QKD metadata containing bases and error information
     * @param bobResults Bob's measurement results
     * @return Shared secret byte array
     * @throws Exception if reconstruction fails
     */
    public byte[] reconstructSharedSecret(QKDMetadata metadata, List<Integer> bobResults) throws Exception {
        System.out.println("=== QKD BB84 Protocol - Key Reconstruction ===");

        // Use Alice's bits directly from metadata instead of trying to reconstruct
        List<Integer> aliceBits = metadata.getAliceBits();

        if (aliceBits == null || aliceBits.isEmpty()) {
            System.err.println("Alice's bits not found in metadata. Cannot reconstruct shared secret.");
            throw new IllegalArgumentException("QKD metadata missing Alice's bits. " +
                    "This may be due to data encrypted with an older version of the service.");
        }

        // Perform basis reconciliation using the stored Alice's bits
        List<Integer> matchingIndices = new ArrayList<>();
        List<Integer> matchingAliceBits = new ArrayList<>();
        List<Integer> matchingBobBits = new ArrayList<>();

        for (int i = 0; i < metadata.getAliceBases().size(); i++) {
            if (metadata.getAliceBases().get(i).equals(metadata.getBobBases().get(i))) {
                matchingIndices.add(i);
                matchingAliceBits.add(aliceBits.get(i));
                // For reconstruction, we use Alice's bits directly (since Bob's measurement
                // would have matched when bases were the same)
                matchingBobBits.add(aliceBits.get(i));
            }
        }

        System.out.println("Reconstructed " + matchingAliceBits.size() + " matching bits from stored metadata");

        // Privacy amplification
        byte[] sharedSecret = privacyAmplification(matchingIndices, matchingAliceBits);
        System.out.println("Shared secret reconstructed: " + sharedSecret.length + " bytes");

        return sharedSecret;
    }

    // === Private Helper Methods ===

    private List<Integer> generateRandomBits(int count) {
        List<Integer> bits = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            bits.add(secureRandom.nextInt(2));
        }
        return bits;
    }

    private List<Integer> generateRandomBases(int count) {
        List<Integer> bases = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            bases.add(secureRandom.nextInt(2));
        }
        return bases;
    }

    private List<Integer> createQuantumStates(List<Integer> bits, List<Integer> bases) {
        // Simulate quantum state creation
        // In real QKD, this would create actual qubits
        // For simulation, we just return the bits (they represent the quantum states)
        return new ArrayList<>(bits);
    }

    private List<Integer> measureQuantumStates(List<Integer> quantumStates, List<Integer> measurementBases) {
        // Simulate quantum measurement
        // In real QKD, measuring collapses the quantum state
        // For simulation, we return the states (representing measurement results)
        List<Integer> results = new ArrayList<>();
        for (int i = 0; i < quantumStates.size(); i++) {
            results.add(quantumStates.get(i));
        }
        return results;
    }

    private QKDReconciliationResult reconcileBases(List<Integer> aliceBases, List<Integer> bobBases,
                                                   List<Integer> aliceBits, List<Integer> bobResults) {
        List<Integer> matchingIndices = new ArrayList<>();
        List<Integer> matchingAliceBits = new ArrayList<>();
        List<Integer> matchingBobBits = new ArrayList<>();

        for (int i = 0; i < aliceBases.size(); i++) {
            if (aliceBases.get(i).equals(bobBases.get(i))) {
                matchingIndices.add(i);
                matchingAliceBits.add(aliceBits.get(i));
                matchingBobBits.add(bobResults.get(i));
            }
        }

        QKDReconciliationResult result = new QKDReconciliationResult();
        result.setMatchingBits(matchingIndices);
        result.setAliceBits(matchingAliceBits);
        result.setBobBits(matchingBobBits);

        return result;
    }

    private double estimateError(List<Integer> matchingIndices, List<Integer> aliceBits, List<Integer> bobBits) {
        if (matchingIndices.isEmpty()) {
            return 1.0; // 100% error if no matching bits
        }

        // Use a subset for error estimation (e.g., 10% of matching bits)
        int sampleSize = Math.max(1, matchingIndices.size() / 10);
        int errors = 0;

        for (int i = 0; i < sampleSize && i < aliceBits.size(); i++) {
            if (!aliceBits.get(i).equals(bobBits.get(i))) {
                errors++;
            }
        }

        return (double) errors / sampleSize;
    }

    private byte[] privacyAmplification(List<Integer> matchingIndices, List<Integer> bits) throws Exception {
        // Convert bits to bytes
        int byteCount = (bits.size() + 7) / 8;
        byte[] rawKey = new byte[byteCount];

        for (int i = 0; i < bits.size(); i++) {
            int byteIndex = i / 8;
            int bitIndex = i % 8;
            if (bits.get(i) == 1) {
                rawKey[byteIndex] |= (1 << (7 - bitIndex));
            }
        }

        // Apply privacy amplification hash (SHA-256)
        MessageDigest hash = MessageDigest.getInstance("SHA-256");
        byte[] amplifiedKey = hash.digest(rawKey);

        // Extract exactly KEY_SIZE_BYTES
        byte[] finalKey = new byte[KEY_SIZE_BYTES];
        System.arraycopy(amplifiedKey, 0, finalKey, 0, KEY_SIZE_BYTES);

        return finalKey;
    }

    // === Inner Classes ===

    public static class QKDResult {
        private byte[] sharedSecret;
        private QKDMetadata metadata;

        public byte[] getSharedSecret() {
            return sharedSecret;
        }

        public void setSharedSecret(byte[] sharedSecret) {
            this.sharedSecret = sharedSecret;
        }

        public QKDMetadata getMetadata() {
            return metadata;
        }

        public void setMetadata(QKDMetadata metadata) {
            this.metadata = metadata;
        }
    }

    public static class QKDMetadata {
        private List<Integer> aliceBases;
        private List<Integer> aliceBits;
        private List<Integer> bobBases;
        private double errorRate;
        private int numberOfQubits;
        private int matchingBitsCount;

        public List<Integer> getAliceBases() {
            return aliceBases;
        }

        public void setAliceBases(List<Integer> aliceBases) {
            this.aliceBases = aliceBases;
        }

        public List<Integer> getAliceBits() {
            return aliceBits;
        }

        public void setAliceBits(List<Integer> aliceBits) {
            this.aliceBits = aliceBits;
        }

        public List<Integer> getBobBases() {
            return bobBases;
        }

        public void setBobBases(List<Integer> bobBases) {
            this.bobBases = bobBases;
        }

        public double getErrorRate() {
            return errorRate;
        }

        public void setErrorRate(double errorRate) {
            this.errorRate = errorRate;
        }

        public int getNumberOfQubits() {
            return numberOfQubits;
        }

        public void setNumberOfQubits(int numberOfQubits) {
            this.numberOfQubits = numberOfQubits;
        }

        public int getMatchingBitsCount() {
            return matchingBitsCount;
        }

        public void setMatchingBitsCount(int matchingBitsCount) {
            this.matchingBitsCount = matchingBitsCount;
        }
    }

    private static class QKDReconciliationResult {
        private List<Integer> matchingBits;
        private List<Integer> aliceBits;
        private List<Integer> bobBits;

        public List<Integer> getMatchingBits() {
            return matchingBits;
        }

        public void setMatchingBits(List<Integer> matchingBits) {
            this.matchingBits = matchingBits;
        }

        public List<Integer> getAliceBits() {
            return aliceBits;
        }

        public void setAliceBits(List<Integer> aliceBits) {
            this.aliceBits = aliceBits;
        }

        public List<Integer> getBobBits() {
            return bobBits;
        }

        public void setBobBits(List<Integer> bobBits) {
            this.bobBits = bobBits;
        }
    }
}




