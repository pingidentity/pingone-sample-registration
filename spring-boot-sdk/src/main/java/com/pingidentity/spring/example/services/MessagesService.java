package com.pingidentity.spring.example.services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pingidentity.spring.example.data.ErrorMessage;
import java.io.IOException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Slf4j
@Service
public class MessagesService {

  private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  public void error(Map<String, Object> modelAttr, String initMessage, Exception... exceptions) {
    StringBuilder message = getMessage(initMessage, exceptions);
    addMessageDetails(modelAttr, message.toString(), "danger");
  }

  public void success(Map<String, Object> modelAttr, String initMessage) {
    addMessageDetails(modelAttr, initMessage, "success");
  }

  public StringBuilder getMessage(String initMessage, Exception... exceptions) {
    StringBuilder message = new StringBuilder(initMessage);
    for (Exception exception : exceptions) {
      if (exception instanceof HttpClientErrorException) {
        try {
          ErrorMessage errorMessage = OBJECT_MAPPER
              .readValue(((HttpClientErrorException) exception).getResponseBodyAsString(), ErrorMessage.class);
          message.append("\n").append(errorMessage.getMessage()).append("\n");

          if (errorMessage.getDetails() != null) {
            message.append(errorMessage.getDetails()[0].getTarget() != null ? errorMessage.getDetails()[0].getTarget()
                : "" + "\n");
            message.append(errorMessage.getDetails()[0].getMessage() != null ? errorMessage.getDetails()[0].getMessage()
                : "" + "\n");
          }
        } catch (IOException e1) {
          log.error("Error while parsing an error", e1);
        }
      } else {
        message.append(exception.getMessage());
      }
      log.error(message.toString(), exception);
    }
    return message;
  }

  private void addMessageDetails(Map<String, Object> modelAttr, String s, String alertType) {
    modelAttr.put("message", s);
    modelAttr.put("alertClass", alertType);
  }
}
