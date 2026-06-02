package com.edabip.repository;

import com.edabip.entity.Employee;
import com.edabip.entity.enums.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Employee> findByDepartmentId(Long departmentId);

    List<Employee> findByStatus(EmployeeStatus status);

    long countByStatus(EmployeeStatus status);

    long countByDepartmentId(Long departmentId);

    @Query("SELECT e FROM Employee e WHERE " +
           "(:name IS NULL OR LOWER(CONCAT(e.firstName, ' ', e.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:departmentId IS NULL OR e.department.id = :departmentId) AND " +
           "(:status IS NULL OR e.status = :status)")
    List<Employee> searchEmployees(
        @Param("name") String name,
        @Param("departmentId") Long departmentId,
        @Param("status") EmployeeStatus status
    );

    @Query("SELECT e FROM Employee e WHERE e.department.id = :deptId ORDER BY e.firstName")
    List<Employee> findByDepartmentIdOrderByName(@Param("deptId") Long departmentId);
}
