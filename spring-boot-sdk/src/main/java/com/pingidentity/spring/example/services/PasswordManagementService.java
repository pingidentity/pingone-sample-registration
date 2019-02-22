package com.pingidentity.spring.example.services;

import com.pingidentity.spring.example.data.PasswordPolicy;
import java.net.URI;
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

  private static final Pattern SPECIAL_REGEX_CHARS = Pattern.compile("[\\{\\}\\(\\)\\[\\]\\.\\+\\*\\?\\^\\$\\\\|-]");
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
    Map<String, String> changePasswordData = new HashMap<>();
    changePasswordData.put("currentPassword", currentPassword);
    changePasswordData.put("newPassword", newPassword);

    return restTemplate.exchange(
        RequestEntity
            .put(URI.create(
                "https://api.pingone.com/v1/environments/" + environmentId + "/users/" + userId + "/password"))
            .header(HttpHeaders.CONTENT_TYPE, "application/vnd.pingidentity.password.reset+json")
            .body(changePasswordData),
        Map.class);
  }

  /**
   * Set a user’s password
   *
   * @param passwordSetLink endpoint to call for this action
   * @param userPassword  specifies the value of the new password assigned to this user.  The password can be either cleartext or pre-encoded. Cleartext passwords are evaluated against the current password policy. Pre-encoded passwords are not evaluated against the password policy, and they are specified by the name of the encoding scheme followed by an encoded representation of the password (for example, {SSHA512}UkGWfORubNKFpFBWh+Lgy4FrciclzUXneuryV+B+zBDR4Gqd5wvMqAvKRixgQWoZlZUgq8Wh40uMK3s6bWpzWt1/TqQH02hX).
   * @param forceChange   specifies whether the user must change the current password on the next login. If forceChange is
   *                      set to true, the status attribute value is changed to MUST_CHANGE_PASSWORD. If forceChange is omitted from the
   *                      request, its value is set to false by default, and the status attribute value is set to OK.
   * @return {@link ResponseEntity} with OK or MUST_CHANGE_PASSWORD status attribute value if succeeded; or error - if failed
   *
   * See <a href="https://apidocs.pingidentity.com/pingone/customer/v1/api/man/p1_Users/p1_Password">Set password API</a>
   */
  public ResponseEntity<Map> setPassword(String passwordSetLink, String userPassword, boolean forceChange) {
    Map<String, Object> setPasswordData = new HashMap<>();
    setPasswordData.put("value", userPassword);
    setPasswordData.put("forceChange", forceChange);

    return clientRestTemplate.exchange(
        RequestEntity
            .put(URI.create(passwordSetLink))
            .header(HttpHeaders.CONTENT_TYPE, "application/vnd.pingidentity.password.set+json")
            .body(setPasswordData),
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
    Map<String, String> resetPasswordData = new HashMap<>();
    resetPasswordData.put("recoveryCode", recoveryCode);
    resetPasswordData.put("newPassword", newPassword);

    return clientRestTemplate.exchange(
        RequestEntity
            .post(URI.create(
                "https://api.pingone.com/v1/environments/" + environmentId + "/users/" + userId + "/password"))
            .header(HttpHeaders.CONTENT_TYPE, "application/vnd.pingidentity.password.recover+json")
            .body(resetPasswordData),
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
    return getPasswordRegex(policy);
  }

  /**
   * Get password validation pattern (regex) based by policy
   *
   * Example: ^(?:(?=(?:.*[ABCDEFGHIZ]){3,4})(?=(?:.*[123456890]){1,4})(?=.*[abcdefghijklmnopqrstuvwxyz])(?=(?:.*[~!@#\$%\^&\*\(\)\-_=\+\[\]\{\}\|;:,\.<>/\?]){1,4}))(?!.*(.)\1{3,}).{6,20}$
   * ^		                      #   start of the password
   * (?:        		            #   non-capturing group to assert the whole password phrase
   * (?=		                    #   lookahead assertion for the following group of characters
   * (?:.*[ABCDEFGHIZ]){3,4})		#   must contains from 3 to 4 uppercase characters
   * .....
   * (?!.*(.)\1{3,})            #   allow up to three repeated characters
   * .		                      #   match anything with previous condition checking
   * {6,20}	                    #   length at least 6 characters and maximum of 20
   * $		                      #   the end of the password
   *
   * See <a href="https://www.rexegg.com/regex-lookarounds.html#password">Mastering Lookahead and Lookbehind</a> for better understanding some parts in this regex
   */
  public String getPasswordRegex(PasswordPolicy passwordPolicy) {
    StringBuilder pattern = new StringBuilder("^(?:");
    // Construct lookahead assertion for each policy "minCharacters" group
    passwordPolicy.getMinCharacters()
        .forEach((str, number) -> pattern.append("(?=(?:.*[")
            // Escape all special for javascript characters
            .append(SPECIAL_REGEX_CHARS.matcher(str).replaceAll("\\\\$0"))
            .append("]){").append(number).append(",})"));

    return pattern.append(")")
        // Set how many consecutive characters are allowed
        .append("(?!.*(.)\\1{" + passwordPolicy.getMaxRepeatedCharacters() + ",})")
        // Set how many characters password should have
        .append(".{").append(passwordPolicy.getLength().getMin()).append(",")
        .append(passwordPolicy.getLength().getMax())
        .append("}$")
        .toString();
  }


}
