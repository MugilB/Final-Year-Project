package com.securevoting.service;

import com.securevoting.model.Block;
import com.securevoting.model.Election;
import com.securevoting.model.UserDetails;
import com.securevoting.repository.BlockRepository;
import com.securevoting.repository.ElectionRepository;
import com.securevoting.repository.UserDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class VoteService {

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @Autowired
    private ElectionRepository electionRepository;

    @Autowired
    private CryptoService cryptoService;

    @Autowired
    private SteganographyService steganographyService;

    public boolean castVote(String voterId, String voteData, int electionId) {
        Optional<UserDetails> userDetailsOpt = userDetailsRepository.findByVoterId(voterId);
        if (userDetailsOpt.isEmpty()) {
            return false;
        }
        String userVoterId = userDetailsOpt.get().getVoterId();

        if (blockRepository.existsByVoterIdAndElectionId(userVoterId, electionId)) {
            return false;
        }

        Optional<Election> electionOpt = electionRepository.findById(electionId);
        if (electionOpt.isEmpty()) {
            return false;
        }
        String electionName = electionOpt.get().getName();

        try {
            String voteJson = "{\"voterId\":\"" + userVoterId + "\", \"voteData\":\"" + voteData + "\", \"electionId\":" + electionId + ", \"electionName\":\"" + electionName + "\"}";
            String encryptedPayloadJson = cryptoService.encryptVote(voteJson);
            byte[] stegoImageData = steganographyService.embedData(encryptedPayloadJson.getBytes());

            String previousHash = blockRepository.findTopByOrderByBlockHeightDesc().map(Block::getHash).orElse("0");
            int newBlockHeight = blockRepository.findTopByOrderByBlockHeightDesc().map(b -> b.getBlockHeight() + 1).orElse(0);

            Block newBlock = new Block("Encrypted vote saved in stego_image_data.", previousHash, userVoterId, newBlockHeight, electionId, electionName);
            newBlock.mineBlock(4);
            newBlock.setStegoImageData(stegoImageData);
            blockRepository.save(newBlock);

            UserDetails userDetails = userDetailsOpt.get();
            userDetails.setNoElectionsVoted(userDetails.getNoElectionsVoted() + 1);
            userDetailsRepository.save(userDetails);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}