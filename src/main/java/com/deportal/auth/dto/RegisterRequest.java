package com.deportal.auth.dto;

import com.deportal.users.enums.CustomerType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre no puede superar 120 caracteres")
        String name,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email debe tener un formato valido")
        @Size(max = 180, message = "El email no puede superar 180 caracteres")
        String email,

        @NotBlank(message = "La contrasena es obligatoria")
        @Size(min = 8, max = 80, message = "La contrasena debe tener entre 8 y 80 caracteres")
        String password,

        @NotNull(message = "El tipo de cliente es obligatorio")
        CustomerType customerType) {
}
