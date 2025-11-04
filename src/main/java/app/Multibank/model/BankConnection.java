package app.Multibank.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bank_connections")
public class BankConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "bank_id", nullable = false)
    private String bankId;

    @Column(name = "access_token", length = 2000)
    private String accessToken;

    @Column(name = "refresh_token", length = 2000)
    private String refreshToken;

    @Column(name = "token_expiry")
    private LocalDateTime tokenExpiry;

    @Column(name = "scope")
    private String scope;

    @Column(name = "account_id")
    private String accountId;

    @Column(name = "connected_at")
    private LocalDateTime connectedAt;

    @Column(name = "last_sync_at") // ДОБАВЛЕНО НОВОЕ ПОЛЕ
    private LocalDateTime lastSyncAt;

    @Column(name = "is_active")
    private Boolean active;

    // Конструкторы
    public BankConnection() {
        this.connectedAt = LocalDateTime.now();
        this.active = true;
    }

    public BankConnection(User user, String bankName, String bankId) {
        this();
        this.user = user;
        this.bankName = bankName;
        this.bankId = bankId;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public LocalDateTime getTokenExpiry() {
        return tokenExpiry;
    }

    public void setTokenExpiry(LocalDateTime tokenExpiry) {
        this.tokenExpiry = tokenExpiry;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public LocalDateTime getConnectedAt() {
        return connectedAt;
    }

    public void setConnectedAt(LocalDateTime connectedAt) {
        this.connectedAt = connectedAt;
    }

    // ДОБАВЛЕНЫ ГЕТТЕР И СЕТТЕР ДЛЯ lastSyncAt
    public LocalDateTime getLastSyncAt() {
        return lastSyncAt;
    }

    public void setLastSyncAt(LocalDateTime lastSyncAt) {
        this.lastSyncAt = lastSyncAt;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active != null && active;
    }
}