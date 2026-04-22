package com.example.demo.controller;

import com.example.demo.dto.response.MonthlyReportResponse;
import com.example.demo.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Báo cáo tài chính")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/monthly")
    @Operation(summary = "Lấy báo cáo chi tiêu theo tháng")
    public ResponseEntity<MonthlyReportResponse> getMonthlyReport(
            @AuthenticationPrincipal UserDetails currentUser,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month
    ) {
        // Mặc định: tháng hiện tại
        if (year == 0) year = LocalDate.now().getYear();
        if (month == 0) month = LocalDate.now().getMonthValue();

        MonthlyReportResponse report = reportService.getMonthlyReport(
                currentUser.getUsername(), year, month
        );
        return ResponseEntity.ok(report);
    }
}
