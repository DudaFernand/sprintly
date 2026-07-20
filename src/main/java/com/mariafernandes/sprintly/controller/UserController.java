package com.mariafernandes.sprintly.controller;

import com.mariafernandes.sprintly.config.OpenApiConfig;
import com.mariafernandes.sprintly.domain.User;
import com.mariafernandes.sprintly.dto.UserResponse;
import com.mariafernandes.sprintly.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
@Tag(name = "Users")
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME)
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Listar usuários da organização",
        description = "Retorna só membros da organização informada. Exige membership nela.")
    @GetMapping
    public List<UserResponse> findByOrganization(
            @RequestParam Long organizationId,
            @AuthenticationPrincipal User currentUser) {
        return userService.listByOrganization(currentUser, organizationId);
    }
}
