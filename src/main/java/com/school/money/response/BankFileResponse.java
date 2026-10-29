package com.school.money.response;
import java.nio.charset.StandardCharsets;
public record BankFileResponse(String batchId, String filename, byte[] content) {
    public static BankFileResponse of(String batchId, String csv) {
        return new BankFileResponse(batchId, batchId + ".csv", csv.getBytes(StandardCharsets.UTF_8));
    }
}