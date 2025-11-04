package app.Multibank.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;
import java.util.Random;

@Service
public class BankingApiService {

    private static final Logger logger = LoggerFactory.getLogger(BankingApiService.class);

    @Value("${bankingapi.auth.url:https://auth.bankingapi.ru/auth/realms/kubernetes/protocol/openid-connect/token}")
    private String authUrl;

    @Value("${bankingapi.base.url:https://api.bankingapi.ru}")
    private String baseUrl;

    @Value("${bankingapi.client.id:test-client}")
    private String clientId;

    @Value("${bankingapi.client.secret:test-secret}")
    private String clientSecret;

    @Value("${bankingapi.mock.enabled:true}") // ВКЛЮЧАЕМ MOCK РЕЖИМ ПО УМОЛЧАНИЮ
    private boolean mockEnabled;

    private final WebClient webClient;
    private final Random random = new Random();
    private String accessToken;
    private long tokenExpiryTime;

    public BankingApiService() {
        this.webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Получение access_token через OAuth2 client_credentials
     */
    private synchronized String getAccessToken() {
        if (mockEnabled) {
            return "mock_access_token_" + System.currentTimeMillis();
        }

        if (accessToken != null && System.currentTimeMillis() < tokenExpiryTime) {
            return accessToken;
        }

        try {
            logger.info("Получение нового access_token для client_id: {}", clientId);

            Map<String, String> authData = new HashMap<>();
            authData.put("grant_type", "client_credentials");
            authData.put("client_id", clientId);
            authData.put("client_secret", clientSecret);

            Map response = webClient.post()
                    .uri(authUrl)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .bodyValue(authData)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("access_token")) {
                this.accessToken = (String) response.get("access_token");
                Integer expiresIn = (Integer) response.get("expires_in");
                this.tokenExpiryTime = System.currentTimeMillis() + (expiresIn * 1000) - 60000;

                logger.info("Access_token успешно получен, срок действия: {} секунд", expiresIn);
                return accessToken;
            }
        } catch (Exception e) {
            logger.warn("Ошибка получения access_token, переключаемся в mock режим: {}", e.getMessage());
            mockEnabled = true;
        }

        return "mock_fallback_token";
    }

