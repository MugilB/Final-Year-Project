package com.securevoting.repository;

import com.securevoting.model.Block;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlockRepository extends JpaRepository<Block, Integer> {
    
    // Find blocks by election ID
    List<Block> findByElectionIdOrderByBlockHeightAsc(Integer electionId);
    
    Optional<Block> findByVoterIdAndElectionId(String voterId, Integer electionId);
    
    // Find blocks by voter ID
    List<Block> findByVoterIdOrderByBlockHeightAsc(String voterId);
    
    // Find the latest block
    @Query("SELECT b FROM Block b ORDER BY b.blockHeight DESC")
    List<Block> findLatestBlock();
    
    // Count total blocks
    @Query("SELECT COUNT(b) FROM Block b")
    Long countTotalBlocks();
    
    // Count blocks by election
    @Query("SELECT COUNT(b) FROM Block b WHERE b.electionId = :electionId")
    Long countBlocksByElection(@Param("electionId") Integer electionId);
    
    // Get blockchain statistics
    @Query("SELECT b.electionId, b.electionName, COUNT(b) as blockCount FROM Block b GROUP BY b.electionId, b.electionName")
    List<Object[]> getBlockchainStatistics();
    
    // Find blocks by hash
    Optional<Block> findByHash(String hash);
    
    // Get blocks in a range
    @Query("SELECT b FROM Block b WHERE b.blockHeight BETWEEN :startHeight AND :endHeight ORDER BY b.blockHeight ASC")
    List<Block> findBlocksInRange(@Param("startHeight") Integer startHeight, @Param("endHeight") Integer endHeight);
    
    // Check if voter has already voted in an election
    boolean existsByVoterIdAndElectionId(String voterId, Integer electionId);
    
    // Find the latest block by block height
    Optional<Block> findTopByOrderByBlockHeightDesc();
    
    // Get total number of blocks
    @Query("SELECT COUNT(b) FROM Block b")
    Long getTotalBlocks();
    
    // Get count of unique voters
    @Query("SELECT COUNT(DISTINCT b.voterId) FROM Block b WHERE b.voterId != 'SYSTEM'")
    Long getUniqueVoters();
    
    // Update voter_id in blocks (for username changes) - using native SQL to bypass Hibernate constraints
    @Modifying
    @Query(value = "UPDATE blocks SET voter_id = :newVoterId WHERE voter_id = :oldVoterId", nativeQuery = true)
    void updateVoterIdInBlocks(@Param("oldVoterId") String oldVoterId, @Param("newVoterId") String newVoterId);
}