package com.jonasdurau.ceramicmanagement.shared.dto;

import java.math.BigDecimal;

public record EmployeeUsageResponseDTO(
    Long employeeId,
    String employeeName,
    double usageTime,
    BigDecimal employeeCost
) {}
