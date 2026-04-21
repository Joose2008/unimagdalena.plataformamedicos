package unimag.plataformamedicos.api.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import unimag.plataformamedicos.api.dtos.DoctorDtos.*;
import unimag.plataformamedicos.api.dtos.SpecialtyDtos.SpecialtyResponse;
import unimag.plataformamedicos.service.interfaces.DoctorService;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = DoctorController.class)
@AutoConfigureMockMvc
class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DoctorService doctorService;

    private final UUID doctorId    = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID specialtyId = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private DoctorResponse buildDoctorResponse() {
        SpecialtyResponse specialty = new SpecialtyResponse(specialtyId, "Cardiología", "Especialidad del corazón");
        return new DoctorResponse(doctorId, "Dr. Juan Pérez", "LIC-001", "juan@example.com", true, specialty);
    }

    @Test
    @DisplayName("POST /api/doctors - debe crear médico y retornar 201")
    void create_shouldReturn201() throws Exception {
        when(doctorService.create(any(CreateDoctorRequest.class))).thenReturn(buildDoctorResponse());

        mockMvc.perform(post("/api/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Dr. Juan Pérez",
                                  "licenceNumber": "LIC-001",
                                  "email": "juan@example.com",
                                  "specialtyId": "22222222-2222-2222-2222-222222222222"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(doctorId.toString()))
                .andExpect(jsonPath("$.name").value("Dr. Juan Pérez"))
                .andExpect(jsonPath("$.licenceNumber").value("LIC-001"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("GET /api/doctors/{id} - debe retornar médico con 200")
    void findById_shouldReturn200() throws Exception {
        when(doctorService.findById(doctorId)).thenReturn(buildDoctorResponse());

        mockMvc.perform(get("/api/doctors/{id}", doctorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(doctorId.toString()))
                .andExpect(jsonPath("$.name").value("Dr. Juan Pérez"));
    }

    @Test
    @DisplayName("GET /api/doctors - debe retornar lista con 200")
    void findAll_shouldReturn200() throws Exception {
        when(doctorService.findAll()).thenReturn(List.of(buildDoctorResponse()));

        mockMvc.perform(get("/api/doctors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Dr. Juan Pérez"));
    }

    @Test
    @DisplayName("GET /api/doctors - lista vacía retorna 200")
    void findAll_empty_shouldReturn200() throws Exception {
        when(doctorService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/doctors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("PUT /api/doctors/{id} - debe actualizar médico y retornar 200")
    void update_shouldReturn200() throws Exception {
        DoctorResponse updated = new DoctorResponse(doctorId, "Dr. Juan Actualizado", "LIC-001",
                "nuevo@example.com", true,
                new SpecialtyResponse(specialtyId, "Cardiología", "Especialidad del corazón"));

        when(doctorService.update(eq(doctorId), any(UpdateDoctorRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/doctors/{id}", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Dr. Juan Actualizado",
                                  "email": "nuevo@example.com",
                                  "active": true,
                                  "specialtyId": "22222222-2222-2222-2222-222222222222"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Dr. Juan Actualizado"))
                .andExpect(jsonPath("$.email").value("nuevo@example.com"));
    }

    @Test
    @DisplayName("PATCH /api/doctors/{id}/licence - debe actualizar licencia y retornar 200")
    void updateLicence_shouldReturn200() throws Exception {
        DoctorResponse updated = new DoctorResponse(doctorId, "Dr. Juan Pérez", "LIC-999",
                "juan@example.com", true,
                new SpecialtyResponse(specialtyId, "Cardiología", "Especialidad del corazón"));

        when(doctorService.update(eq(doctorId), any(UpdateDoctorLicenceRequest.class))).thenReturn(updated);

        mockMvc.perform(patch("/api/doctors/{id}/licence", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "licenceNumber": "LIC-999" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licenceNumber").value("LIC-999"));
    }
}
