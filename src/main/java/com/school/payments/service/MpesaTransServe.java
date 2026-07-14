package com.school.payments.service;

import com.school.payments.model.MpesaTransactionRequest;
import com.school.payments.model.MpesaTransactionsResponse;

public interface MpesaTransServe {

    MpesaTransactionsResponse initiateStkPush(MpesaTransactionRequest transactionRequest, String idempotencyKey);
}
