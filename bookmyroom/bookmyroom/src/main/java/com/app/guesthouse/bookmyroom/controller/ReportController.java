package com.app.guesthouse.bookmyroom.controller;

import com.app.guesthouse.bookmyroom.dto.DashboardResponse;
import com.app.guesthouse.bookmyroom.dto.OccupancyReport;
import com.app.guesthouse.bookmyroom.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /** Feature 5 - occupancy report: /api/reports/occupancy?month=2026-10 */
    @GetMapping("/reports/occupancy")
    public OccupancyReport occupancy(@RequestParam(name = "month") String month) {
        return reportService.occupancyReport(month);
    }

    /** Dashboard totals (rooms, guests, bookings, today's occupancy, revenue) */
    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {
        return reportService.dashboard();
    }
}
