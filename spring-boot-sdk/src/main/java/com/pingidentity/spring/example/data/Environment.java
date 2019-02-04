package com.pingidentity.spring.example.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.openapitools.jackson.dataformat.hal.annotation.Resource;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Resource
public class Environment {

  private String id;
}
