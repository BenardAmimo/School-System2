package com.school.payments.service;

import com.school.entity.Funds;
import com.school.error.MpesaException;
import com.school.payments.MpesaConfig;
import com.school.payments.entity.IdempotencyRecord;
import com.school.payments.entity.MpesaTransactions;
import com.school.payments.entity.Status;
import com.school.payments.model.MpesaTransactionRequest;
import com.school.payments.model.MpesaTransactionsResponse;
import com.school.payments.repository.IdempotencyKeyRepo;
import com.school.payments.repository.MpesaTransactionsRepository;
import com.school.repo.FundsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class MpesaTransactionService implements MpesaTransServe {
    private final MpesaTransactionsRepository transactionsRepository;
    private final IdempotencyKeyRepo idempotencyKeyRepo;
    private final MpesaAuthService authService;
    private final MpesaConfig mpesaConfig;
    private final WebClient mpesaWebclient;
    private final FundsRepository fundsRepository;

    public MpesaTransactionService(MpesaTransactionsRepository transactionsRepository, IdempotencyKeyRepo idempotencyKeyRepo, MpesaAuthService authService, MpesaConfig mpesaConfig, WebClient mpesaWebclient, FundsRepository fundsRepository) {
        this.transactionsRepository = transactionsRepository;
        this.idempotencyKeyRepo = idempotencyKeyRepo;
        this.authService = authService;
        this.mpesaConfig = mpesaConfig;
        this.mpesaWebclient = mpesaWebclient;
        this.fundsRepository = fundsRepository;
    }

    @Override
    public MpesaTransactionsResponse initiateStkPush(MpesaTransactionRequest transactionRequest, String idempotencyKey) {

       Optional<IdempotencyRecord> exists = idempotencyKeyRepo.findById(idempotencyKey);

        if (exists.isPresent()) {
            IdempotencyRecord record = exists.get();
            //Logging
            MpesaTransactions transact = transactionsRepository.findByCheckoutRequestId(record.getCheckoutRequestId())
                    .orElseThrow(() -> new MpesaException("Mpesa transaction Not found for idempotency key "));
            return toDto(transact,"Duplicate Request");

        }

        Funds funds = fundsRepository.findById(transactionRequest.getFundsId())
                .orElseThrow(()->new MpesaException("Funds are not yet into the account"));



        String token = authService.generateAccessToken();
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String passCode = Base64.getEncoder()
                .encodeToString((mpesaConfig.getShortCode() +
                        mpesaConfig.getPasskey() + timestamp).getBytes());

        log.info("Token being used for STK push: [{}]", token);

        Map<String,Object> body = new LinkedHashMap<>();
        body.put("BusinessShortCode",mpesaConfig.getShortCode());
        body.put("Password",passCode);
        body.put("Timestamp",timestamp);
        body.put("TransactionType","CustomerPayBillOnline");
        body.put("Amount",transactionRequest.getAmount());
        body.put("PartyA",transactionRequest.getPhoneNumber());
        body.put("PartyB",mpesaConfig.getShortCode());
        body.put("PhoneNumber",transactionRequest.getPhoneNumber());
        body.put("CallBackURL",mpesaConfig.getCallbackUrl());
        body.put("AccountReference",transactionRequest.getAccountRef());
        body.put("TransactionDesc",transactionRequest.getTransactionDescription());

        log.info("STK push request body: {}", body);

        Map<String,Object> response = mpesaWebclient
                .post()
                .uri(mpesaConfig.getStkPushUrl())
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError,clientResponse -> clientResponse
                        .bodyToMono(String.class)
                        .flatMap(error->{
                            log.error("Mpesa STK push HTTP error: {}", error);
                            return Mono.error(new MpesaException("Mpesa rejected the STK push request"));
                        }))
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block(Duration.ofSeconds(15));

        if(response == null){
            throw new MpesaException("No response from Safaricom");
        }
        String responseCode =String.valueOf(response.get("ResponseCode"));

        if (!"0".equals(responseCode)) {
            log.warn("STK push rejected: {}", response.get("ResponseDescription"));
            throw new MpesaException("STK push rejected: " + response.get("ResponseDescription"));
        }

        String checkoutRequestId = (String) response.get("CheckoutRequestID");

        MpesaTransactions mpesaTransactions = new MpesaTransactions();

        mpesaTransactions.setCheckoutRequestId(checkoutRequestId);
        mpesaTransactions.setAmount(transactionRequest.getAmount());
        mpesaTransactions.setFunds(funds);
        mpesaTransactions.setAccountReference(transactionRequest.getAccountRef());
        mpesaTransactions.setStatus(Status.PENDING);
        mpesaTransactions.setMerchantRequestId((String) response.get("MerchantRequestID"));
        mpesaTransactions.setCreatedAt(LocalDateTime.now());
        mpesaTransactions.setPhoneNumber(transactionRequest.getPhoneNumber());

        transactionsRepository.save(mpesaTransactions);

        IdempotencyRecord recordings = new IdempotencyRecord();

        recordings.setIdempotencyKey(idempotencyKey);
        recordings.setStatus(Status.PENDING);
        recordings.setCheckoutRequestId(checkoutRequestId);
        recordings.setCreatedAt(LocalDateTime.now());
        idempotencyKeyRepo.save(recordings);

        return toDto(mpesaTransactions, (String) response.get("ResponseDescription"));
    }

    public MpesaTransactionsResponse toDto(MpesaTransactions transaction, String description) {
        MpesaTransactionsResponse transact = new MpesaTransactionsResponse();
        transact.setCheckoutRequestId(transaction.getCheckoutRequestId());
        transact.setMerchantRequestId(transaction.getMerchantRequestId());
        transact.setResponseDescription(description);
        transact.setStatus(transaction.getStatus());

        return transact;
    }


}

