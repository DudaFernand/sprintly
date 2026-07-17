package com.mariafernandes.sprintly.dto;

public record ErrorResponse(int status, String error, String message) {}
