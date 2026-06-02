package unimag.plataformamedicos.security.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import unimag.plataformamedicos.exception.ConflictException;
import unimag.plataformamedicos.security.domine.AppUser;
import unimag.plataformamedicos.security.domine.Role;
import unimag.plataformamedicos.security.dto.AuthDtos.RegisterRequest;
import unimag.plataformamedicos.security.dto.AuthDtos.UserResponse;
import unimag.plataformamedicos.security.repo.AppUserRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final AppUserRepository users;
    private final PasswordEncoder encoder;

    @GetMapping("/roles")
    public ResponseEntity<List<Role>> roles() {
        return ResponseEntity.ok(Arrays.asList(Role.values()));
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody RegisterRequest request) {
        if (users.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("Ya existe un usuario con ese correo.");
        }

        Set<Role> roles = Optional.ofNullable(request.roles())
                .filter(selectedRoles -> !selectedRoles.isEmpty())
                .orElseGet(() -> Set.of(Role.ROLE_USER));

        AppUser user = AppUser.builder()
                .email(request.email().trim())
                .password(encoder.encode(request.password()))
                .roles(roles)
                .build();

        AppUser savedUser = users.save(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new UserResponse(savedUser.getId(), savedUser.getEmail(), savedUser.getRoles()));
    }
}
