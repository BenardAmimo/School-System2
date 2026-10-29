package com.school.money.request;
import java.math.BigDecimal;
public record ReceiptRequest(String invoiceNumber, BigDecimal invoiceAmount) {}