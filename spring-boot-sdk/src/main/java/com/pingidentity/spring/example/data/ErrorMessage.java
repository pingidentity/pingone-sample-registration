package com.pingidentity.spring.example.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorMessage {

  String id;
  String code;
  String message;
  ErrorMessageDetails[] details;

  @Data
  @Accessors(chain = true)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ErrorMessageDetails {

    String code;
    String message;
    String target;
    InnerError innerError;
  }

  @Data
  @Accessors(chain = true)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class InnerError {

    List<String> unsatisfiedRequirements;
  }
}
