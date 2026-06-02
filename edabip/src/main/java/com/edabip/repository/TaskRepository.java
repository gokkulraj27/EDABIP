package com.edabip.repository;

import com.edabip.entity.Task;
import com.edabip.entity.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProjectId(Long projectId);

    List<Task> findByAssignedToId(Long employeeId);

    List<Task> findByStatus(TaskStatus status);

    long countByStatus(TaskStatus status);

    long countByProjectId(Long projectId);

    long countByProjectIdAndStatus(Long projectId, TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.assignedTo.id = :empId AND t.status <> 'COMPLETED' ORDER BY t.priority DESC")
    List<Task> findPendingTasksByEmployee(@Param("empId") Long employeeId);

    @Query("SELECT MONTH(t.completedAt), COUNT(t) FROM Task t " +
           "WHERE t.status = 'COMPLETED' AND YEAR(t.completedAt) = :year " +
           "GROUP BY MONTH(t.completedAt)")
    List<Object[]> findMonthlyCompletedTasks(@Param("year") int year);

    @Query("SELECT t FROM Task t WHERE t.dueDate < CURRENT_DATE AND t.status <> 'COMPLETED'")
    List<Task> findOverdueTasks();
}
