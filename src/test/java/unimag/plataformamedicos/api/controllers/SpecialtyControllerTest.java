package unimag.plataformamedicos.api.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import unimag.plataformamedicos.api.dtos.SpecialtyDtos.*;
import unimag.plataformamedicos.service.interfaces.SpecialtyService;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = SpecialtyController.class)
@AutoConfigureMockMvc
class SpecialtyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SpecialtyService specialtyService;

    private final UUID specialtyId = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private SpecialtyResponse buildResponse() {
        return new SpecialtyResponse(specialtyId, "Cardiología", "Especialidad del corazón");
    }

    @Test
    @DisplayName("POST /api/specialties - debe crear especialidad y retornar 201")
    void create_shouldReturn201() throws Exception {
        when(specialtyService.create(any(CreateSpecialtyRequest.class))).thenReturn(buildResponse());

        mockMvc.perform(post("/api/specialties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Cardiología",
                                  "description": "Especialidad del corazón"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(specialtyId.toString()))
                .andExpect(jsonPath("$.name").value("Cardiología"))
                .andExpect(jsonPath("$.description").value("Especialidad del corazón"));
    }

    @Test
    @DisplayName("GET /api/specialties - debe retornar lista con 200")
    void findAll_shouldReturn200() throws Exception {
        when(specialtyService.findAll()).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/specialties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Cardiología"));
    }

    @Test
    @DisplayName("GET /api/specialties - lista vacía retorna 200")
    void findAll_empty_shouldReturn200() throws Exception {
        when(specialtyService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/specialties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
