package com.school.payments.model;

import com.school.payments.entity.Status;
import lombok.Data;

@Data
public class MpesaTransactionsResponse {
    private String merchantRequestId;
    private String checkoutRequestId;
    private String responseDescription;
    private Status status;
}
