package com.pingidentity.spring.example.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.openapitools.jackson.dataformat.hal.annotation.EmbeddedResource;
import io.openapitools.jackson.dataformat.hal.annotation.Resource;
import java.util.Collection;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Resource
public class Population {

  private String id;
  private String description;
  private String name;
  private Environment environment;

  @Data
  @Accessors(chain = true)
  @JsonIgnoreProperties(ignoreUnknown = true)
  @Resource
  public static class Populations {

    @EmbeddedResource
    private Collection<Population> populations;
  }
}
