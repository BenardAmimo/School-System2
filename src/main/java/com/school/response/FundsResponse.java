package com.school.response;

import com.school.entity.FundsType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@Builder
public class FundsResponse {
    private Long fundsId;
    private FundsType fundsType;
    private BigDecimal amountDue;
    private BigDecimal amountPaid;
    private BigDecimal balance;
    private LocalDateTime createdAt;
}
