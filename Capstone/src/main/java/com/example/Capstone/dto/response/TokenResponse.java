package com.example.Capstone.dto.response;

public record TokenResponse(
    String accessToken,
    String refreshToken
) {}
