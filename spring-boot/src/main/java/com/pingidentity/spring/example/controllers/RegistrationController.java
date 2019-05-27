package com.pingidentity.spring.example.controllers;

import com.pingidentity.spring.example.data.Population;
import com.pingidentity.spring.example.data.User;
import com.pingidentity.spring.example.services.MessagesService;
import com.pingidentity.spring.example.services.PasswordManagementService;
import com.pingidentity.spring.example.services.PopulationService;
import com.pingidentity.spring.example.services.UserService;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

  @Value("${ping.registration.filterPopulationByDescription}")
  private boolean filterPopulationByDescription;

  @GetMapping
  public String registrationHomePage(Model model) {
    model.addAttribute("user", new User());
    model.addAttribute("passwordPattern", passwordManagementService.getPasswordPattern());
    if (!filterPopulationByDescription) {
      model.addAttribute("populations", populationService.getAllPopulations().getPopulations());
    }
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
      if (filterPopulationByDescription) {
        String userEmail = user.getEmail();
        // Set population to which the user will belong to basing on its email
        user.setPopulation(getUserPopulationByFilter(userEmail.substring(userEmail.indexOf("@") + 1)));
      }

      //  Add new user resource to the specified environment
      ResponseEntity<User> result = userService.createUser(user);

      // Set user password without necessity for the user to change the current password on the next login
      if (result.getStatusCode() == HttpStatus.CREATED) {
        User newUser = result.getBody();
        userId = newUser.getId();
        ResponseEntity<Map> passwordResult = passwordManagementService
            .setPassword(newUser.getPasswordSet().getHref(), user.getPassword(), false);
        if (passwordResult.getStatusCode() == HttpStatus.OK) {
          messagesService
              .success((Map<String, Object>) redirectAttrs.getFlashAttributes(), "User was properly created. ");
          return "redirect:/login";
        } else {
          messagesService.error(model.asMap(),
              "Setting user password failed" + (passwordResult != null ? " with status: " + passwordResult
                  .getStatusCode() + " having in response " + passwordResult.getBody() : ""));
        }
      }
    } catch (Exception e) {
      messagesService.error(model.asMap(), "User was not properly created. ", e);
    }

    // Cleanup all data in case of unsuccessful user create operation
    model.addAttribute("passwordPattern", passwordManagementService.getPasswordPattern());
    if (!filterPopulationByDescription) {
      model.addAttribute("populations", populationService.getAllPopulations().getPopulations());
    }
    if (userId != null) {
      userService.deleteUser(userId);
    }
    return "register";
  }

  /**
   * Filter all populations basing on a filter value and select the first one
   *
   * @param filterValue words, separated by comma, to filter population basing on its description. This string will be
   * converted to regex, that will try to find any word matches. For example: the population matches if description is
   * {@code airfrance.com, air-france.com, france.com} and user email is {@code air-france.com}
   * @return population to which the user will belong to
   * @see <a href="https://docs.oracle.com/javase/tutorial/essential/regex/bounds.html">Java Boundary Matchers</a>
   */
  private Population getUserPopulationByFilter(String filterValue) {

    Optional<Population> filteredPopulation = populationService.getAllPopulations().getPopulations().stream()
        .filter(population -> {
              String filter = Arrays.stream(population.getDescription().split(","))
                  .map(regex -> "(.*\\b" + regex.trim() + "\\b.*)").collect(Collectors.joining("|"));
              return filterValue.matches(filter);
            }
        )
        .findFirst();

    return filteredPopulation
        .orElseThrow(() -> new RuntimeException("There is no population that matches to your population filter."));

  }

}
