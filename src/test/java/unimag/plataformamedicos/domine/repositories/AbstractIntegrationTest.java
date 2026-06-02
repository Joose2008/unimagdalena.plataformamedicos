package unimag.plataformamedicos.domine.repositories;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import unimag.plataformamedicos.security.jwt.JwtService;
import unimag.plataformamedicos.security.service.JpaUserDetailsService;

@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @MockBean
    protected JwtService jwtService;

    @MockBean
    protected JpaUserDetailsService jpaUserDetailsService;

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