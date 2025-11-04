package app.Multibank.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VBankTokenResponseDto(
    String access_token,
    String token_type,
    String client_id,
    int expires_in
) {}
