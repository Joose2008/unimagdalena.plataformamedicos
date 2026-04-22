package unimag.plataformamedicos.api.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import unimag.plataformamedicos.api.dtos.ReportDtos.*;
import unimag.plataformamedicos.service.interfaces.ReportService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)   // ← Cambio principal
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    private final String start = "2025-04-01T00:00:00";
    private final String end   = "2025-04-30T23:59:59";

    // ─── Office Occupancy ────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/reports/office-occupancy - debe retornar reporte de ocupación con 200")
    void getOfficeOccupancy_shouldReturn200WithReport() throws Exception {
        List<OfficeOccupancyResponse> report = List.of(
                new OfficeOccupancyResponse("Consultorio 1", 480L, 600L, 80.0),
                new OfficeOccupancyResponse("Consultorio 2", 300L, 600L, 50.0)
        );

        when(reportService.getOfficeOccupancy(
                eq(LocalDateTime.of(2025, 4, 1, 0, 0, 0)),
                eq(LocalDateTime.of(2025, 4, 30, 23, 59, 59))
        )).thenReturn(report);

        mockMvc.perform(get("/api/reports/office-occupancy")
                        .param("start", start)
                        .param("end", end))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].officeName").value("Consultorio 1"))
                .andExpect(jsonPath("$[0].occupancyPercentage").value(80.0))
                .andExpect(jsonPath("$[1].officeName").value("Consultorio 2"));
    }

    @Test
    @DisplayName("GET /api/reports/office-occupancy - sin resultados retorna lista vacía con 200")
    void getOfficeOccupancy_noResults_shouldReturn200WithEmptyList() throws Exception {
        when(reportService.getOfficeOccupancy(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/reports/office-occupancy")
                        .param("start", start)
                        .param("end", end))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/reports/office-occupancy - sin parámetros retorna 400")
    void getOfficeOccupancy_missingParams_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/reports/office-occupancy"))
                .andExpect(status().isBadRequest());
    }

    // ─── Doctor Productivity ─────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/reports/doctor-productivity - debe retornar reporte de productividad con 200")
    void getDoctorProductivity_shouldReturn200WithReport() throws Exception {
        List<DoctorProductivityResponse> report = List.of(
                new DoctorProductivityResponse("Dr. Juan Pérez", "Cardiología", 42L),
                new DoctorProductivityResponse("Dra. Ana Gómez", "Pediatría", 35L)
        );

        when(reportService.getDoctorProductivity()).thenReturn(report);

        mockMvc.perform(get("/api/reports/doctor-productivity"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].doctorName").value("Dr. Juan Pérez"))
                .andExpect(jsonPath("$[0].specialtyName").value("Cardiología"))
                .andExpect(jsonPath("$[0].completedAppointments").value(42))
                .andExpect(jsonPath("$[1].doctorName").value("Dra. Ana Gómez"));
    }

    @Test
    @DisplayName("GET /api/reports/doctor-productivity - sin resultados retorna lista vacía con 200")
    void getDoctorProductivity_noResults_shouldReturn200WithEmptyList() throws Exception {
        when(reportService.getDoctorProductivity()).thenReturn(List.of());

        mockMvc.perform(get("/api/reports/doctor-productivity"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ─── No-Show Patients ────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/reports/no-show-patients - debe retornar reporte de inasistencias con 200")
    void getNoShowPatients_shouldReturn200WithReport() throws Exception {
        List<NoShowPatientResponse> report = List.of(
                new NoShowPatientResponse("María López", "1234567890", 3L),
                new NoShowPatientResponse("Carlos Ruiz", "0987654321", 1L)
        );

        when(reportService.getNoShowPatients(
                eq(LocalDateTime.of(2025, 4, 1, 0, 0, 0)),
                eq(LocalDateTime.of(2025, 4, 30, 23, 59, 59))
        )).thenReturn(report);

        mockMvc.perform(get("/api/reports/no-show-patients")
                        .param("start", start)
                        .param("end", end))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].patientName").value("María López"))
                .andExpect(jsonPath("$[0].noShowCount").value(3))
                .andExpect(jsonPath("$[1].patientName").value("Carlos Ruiz"));
    }

    @Test
    @DisplayName("GET /api/reports/no-show-patients - sin resultados retorna lista vacía con 200")
    void getNoShowPatients_noResults_shouldReturn200WithEmptyList() throws Exception {
        when(reportService.getNoShowPatients(any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/reports/no-show-patients")
                        .param("start", start)
                        .param("end", end))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /api/reports/no-show-patients - sin parámetros retorna 400")
    void getNoShowPatients_missingParams_shouldReturn400() throws Exception {
        mockMvc.perform(get("/api/reports/no-show-patients"))
                .andExpect(status().isBadRequest());
    }
}