package com.soccer.forum.service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Soccer Forum API")
                        .version("1.0.0")
                        .description("Soccer Forum后端接口开发文档")
                        .contact(new Contact()
                                .name("翎羽")
                                .email("zhangchao2903@163.com")));
    }
}
