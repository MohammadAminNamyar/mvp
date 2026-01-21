package com.example.squarespool.mocktpi;

import com.example.squarespool.tpi.dto.DebitRequest;
import com.example.squarespool.tpi.dto.DebitResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mock-tpi/accountservice")
public class MockTpiController {
    private static final Logger log = LoggerFactory.getLogger(MockTpiController.class);
    private final MockTpiService mockTpiService;

    public MockTpiController(MockTpiService mockTpiService) {
        this.mockTpiService = mockTpiService;
    }

    @PostMapping("/debit")
    public ResponseEntity<DebitResponse> debit(@Validated @RequestBody DebitRequest request) {
        log.info("Mock TPI debit request customer={} amount={} currency={} txnId={}", request.getCustomerId(), request.getAmount() != null ? request.getAmount().getValue() : null, request.getAmount() != null ? request.getAmount().getCurrency() : null, request.getThirdPartyTransactionId());
        DebitResponse response = mockTpiService.debit(request);
        HttpStatus status = response.getResponseCode() != null && response.getResponseCode() == 0
                ? HttpStatus.OK
                : HttpStatus.CONFLICT;
        log.info("Mock TPI debit response customer={} code={} message={}", request.getCustomerId(), response.getResponseCode(), response.getResponseMessage());
        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/credit")
    public ResponseEntity<DebitResponse> credit(@Validated @RequestBody DebitRequest request) {
        log.info("Mock TPI credit request customer={} amount={} currency={} txnId={}", request.getCustomerId(), request.getAmount() != null ? request.getAmount().getValue() : null, request.getAmount() != null ? request.getAmount().getCurrency() : null, request.getThirdPartyTransactionId());
        DebitResponse response = mockTpiService.credit(request);
        HttpStatus status = response.getResponseCode() != null && response.getResponseCode() == 0
                ? HttpStatus.OK
                : HttpStatus.CONFLICT;
        log.info("Mock TPI credit response customer={} code={} message={}", request.getCustomerId(), response.getResponseCode(), response.getResponseMessage());
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
