package com.edabip.service;

import com.edabip.dto.request.EmployeeRequest;
import com.edabip.dto.response.EmployeeResponse;
import com.edabip.entity.Department;
import com.edabip.entity.Employee;
import com.edabip.entity.User;
import com.edabip.entity.enums.EmployeeStatus;
import com.edabip.exception.BadRequestException;
import com.edabip.exception.ResourceNotFoundException;
import com.edabip.repository.DepartmentRepository;
import com.edabip.repository.EmployeeRepository;
import com.edabip.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional
    public EmployeeResponse createEmployee(EmployeeRequest req) {
        if (employeeRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email already registered: " + req.getEmail());
        }
        Employee emp = Employee.builder()
            .firstName(req.getFirstName())
            .lastName(req.getLastName())
            .email(req.getEmail())
            .phone(req.getPhone())
            .gender(req.getGender())
            .designation(req.getDesignation())
            .salary(req.getSalary())
            .joiningDate(req.getJoiningDate())
            .status(req.getStatus() != null ? req.getStatus() : EmployeeStatus.ACTIVE)
            .build();

        if (req.getDepartmentId() != null) {
            emp.setDepartment(findDept(req.getDepartmentId()));
        }
        if (req.getUserId() != null) {
            emp.setUser(findUser(req.getUserId()));
        }

        Employee saved = employeeRepository.save(emp);
        auditService.log(null, "SYSTEM", "CREATE", "EMPLOYEE", saved.getId(), "Created: " + saved.getEmail());
        return toResponse(saved);
    }

    public EmployeeResponse getEmployee(Long id) {
        return toResponse(findById(id));
    }

    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<EmployeeResponse> searchEmployees(String name, Long departmentId, EmployeeStatus status) {
        return employeeRepository.searchEmployees(name, departmentId, status)
            .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest req) {
        Employee emp = findById(id);
        if (!emp.getEmail().equals(req.getEmail()) && employeeRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email already registered: " + req.getEmail());
        }
        emp.setFirstName(req.getFirstName());
        emp.setLastName(req.getLastName());
        emp.setEmail(req.getEmail());
        emp.setPhone(req.getPhone());
        emp.setGender(req.getGender());
        emp.setDesignation(req.getDesignation());
        emp.setSalary(req.getSalary());
        emp.setJoiningDate(req.getJoiningDate());
        if (req.getStatus() != null) emp.setStatus(req.getStatus());
        if (req.getDepartmentId() != null) emp.setDepartment(findDept(req.getDepartmentId()));

        auditService.log(null, "SYSTEM", "UPDATE", "EMPLOYEE", id, "Updated: " + emp.getEmail());
        return toResponse(employeeRepository.save(emp));
    }

    @Transactional
    public void deleteEmployee(Long id) {
        Employee emp = findById(id);
        employeeRepository.delete(emp);
        auditService.log(null, "SYSTEM", "DELETE", "EMPLOYEE", id, "Deleted: " + emp.getEmail());
    }

    public Employee findById(Long id) {
        return employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
    }

    public EmployeeResponse toResponse(Employee emp) {
        return EmployeeResponse.builder()
            .id(emp.getId())
            .firstName(emp.getFirstName())
            .lastName(emp.getLastName())
            .fullName(emp.getFirstName() + " " + emp.getLastName())
            .email(emp.getEmail())
            .phone(emp.getPhone())
            .gender(emp.getGender())
            .designation(emp.getDesignation())
            .departmentId(emp.getDepartment() != null ? emp.getDepartment().getId() : null)
            .departmentName(emp.getDepartment() != null ? emp.getDepartment().getName() : null)
            .salary(emp.getSalary())
            .joiningDate(emp.getJoiningDate())
            .status(emp.getStatus())
            .createdAt(emp.getCreatedAt())
            .build();
    }

    private Department findDept(Long id) {
        return departmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }
}
