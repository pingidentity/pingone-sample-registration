package com.pingidentity.spring.example.config;

import com.pingidentity.spring.example.services.MessagesService;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@ControllerAdvice
public class DefaultControllerAdvice {

  @Autowired
  MessagesService messagesService;

  @ExceptionHandler(Throwable.class)
  public String handleException(Exception ex, RedirectAttributes redirectAttrs) {
    log.error("Something went wrong. ", ex);
    messagesService.error((Map<String, Object>) redirectAttrs.getFlashAttributes(), "Something went wrong. ");
    return "redirect:/login";
  }
}
