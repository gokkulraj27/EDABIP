package com.edabip.service;

import com.edabip.dto.response.DashboardStatsResponse;
import com.edabip.entity.enums.EmployeeStatus;
import com.edabip.entity.enums.ProjectStatus;
import com.edabip.entity.enums.TaskStatus;
import com.edabip.repository.DepartmentRepository;
import com.edabip.repository.EmployeeRepository;
import com.edabip.repository.ProjectRepository;
import com.edabip.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats() {
        long totalEmployees = employeeRepository.count();
        long activeEmployees = employeeRepository.countByStatus(EmployeeStatus.ACTIVE);
        long totalDepartments = departmentRepository.count();
        long totalProjects = projectRepository.count();
        long activeProjects = projectRepository.countByStatus(ProjectStatus.IN_PROGRESS);
        long totalTasks = taskRepository.count();
        long completedTasks = taskRepository.countByStatus(TaskStatus.COMPLETED);
        long pendingTasks = taskRepository.countByStatus(TaskStatus.TODO)
                          + taskRepository.countByStatus(TaskStatus.IN_PROGRESS);
        double completionRate = totalTasks > 0
            ? Math.round((double) completedTasks / totalTasks * 10000.0) / 100.0
            : 0.0;

        return DashboardStatsResponse.builder()
            .totalEmployees(totalEmployees)
            .activeEmployees(activeEmployees)
            .totalDepartments(totalDepartments)
            .totalProjects(totalProjects)
            .activeProjects(activeProjects)
            .totalTasks(totalTasks)
            .completedTasks(completedTasks)
            .pendingTasks(pendingTasks)
            .taskCompletionRate(completionRate)
            .departmentStats(buildDepartmentStats())
            .monthlyStats(buildMonthlyStats())
            .build();
    }

    private List<Map<String, Object>> buildDepartmentStats() {
        List<Map<String, Object>> stats = new ArrayList<>();
        departmentRepository.findAll().forEach(dept -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("departmentId", dept.getId());
            m.put("departmentName", dept.getName());
            m.put("employeeCount", employeeRepository.countByDepartmentId(dept.getId()));
            stats.add(m);
        });
        return stats;
    }

    private List<Map<String, Object>> buildMonthlyStats() {
        int year = LocalDate.now().getYear();
        List<Object[]> rows = taskRepository.findMonthlyCompletedTasks(year);
        Map<Integer, Long> monthMap = new HashMap<>();
        for (Object[] row : rows) {
            monthMap.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }

        String[] monthNames = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("month", monthNames[i - 1]);
            m.put("year", year);
            m.put("completedTasks", monthMap.getOrDefault(i, 0L));
            result.add(m);
        }
        return result;
    }
}
