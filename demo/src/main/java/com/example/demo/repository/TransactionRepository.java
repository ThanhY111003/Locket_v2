package com.example.demo.repository;

import com.example.demo.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByPostId(UUID postId);

    @Query("""
            SELECT t FROM Transaction t
            JOIN FETCH t.category
            WHERE t.user.id = :userId
              AND YEAR(t.transactionDate) = :year
              AND MONTH(t.transactionDate) = :month
            ORDER BY t.transactionDate DESC
            """)
    List<Transaction> findByUserIdAndYearAndMonth(
            @Param("userId") UUID userId,
            @Param("year") int year,
            @Param("month") int month
    );

    /** Lấy giao dịch trong khoảng ngày (dùng cho weekly chart) */
    @Query("""
            SELECT t FROM Transaction t
            WHERE t.user.id = :userId
              AND t.transactionDate >= :from
              AND t.transactionDate <= :to
            ORDER BY t.transactionDate ASC
            """)
    List<Transaction> findByUserIdBetweenDates(
            @Param("userId") UUID userId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );
}

