package app.Multibank.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id", nullable = false)
    private BankAccount bankAccount;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId; // ID транзакции в банке

    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", length = 3)
    private String currency;

    @Column(name = "description")
    private String description;

    @Column(name = "merchant_name")
    private String merchantName;

    @Column(name = "category")
    private String category;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @Column(name = "booking_date")
    private LocalDateTime bookingDate;

    @Column(name = "transaction_type")
    private String transactionType; // INCOME, EXPENSE, TRANSFER

    @Column(name = "status")
    private String status; // PENDING, COMPLETED, CANCELLED

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Конструкторы
    public Transaction() {
        this.createdAt = LocalDateTime.now();
    }

    public Transaction(BankAccount bankAccount, String transactionId, BigDecimal amount, String description) {
        this();
        this.bankAccount = bankAccount;
        this.transactionId = transactionId;
        this.amount = amount;
        this.description = description;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BankAccount getBankAccount() { return bankAccount; }
    public void setBankAccount(BankAccount bankAccount) { this.bankAccount = bankAccount; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMerchantName() { return merchantName; }
    public void setMerchantName(String merchantName) { this.merchantName = merchantName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }

    public LocalDateTime getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDateTime bookingDate) { this.bookingDate = bookingDate; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}