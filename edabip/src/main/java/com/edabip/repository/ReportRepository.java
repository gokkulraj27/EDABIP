package com.edabip.repository;

import com.edabip.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByDepartmentId(Long departmentId);

    List<Report> findByGeneratedById(Long userId);

    @Query("SELECT r FROM Report r WHERE " +
           "(:departmentId IS NULL OR r.departmentId = :departmentId) AND " +
           "(:fromDate IS NULL OR r.fromDate >= :fromDate) AND " +
           "(:toDate IS NULL OR r.toDate <= :toDate) " +
           "ORDER BY r.createdAt DESC")
    List<Report> filterReports(
        @Param("departmentId") Long departmentId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate
    );
}
