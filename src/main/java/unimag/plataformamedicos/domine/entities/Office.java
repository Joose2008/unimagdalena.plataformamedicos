package unimag.plataformamedicos.domine.entities;

import jakarta.persistence.*;
import lombok.*;
import unimag.plataformamedicos.enums.OfficeStatus;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "offices")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Office {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OfficeStatus status = OfficeStatus.AVAILABLE;

    @Column(name = "created_at")
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    @Builder.Default
    private Instant updatedAt = Instant.now();
}