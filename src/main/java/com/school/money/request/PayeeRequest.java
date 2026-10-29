package com.school.money.request;

import com.school.money.entity.Channel;
import com.school.money.entity.PayeeType;

public record PayeeRequest(PayeeType payeeType, String payeeRef, String displayName, Channel channel, String phone,
                           String businessNumber, String accountReference, String bankName,
                           String bankAccountNumber, String bankAccountName) {}