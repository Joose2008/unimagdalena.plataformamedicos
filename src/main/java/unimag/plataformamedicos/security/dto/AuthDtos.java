package unimag.plataformamedicos.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import unimag.plataformamedicos.security.domine.Role;

import java.util.Set;
import java.util.UUID;

public class AuthDtos {

    public record RegisterRequest(
            @Email(message = "Debe ser un correo valido")
            @NotBlank(message = "El correo es obligatorio")
            @Size(max = 120, message = "El correo no puede superar 120 caracteres")
            String email,

            @NotBlank(message = "La contrasena es obligatoria")
            @Size(min = 8, max = 72, message = "La contrasena debe tener entre 8 y 72 caracteres")
            @Pattern(
                    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                    message = "La contrasena debe tener mayuscula, minuscula y numero"
            )
            String password,

            Set<Role> roles
    ){}

    public record LoginRequest(
            @Email(message = "Debe ser un correo valido")
            @NotBlank(message = "El correo es obligatorio")
            @Size(max = 120, message = "El correo no puede superar 120 caracteres")
            String email,

            @NotBlank(message = "La contrasena es obligatoria")
            String password
    ){}

    public record AuthResponse(
            String accessToken,
            String tokenType,
            long expiresInSeconds
    ){}

    public record UserResponse(
            UUID id,
            String email,
            Set<Role> roles
    ){}
}
