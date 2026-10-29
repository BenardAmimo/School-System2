package com.school.money.response;
import java.math.BigDecimal;
import java.util.List;
public record AdvanceSummaryResponse(BigDecimal limit, List<AdvanceResponse> advances) {}