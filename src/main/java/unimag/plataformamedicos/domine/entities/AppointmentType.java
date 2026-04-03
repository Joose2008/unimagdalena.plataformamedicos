package unimag.plataformamedicos.domine.entities;


import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "appointment_types")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AppointmentType {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;
}