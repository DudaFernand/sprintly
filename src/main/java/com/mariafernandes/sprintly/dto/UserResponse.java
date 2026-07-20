package com.mariafernandes.sprintly.dto;

import com.mariafernandes.sprintly.domain.User;

public record UserResponse(Long id, String email) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail());
    }
}
