package com.securevoting.controller;

import com.securevoting.model.ElectionDetails;
import com.securevoting.service.ElectionDetailsService;
import com.securevoting.repository.ElectionDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private ElectionDetailsService electionDetailsService;

    @Autowired
    private ElectionDetailsRepository electionDetailsRepository;

    @PostMapping("/election-details")
    public String testElectionDetails(@RequestParam int electionId, 
                                    @RequestParam(required = false) String description,
                                    @RequestParam(required = false) String rules) {
        try {
            ElectionDetails details = new ElectionDetails();
            details.setElectionId(electionId);
            details.setDescription(description);
            details.setRules(rules);
            
            ElectionDetails saved = electionDetailsService.createElectionDetails(details);
            return "Election details saved successfully with ID: " + saved.getElectionId();
        } catch (Exception e) {
            return "Error saving election details: " + e.getMessage();
        }
    }

    @GetMapping("/election-details/{electionId}")
    public String getElectionDetails(@PathVariable int electionId) {
        try {
            return electionDetailsService.getElectionDetailsByElectionId(electionId)
                    .map(details -> "Found: ID=" + details.getElectionId() + 
                                   ", Description=" + details.getDescription() + 
                                   ", Rules=" + details.getRules())
                    .orElse("No election details found for ID: " + electionId);
        } catch (Exception e) {
            return "Error retrieving election details: " + e.getMessage();
        }
    }

    @GetMapping("/election-details/count")
    public String getElectionDetailsCount() {
        try {
            long count = electionDetailsRepository.countAllElectionDetails();
            return "Total election details count: " + count;
        } catch (Exception e) {
            return "Error getting count: " + e.getMessage();
        }
    }
}
