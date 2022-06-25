package ru.megamarket.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Mega Market Open API")
                                .version("1.0")
                                .description("Вступительное задание в Летнюю Школу Бэкенд Разработки Яндекса 2022")
                );
    }

}
