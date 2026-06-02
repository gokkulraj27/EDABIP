package com.edabip.repository;

import com.edabip.entity.Project;
import com.edabip.entity.enums.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByDepartmentId(Long departmentId);

    List<Project> findByStatus(ProjectStatus status);

    long countByStatus(ProjectStatus status);
}
