package unimag.plataformamedicos.api.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import unimag.plataformamedicos.api.dtos.ReportDtos.AvailabilitySlotResponse;
import unimag.plataformamedicos.service.interfaces.AvailabilityService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = AvailabilityController.class)
@AutoConfigureMockMvc
class AvailabilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AvailabilityService availabilityService;

    private final UUID doctorId          = UUID.randomUUID();
    private final UUID appointmentTypeId = UUID.randomUUID();

    @Test
    @DisplayName("GET /api/availability/doctors/{doctorId} - debe retornar slots disponibles con 200")
    void getAvailableSlots_shouldReturn200WithSlotList() throws Exception {
        List<AvailabilitySlotResponse> slots = List.of(
                new AvailabilitySlotResponse(
                        LocalDateTime.of(2025, 5, 10, 9, 0),
                        LocalDateTime.of(2025, 5, 10, 9, 30)
                ),
                new AvailabilitySlotResponse(
                        LocalDateTime.of(2025, 5, 10, 10, 0),
                        LocalDateTime.of(2025, 5, 10, 10, 30)
                )
        );

        when(availabilityService.getAvailableSlots(
                eq(doctorId),
                eq(LocalDate.of(2025, 5, 10)),
                eq(appointmentTypeId)
        )).thenReturn(slots);

        mockMvc.perform(get("/api/availability/doctors/{doctorId}", doctorId)
                        .param("date", "2025-05-10")
                        .param("appointmentTypeId", appointmentTypeId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].startAt").value("2025-05-10T09:00:00"))
                .andExpect(jsonPath("$[0].endAt").value("2025-05-10T09:30:00"))
                .andExpect(jsonPath("$[1].startAt").value("2025-05-10T10:00:00"));
    }

    @Test
    @DisplayName("GET /api/availability/doctors/{doctorId} - sin slots disponibles retorna lista vacía con 200")
    void getAvailableSlots_noSlotsAvailable_shouldReturn200WithEmptyList() throws Exception {
        when(availabilityService.getAvailableSlots(any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/availability/doctors/{doctorId}", doctorId)
                        .param("date", "2025-05-10")
                        .param("appointmentTypeId", appointmentTypeId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/availability/doctors/{doctorId} - sin param 'date' debe retornar 400")
    void getAvailableSlots_missingDate_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/availability/doctors/{doctorId}", doctorId)
                        .param("appointmentTypeId", appointmentTypeId.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/availability/doctors/{doctorId} - sin param 'appointmentTypeId' debe retornar 400")
    void getAvailableSlots_missingAppointmentTypeId_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/availability/doctors/{doctorId}", doctorId)
                        .param("date", "2025-05-10"))
                .andExpect(status().isBadRequest());
    }
}
