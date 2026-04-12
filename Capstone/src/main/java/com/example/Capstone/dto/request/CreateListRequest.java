package com.example.Capstone.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateListRequest(
        @NotBlank String title,
        String description,
        @NotBlank String regionName,
        @NotNull @Size(min = 5) List<@Valid AddRestaurantRequest> restaurants
) {}
