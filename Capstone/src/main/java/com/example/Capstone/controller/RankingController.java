package com.example.Capstone.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.Capstone.dto.response.RestaurantRankingResponse;
import com.example.Capstone.service.RankingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/rankings")
@RequiredArgsConstructor
@Tag(name = "Ranking", description = "랭킹 API")
public class RankingController {

    private final RankingService rankingService;

    @Operation(summary = "식당 랭킹 조회")
    @GetMapping("/restaurants")
    public ResponseEntity<RestaurantRankingResponse> getRestaurantRankings(
            @RequestParam(required = false) String regionName,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(rankingService.getRestaurantRankings(regionName, category, limit));
    }
}
