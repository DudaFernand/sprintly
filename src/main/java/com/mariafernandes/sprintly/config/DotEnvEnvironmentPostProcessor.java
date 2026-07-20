package com.mariafernandes.sprintly.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Carrega o {@code .env} da raiz do projeto no Environment do Spring,
 * sem sobrescrever variáveis já definidas no SO.
 */
public class DotEnvEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String PROPERTY_SOURCE_NAME = "dotenv";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Path path = Path.of(".env");
        if (!Files.isRegularFile(path)) {
            return;
        }

        Map<String, Object> props = new LinkedHashMap<>();
        try {
            for (String raw : Files.readAllLines(path)) {
                String line = raw.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                int eq = line.indexOf('=');
                if (eq <= 0) {
                    continue;
                }

                String key = line.substring(0, eq).trim();
                String value = stripQuotes(line.substring(eq + 1).trim());

                if (environment.getProperty(key) == null && System.getenv(key) == null) {
                    props.put(key, value);
                    // Spring resolve jwt.secret; .env costuma trazer JWT_SECRET
                    if ("JWT_SECRET".equals(key)) {
                        props.put("jwt.secret", value);
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao ler .env", e);
        }

        if (!props.isEmpty()) {
            environment.getPropertySources().addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, props));
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    private static String stripQuotes(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }
}
