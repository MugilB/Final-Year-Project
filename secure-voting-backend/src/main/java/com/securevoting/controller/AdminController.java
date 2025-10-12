package com.securevoting.controller;

import com.securevoting.model.User;
import com.securevoting.service.BlockchainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private BlockchainService blockchainService;

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return blockchainService.getAllUsers();
    }

    @GetMapping("/blockchain")
    public ResponseEntity<?> getBlockchain() {
        return ResponseEntity.ok(blockchainService.getBlockchain());
    }

    @GetMapping("/tally")
    public ResponseEntity<Map<String, Map<String, Integer>>> tallyVotes() {
        return ResponseEntity.ok(blockchainService.tallyVotes());
    }

    @GetMapping("/decrypt/{blockHeight}")
    public ResponseEntity<String> decryptVote(@PathVariable int blockHeight) {
        return ResponseEntity.ok(blockchainService.decryptVote(blockHeight));
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getVoteStatistics() {
        return ResponseEntity.ok(blockchainService.getVoteStatistics());
    }
}