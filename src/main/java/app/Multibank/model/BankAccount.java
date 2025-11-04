package app.Multibank.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bank_accounts")
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_connection_id", nullable = false)
    private BankConnection bankConnection;

    @Column(name = "account_id", nullable = false)
    private String accountId; // ID счета в банке

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "account_name")
    private String accountName;

    @Column(name = "account_type")
    private String accountType; // DEBIT, CREDIT, SAVINGS, etc.

    @Column(name = "currency", length = 3)
    private String currency; // RUB, USD, EUR

    @Column(name = "balance", precision = 15, scale = 2)
    private BigDecimal balance;

    @Column(name = "available_balance", precision = 15, scale = 2)
    private BigDecimal availableBalance;

    @Column(name = "credit_limit", precision = 15, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "is_active")
    private Boolean active;

    @Column(name = "last_sync_at")
    private LocalDateTime lastSyncAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Конструкторы
    public BankAccount() {
        this.createdAt = LocalDateTime.now();
        this.active = true;
    }

    public BankAccount(BankConnection bankConnection, String accountId, String accountName) {
        this();
        this.bankConnection = bankConnection;
        this.accountId = accountId;
        this.accountName = accountName;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BankConnection getBankConnection() { return bankConnection; }
    public void setBankConnection(BankConnection bankConnection) { this.bankConnection = bankConnection; }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public BigDecimal getAvailableBalance() { return availableBalance; }
    public void setAvailableBalance(BigDecimal availableBalance) { this.availableBalance = availableBalance; }

    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getLastSyncAt() { return lastSyncAt; }
    public void setLastSyncAt(LocalDateTime lastSyncAt) { this.lastSyncAt = lastSyncAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isActive() {
        return active != null && active;
    }
}