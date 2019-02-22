package com.pingidentity.spring.example.services;

import com.pingidentity.spring.example.data.Population;
import java.net.URI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PopulationService {

  @Autowired
  RestTemplate clientRestTemplate;

  @Value("${ping.environmentId}")
  private String environmentId;

  public Population.Populations getAllPopulations() {
    RequestEntity<Void> requestEntity = RequestEntity
        .get(URI.create("https://api.pingone.com/v1/environments/" + environmentId + "/populations"))
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build();

    return clientRestTemplate.exchange(requestEntity, Population.Populations.class).getBody();
  }
}
