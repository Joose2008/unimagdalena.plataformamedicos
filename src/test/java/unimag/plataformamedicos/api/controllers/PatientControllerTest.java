package unimag.plataformamedicos.api.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import unimag.plataformamedicos.api.dtos.PatientDtos.*;
import unimag.plataformamedicos.enums.PatientStatus;
import unimag.plataformamedicos.service.interfaces.PatientService;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PatientController.class)   // ← Cambio principal
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientService patientService;

    private final UUID patientId = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private PatientResponse buildPatientResponse() {
        return new PatientResponse(patientId, "María López", "1234567890",
                "maria@example.com", "3001234567", PatientStatus.ACTIVE);
    }

    @Test
    @DisplayName("POST /api/patients - debe crear paciente y retornar 201")
    void create_shouldReturn201() throws Exception {
        when(patientService.create(any(CreatePatientRequest.class))).thenReturn(buildPatientResponse());

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "María López",
                                  "documentNumber": "1234567890",
                                  "email": "maria@example.com",
                                  "phone": "3001234567"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(patientId.toString()))
                .andExpect(jsonPath("$.name").value("María López"))
                .andExpect(jsonPath("$.documentNumber").value("1234567890"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("GET /api/patients/{id} - debe retornar paciente con 200")
    void findById_shouldReturn200() throws Exception {
        when(patientService.findById(patientId)).thenReturn(buildPatientResponse());

        mockMvc.perform(get("/api/patients/{id}", patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(patientId.toString()))
                .andExpect(jsonPath("$.name").value("María López"));
    }

    @Test
    @DisplayName("GET /api/patients - debe retornar lista con 200")
    void findAll_shouldReturn200() throws Exception {
        when(patientService.findAll()).thenReturn(List.of(buildPatientResponse()));

        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("María López"));
    }

    @Test
    @DisplayName("GET /api/patients - lista vacía retorna 200")
    void findAll_empty_shouldReturn200() throws Exception {
        when(patientService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("PUT /api/patients/{id} - debe actualizar paciente y retornar 200")
    void update_shouldReturn200() throws Exception {
        PatientResponse updated = new PatientResponse(patientId, "María Actualizada", "1234567890",
                "nueva@example.com", "3109999999", PatientStatus.INACTIVE);

        when(patientService.update(eq(patientId), any(UpdatePatientRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/patients/{id}", patientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "María Actualizada",
                                  "email": "nueva@example.com",
                                  "phone": "3109999999",
                                  "patientStatus": "INACTIVE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("María Actualizada"))
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    @DisplayName("PATCH /api/patients/{id}/document - debe actualizar documento y retornar 200")
    void updateDocument_shouldReturn200() throws Exception {
        PatientResponse updated = new PatientResponse(patientId, "María López", "9999999999",
                "maria@example.com", "3001234567", PatientStatus.ACTIVE);

        when(patientService.updateDocument(eq(patientId), any(UpdatePatientDocumentRequest.class))).thenReturn(updated);

        mockMvc.perform(patch("/api/patients/{id}/document", patientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "documentNumber": "9999999999" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentNumber").value("9999999999"));
    }
}