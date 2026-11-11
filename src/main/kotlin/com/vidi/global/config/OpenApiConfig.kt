package com.vidi.global.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun openApi(): OpenAPI {
        val schemeName = "bearerAuth"
        val securityScheme =
            SecurityScheme()
                .name(schemeName)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")

        return OpenAPI()
            .info(
                Info()
                    .title("VIDI API")
                    .description("VIDI backend API")
                    .version("v1"),
            ).components(Components().addSecuritySchemes(schemeName, securityScheme))
            .addSecurityItem(SecurityRequirement().addList(schemeName))
    }
}
