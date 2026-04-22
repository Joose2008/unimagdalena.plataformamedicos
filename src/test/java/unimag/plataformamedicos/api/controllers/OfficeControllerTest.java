package unimag.plataformamedicos.api.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import unimag.plataformamedicos.api.dtos.OfficeDtos.*;
import unimag.plataformamedicos.enums.OfficeStatus;
import unimag.plataformamedicos.service.interfaces.OfficeService;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OfficeController.class)
class OfficeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OfficeService officeService;

    private final UUID officeId = UUID.fromString("44444444-4444-4444-4444-444444444444");

    private OfficeResponse buildOfficeResponse() {
        return new OfficeResponse(officeId, "Consultorio 1", "Piso 2", OfficeStatus.AVAILABLE);
    }

    @Test
    @DisplayName("POST /api/offices - debe crear consultorio y retornar 201")
    void create_shouldReturn201() throws Exception {
        when(officeService.create(any(CreateOfficeRequest.class))).thenReturn(buildOfficeResponse());

        mockMvc.perform(post("/api/offices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Consultorio 1",
                                  "location": "Piso 2"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(officeId.toString()))
                .andExpect(jsonPath("$.name").value("Consultorio 1"))
                .andExpect(jsonPath("$.location").value("Piso 2"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    @DisplayName("GET /api/offices - debe retornar lista con 200")
    void findAll_shouldReturn200() throws Exception {
        when(officeService.findAll()).thenReturn(List.of(buildOfficeResponse()));

        mockMvc.perform(get("/api/offices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Consultorio 1"));
    }

    @Test
    @DisplayName("GET /api/offices - lista vacía retorna 200")
    void findAll_empty_shouldReturn200() throws Exception {
        when(officeService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/offices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("PUT /api/offices/{id} - debe actualizar consultorio y retornar 200")
    void update_shouldReturn200() throws Exception {
        // Usamos INACTIVE en lugar de UNDER_MAINTENANCE
        OfficeResponse updated = new OfficeResponse(officeId, "Consultorio Renovado", "Piso 3", OfficeStatus.INACTIVE);

        when(officeService.update(eq(officeId), any(UpdateOfficeRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/offices/{id}", officeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Consultorio Renovado",
                                  "location": "Piso 3",
                                  "status": "INACTIVE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Consultorio Renovado"))
                .andExpect(jsonPath("$.location").value("Piso 3"))
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }
}