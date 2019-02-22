package com.pingidentity.spring.example.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.openapitools.jackson.dataformat.hal.HALLink;
import io.openapitools.jackson.dataformat.hal.annotation.EmbeddedResource;
import io.openapitools.jackson.dataformat.hal.annotation.Link;
import io.openapitools.jackson.dataformat.hal.annotation.Resource;
import java.util.Collection;
import javax.validation.constraints.Email;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Resource
public class User {

  private String id;
  private String username;
  @Email(message = "Email should be valid")
  private String email;
  private Population population;
  private String password;
  private String confirmPassword;
  private String oldPassword;
  private String recoveryCode;

  @Link("password.set")
  private HALLink passwordSet;

  @Data
  @Accessors(chain = true)
  @JsonIgnoreProperties(ignoreUnknown = true)
  @Resource
  public static class Users {

    @EmbeddedResource
    private Collection<User> users;
  }
}



