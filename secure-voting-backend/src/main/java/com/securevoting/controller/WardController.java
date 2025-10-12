package com.securevoting.controller;

import com.securevoting.model.Ward;
import com.securevoting.repository.WardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wards")
@CrossOrigin(origins = "*")
public class WardController {

    @Autowired
    private WardRepository wardRepository;

    @GetMapping
    public ResponseEntity<List<Ward>> getAllWards() {
        try {
            List<Ward> wards = wardRepository.findAll();
            return ResponseEntity.ok(wards);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{wardId}")
    public ResponseEntity<Ward> getWardById(@PathVariable Integer wardId) {
        try {
            return wardRepository.findById(wardId)
                    .map(ward -> ResponseEntity.ok(ward))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}




