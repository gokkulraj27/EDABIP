package com.edabip.service;

import com.edabip.dto.request.TaskRequest;
import com.edabip.dto.response.TaskResponse;
import com.edabip.entity.Employee;
import com.edabip.entity.Project;
import com.edabip.entity.Task;
import com.edabip.entity.enums.TaskPriority;
import com.edabip.entity.enums.TaskStatus;
import com.edabip.exception.ResourceNotFoundException;
import com.edabip.repository.EmployeeRepository;
import com.edabip.repository.ProjectRepository;
import com.edabip.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditService auditService;

    @Transactional
    public TaskResponse createTask(TaskRequest req) {
        Project project = projectRepository.findById(req.getProjectId())
            .orElseThrow(() -> new ResourceNotFoundException("Project", "id", req.getProjectId()));

        Task task = Task.builder()
            .title(req.getTitle())
            .description(req.getDescription())
            .project(project)
            .status(req.getStatus() != null ? req.getStatus() : TaskStatus.TODO)
            .priority(req.getPriority() != null ? req.getPriority() : TaskPriority.MEDIUM)
            .dueDate(req.getDueDate())
            .build();

        if (req.getAssignedToId() != null) {
            Employee emp = employeeRepository.findById(req.getAssignedToId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", req.getAssignedToId()));
            task.setAssignedTo(emp);
        }

        Task saved = taskRepository.save(task);
        auditService.log(null, "SYSTEM", "CREATE", "TASK", saved.getId(), "Created: " + saved.getTitle());
        return toResponse(saved);
    }

    public TaskResponse getTask(Long id) {
        return toResponse(findById(id));
    }

    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<TaskResponse> getByProject(Long projectId) {
        return taskRepository.findByProjectId(projectId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<TaskResponse> getByEmployee(Long employeeId) {
        return taskRepository.findByAssignedToId(employeeId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<TaskResponse> getPendingByEmployee(Long employeeId) {
        return taskRepository.findPendingTasksByEmployee(employeeId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<TaskResponse> getOverdueTasks() {
        return taskRepository.findOverdueTasks().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest req) {
        Task task = findById(id);
        task.setTitle(req.getTitle());
        task.setDescription(req.getDescription());

        if (req.getStatus() != null) {
            if (req.getStatus() == TaskStatus.COMPLETED && task.getStatus() != TaskStatus.COMPLETED) {
                task.setCompletedAt(LocalDateTime.now());
            }
            task.setStatus(req.getStatus());
        }
        if (req.getPriority() != null) task.setPriority(req.getPriority());
        if (req.getDueDate() != null) task.setDueDate(req.getDueDate());

        if (req.getAssignedToId() != null) {
            Employee emp = employeeRepository.findById(req.getAssignedToId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", req.getAssignedToId()));
            task.setAssignedTo(emp);
        }

        auditService.log(null, "SYSTEM", "UPDATE", "TASK", id, "Updated: " + task.getTitle());
        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(Long id) {
        Task task = findById(id);
        taskRepository.delete(task);
        auditService.log(null, "SYSTEM", "DELETE", "TASK", id, "Deleted: " + task.getTitle());
    }

    private Task findById(Long id) {
        return taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
    }

    public TaskResponse toResponse(Task t) {
        return TaskResponse.builder()
            .id(t.getId())
            .title(t.getTitle())
            .description(t.getDescription())
            .projectId(t.getProject() != null ? t.getProject().getId() : null)
            .projectName(t.getProject() != null ? t.getProject().getName() : null)
            .assignedToId(t.getAssignedTo() != null ? t.getAssignedTo().getId() : null)
            .assignedToName(t.getAssignedTo() != null
                ? t.getAssignedTo().getFirstName() + " " + t.getAssignedTo().getLastName() : null)
            .status(t.getStatus())
            .priority(t.getPriority())
            .dueDate(t.getDueDate())
            .completedAt(t.getCompletedAt())
            .createdAt(t.getCreatedAt())
            .build();
    }
}