    /**
     * Универсальный метод для вызова API
     */
    public Mono<Map> callApi(String endpoint, HttpMethod method, Object requestBody) {
        if (mockEnabled) {
            logger.info("Используем MOCK данные для: {} {}", method, endpoint);
            return Mono.just(createMockResponse(endpoint, method, requestBody));
        }

        String token = getAccessToken();
        if (token == null || token.startsWith("mock")) {
            logger.info("Токен недоступен, используем MOCK данные");
            return Mono.just(createMockResponse(endpoint, method, requestBody));
        }

        String fullUrl = baseUrl + endpoint;

        logger.info("Вызов реального Banking API: {} {}", method, fullUrl);

        return webClient.method(method)
                .uri(fullUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header("X-Client-ID", clientId)
                .header("X-Request-ID", java.util.UUID.randomUUID().toString())
                .bodyValue(requestBody != null ? requestBody : new HashMap<>())
                .retrieve()
                .onStatus(status -> status.isError(), response -> {
                    logger.error("Ошибка API: {} {}", response.statusCode(), endpoint);
                    return Mono.error(new RuntimeException("HTTP error: " + response.statusCode()));
                })
                .bodyToMono(Map.class)
                .doOnSuccess(response -> logger.info("API call successful: {}", endpoint))
                .doOnError(error -> {
                    logger.warn("API call failed, using mock data: {} - {}", endpoint, error.getMessage());
                    // При ошибке возвращаем mock данные
                })
                .onErrorReturn(createMockResponse(endpoint, method, requestBody));
    }

    /**
     * Создание mock ответов для тестирования
     */
    private Map<String, Object> createMockResponse(String endpoint, HttpMethod method, Object requestBody) {
        Map<String, Object> response = new HashMap<>();
        response.put("mock", true);
        response.put("endpoint", endpoint);
        response.put("method", method.toString());
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        response.put("source", "MOCK_DATA");

        if (endpoint.contains("/accounts")) {
            response.put("accounts", createMockAccounts());
        } else if (endpoint.contains("/payment-consents")) {
            response.put("id", "mock_consent_" + System.currentTimeMillis());
            response.put("status", "AUTHORIZED");
            response.put("type", ((Map)requestBody).get("type"));
        } else if (endpoint.contains("/payments")) {
            response.put("id", "mock_payment_" + System.currentTimeMillis());
            response.put("status", "COMPLETED");
            response.put("amount", ((Map)requestBody).get("amount"));
        } else if (endpoint.contains("/transactions")) {
            response.put("transactions", createMockTransactions());
        } else if (endpoint.contains("/cards")) {
            response.put("cards", createMockCards());
        } else if (endpoint.contains("/balance")) {
            response.put("balance", 50000 + random.nextInt(100000));
            response.put("currency", "RUB");
        }

        return response;
    }

    /**
     * Создание mock счетов
     */
    private Object createMockAccounts() {
        Map<String, Object> account1 = new HashMap<>();
        account1.put("id", "mock_acc_001");
        account1.put("number", "40817810099910004321");
        account1.put("name", "Основной счет (MOCK)");
        account1.put("balance", 75000.50);
        account1.put("currency", "RUB");
        account1.put("type", "CURRENT");
        account1.put("status", "ACTIVE");

        Map<String, Object> account2 = new HashMap<>();
        account2.put("id", "mock_acc_002");
        account2.put("number", "40817810099910005678");
        account2.put("name", "Накопительный счет (MOCK)");
        account2.put("balance", 150000.0);
        account2.put("currency", "RUB");
        account2.put("type", "SAVINGS");
        account2.put("status", "ACTIVE");

        Map<String, Object> account3 = new HashMap<>();
        account3.put("id", "mock_acc_003");
        account3.put("number", "40817810099910009012");
        account3.put("name", "Кредитный счет (MOCK)");
        account3.put("balance", -50000.0);
        account3.put("currency", "RUB");
        account3.put("type", "CREDIT");
        account3.put("status", "ACTIVE");

        return new Map[]{account1, account2, account3};
    }

    /**
     * Создание mock транзакций
     */
    private Object createMockTransactions() {
        Map<String, Object> t1 = new HashMap<>();
        t1.put("id", "mock_txn_001");
        t1.put("amount", -1500.00);
        t1.put("currency", "RUB");
        t1.put("description", "Оплата в супермаркете Пятерочка");
        t1.put("merchantName", "Пятерочка");
        t1.put("category", "Продукты");
        t1.put("transactionDate", java.time.LocalDateTime.now().minusDays(1).toString());
        t1.put("transactionType", "EXPENSE");
        t1.put("status", "COMPLETED");

        Map<String, Object> t2 = new HashMap<>();
        t2.put("id", "mock_txn_002");
        t2.put("amount", 50000.00);
        t2.put("currency", "RUB");
        t2.put("description", "Зарплата");
        t2.put("merchantName", "ООО Работодатель");
        t2.put("category", "Зарплата");
        t2.put("transactionDate", java.time.LocalDateTime.now().minusDays(5).toString());
        t2.put("transactionType", "INCOME");
        t2.put("status", "COMPLETED");

        Map<String, Object> t3 = new HashMap<>();
        t3.put("id", "mock_txn_003");
        t3.put("amount", -2500.00);
        t3.put("currency", "RUB");
        t3.put("description", "Кафе Starbucks");
        t3.put("merchantName", "Starbucks");
        t3.put("category", "Развлечения");
        t3.put("transactionDate", java.time.LocalDateTime.now().minusHours(3).toString());
        t3.put("transactionType", "EXPENSE");
        t3.put("status", "COMPLETED");

        return new Map[]{t1, t2, t3};
    }

    /**
     * Создание mock карт
     */
    private Object createMockCards() {
        Map<String, Object> card1 = new HashMap<>();
        card1.put("id", "mock_card_001");
        card1.put("maskedNumber", "553691******1234");
        card1.put("cardHolderName", "IVAN IVANOV");
        card1.put("cardType", "DEBIT");
        card1.put("paymentSystem", "MASTERCARD");
        card1.put("status", "ACTIVE");

        Map<String, Object> card2 = new HashMap<>();
        card2.put("id", "mock_card_002");
        card2.put("maskedNumber", "220138******5678");
        card2.put("cardHolderName", "IVAN IVANOV");
        card2.put("cardType", "CREDIT");
        card2.put("paymentSystem", "MIR");
        card2.put("status", "ACTIVE");

        return new Map[]{card1, card2};
    }

    // Специфические методы для банковских операций (остаются без изменений)

    public Mono<Map> getAccounts() {
        return callApi("/rbs/accounts/v1/accounts", HttpMethod.GET, null);
    }

    public Mono<Map> getAccountDetails(String accountId) {
        return callApi("/rbs/accounts/v1/accounts/" + accountId, HttpMethod.GET, null);
    }

    public Mono<Map> getAccountTransactions(String accountId, String fromDate, String toDate) {
        String endpoint = String.format("/rbs/accounts/v1/accounts/%s/transactions?fromDate=%s&toDate=%s",
                accountId, fromDate, toDate);
        return callApi(endpoint, HttpMethod.GET, null);
    }

    public Mono<Map> createPaymentConsent(Map<String, Object> consentData) {
        return callApi("/rbs/pis/v1/payment-consents", HttpMethod.POST, consentData);
    }

    public Mono<Map> createPayment(Map<String, Object> paymentData) {
        return callApi("/rbs/pis/v1/payments", HttpMethod.POST, paymentData);
    }

    public Mono<Map> getAccountBalance(String accountId) {
        return callApi("/rbs/accounts/v1/accounts/" + accountId + "/balance", HttpMethod.GET, null);
    }

    public Mono<Map> getCards() {
        return callApi("/rbs/cards/v1/cards", HttpMethod.GET, null);
    }

    /**
     * Метод для проверки режима работы
     */
    public boolean isMockEnabled() {
        return mockEnabled;
    }

    /**
     * Метод для переключения режима
     */
    public void setMockEnabled(boolean mockEnabled) {
        this.mockEnabled = mockEnabled;
        logger.info("Mock режим {}!", mockEnabled ? "ВКЛЮЧЕН" : "ВЫКЛЮЧЕН");
    }
}