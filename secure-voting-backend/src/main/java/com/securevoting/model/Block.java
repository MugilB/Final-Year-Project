package com.securevoting.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Entity
@Table(name = "blocks")
public class Block {
    
    @Id
    @Column(name = "block_height")
    private Integer blockHeight;
    
    @Column(name = "hash", unique = true, nullable = false, length = 64)
    private String hash;
    
    @Column(name = "previous_hash", nullable = false, length = 64)
    private String previousHash;
    
    @Column(name = "election_id")
    private Integer electionId;
    
    @Column(name = "election_name", length = 255)
    private String electionName;
    
    @Column(name = "voter_id", nullable = false, length = 50)
    private String voterId;
    
    @Column(name = "data", columnDefinition = "TEXT", nullable = false)
    private String data;
    
    @Lob
    @Column(name = "stego_image_data")
    private byte[] stegoImageData;
    
    @Column(name = "timestamp", nullable = false)
    private Long timestamp;
    
    @Column(name = "nonce", nullable = false)
    private Integer nonce;
    
    // Constructors
    public Block() {}
    
    public Block(Integer blockHeight, String hash, String previousHash, Integer electionId, 
                String electionName, String voterId, String data, byte[] stegoImageData, 
                Long timestamp, Integer nonce) {
        this.blockHeight = blockHeight;
        this.hash = hash;
        this.previousHash = previousHash;
        this.electionId = electionId;
        this.electionName = electionName;
        this.voterId = voterId;
        this.data = data;
        this.stegoImageData = stegoImageData;
        this.timestamp = timestamp;
        this.nonce = nonce;
    }
    
    // Constructor for VoteService
    public Block(String data, String previousHash, String voterId, Integer blockHeight, Integer electionId, String electionName) {
        this.data = data;
        this.previousHash = previousHash;
        this.voterId = voterId;
        this.blockHeight = blockHeight;
        this.electionId = electionId;
        this.electionName = electionName;
        this.timestamp = System.currentTimeMillis();
        this.nonce = 0;
        this.hash = ""; // Will be set by mineBlock method
    }
    
    // Getters and Setters
    public Integer getBlockHeight() {
        return blockHeight;
    }
    
    public void setBlockHeight(Integer blockHeight) {
        this.blockHeight = blockHeight;
    }
    
    public String getHash() {
        return hash;
    }
    
    public void setHash(String hash) {
        this.hash = hash;
    }
    
    public String getPreviousHash() {
        return previousHash;
    }
    
    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }
    
    public Integer getElectionId() {
        return electionId;
    }
    
    public void setElectionId(Integer electionId) {
        this.electionId = electionId;
    }
    
    public String getElectionName() {
        return electionName;
    }
    
    public void setElectionName(String electionName) {
        this.electionName = electionName;
    }
    
    public String getVoterId() {
        return voterId;
    }
    
    public void setVoterId(String voterId) {
        this.voterId = voterId;
    }
    
    public String getData() {
        return data;
    }
    
    public void setData(String data) {
        this.data = data;
    }
    
    public byte[] getStegoImageData() {
        return stegoImageData;
    }
    
    public void setStegoImageData(byte[] stegoImageData) {
        this.stegoImageData = stegoImageData;
    }
    
    public Long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
    
    public Integer getNonce() {
        return nonce;
    }
    
    public void setNonce(Integer nonce) {
        this.nonce = nonce;
    }
    
    // Mining method for proof of work
    public void mineBlock(int difficulty) {
        String target = new String(new char[difficulty]).replace('\0', '0');
        hash = calculateHash(); // Initialize hash before mining
        
        while (!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
    }
    
    // Calculate hash for the block
    public String calculateHash() {
        String dataToHash = previousHash + Long.toString(timestamp) + Integer.toString(nonce) + data;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(dataToHash.getBytes());
            
            // Convert byte array to hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}