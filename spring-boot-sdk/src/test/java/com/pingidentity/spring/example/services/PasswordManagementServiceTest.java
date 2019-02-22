package com.pingidentity.spring.example.services;

import com.pingidentity.spring.example.data.PasswordPolicy;
import com.pingidentity.spring.example.data.PasswordPolicy.Length;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import org.unbescape.java.JavaEscape;

public class PasswordManagementServiceTest {

  private PasswordManagementService passwordManagementService = new PasswordManagementService();
  private SoftAssert softAssert = new SoftAssert();
  private static final String PASSWORD_MATCHED = "Password should match, because it has all characters the password policy requires";

  @Test
  public void getPasswordPattern() {

    Map<String, Integer> minCharacters = new HashMap<>();
    minCharacters.put("abcdefghijklmnopqrstuvwxyz", 2);
    minCharacters.put("ABCDEFGHIJKLMNOPQRSTUVWXYZ", 1);
    minCharacters.put("123456890", 1);
    minCharacters.put("~!@#$%^&*()-_=+[]{}|;:,.<>/?", 1);

    PasswordPolicy passwordPolicy = new PasswordPolicy()
        .setLength(new Length().setMax(8).setMin(4))
        .setMaxRepeatedCharacters(2)
        .setMinCharacters(
            minCharacters
        );
    String passwordPattern = passwordManagementService.getPasswordRegex(passwordPolicy);
    Pattern pattern = Pattern.compile(JavaEscape.escapeJava(passwordPattern));

    // Verify correct passwords are caught by password pattern we build from our password policy
    softAssert.assertTrue(pattern.matcher("Ah16^fgk").matches(), PASSWORD_MATCHED);
    softAssert.assertTrue(pattern.matcher("AA16^fgk").matches(), PASSWORD_MATCHED);
    softAssert.assertTrue(pattern.matcher("AA16^!gk").matches(), PASSWORD_MATCHED);
    softAssert.assertTrue(pattern.matcher("AA16^!gg").matches(), PASSWORD_MATCHED);

    // Verify wrong passwords are caught by password pattern we build from our password policy
    softAssert.assertFalse("h16^fgk".matches(passwordPattern),
        "Password should not match, because it is without any capital character the password policy requires");
    softAssert.assertFalse(
        "AB16^CD".matches(passwordPattern),
        "Password should not match, because it is without any lowercase character the password policy requires");
    softAssert.assertFalse(
        "AhBl^fgk".matches(passwordPattern),
        "Password should not match, because it is without any digital number the password policy requires");
    softAssert.assertFalse(
        "Ah16Hfgk".matches(passwordPattern),
        "Password should not match, because it is without any special character the password policy requires");
    softAssert.assertFalse(

        "Ah16^JDL".matches(passwordPattern),
        "Password should not match, because it is without a certain amount of lowercase character the password policy requires");
    softAssert.assertFalse(
        "Ah16^JJJ".matches(passwordPattern),
        "Password should not match, because it is has more repeated characters the password policy requires");
    softAssert.assertFalse(
        "Ah16^fgkT".matches(passwordPattern),
        "Password should not match, because it is has more characters the password policy requires");
    softAssert.assertFalse(
        "A6J".matches(passwordPattern),
        "Password should not match, because it is has less characters the password policy requires");
    softAssert.assertAll();

  }

}
