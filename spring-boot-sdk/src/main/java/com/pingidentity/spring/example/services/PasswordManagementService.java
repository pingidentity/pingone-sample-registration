package com.pingidentity.spring.example.services;

import com.pingidentity.spring.example.data.PasswordPolicy;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * User passwords and password policies management service
 * <p>
 * See <a href="https://apidocs.pingidentity.com/pingone/customer/v1/api/man/p1_Users/p1_Password">User password
 * management API</a> and
 * <a href="https://apidocs.pingidentity.com/pingone/customer/v1/api/man/p1_Passwords">Password policies API</a> and
 */
@Service
public class PasswordManagementService {

  private static final Pattern SPECIAL_REGEX_CHARS = Pattern.compile("[{}()\\[\\].+*?^$\\\\|]");
  @Autowired
  RestTemplate restTemplate;
  @Autowired
  RestTemplate clientRestTemplate;
  @Value("${ping.environmentId}")
  private String environmentId;

  /**
   * Self-change reset of the password identified by the user ID and environment ID.
   */
  public ResponseEntity<Map> changePassword(String userId, String currentPassword, String newPassword) {
    Map<String, String> map = new HashMap<>();
    map.put("currentPassword", SPECIAL_REGEX_CHARS.matcher(currentPassword).replaceAll("\\\\$0"));
    map.put("newPassword", SPECIAL_REGEX_CHARS.matcher(newPassword).replaceAll("\\\\$0"));

    return restTemplate.exchange(
        RequestEntity
            .put(URI.create(
                "https://api.pingone.com/v1/environments/" + environmentId + "/users/" + userId + "/password"))
            .header(HttpHeaders.CONTENT_TYPE, "application/vnd.pingidentity.password.reset+json")
            .body(map),
        Map.class);
  }

  /**
   * Set a user’s password
   */
  public ResponseEntity<Map> setPassword(String passwordSetLink, String userPassword) {
    String escapedPass = SPECIAL_REGEX_CHARS.matcher(userPassword).replaceAll("\\\\$0");
    return clientRestTemplate.exchange(
        RequestEntity
            .put(URI.create(passwordSetLink))
            .header(HttpHeaders.CONTENT_TYPE, "application/vnd.pingidentity.password.set+json")
            .body(Collections.singletonMap("value", escapedPass)),
        Map.class);
  }

  /**
   * Recover a forgotten password
   * <p>
   * Send a one-time-password (OTP) that is used to reset the password. The OTP is a randomly generated eight-character
   * alphanumeric string sent to the user’s email address, and the code is valid for five minutes.
   * <p>
   * Note: This operation cannot be used if the user is disabled or if the password status is NO_PASSWORD or
   * PASSWORD_LOCKED_OUT.
   */
  public ResponseEntity<Map> recoverPassword(String userId) {

    return clientRestTemplate.exchange(
        RequestEntity
            .post(URI.create(
                "https://api.pingone.com/v1/environments/" + environmentId + "/users/" + userId + "/password"))
            .header(HttpHeaders.CONTENT_TYPE, "application/vnd.pingidentity.password.sendRecoveryCode+json")
            .build(),
        Map.class);
  }

  /**
   * Reset the locked-out password using a recovery code.
   *
   * @param userId user identifier
   * @param recoveryCode recovery code
   * @param newPassword new reset password
   */
  public ResponseEntity<Map> resetPassword(String userId, String recoveryCode, String newPassword) {
    Map<String, String> map = new HashMap<>();
    map.put("recoveryCode", recoveryCode);
    map.put("newPassword", newPassword);

    return clientRestTemplate.exchange(
        RequestEntity
            .post(URI.create(
                "https://api.pingone.com/v1/environments/" + environmentId + "/users/" + userId + "/password"))
            .header(HttpHeaders.CONTENT_TYPE, "application/vnd.pingidentity.password.recover+json")
            .body(map),
        Map.class);
  }

  /**
   * Get a list of password policies of the specified environment
   */
  public PasswordPolicy.PasswordPolicies getPasswordPolicies() {
    return clientRestTemplate.exchange(
        RequestEntity
            .get(URI.create("https://api.pingone.com/v1/environments/" + environmentId + "/passwordPolicies"))
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build(),
        PasswordPolicy.PasswordPolicies.class)
        .getBody();

  }

  /**
   * Get password validation pattern from default password policy
   *
   * @return password validation pattern in a regex format
   */
  public String getPasswordPattern() {
    PasswordPolicy.PasswordPolicies passwordPolicies = getPasswordPolicies();
    PasswordPolicy policy = passwordPolicies.getPasswordPolicies().stream()
        .filter(passwordPolicy -> passwordPolicy.isDefault())
        .findFirst().get();
    return getPasswordPattern(policy);
  }

  /**
   * Get password validation pattern based by policy Example: ((?=.*\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{6,20})\1{3}
   * (?=.*[0-9])		#   must contains at least one digit from 0-9 (?=.*[a-z])		#   must contains at least one lowercase
   * characters (?=.*[A-Z])		#   must contains at least one uppercase characters (?=.*[@#$%])		#   must contains at
   * least one special symbols in the list "@#$%" .		        #   match anything with previous condition checking {6,20}
   *        #   length at least 6 characters and maximum of 20 \1{3}            #   allow up to three repeated
   * characters
   */
  public String getPasswordPattern(PasswordPolicy passwordPolicy) {
    StringBuilder pattern = new StringBuilder("((");
    passwordPolicy.getMinCharacters()
        .forEach((str, number) -> pattern.append("(?=.*[")
            .append(SPECIAL_REGEX_CHARS.matcher(str).replaceAll("\\\\$0")).append("]{").append(number).append(",})"));
    return pattern.append(".{").append(passwordPolicy.getLength().getMin()).append(",")
        .append(passwordPolicy.getLength().getMax())
        .append("})")
        .append("\\1{" + passwordPolicy.getMaxRepeatedCharacters() + "})")
        .toString();
  }

}
