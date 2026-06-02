package com.edabip.service;

import com.edabip.dto.request.DepartmentRequest;
import com.edabip.dto.response.DepartmentResponse;
import com.edabip.entity.Department;
import com.edabip.exception.BadRequestException;
import com.edabip.exception.ResourceNotFoundException;
import com.edabip.repository.DepartmentRepository;
import com.edabip.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final AuditService auditService;

    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        if (departmentRepository.existsByName(request.getName())) {
            throw new BadRequestException("Department already exists: " + request.getName());
        }
        Department dept = Department.builder()
            .name(request.getName())
            .description(request.getDescription())
            .managerId(request.getManagerId())
            .build();
        Department saved = departmentRepository.save(dept);
        auditService.log(null, "SYSTEM", "CREATE", "DEPARTMENT", saved.getId(), "Created: " + saved.getName());
        return toResponse(saved);
    }

    public DepartmentResponse getDepartment(Long id) {
        return toResponse(findById(id));
    }

    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        Department dept = findById(id);
        if (!dept.getName().equals(request.getName()) && departmentRepository.existsByName(request.getName())) {
            throw new BadRequestException("Department name already in use: " + request.getName());
        }
        dept.setName(request.getName());
        dept.setDescription(request.getDescription());
        dept.setManagerId(request.getManagerId());
        auditService.log(null, "SYSTEM", "UPDATE", "DEPARTMENT", id, "Updated: " + dept.getName());
        return toResponse(departmentRepository.save(dept));
    }

    @Transactional
    public void deleteDepartment(Long id) {
        Department dept = findById(id);
        long empCount = employeeRepository.countByDepartmentId(id);
        if (empCount > 0) {
            throw new BadRequestException(
                "Cannot delete department '" + dept.getName() + "': it has " + empCount + " employee(s)"
            );
        }
        departmentRepository.delete(dept);
        auditService.log(null, "SYSTEM", "DELETE", "DEPARTMENT", id, "Deleted: " + dept.getName());
    }

    public Department findById(Long id) {
        return departmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
    }

    private DepartmentResponse toResponse(Department dept) {
        long empCount = employeeRepository.countByDepartmentId(dept.getId());
        String managerName = resolveManagerName(dept.getManagerId());
        return DepartmentResponse.builder()
            .id(dept.getId())
            .name(dept.getName())
            .description(dept.getDescription())
            .managerId(dept.getManagerId())
            .managerName(managerName)
            .employeeCount(empCount)
            .createdAt(dept.getCreatedAt())
            .build();
    }

    private String resolveManagerName(Long managerId) {
        if (managerId == null) return null;
        return employeeRepository.findById(managerId)
            .map(e -> e.getFirstName() + " " + e.getLastName())
            .orElse(null);
    }
}
