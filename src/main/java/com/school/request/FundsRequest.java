package com.school.request;

import com.school.entity.FundsType;

import java.math.BigDecimal;

public record FundsRequest(Long classesId, Long termId, FundsType fundsType, BigDecimal amount) {

}
