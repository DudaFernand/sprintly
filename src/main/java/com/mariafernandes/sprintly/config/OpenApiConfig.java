package com.mariafernandes.sprintly.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.tags.Tag;

@Configuration
@SecurityScheme(
    name = OpenApiConfig.SECURITY_SCHEME,
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer",
    description = "Cole aqui o accessToken retornado por POST /auth/register ou POST /auth/login. "
        + "Não inclua a palavra Bearer — o Swagger adiciona sozinho."
)
public class OpenApiConfig {

    public static final String AUTH_TAG = "1. Auth";
    public static final String SECURITY_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI sprintlyOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Sprintly API")
                .description("""
                    ## Como autenticar no Swagger
                    1. Abra **1. Auth** → `POST /auth/register` (ou `/login`) e Execute
                    2. Copie o campo **accessToken** da resposta
                    3. Clique em **Authorize** (cadeado no topo)
                    4. Cole o token e confirme
                    5. Chame os demais endpoints (ex.: `GET /users`)
                    """)
                .version("v1"))
            .addTagsItem(new Tag().name(AUTH_TAG).description("Comece por aqui: register / login / refresh"))
            .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME))
            .components(new Components());
    }
}
