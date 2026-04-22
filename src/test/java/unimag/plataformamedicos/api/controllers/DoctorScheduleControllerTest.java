package unimag.plataformamedicos.api.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import unimag.plataformamedicos.api.dtos.DoctorScheduleDtos.*;
import unimag.plataformamedicos.service.interfaces.DoctorScheduleService;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DoctorScheduleController.class)   // ← Cambio principal
class DoctorScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DoctorScheduleService doctorScheduleService;

    private final UUID doctorId   = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID scheduleId = UUID.fromString("66666666-6666-6666-6666-666666666666");

    private DoctorScheduleResponse buildResponse() {
        return new DoctorScheduleResponse(scheduleId, DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(12, 0));
    }

    @Test
    @DisplayName("POST /api/doctors/{doctorId}/schedules - debe crear horario y retornar 201")
    void create_shouldReturn201() throws Exception {
        when(doctorScheduleService.create(eq(doctorId), any(CreateDoctorScheduleRequest.class)))
                .thenReturn(buildResponse());

        mockMvc.perform(post("/api/doctors/{doctorId}/schedules", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dayOfWeek": "MONDAY",
                                  "startTime": "08:00:00",
                                  "endTime": "12:00:00"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(scheduleId.toString()))
                .andExpect(jsonPath("$.dayOfWeek").value("MONDAY"))
                .andExpect(jsonPath("$.startTime").value("08:00:00"))
                .andExpect(jsonPath("$.endTime").value("12:00:00"));
    }

    @Test
    @DisplayName("GET /api/doctors/{doctorId}/schedules - debe retornar horarios con 200")
    void findByDoctor_shouldReturn200() throws Exception {
        when(doctorScheduleService.findByDoctor(doctorId)).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/doctors/{doctorId}/schedules", doctorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].dayOfWeek").value("MONDAY"));
    }

    @Test
    @DisplayName("GET /api/doctors/{doctorId}/schedules - lista vacía retorna 200")
    void findByDoctor_empty_shouldReturn200() throws Exception {
        when(doctorScheduleService.findByDoctor(doctorId)).thenReturn(List.of());

        mockMvc.perform(get("/api/doctors/{doctorId}/schedules", doctorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("POST /api/doctors/{doctorId}/schedules - horario viernes retorna 201")
    void create_friday_shouldReturn201() throws Exception {
        DoctorScheduleResponse friday = new DoctorScheduleResponse(
                UUID.randomUUID(), DayOfWeek.FRIDAY, LocalTime.of(14, 0), LocalTime.of(18, 0));

        when(doctorScheduleService.create(eq(doctorId), any(CreateDoctorScheduleRequest.class)))
                .thenReturn(friday);

        mockMvc.perform(post("/api/doctors/{doctorId}/schedules", doctorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dayOfWeek": "FRIDAY",
                                  "startTime": "14:00:00",
                                  "endTime": "18:00:00"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dayOfWeek").value("FRIDAY"))
                .andExpect(jsonPath("$.startTime").value("14:00:00"));
    }
}