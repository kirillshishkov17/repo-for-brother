package app.Multibank.repository;

import app.Multibank.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByBankAccountIdOrderByTransactionDateDesc(Long bankAccountId);
    Optional<Transaction> findByBankAccountIdAndTransactionId(Long bankAccountId, String transactionId);

    @Query("SELECT t FROM Transaction t WHERE t.bankAccount.bankConnection.user.id = :userId ORDER BY t.transactionDate DESC")
    List<Transaction> findByUserId(@Param("userId") Long userId);

    @Query("SELECT t FROM Transaction t WHERE t.bankAccount.bankConnection.user.id = :userId AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<Transaction> findByUserIdAndDateRange(@Param("userId") Long userId,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.bankAccount.bankConnection.user.id = :userId AND t.transactionType = 'EXPENSE' AND t.transactionDate BETWEEN :startDate AND :endDate")
    Double getTotalExpensesByUserIdAndDateRange(@Param("userId") Long userId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.bankAccount.bankConnection.user.id = :userId AND t.transactionType = 'INCOME' AND t.transactionDate BETWEEN :startDate AND :endDate")
    Double getTotalIncomeByUserIdAndDateRange(@Param("userId") Long userId,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);
}