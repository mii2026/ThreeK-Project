package com.ThreeK_Project.api_server.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "3k API",
                version = "v1"
        )
)
@Configuration
public class SwaggerConfig {

    private final String HEADER_NAME = "X-User-Name";

    @Bean
    public OpenAPI openAPI(){
        SecurityScheme userNameScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name(HEADER_NAME);

        SecurityRequirement securityRequirement = new SecurityRequirement().addList("userNameAuth");

        return new OpenAPI()
                .components(new Components().addSecuritySchemes("userNameAuth", userNameScheme))
                .security(Arrays.asList(securityRequirement));
    }

}