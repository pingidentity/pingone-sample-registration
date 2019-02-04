package com.pingidentity.spring.example;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.client.RestTemplate;

public class GeneralSampleApplication {

  @Bean
  RestTemplate restTemplate(OAuth2AuthorizedClientService clientService) {
    return new RestTemplateBuilder()
        .interceptors((ClientHttpRequestInterceptor) (httpRequest, bytes, clientHttpRequestExecution) -> {

          OAuth2AuthenticationToken token = OAuth2AuthenticationToken.class.cast(
              SecurityContextHolder.getContext().getAuthentication()
          );
          // Get the client for the authorized user
          OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(
              token.getAuthorizedClientRegistrationId(),
              token.getName()
          );

          if (client != null) {
            httpRequest.getHeaders()
                .add(HttpHeaders.AUTHORIZATION, "Bearer " + client.getAccessToken().getTokenValue());
          }
          return clientHttpRequestExecution.execute(httpRequest, bytes);
        }).build();
  }

}



