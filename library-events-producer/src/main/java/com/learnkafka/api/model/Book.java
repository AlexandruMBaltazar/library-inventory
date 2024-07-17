package com.learnkafka.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record Book(
        @NotNull
        Long id,

        @NotBlank
        String name,

        @NotBlank
        String author
) {
}
