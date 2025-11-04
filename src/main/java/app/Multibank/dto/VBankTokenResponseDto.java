package app.Multibank.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VBankTokenResponseDto(
    String accessToken,
    String tokenType,
    String clientId,
    int expiresIn
) {}
