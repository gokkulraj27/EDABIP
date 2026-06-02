package com.edabip.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsResponse {

    private long totalEmployees;
    private long activeEmployees;
    private long totalDepartments;
    private long totalProjects;
    private long activeProjects;
    private long totalTasks;
    private long completedTasks;
    private long pendingTasks;
    private double taskCompletionRate;
    private List<Map<String, Object>> departmentStats;
    private List<Map<String, Object>> monthlyStats;
}
