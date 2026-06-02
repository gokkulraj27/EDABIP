package com.edabip.controller;

import com.edabip.dto.response.ApiResponse;
import com.edabip.entity.Report;
import com.edabip.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Report>> generate(@RequestBody Map<String, Object> body) {
        String title        = (String) body.get("title");
        String type         = (String) body.get("type");
        String content      = (String) body.get("content");
        Long departmentId   = body.get("departmentId") != null
                              ? Long.valueOf(body.get("departmentId").toString()) : null;
        LocalDate fromDate  = body.get("fromDate") != null
                              ? LocalDate.parse(body.get("fromDate").toString()) : null;
        LocalDate toDate    = body.get("toDate") != null
                              ? LocalDate.parse(body.get("toDate").toString()) : null;

        Report report = reportService.generateReport(title, type, content, departmentId, fromDate, toDate);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Report generated", report));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Report>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Success", reportService.getReport(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Report>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Success", reportService.getAllReports()));
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<Report>>> filter(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(
            ApiResponse.success("Filtered reports", reportService.filterReports(departmentId, fromDate, toDate))
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        reportService.deleteReport(id);
        return ResponseEntity.ok(ApiResponse.success("Report deleted", null));
    }
}
