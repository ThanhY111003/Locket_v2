package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionInfo {
    private UUID id;
    private BigDecimal amount;
    private String categoryName;
    private Integer categoryId;
    private LocalDate transactionDate;
    private String notes;
    private Instant createdAt;
}
