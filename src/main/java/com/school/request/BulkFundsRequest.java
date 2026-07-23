package com.school.request;

import com.school.entity.FundsType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BulkFundsRequest {
    private Long termId;
    private FundsType fundsType;
    private BigDecimal amount;
}