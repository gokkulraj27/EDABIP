package com.edabip.controller;

import com.edabip.dto.request.TaskRequest;
import com.edabip.dto.response.ApiResponse;
import com.edabip.dto.response.TaskResponse;
import com.edabip.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<TaskResponse>> create(@Valid @RequestBody TaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Task created", taskService.createTask(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Success", taskService.getTask(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Success", taskService.getAllTasks()));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(ApiResponse.success("Success", taskService.getByProject(projectId)));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(ApiResponse.success("Success", taskService.getByEmployee(employeeId)));
    }

    @GetMapping("/employee/{employeeId}/pending")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getPendingByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(ApiResponse.success("Success", taskService.getPendingByEmployee(employeeId)));
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getOverdue() {
        return ResponseEntity.ok(ApiResponse.success("Overdue tasks", taskService.getOverdueTasks()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> update(
            @PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Task updated", taskService.updateTask(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok(ApiResponse.success("Task deleted", null));
    }
}
