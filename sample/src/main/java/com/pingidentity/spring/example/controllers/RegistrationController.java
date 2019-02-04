package com.pingidentity.spring.example.controllers;

import com.pingidentity.spring.example.data.User;
import com.pingidentity.spring.example.services.MessagesService;
import com.pingidentity.spring.example.services.PasswordManagementService;
import com.pingidentity.spring.example.services.PopulationService;
import com.pingidentity.spring.example.services.UserService;
import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/register")
public class RegistrationController {

  @Autowired
  PopulationService populationService;
  @Autowired
  PasswordManagementService passwordManagementService;
  @Autowired
  MessagesService messagesService;
  @Autowired
  UserService userService;

  @GetMapping
  public String registrationHomePage(Model model) {
    model.addAttribute("user", new User());
    model.addAttribute("passwordPattern", passwordManagementService.getPasswordPattern());
    model.addAttribute("populations", populationService.getAllPopulations().getPopulations());
    return "register";
  }

  @PostMapping
  public String registerNewUser(@ModelAttribute @Valid User user, BindingResult bindingResult,
      RedirectAttributes redirectAttrs, Model model) {
    if (bindingResult.hasErrors()) {
      return "register";
    }

    String userId = null;
    try {
      // Step 1. Add new user resource to the specified environment
      ResponseEntity<User> result = userService.createUser(user);

      // Step 2. Set user password without necessity for the user to change the current password on the next login
      if (result.getStatusCode() == HttpStatus.CREATED) {
        User newUser = result.getBody();
        userId = newUser.getId();
        ResponseEntity<Map> passwordResult = passwordManagementService
            .setPassword(newUser.getPasswordSet().getHref(), user.getPassword());
        if (passwordResult.getStatusCode() == HttpStatus.OK) {
          messagesService
              .success((Map<String, Object>) redirectAttrs.getFlashAttributes(), "User was properly created. ");
          return "redirect:/login";
        } else {
          messagesService.error(model.asMap(),
              "User password was not properly set" + (passwordResult != null ? "" + passwordResult.getStatusCode()
                  + passwordResult.getBody() : ""));
        }
      }
    } catch (Exception e) {
      messagesService.error(model.asMap(), "User was not properly created", e);
    }

    // Cleanup all data in case of unsuccessful user create operation
    model.addAttribute("populations", populationService.getAllPopulations().getPopulations());
    if (userId != null) {
      userService.deleteUser(userId);
    }
    return "register";
  }
}
