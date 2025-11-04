package app.Multibank.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bank_cards")
public class BankCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id", nullable = false)
    private BankAccount bankAccount;

    @Column(name = "card_id", nullable = false)
    private String cardId; // ID карты в банке

    @Column(name = "masked_card_number")
    private String maskedCardNumber; // ****1234

    @Column(name = "card_holder_name")
    private String cardHolderName;

    @Column(name = "card_type")
    private String cardType; // VISA, MASTERCARD, MIR

    @Column(name = "payment_system")
    private String paymentSystem; // VISA, MASTERCARD, MIR

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "is_active")
    private Boolean active;

    @Column(name = "is_virtual")
    private Boolean virtual;

    @Column(name = "last_sync_at")
    private LocalDateTime lastSyncAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Конструкторы
    public BankCard() {
        this.createdAt = LocalDateTime.now();
        this.active = true;
    }

    public BankCard(BankAccount bankAccount, String cardId, String maskedCardNumber) {
        this();
        this.bankAccount = bankAccount;
        this.cardId = cardId;
        this.maskedCardNumber = maskedCardNumber;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BankAccount getBankAccount() { return bankAccount; }
    public void setBankAccount(BankAccount bankAccount) { this.bankAccount = bankAccount; }

    public String getCardId() { return cardId; }
    public void setCardId(String cardId) { this.cardId = cardId; }

    public String getMaskedCardNumber() { return maskedCardNumber; }
    public void setMaskedCardNumber(String maskedCardNumber) { this.maskedCardNumber = maskedCardNumber; }

    public String getCardHolderName() { return cardHolderName; }
    public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }

    public String getCardType() { return cardType; }
    public void setCardType(String cardType) { this.cardType = cardType; }

    public String getPaymentSystem() { return paymentSystem; }
    public void setPaymentSystem(String paymentSystem) { this.paymentSystem = paymentSystem; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Boolean getVirtual() { return virtual; }
    public void setVirtual(Boolean virtual) { this.virtual = virtual; }

    public LocalDateTime getLastSyncAt() { return lastSyncAt; }
    public void setLastSyncAt(LocalDateTime lastSyncAt) { this.lastSyncAt = lastSyncAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isActive() {
        return active != null && active;
    }

    public boolean isVirtual() {
        return virtual != null && virtual;
    }
}