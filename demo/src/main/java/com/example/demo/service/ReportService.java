package com.example.demo.service;

import com.example.demo.dto.response.MonthlyReportResponse;
import com.example.demo.dto.response.TransactionInfo;
import com.example.demo.entity.Transaction;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public MonthlyReportResponse getMonthlyReport(String username, int year, int month) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("User not found", HttpStatus.NOT_FOUND));

        List<Transaction> transactions = transactionRepository
                .findByUserIdAndYearAndMonth(user.getId(), year, month);

        BigDecimal totalSpending = transactions.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> spendingByCategory = transactions.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCategory() != null ? t.getCategory().getName() : "Khác",
                        LinkedHashMap::new,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));

        List<TransactionInfo> transactionDtos = transactions.stream()
                .map(t -> TransactionInfo.builder()
                        .id(t.getId())
                        .amount(t.getAmount())
                        .categoryId(t.getCategory() != null ? t.getCategory().getId() : null)
                        .categoryName(t.getCategory() != null ? t.getCategory().getName() : "Khác")
                        .transactionDate(t.getTransactionDate())
                        .notes(t.getNotes())
                        .createdAt(t.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        // Tính weekly data (7 ngày gần nhất)
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);
        List<Transaction> weeklyTxs = transactionRepository
                .findByUserIdBetweenDates(user.getId(), weekStart, today);

        List<Map<String, Object>> weeklyData = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            String dayLabel = getDayLabel(day.getDayOfWeek());
            BigDecimal dayTotal = weeklyTxs.stream()
                    .filter(t -> t.getTransactionDate().equals(day))
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            weeklyData.add(Map.of("day", dayLabel, "amount", dayTotal));
        }

        return MonthlyReportResponse.builder()
                .year(year)
                .month(month)
                .totalSpending(totalSpending)
                .spendingByCategory(spendingByCategory)
                .transactions(transactionDtos)
                .weeklyData(weeklyData)
                .build();
    }

    private String getDayLabel(DayOfWeek dow) {
        return switch (dow) {
            case MONDAY -> "T2";
            case TUESDAY -> "T3";
            case WEDNESDAY -> "T4";
            case THURSDAY -> "T5";
            case FRIDAY -> "T6";
            case SATURDAY -> "T7";
            case SUNDAY -> "CN";
        };
    }
}
