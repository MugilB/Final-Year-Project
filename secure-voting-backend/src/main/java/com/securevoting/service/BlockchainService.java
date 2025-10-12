package com.securevoting.service;

import com.google.gson.Gson;
import com.securevoting.model.Block;
import com.securevoting.model.User;
import com.securevoting.repository.BlockRepository;
import com.securevoting.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BlockchainService {

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CryptoService cryptoService;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<Block> getBlockchain() {
        return blockRepository.findAll();
    }

    public Map<String, Map<String, Integer>> tallyVotes() {
        List<Block> blocks = blockRepository.findAll();
        Map<String, Map<String, Integer>> electionResults = new HashMap<>();

        for (Block block : blocks) {
            if ("SYSTEM".equals(block.getVoterId())) {
                continue; // Skip Genesis Block
            }

            try {
                String decryptedVoteJson = cryptoService.decryptVote(block.getStegoImageData());
                Gson gson = new Gson();
                Map<String, Object> voteMap = gson.fromJson(decryptedVoteJson, Map.class);
                String electionName = (String) voteMap.get("electionName");
                String vote = (String) voteMap.get("voteData");

                electionResults
                        .computeIfAbsent(electionName, k -> new HashMap<>())
                        .merge(vote, 1, Integer::sum);
            } catch (Exception e) {
                System.err.println("Could not process vote from block " + block.getBlockHeight() + ". Error: " + e.getMessage());
            }
        }
        return electionResults;
    }

    public String decryptVote(int blockHeight) {
        Optional<Block> blockOpt = blockRepository.findById(blockHeight);
        if (blockOpt.isEmpty()) {
            return "Block not found.";
        }
        Block block = blockOpt.get();
        if ("SYSTEM".equals(block.getVoterId())) {
            return "This is the Genesis Block. It contains no vote data.";
        }
        try {
            return cryptoService.decryptVote(block.getStegoImageData());
        } catch (Exception e) {
            return "Decryption failed: " + e.getMessage();
        }
    }

    public Map<String, Long> getVoteStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalBlocks", blockRepository.getTotalBlocks());
        stats.put("uniqueVoters", blockRepository.getUniqueVoters());
        return stats;
    }
}