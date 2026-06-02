package com.edabip.service;

import com.edabip.dto.request.ProjectRequest;
import com.edabip.dto.response.ProjectResponse;
import com.edabip.entity.Department;
import com.edabip.entity.Project;
import com.edabip.entity.User;
import com.edabip.entity.enums.ProjectStatus;
import com.edabip.entity.enums.TaskStatus;
import com.edabip.exception.ResourceNotFoundException;
import com.edabip.repository.DepartmentRepository;
import com.edabip.repository.ProjectRepository;
import com.edabip.repository.TaskRepository;
import com.edabip.repository.UserRepository;
import com.edabip.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final AuditService auditService;

    @Transactional
    public ProjectResponse createProject(ProjectRequest req) {
        Project project = Project.builder()
            .name(req.getName())
            .description(req.getDescription())
            .startDate(req.getStartDate())
            .endDate(req.getEndDate())
            .status(req.getStatus() != null ? req.getStatus() : ProjectStatus.PLANNING)
            .build();

        if (req.getDepartmentId() != null) {
            project.setDepartment(findDept(req.getDepartmentId()));
        }

        getCurrentUserId().ifPresent(uid ->
            userRepository.findById(uid).ifPresent(project::setCreatedBy)
        );

        Project saved = projectRepository.save(project);
        auditService.log(null, "SYSTEM", "CREATE", "PROJECT", saved.getId(), "Created: " + saved.getName());
        return toResponse(saved);
    }

    public ProjectResponse getProject(Long id) {
        return toResponse(findById(id));
    }

    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<ProjectResponse> getByDepartment(Long deptId) {
        return projectRepository.findByDepartmentId(deptId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<ProjectResponse> getByStatus(ProjectStatus status) {
        return projectRepository.findByStatus(status).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public ProjectResponse updateProject(Long id, ProjectRequest req) {
        Project project = findById(id);
        project.setName(req.getName());
        project.setDescription(req.getDescription());
        project.setStartDate(req.getStartDate());
        project.setEndDate(req.getEndDate());
        if (req.getStatus() != null) project.setStatus(req.getStatus());
        if (req.getDepartmentId() != null) project.setDepartment(findDept(req.getDepartmentId()));

        auditService.log(null, "SYSTEM", "UPDATE", "PROJECT", id, "Updated: " + project.getName());
        return toResponse(projectRepository.save(project));
    }

    @Transactional
    public void deleteProject(Long id) {
        Project project = findById(id);
        projectRepository.delete(project);
        auditService.log(null, "SYSTEM", "DELETE", "PROJECT", id, "Deleted: " + project.getName());
    }

    public Project findById(Long id) {
        return projectRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
    }

    public ProjectResponse toResponse(Project p) {
        long total = taskRepository.countByProjectId(p.getId());
        long done = taskRepository.countByProjectIdAndStatus(p.getId(), TaskStatus.COMPLETED);
        double rate = total > 0 ? Math.round((double) done / total * 10000.0) / 100.0 : 0.0;

        return ProjectResponse.builder()
            .id(p.getId())
            .name(p.getName())
            .description(p.getDescription())
            .startDate(p.getStartDate())
            .endDate(p.getEndDate())
            .status(p.getStatus())
            .departmentId(p.getDepartment() != null ? p.getDepartment().getId() : null)
            .departmentName(p.getDepartment() != null ? p.getDepartment().getName() : null)
            .taskCount(total)
            .completedTaskCount(done)
            .completionRate(rate)
            .createdAt(p.getCreatedAt())
            .build();
    }

    private Department findDept(Long id) {
        return departmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
    }

    private java.util.Optional<Long> getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails ud) {
            return java.util.Optional.of(ud.getId());
        }
        return java.util.Optional.empty();
    }
}
