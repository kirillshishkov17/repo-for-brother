package app.Multibank.clinets;

import app.Multibank.dto.VBankTokenResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class VBankClient {
    private final CloseableHttpClient client;
    private final ObjectMapper mapper;
    @Value("${vbank.baseUrl}")
    private String baseUrl;
    @Value("${vbank.clientId}")
    private String clientId;
    @Value("${vbank.clientSecret}")
    private String clientSecret;

    public VBankClient(CloseableHttpClient client, ObjectMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    public String getVBankAccessToken() throws IOException {
        HttpPost request = new HttpPost(baseUrl + "/auth/bank-token?client_id=" + clientId + "&client_secret=" + clientSecret);
        try (CloseableHttpResponse response = client.execute(request)) {
            String json = new String(response.getEntity().getContent().readAllBytes());
            return mapper.readValue(json, VBankTokenResponseDto.class).accessToken();
        }
    }
}
