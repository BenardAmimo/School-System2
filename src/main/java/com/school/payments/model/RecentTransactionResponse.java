package com.school.payments.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecentTransactionResponse(String studentName,
                                        BigDecimal amount,
                                        String status,
                                        LocalDateTime date) {
}
