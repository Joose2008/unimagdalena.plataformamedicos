package unimag.plataformamedicos.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import unimag.plataformamedicos.security.domine.Role;

import java.util.Set;

public class AuthDtos {

    public record RegisterRequest(
            @Email @NotBlank String email,
            @NotBlank String password,
            Set<Role> roles
    ){}

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ){}

    public record AuthResponse(
            String accessToken,
            String tokenType,
            long expiresInSeconds
    ){}
}
