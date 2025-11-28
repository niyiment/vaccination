package com.niyiment.authservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
public class SwaggerConfig {

  @Value("${springdoc.info.title}")
  private String title;

  @Value("${springdoc.info.description}")
  private String description;

  @Value("${springdoc.info.version}")
  private String version;

  @Value("${springdoc.info.contact.name}")
  private String contactName;

  @Value("${springdoc.info.contact.email}")
  private String contactEmail;

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title(title)
                .version(version)
                .description(description)
                .contact(new Contact().name(contactName).email(contactEmail))
                .license(new License().name("Apache 2.0").url("http://springdoc.org")))
        .servers(List.of(
                new Server()
                        .url("http://localhost:8080")
                        .description("Development Server"),
                new Server()
                        .url("https://your-production-url.com")
                        .description("Production Server")
        ));
  }
}
