package com.school.money.contoller;

import com.school.money.MoneyProperties;
import com.school.money.service.PaymentOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

/**
 * Safaricom posts the outcome here. Must be permitAll in the security config:
 *   .requestMatchers("/money/mpesa/**").permitAll()
 * The secret in the path (app.money.b2b.callback-secret) is the protection: a wrong secret gets a quiet 200 and nothing happens.
 */
@RestController
@RequestMapping("/money/mpesa")
public class MpesaCallbackController {

    private static final Logger log = LoggerFactory.getLogger(MpesaCallbackController.class);
    private static final Map<String, Object> ACK = Map.of("ResultCode", 0, "ResultDesc", "Accepted");

    private final PaymentOrderService ordersService;
    private final MoneyProperties props;

    public MpesaCallbackController(PaymentOrderService orders, MoneyProperties props) {
        this.ordersService = orders; this.props = props;
    }

    @PostMapping("/result/{secret}/{orderId}")
    public Map<String, Object> result(@PathVariable String secret, @PathVariable Long orderId, @RequestBody Map<String, Object> body) {
        if (!secretOk(secret)) { log.warn("M-Pesa result with wrong secret for order {}", orderId); return ACK; }
        try {
            Map<String, Object> r = map(body.get("Result"));
            int code = Integer.parseInt(String.valueOf(r.get("ResultCode")));
            String desc = String.valueOf(r.get("ResultDesc"));
            ordersService.applyMpesaResult(orderId, code, desc, receipt(r));
        } catch (Exception e) {
            // Never let Safaricom see an error and retry in a loop; the stuck job and the review screen catch anything lost.
            log.error("Could not process M-Pesa result for order {}", orderId, e);
        }
        return ACK;
    }

    @PostMapping("/timeout/{secret}/{orderId}")
    public Map<String, Object> timeout(@PathVariable String secret, @PathVariable Long orderId) {
        if (secretOk(secret)) {
            try { ordersService.applyMpesaTimeout(orderId); } catch (Exception e) { log.error("Timeout handling failed for {}", orderId, e); }
        }
        return ACK;
    }

    /** Answer to "what happened to this payment?". CHECK key names against the Transaction Status documentation. */
    @PostMapping("/status-result/{secret}/{orderId}")
    public Map<String, Object> statusResult(@PathVariable String secret, @PathVariable Long orderId, @RequestBody Map<String, Object> body) {
        if (!secretOk(secret)) return ACK;
        try {
            Map<String, Object> r = map(body.get("Result"));
            int code = Integer.parseInt(String.valueOf(r.get("ResultCode")));
            String status = param(r, "TransactionStatus");
            String receipt = param(r, "ReceiptNo");
            if (receipt == null) receipt = receipt(r);
            boolean completed = code == 0 && status != null && status.equalsIgnoreCase("Completed");
            ordersService.applyStatusResult(orderId, completed, receipt, "code " + code + ", status " + status + ", " + r.get("ResultDesc"));
        } catch (Exception e) {
            log.error("Could not process status result for order {}", orderId, e);
        }
        return ACK;
    }

    @PostMapping("/timeout-status/{secret}/{orderId}")
    public Map<String, Object> statusTimeout(@PathVariable String secret, @PathVariable Long orderId) {
        return ACK;   // nothing to do: the next scheduled check or a person will follow up
    }

    private static String param(Map<String, Object> r, String key) {
        Object params = map(r.get("ResultParameters")).get("ResultParameter");
        if (params instanceof List<?> list) {
            for (Object item : list) {
                Map<String, Object> p = map(item);
                if (key.equals(String.valueOf(p.get("Key"))) && p.get("Value") != null) return String.valueOf(p.get("Value"));
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object o) { return o instanceof Map<?, ?> m ? (Map<String, Object>) m : Map.of(); }

    /** B2C puts TransactionReceipt in ResultParameters; B2B uses TransactionID. */
    private static String receipt(Map<String, Object> r) {
        Object params = map(r.get("ResultParameters")).get("ResultParameter");
        if (params instanceof List<?> list) {
            for (Object item : list) {
                Map<String, Object> p = map(item);
                String k = String.valueOf(p.get("Key"));
                if ("TransactionReceipt".equals(k) || "TransactionID".equals(k)) return String.valueOf(p.get("Value"));
            }
        }
        Object id = r.get("TransactionID");
        return id == null ? null : id.toString();
    }

    private boolean secretOk(String given) {
        String expected = props.getB2b().getCallbackSecret();
        if (expected == null || expected.length() < 24) return false;
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), given.getBytes(StandardCharsets.UTF_8));
    }
}
