package com.chitralekha.controller;

import com.chitralekha.service.TasteDiscoveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/discovery")
public class DiscoveryController {
    private final TasteDiscoveryService tasteDiscoveryService;

    public DiscoveryController(TasteDiscoveryService tasteDiscoveryService) {
        this.tasteDiscoveryService = tasteDiscoveryService;
    }

    @GetMapping("/people")
    public ResponseEntity<List<Map<String, Object>>> getTasteRecommendations() {
        return ResponseEntity.ok(tasteDiscoveryService.discoverSimilarUsers());
    }
}
