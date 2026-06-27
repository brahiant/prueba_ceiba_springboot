package com.deportal.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI deportalOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Deportal API")
                        .version("v1")
                        .description("API local para gestion de canchas deportivas, reservas, pagos, cancelaciones, waitlist y reportes. Los endpoints protegidos usan JWT con el esquema Bearer.")
                        .contact(new Contact().name("Deportal Technical Test"))
                        .license(new License().name("Technical test/demo usage")))
                .components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Pegar solamente el token JWT retornado por /api/auth/login o /api/auth/register.")))
                .addServersItem(new Server().url("http://localhost:8080").description("Local host"))
                .addServersItem(new Server().url("http://localhost:8080").description("Docker local"));
    }
}
