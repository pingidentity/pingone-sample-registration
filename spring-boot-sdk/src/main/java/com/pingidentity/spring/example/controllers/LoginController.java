package com.pingidentity.spring.example.controllers;

import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class LoginController {

  private static String authorizationRequestBaseUri = "oauth2/authorization";

  @Autowired
  private ClientRegistrationRepository clientRegistrationRepository;

  @GetMapping("/login")
  public ModelAndView login() {
    return new ModelAndView("oauthLogin",
        Collections.singletonMap("url", authorizationRequestBaseUri + "/" +
            clientRegistrationRepository.findByRegistrationId("pingidentity").getRegistrationId()));

  }

}
