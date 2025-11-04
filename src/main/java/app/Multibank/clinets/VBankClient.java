package app.Multibank.clinets;

import app.Multibank.controller.AuthController;
import app.Multibank.dto.VBankTokenResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class VBankClient {
    private final CloseableHttpClient client;
    private final ObjectMapper mapper;
    private final String baseUrl;
    private final String clientId;
    private final String clientSecret;
    private static final Logger log = LoggerFactory.getLogger(VBankClient.class);

    public VBankClient(
        CloseableHttpClient client, ObjectMapper mapper,
        @Value("${vbank.clientSecret}")
        String clientSecret,
        @Value("${vbank.baseUrl}")
        String baseUrl,
        @Value("${vbank.clientId}")
        String clientId
    ) {
        this.client = client;
        this.mapper = mapper;
        this.clientSecret = clientSecret;
        this.baseUrl = baseUrl;
        this.clientId = clientId;
    }

    public String getVBankAccessToken() {
        HttpPost request = new HttpPost(baseUrl + "/auth/bank-token?client_id=" + clientId + "&client_secret=" + clientSecret);
        try (CloseableHttpResponse response = client.execute(request)) {
            String json = new String(response.getEntity().getContent().readAllBytes());
            return mapper.readValue(json, VBankTokenResponseDto.class).accessToken();
        } catch (IOException e) {
            log.warn("request to VBank API failed");
            //todo обработать исключение. Пока что возвращаю пустую строку.
            return "";
        }
    }
}
