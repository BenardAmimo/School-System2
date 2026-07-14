package com.school.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class FundsResponse {
    private Long fundsId;
    private String studentsName;
    private LocalDateTime createdAt;
    private BigDecimal amount;
}
