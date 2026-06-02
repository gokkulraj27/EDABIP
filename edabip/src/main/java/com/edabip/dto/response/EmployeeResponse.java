package com.edabip.dto.response;

import com.edabip.entity.enums.EmployeeStatus;
import com.edabip.entity.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private Gender gender;
    private String designation;
    private Long departmentId;
    private String departmentName;
    private BigDecimal salary;
    private LocalDate joiningDate;
    private EmployeeStatus status;
    private LocalDateTime createdAt;
}
