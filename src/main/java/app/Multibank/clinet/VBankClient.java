package app.Multibank.clinet;

import app.Multibank.dto.VBankTokenResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import java.io.IOException;

public class VBankClient {
    private static final CloseableHttpClient client = HttpClients.createDefault();
    private static final ObjectMapper mapper = new ObjectMapper();

    //todo доставать эти переменные из конфига
    private final String baseUrl = "https://vbank.open.bankingapi.ru/auth/bank-token";
    private final String client_id = "team020";
    private final String client_secret = "PfA7bDk1k14ppzJO9j7ZCTyNEIgOlF45";
    private final String url = String.format("%s?client_id=%s&client_secret=%s", baseUrl, client_id, client_secret);

    public String getVBankAccessToken() throws IOException {
        HttpPost request = new HttpPost(url);
        try (CloseableHttpResponse response = client.execute(request)) {
            String json = new String(response.getEntity().getContent().readAllBytes());
            return mapper.readValue(json, VBankTokenResponseDto.class).access_token();
        }
    }
}
