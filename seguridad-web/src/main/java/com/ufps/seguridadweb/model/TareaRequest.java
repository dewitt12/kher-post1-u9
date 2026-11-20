package com.ufps.seguridadweb.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TareaRequest(@NotBlank @Size(max = 120) String titulo) {
}
