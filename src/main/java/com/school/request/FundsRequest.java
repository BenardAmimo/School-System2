package com.school.request;

import com.school.entity.FundsType;


import java.math.BigDecimal;
public record FundsRequest(
        Long studentId,
        BigDecimal amount,
        Long termId,
        FundsType fundsType
) {

}