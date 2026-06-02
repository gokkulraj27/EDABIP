package com.edabip.service;

import com.edabip.entity.Report;
import com.edabip.exception.ResourceNotFoundException;
import com.edabip.repository.DepartmentRepository;
import com.edabip.repository.ReportRepository;
import com.edabip.repository.UserRepository;
import com.edabip.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional
    public Report generateReport(String title, String type, String content,
                                  Long departmentId, LocalDate fromDate, LocalDate toDate) {
        if (departmentId != null && !departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException("Department", "id", departmentId);
        }

        Long currentUserId = getCurrentUserId();

        Report report = Report.builder()
            .title(title)
            .type(type)
            .content(content)
            .generatedById(currentUserId)
            .departmentId(departmentId)
            .fromDate(fromDate)
            .toDate(toDate)
            .build();

        Report saved = reportRepository.save(report);
        auditService.log(currentUserId, "SYSTEM", "GENERATE", "REPORT", saved.getId(), "Generated: " + title);
        return saved;
    }

    public Report getReport(Long id) {
        return reportRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Report", "id", id));
    }

    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    public List<Report> filterReports(Long departmentId, LocalDate fromDate, LocalDate toDate) {
        return reportRepository.filterReports(departmentId, fromDate, toDate);
    }

    @Transactional
    public void deleteReport(Long id) {
        Report report = getReport(id);
        reportRepository.delete(report);
        auditService.log(getCurrentUserId(), "SYSTEM", "DELETE", "REPORT", id, "Deleted: " + report.getTitle());
    }

    private Long getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails ud) {
            return ud.getId();
        }
        return null;
    }
}
