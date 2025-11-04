package app.Multibank.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class RealBankApiService {

    @Value("${bankingapi.auth.url:https://auth.bankingapi.ru/auth/realms/kubernetes/protocol/openid-connect/token}")
    private String authUrl;

    @Value("${bankingapi.base.url:https://api.bankingapi.ru}")
    private String baseUrl;

    @Value("${bankingapi.client.id}")
    private String clientId;

    @Value("${bankingapi.client.secret}")
    private String clientSecret;

    private final WebClient webClient;
    private final RestTemplate restTemplate;
    private String accessToken;
    private long tokenExpiryTime;

    public RealBankApiService() {
        this.webClient = WebClient.builder().build();
        this.restTemplate = new RestTemplate();
    }

    /**
     * Получение access_token через client_credentials
     */
    public String getAccessToken() {
        if (accessToken != null && System.currentTimeMillis() < tokenExpiryTime) {
            return accessToken;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            String requestBody = "grant_type=client_credentials" +
                    "&client_id=" + clientId +
                    "&client_secret=" + clientSecret;

            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(authUrl, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                this.accessToken = (String) response.getBody().get("access_token");
                Integer expiresIn = (Integer) response.getBody().get("expires_in");
                this.tokenExpiryTime = System.currentTimeMillis() + (expiresIn * 1000) - 60000; // минус 1 минута для запаса
                return accessToken;
            }
        } catch (Exception e) {
            System.err.println("Ошибка получения access_token: " + e.getMessage());
        }

        return null;
    }

    /**
     * Базовый метод для вызова API
     */
    private Mono<Map> callBankingApi(String endpoint, HttpMethod method, Object body) {
        String token = getAccessToken();
        if (token == null) {
            return Mono.error(new RuntimeException("Не удалось получить access token"));
        }

        return webClient.method(method)
                .uri(baseUrl + endpoint)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(body != null ? body : "")
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorResume(e -> {
                    System.err.println("Ошибка вызова API " + endpoint + ": " + e.getMessage());
                    return Mono.empty();
                });
    }

    /**
     * Получение счетов через реальное API
     */
    public Mono<Map> getRealAccounts(String bankId) {
        // Здесь будет реальный endpoint для получения счетов
        String endpoint = "/api/" + bankId + "/accounts";
        return callBankingApi(endpoint, HttpMethod.GET, null);
    }

    /**
     * Создание платежного согласия через реальное API
     */
    public Mono<Map> createRealPaymentConsent(String bankId, String type, Map<String, Object> consentData) {
        String endpoint = "/api/" + bankId + "/payment-consents";
        return callBankingApi(endpoint, HttpMethod.POST, consentData);
    }

    /**
     * Создание платежа через реальное API
     */
    public Mono<Map> createRealPayment(String bankId, Map<String, Object> paymentData) {
        String endpoint = "/api/" + bankId + "/payments";
        return callBankingApi(endpoint, HttpMethod.POST, paymentData);
    }
}