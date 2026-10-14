package com.campusfind.lostfound.controller;

import com.campusfind.lostfound.dto.ApiResponse;
import com.campusfind.lostfound.dto.StatsSummaryDto;
import com.campusfind.lostfound.service.StatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<StatsSummaryDto>> getSummaryStats() {
        StatsSummaryDto stats = statsService.getSummaryStats();
        return ResponseEntity.ok(ApiResponse.ok("System statistics retrieved", stats));
    }
}
