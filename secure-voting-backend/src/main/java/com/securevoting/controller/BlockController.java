package com.securevoting.controller;

import com.securevoting.model.Block;
import com.securevoting.service.BlockService;
import com.securevoting.service.UnifiedCryptoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/blocks")
@CrossOrigin(origins = "*")
public class BlockController {
    
    @Autowired
    private BlockService blockService;
    
    @Autowired
    private UnifiedCryptoService cryptoService;
    
    @GetMapping
    public ResponseEntity<List<Block>> getAllBlocks() {
        List<Block> blocks = blockService.getAllBlocks();
        return ResponseEntity.ok(blocks);
    }
    
    @GetMapping("/latest")
    public ResponseEntity<Block> getLatestBlock() {
        Optional<Block> block = blockService.getLatestBlock();
        return block.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/count")
    public ResponseEntity<Long> getTotalBlockCount() {
        Long count = blockService.getTotalBlockCount();
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/election/{electionId}")
    public ResponseEntity<List<Block>> getBlocksByElection(@PathVariable Integer electionId) {
        List<Block> blocks = blockService.getBlocksByElection(electionId);
        return ResponseEntity.ok(blocks);
    }
    
    @GetMapping("/voter/{voterId}")
    public ResponseEntity<List<Block>> getBlocksByVoter(@PathVariable String voterId) {
        List<Block> blocks = blockService.getBlocksByVoter(voterId);
        return ResponseEntity.ok(blocks);
    }
    
    @GetMapping("/height/{blockHeight}")
    public ResponseEntity<Block> getBlockByHeight(@PathVariable Integer blockHeight) {
        Optional<Block> block = blockService.getBlockByHeight(blockHeight);
        return block.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/hash/{hash}")
    public ResponseEntity<Block> getBlockByHash(@PathVariable String hash) {
        Optional<Block> block = blockService.getBlockByHash(hash);
        return block.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/range")
    public ResponseEntity<List<Block>> getBlocksInRange(
            @RequestParam Integer startHeight, 
            @RequestParam Integer endHeight) {
        List<Block> blocks = blockService.getBlocksInRange(startHeight, endHeight);
        return ResponseEntity.ok(blocks);
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<List<Object[]>> getBlockchainStatistics() {
        List<Object[]> statistics = blockService.getBlockchainStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    @GetMapping("/election/{electionId}/count")
    public ResponseEntity<Long> getBlockCountByElection(@PathVariable Integer electionId) {
        Long count = blockService.getBlockCountByElection(electionId);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/test-encryption")
    public ResponseEntity<Map<String, Object>> testEncryption() {
        try {
            // Test the encryption process
            String testVoteJson = "{\"voterId\":\"test\", \"voteData\":\"test\", \"electionId\":1, \"electionName\":\"test\"}";
            String encryptedPayloadJson = cryptoService.encryptVote(testVoteJson);
            
            Map<String, Object> response = new HashMap<>();
            response.put("testVoteJson", testVoteJson);
            response.put("encryptedPayloadJson", encryptedPayloadJson);
            response.put("encryptedPayloadJsonLength", encryptedPayloadJson.length());
            response.put("encryptedPayloadJsonFirst100", encryptedPayloadJson.substring(0, Math.min(100, encryptedPayloadJson.length())));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error testing encryption: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to test encryption: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/{blockHeight}/analyze-key")
    public ResponseEntity<Map<String, Object>> analyzeKeyFromBlock(@PathVariable Integer blockHeight) {
        try {
            Optional<Block> blockOpt = blockService.getBlockByHeight(blockHeight);
            if (blockOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Block block = blockOpt.get();
            
            if ("SYSTEM".equals(block.getVoterId())) {
                Map<String, Object> response = new HashMap<>();
                response.put("error", "Cannot analyze system blocks");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Extract the steganographic data
            com.securevoting.service.SteganographyService steganographyService = new com.securevoting.service.SteganographyService();
            byte[] extractedData = steganographyService.extractData(block.getStegoImageData());
            String extractedString = new String(extractedData);
            
            // Split the data
            String[] parts = extractedString.split(":");
            
            Map<String, Object> response = new HashMap<>();
            response.put("blockHeight", block.getBlockHeight());
            response.put("extractedDataLength", extractedString.length());
            response.put("parts", parts.length);
            
            if (parts.length >= 2) {
                String keyB64 = parts[0];
                byte[] keyBytes = java.util.Base64.getDecoder().decode(keyB64);
                
                response.put("keyBase64Length", keyB64.length());
                response.put("keyBytesLength", keyBytes.length);
                response.put("keyFirstBytes", java.util.Arrays.toString(java.util.Arrays.copyOfRange(keyBytes, 0, Math.min(10, keyBytes.length))));
                response.put("keyLastBytes", java.util.Arrays.toString(java.util.Arrays.copyOfRange(keyBytes, Math.max(0, keyBytes.length - 10), keyBytes.length)));
                
                if (keyBytes.length > 0) {
                    response.put("firstByteHex", "0x" + Integer.toHexString(keyBytes[0] & 0xFF));
                    response.put("isX509Format", keyBytes[0] == 0x30); // DER SEQUENCE starts with 0x30
                }
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error analyzing key from block " + blockHeight + ": " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to analyze key: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/{blockHeight}/decrypt-vote")
    public ResponseEntity<Map<String, Object>> decryptVoteFromBlock(@PathVariable Integer blockHeight) {
        try {
            Optional<Block> blockOpt = blockService.getBlockByHeight(blockHeight);
            if (blockOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Block block = blockOpt.get();
            
            // Skip system blocks
            if ("SYSTEM".equals(block.getVoterId())) {
                Map<String, Object> response = new HashMap<>();
                response.put("error", "Cannot decrypt system blocks");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Decrypt the vote from the steganographic image
            String decryptedVoteJson = cryptoService.decryptVote(block.getStegoImageData());
            
            // Parse the decrypted JSON
            com.google.gson.Gson gson = new com.google.gson.Gson();
            @SuppressWarnings("unchecked")
            Map<String, Object> voteMap = gson.fromJson(decryptedVoteJson, Map.class);
            
            // Create response with vote details
            Map<String, Object> response = new HashMap<>();
            response.put("blockHeight", block.getBlockHeight());
            response.put("voterId", block.getVoterId());
            response.put("electionId", block.getElectionId());
            response.put("electionName", block.getElectionName());
            response.put("timestamp", block.getTimestamp());
            response.put("rawData", decryptedVoteJson);
            response.put("voteData", voteMap.get("voteData"));
            response.put("candidateName", voteMap.get("voteData"));
            response.put("voteHash", voteMap.get("voteHash"));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error decrypting vote from block " + blockHeight + ": " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to decrypt vote: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}

