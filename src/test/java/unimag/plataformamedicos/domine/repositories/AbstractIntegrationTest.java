package unimag.plataformamedicos.domine.repositories;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withReuse(true);

    static {
        POSTGRES.start();
        System.setProperty("spring.datasource.url", POSTGRES.getJdbcUrl());
        System.setProperty("spring.datasource.username", POSTGRES.getUsername());
        System.setProperty("spring.datasource.password", POSTGRES.getPassword());
    }

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    protected void limpiarTablas() {
        EntityManager em = entityManagerFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createNativeQuery(
                    "TRUNCATE TABLE appointments, doctor_schedules, doctors, " +
                            "patients, offices, appointment_types, specialties RESTART IDENTITY CASCADE"
            ).executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}