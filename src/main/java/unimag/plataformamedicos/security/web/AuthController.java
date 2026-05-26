package unimag.plataformamedicos.security.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import unimag.plataformamedicos.security.domine.AppUser;
import unimag.plataformamedicos.security.domine.Role;
import unimag.plataformamedicos.security.dto.AuthDtos.AuthResponse;
import unimag.plataformamedicos.security.dto.AuthDtos.LoginRequest;
import unimag.plataformamedicos.security.dto.AuthDtos.RegisterRequest;
import unimag.plataformamedicos.security.jwt.JwtService;
import unimag.plataformamedicos.security.repo.AppUserRepository;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwt;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        if (users.existsByEmailIgnoreCase(req.email())) {
            return ResponseEntity.badRequest().build();
        }

        var roles = Optional.ofNullable(req.roles())
                .filter(r -> !r.isEmpty())
                .orElseGet(() -> Set.of(Role.ROLE_USER));

        var user = AppUser.builder()
                .email(req.email())
                .password(encoder.encode(req.password()))
                .roles(roles)
                .build();

        users.save(user);

        var principal = User.withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(roles.stream().map(Enum::name).toArray(String[]::new))
                .build();

        var token = jwt.generateToken(principal, Map.of("roles", roles));
        return ResponseEntity.ok(new AuthResponse(token, "Bearer", jwt.getExpirationSeconds()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(req.email(), req.password()));

        var user = users.findByEmailIgnoreCase(req.email()).orElseThrow();
        var principal = User.withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRoles().stream().map(Enum::name).toArray(String[]::new))
                .build();

        var token = jwt.generateToken(principal, Map.of("roles", user.getRoles()));
        return ResponseEntity.ok(new AuthResponse(token, "Bearer", jwt.getExpirationSeconds()));
    }
}
