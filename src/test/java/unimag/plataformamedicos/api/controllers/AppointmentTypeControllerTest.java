package unimag.plataformamedicos.api.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;  // Cambiado de MockitoBean a MockBean
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import unimag.plataformamedicos.api.dtos.AppointmentTypeDtos.*;
import unimag.plataformamedicos.service.interfaces.AppointmentTypeService;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AppointmentTypeController.class)  // ← Cambio principal
class AppointmentTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean  // ← Cambio: usar @MockBean en lugar de @MockitoBean (funciona igual en Spring Boot 3.4+)
    private AppointmentTypeService appointmentTypeService;

    private final UUID typeId = UUID.fromString("55555555-5555-5555-5555-555555555555");

    private AppointmentTypeResponse buildResponse() {
        return new AppointmentTypeResponse(typeId, "Consulta general", "Consulta estándar", 30);
    }

    @Test
    @DisplayName("POST /api/appointment-types - debe crear tipo de cita y retornar 201")
    void create_shouldReturn201() throws Exception {
        when(appointmentTypeService.create(any(CreateAppointmentTypeRequest.class))).thenReturn(buildResponse());

        mockMvc.perform(post("/api/appointment-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Consulta general",
                                  "description": "Consulta estándar",
                                  "durationMinutes": 30
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(typeId.toString()))
                .andExpect(jsonPath("$.name").value("Consulta general"))
                .andExpect(jsonPath("$.durationMinutes").value(30));
    }

    @Test
    @DisplayName("GET /api/appointment-types - debe retornar lista con 200")
    void findAll_shouldReturn200() throws Exception {
        when(appointmentTypeService.findAll()).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/appointment-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Consulta general"))
                .andExpect(jsonPath("$[0].durationMinutes").value(30));
    }

    @Test
    @DisplayName("GET /api/appointment-types - lista vacía retorna 200")
    void findAll_empty_shouldReturn200() throws Exception {
        when(appointmentTypeService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/appointment-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
