package com.commerce4retail.product.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI productApiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Product API")
                        .description("Commerce4Retail Product API — COM-66")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Commerce4Retail Team")
                                .email("katraviteja@deloitte.com"))
                        .license(new License().name("Proprietary")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development")
                ));
    }
}
