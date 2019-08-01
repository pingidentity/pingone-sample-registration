package com.pingidentity.spring.example.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.openapitools.jackson.dataformat.hal.HALMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.web.client.RestTemplate;

@Configuration
@Order(1)
public class OAuth2SecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

  @Autowired
  List<HttpMessageConverter<?>> messageConverters;
  @Value("${oauth2.client.clientId}")
  private String oAuth2ClientId;
  @Value("${oauth2.client.clientSecret}")
  private String oAuth2ClientSecret;
  @Value("${oauth2.client.accessTokenUri}")
  private String accessTokenUri;

  @Bean
  public RestTemplate clientRestTemplate() {
    ClientCredentialsResourceDetails resourceDetails = new ClientCredentialsResourceDetails();
    resourceDetails.setId("pingidentity");
    resourceDetails.setClientId(oAuth2ClientId);
    resourceDetails.setClientSecret(oAuth2ClientSecret);
    resourceDetails.setAccessTokenUri(accessTokenUri);
    RestTemplate restTemplate = new OAuth2RestTemplate(resourceDetails, new DefaultOAuth2ClientContext());
    restTemplate.setMessageConverters(messageConverters);
    // Add custom HAL message converter
    restTemplate.getMessageConverters().add(0, customJackson2HttpMessageConverter());

    return restTemplate;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.requestMatchers().antMatchers("/register", "/password/forgot", "/password/recover", "/favicon.ico")
        .and().authorizeRequests().anyRequest().permitAll();
  }


  public MappingJackson2HttpMessageConverter customJackson2HttpMessageConverter() {
    MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
    ObjectMapper objectMapper = new HALMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    jsonConverter.setObjectMapper(objectMapper);
    return jsonConverter;
  }
}
