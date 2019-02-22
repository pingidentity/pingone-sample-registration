package com.pingidentity.spring.example.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.openapitools.jackson.dataformat.hal.annotation.EmbeddedResource;
import io.openapitools.jackson.dataformat.hal.annotation.Resource;
import java.util.Collection;
import java.util.Map;
import lombok.Data;
import lombok.experimental.Accessors;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Resource
@Data
public class PasswordPolicy {

  private String name;
  private String description;
  @JsonProperty("default")
  private boolean isDefault;
  private boolean excludesProfileData;
  private boolean notSimilarToCurrent;
  private boolean excludesCommonlyUsed;
  private int minComplexity;
  private int maxAgeDays;
  private int maxRepeatedCharacters;
  private int minUniqueCharacters;
  private Length length;
  /**
   * A set of key-value pairs where the key is a string containing all the characters that may be included and the value
   * is the minimum number of times one of the characters must appear in the password.
   */
  private Map<String, Integer> minCharacters;

  @Accessors(chain = true)
  @JsonIgnoreProperties(ignoreUnknown = true)
  @Resource
  @Data
  public static class Length {

    private int min;
    private int max;
  }

  @Accessors(chain = true)
  @JsonIgnoreProperties(ignoreUnknown = true)
  @Resource
  @Data
  public static class PasswordPolicies {

    @EmbeddedResource
    private Collection<PasswordPolicy> passwordPolicies;
  }
}
