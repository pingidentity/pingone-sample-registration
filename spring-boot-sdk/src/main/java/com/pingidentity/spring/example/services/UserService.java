package com.pingidentity.spring.example.services;

import com.pingidentity.spring.example.data.User;
import com.pingidentity.spring.example.data.User.Users;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UserService {

  @Autowired
  RestTemplate clientRestTemplate;

  @Value("${ping.environmentId}")
  private String environmentId;

  public ResponseEntity<User> createUser(User user) {
    return clientRestTemplate.exchange(
        RequestEntity
            .post(URI.create("https://api.pingone.com/v1/environments/" + environmentId + "/users"))
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(user),
        User.class);
  }

  public ResponseEntity<Map> deleteUser(String userId) {
    return clientRestTemplate.exchange(
        RequestEntity
            .delete(URI.create("https://api.pingone.com/v1/environments/" + environmentId + "/users/" + userId))
            .build(),
        Map.class);
  }

  public ResponseEntity<Users> findUserByName(String userName) {
    try {
      return clientRestTemplate.exchange(
          RequestEntity
              .get(URI.create("https://api.pingone.com/v1/environments/" + environmentId + "/users?filter="
                  + URLEncoder.encode("email eq \"" + userName + "\" or username eq \"" + userName + "\"",
                  StandardCharsets.UTF_8.toString())))
              .build(),
          Users.class);
    } catch (UnsupportedEncodingException e) {
      return new ResponseEntity<>(HttpStatus.CONFLICT);
    }
  }
}
