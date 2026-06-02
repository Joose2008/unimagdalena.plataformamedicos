package unimag.plataformamedicos.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import unimag.plataformamedicos.security.domine.AppUser;
import unimag.plataformamedicos.security.domine.Role;
import unimag.plataformamedicos.security.repo.AppUserRepository;

import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner createDefaultAdmin(
            AppUserRepository users,
            PasswordEncoder encoder,
            @Value("${app.default-admin.email:admin@admin.com}") String adminEmail,
            @Value("${app.default-admin.password:Admin1234}") String adminPassword
    ) {
        return args -> {
            if (!users.existsByEmailIgnoreCase(adminEmail)) {
                users.save(AppUser.builder()
                        .email(adminEmail)
                        .password(encoder.encode(adminPassword))
                        .roles(Set.of(Role.ROLE_ADMIN))
                        .build());
            }
        };
    }
}
