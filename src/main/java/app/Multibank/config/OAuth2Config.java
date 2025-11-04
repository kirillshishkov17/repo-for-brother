package app.Multibank.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

@Configuration
public class OAuth2Config {

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(
                abankClientRegistration(),
                vbankClientRegistration(),
                sbankClientRegistration()
        );
    }

    private ClientRegistration abankClientRegistration() {
        return ClientRegistration.withRegistrationId("abank")
                .clientId("multibank-client-id")
                .clientSecret("multibank-client-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("accounts", "payments", "openid")
                .authorizationUri("https://abank.open.bankingapi.ru/oauth/authorize")
                .tokenUri("https://abank.open.bankingapi.ru/oauth/token")
                .userInfoUri("https://abank.open.bankingapi.ru/openbanking/userinfo")
                .userNameAttributeName("sub")
                .clientName("ABank")
                .build();
    }

    private ClientRegistration vbankClientRegistration() {
        return ClientRegistration.withRegistrationId("vbank")
                .clientId("multibank-client-id")
                .clientSecret("multibank-client-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("accounts", "payments", "openid")
                .authorizationUri("https://vbank.open.bankingapi.ru/oauth/authorize")
                .tokenUri("https://vbank.open.bankingapi.ru/oauth/token")
                .userInfoUri("https://vbank.open.bankingapi.ru/openbanking/userinfo")
                .userNameAttributeName("sub")
                .clientName("VBank")
                .build();
    }

    private ClientRegistration sbankClientRegistration() {
        return ClientRegistration.withRegistrationId("sbank")
                .clientId("multibank-client-id")
                .clientSecret("multibank-client-secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("accounts", "payments", "openid")
                .authorizationUri("https://sbank.open.bankingapi.ru/oauth/authorize")
                .tokenUri("https://sbank.open.bankingapi.ru/oauth/token")
                .userInfoUri("https://sbank.open.bankingapi.ru/openbanking/userinfo")
                .userNameAttributeName("sub")
                .clientName("SBank")
                .build();
    }
}