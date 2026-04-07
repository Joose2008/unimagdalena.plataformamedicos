package unimag.plataformamedicos.domine.entities;

import jakarta.persistence.*;
import lombok.*;
import unimag.plataformamedicos.enums.PatientStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "patients")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "document_number", nullable = false)
    private String documentNumber;

    @Column(nullable = false)
    private String email;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PatientStatus status = PatientStatus.ACTIVE;

    @Column(name = "created_at")
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    @Builder.Default
    private Instant updatedAt = Instant.now();
}