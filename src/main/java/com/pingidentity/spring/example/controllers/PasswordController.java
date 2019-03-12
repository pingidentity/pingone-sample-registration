package com.pingidentity.spring.example.controllers;

import com.pingidentity.spring.example.data.User;
import com.pingidentity.spring.example.data.User.Users;
import com.pingidentity.spring.example.services.MessagesService;
import com.pingidentity.spring.example.services.PasswordManagementService;
import com.pingidentity.spring.example.services.UserService;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/password")
public class PasswordController {

  @Autowired
  UserService userService;
  @Autowired
  MessagesService messagesService;
  @Autowired
  private PasswordManagementService passwordManagementService;
  @Value("${ping.environmentId}")
  private String environmentId;

  @GetMapping("/change")
  public String changePasswordHomePage(Model model) {
    model.addAttribute("passwordPattern", passwordManagementService.getPasswordPattern());
    model.addAttribute("user", new User());
    return "changePassword";
  }

  @GetMapping("/forgot")
  public String forgotPasswordHomePage(Model model) {
    model.addAttribute("user", new User());
    return "forgotPassword";
  }

  @PostMapping("/forgot")
  public String forgotPassword(@ModelAttribute User user, Model model) {
    try {
      String userId = getUserID(user);
      ResponseEntity<Map> responseEntity = passwordManagementService.recoverPassword(userId);
      if (responseEntity.getStatusCode().is2xxSuccessful()) {
        messagesService.success(model.asMap(),
            "If you have an active account with a valid email address, you will receive an email with a recovery code which you may enter here, along with a new password. If you do not have an account or email, please contact your administrator to recover your password.");
        model.addAttribute("passwordPattern", passwordManagementService.getPasswordPattern());
        model.addAttribute("user", user.setId(userId));
        return "recoverPassword";
      } else {
        messagesService.error(model.asMap(),
            "Forgot password failed with status:" + responseEntity.getStatusCode() + " having in response "
                + responseEntity.getBody());
        return "forgotPassword";
      }
    } catch (Exception e) {
      messagesService.error(model.asMap(), "Password recovery failed. ", e);
      return "forgotPassword";
    }
  }

  @PostMapping("/recover")
  public String recoverPassword(@ModelAttribute User user, Model model, RedirectAttributes redirectAttrs) {
    try {
      ResponseEntity<Map> responseEntity = passwordManagementService
          .resetPassword(user.getId(), user.getRecoveryCode(), user
              .getPassword());
      if (responseEntity.getStatusCode().is2xxSuccessful()) {
        messagesService
            .success((Map<String, Object>) redirectAttrs.getFlashAttributes(), "Password recovery succeeded. ");
        return "redirect:/login";
      } else {
        messagesService.error(model.asMap(),
            "Password recovery failed with status: " + responseEntity.getStatusCode() + " having in response "
                + responseEntity.getBody());
      }
    } catch (Exception e) {
      messagesService.error(model.asMap(), "Could not recover password. ", e);
    }
    return "recoverPassword";

  }

  /**
   * Self-change reset of the password identified by the user ID and environment ID.
   */
  @PostMapping("/change")
  public String changePassword(@ModelAttribute User user, Model model, OAuth2AuthenticationToken authentication, RedirectAttributes redirectAttrs) {
    try {
      ResponseEntity<Map> result = passwordManagementService.changePassword(
          (String) authentication.getPrincipal().getAttributes().get("sub"),
          user.getOldPassword(),
          user.getPassword());
      if (!result.getStatusCode().is2xxSuccessful()) {
        messagesService
            .error(model.asMap(),
                "User password reset failed with status: " + result.getStatusCode() + " having in response " + result
                    .getBody());
      } else {
        messagesService
            .success((Map<String, Object>) redirectAttrs.getFlashAttributes(), "Password change succeeded. ");
        return "redirect:/login";
      }
    } catch (Exception e) {
      messagesService.error(model.asMap(), "Could not reset password. ", e);
    }
    return "changePassword";
  }


  private String getUserID(User user) {
    String userId = user.getId();
    if (userId == null) {
      ResponseEntity<Users> result = userService.findUserByName(user.getUsername());
      if (result.getStatusCode().is2xxSuccessful() && !result.getBody().getUsers().isEmpty()) {
        userId = result.getBody().getUsers().stream().findFirst().get().getId();
      } else {
        throw new RuntimeException(
            "There is no such user with a name: " + user.getUsername());
      }
    }
    return userId;
  }

}
