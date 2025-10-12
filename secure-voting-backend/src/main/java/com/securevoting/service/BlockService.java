package com.securevoting.service;

import com.securevoting.model.Block;
import com.securevoting.repository.BlockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BlockService {
    
    @Autowired
    private BlockRepository blockRepository;
    
    public List<Block> getAllBlocks() {
        return blockRepository.findAll();
    }
    
    public Optional<Block> getBlockByHeight(Integer blockHeight) {
        return blockRepository.findById(blockHeight);
    }
    
    public List<Block> getBlocksByElection(Integer electionId) {
        return blockRepository.findByElectionIdOrderByBlockHeightAsc(electionId);
    }
    
    public List<Block> getBlocksByVoter(String voterId) {
        return blockRepository.findByVoterIdOrderByBlockHeightAsc(voterId);
    }
    
    public Optional<Block> getLatestBlock() {
        List<Block> latestBlocks = blockRepository.findLatestBlock();
        return latestBlocks.isEmpty() ? Optional.empty() : Optional.of(latestBlocks.get(0));
    }
    
    public Long getTotalBlockCount() {
        return blockRepository.countTotalBlocks();
    }
    
    public Long getBlockCountByElection(Integer electionId) {
        return blockRepository.countBlocksByElection(electionId);
    }
    
    public List<Object[]> getBlockchainStatistics() {
        return blockRepository.getBlockchainStatistics();
    }
    
    public Optional<Block> getBlockByHash(String hash) {
        return blockRepository.findByHash(hash);
    }
    
    public List<Block> getBlocksInRange(Integer startHeight, Integer endHeight) {
        return blockRepository.findBlocksInRange(startHeight, endHeight);
    }
    
    public Block saveBlock(Block block) {
        return blockRepository.save(block);
    }
}
