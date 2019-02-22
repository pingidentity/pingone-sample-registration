package com.pingidentity.spring.example.config;

import com.pingidentity.spring.example.data.Population;
import java.util.Locale;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.Formatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMVCConfig implements WebMvcConfigurer {

  public void addFormatters(FormatterRegistry registry) {
    registry.addFormatter(populationFormatter());
  }

  @Bean
  public PopulationFormatter populationFormatter() {
    return new PopulationFormatter();
  }

  public class PopulationFormatter implements Formatter<Population> {

    @Override
    public Population parse(String s, Locale locale) {
      return new Population().setId(s);
    }

    @Override
    public String print(Population population, Locale locale) {
      return (population != null ? population.getId() : "");
    }
  }

}
