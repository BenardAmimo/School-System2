package com.school.request;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class FundsRequest {
    private Long studentId;
    private BigDecimal amount;
}
