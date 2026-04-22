package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MonthlyReportResponse {
    private int year;
    private int month;
    private BigDecimal totalSpending;
    /** Tổng chi tiêu theo từng danh mục: { "Ăn uống": 800000, "Di chuyển": 400000 } */
    private Map<String, BigDecimal> spendingByCategory;
    /** Danh sách chi tiết tất cả giao dịch trong tháng */
    private List<TransactionInfo> transactions;
    /** Chi tiêu 7 ngày gần nhất: [{ "day": "T2", "amount": 150000 }, ...] */
    private List<Map<String, Object>> weeklyData;
}
