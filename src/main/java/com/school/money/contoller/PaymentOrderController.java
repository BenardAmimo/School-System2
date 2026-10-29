package com.school.money.contoller;

import com.school.money.entity.OrderStatus;
import com.school.money.request.BankFileRequest;
import com.school.money.request.BatchConfirmRequest;
import com.school.money.request.MarkPaidRequest;
import com.school.money.request.SettleRequest;
import com.school.money.response.BankFileResponse;
import com.school.money.response.FeesResponse;
import com.school.money.response.PaymentOrderResponse;
import com.school.money.service.PaymentOrderService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/money/orders")
public class PaymentOrderController {

    private final PaymentOrderService orderService;

    public PaymentOrderController(PaymentOrderService orderService) { this.orderService = orderService; }

    @GetMapping
    public ResponseEntity<List<PaymentOrderResponse>> list(@RequestParam(required = false) OrderStatus status, Authentication auth) {
        return ResponseEntity.ok(orderService.list(status, auth));
    }

    @PostMapping("/{id}/send")
    public ResponseEntity<PaymentOrderResponse> send(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(orderService.sendMpesa(id, auth));
    }

    @PostMapping("/{id}/mark-paid")
    public ResponseEntity<PaymentOrderResponse> markPaid(@PathVariable Long id, @RequestBody MarkPaidRequest request, Authentication auth) {
        return ResponseEntity.ok(orderService.markPaid(id, request, auth));
    }

    @PostMapping("/{id}/settle")
    public ResponseEntity<PaymentOrderResponse> settle(@PathVariable Long id, @RequestBody SettleRequest request, Authentication auth) {
        return ResponseEntity.ok(orderService.settleReview(id, request, auth));
    }

    @PostMapping("/{id}/check-status")
    public ResponseEntity<PaymentOrderResponse> checkStatus(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(orderService.requestStatusCheck(id, auth));
    }

    /** /money/orders/fees?month=2026-10 */
    @GetMapping("/fees")
    public ResponseEntity<FeesResponse> fees(@RequestParam String month, Authentication auth) {
        return ResponseEntity.ok(orderService.fees(month, auth));
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<PaymentOrderResponse> retry(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(orderService.retry(id, auth));
    }

    /** Returns the CSV; the batch id is in the X-Batch-Id header. */
    @PostMapping("/bank-file")
    public ResponseEntity<byte[]> bankFile(@RequestBody BankFileRequest request, Authentication auth) {
        BankFileResponse file = orderService.exportBankFile(request, auth);
        return ResponseEntity.ok()
                .header("X-Batch-Id", file.batchId())
                .header("Access-Control-Expose-Headers", "X-Batch-Id, Content-Disposition")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.filename() + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(file.content());
    }

    @PostMapping("/batches/{batchId}/confirm")
    public ResponseEntity<List<PaymentOrderResponse>> confirm(@PathVariable String batchId, @RequestBody BatchConfirmRequest request, Authentication auth) {
        return ResponseEntity.ok(orderService.confirmBatch(batchId, request, auth));
    }

    @PostMapping("/batches/{batchId}/release")
    public ResponseEntity<List<PaymentOrderResponse>> release(@PathVariable String batchId, Authentication auth) {
        return ResponseEntity.ok(orderService.releaseBatch(batchId, auth));
    }
}