package unimag.plataformamedicos.api.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import unimag.plataformamedicos.api.dtos.AppointmentDtos.*;
import unimag.plataformamedicos.api.dtos.AppointmentTypeDtos.AppointmentTypeSummaryResponse;
import unimag.plataformamedicos.api.dtos.DoctorDtos.DoctorSummaryResponse;
import unimag.plataformamedicos.api.dtos.OfficeDtos.OfficeResponse;
import unimag.plataformamedicos.api.dtos.PatientDtos.PatientSummaryResponse;
import unimag.plataformamedicos.enums.AppointmentStatus;
import unimag.plataformamedicos.enums.OfficeStatus;
import unimag.plataformamedicos.service.interfaces.AppointmentService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AppointmentController.class)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AppointmentService appointmentService;

    private final UUID appointmentId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private final UUID patientId     = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private final UUID doctorId      = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID officeId      = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private final UUID typeId        = UUID.fromString("55555555-5555-5555-5555-555555555555");

    private AppointmentResponse buildResponse(AppointmentStatus status) {
        return new AppointmentResponse(
                appointmentId,
                new PatientSummaryResponse(patientId, "María López", "123456"),
                new DoctorSummaryResponse(doctorId, "Dr. Juan", "Cardiología"),
                new OfficeResponse(officeId, "Consultorio 1", "Piso 2", OfficeStatus.AVAILABLE),
                new AppointmentTypeSummaryResponse(typeId, "Consulta general", 30),
                LocalDateTime.of(2025, 5, 10, 9, 0),
                LocalDateTime.of(2025, 5, 10, 9, 30),
                status, null, null
        );
    }

    @Test
    @DisplayName("POST /api/appointments - debe crear cita y retornar 201")
    void create_shouldReturn201() throws Exception {
        when(appointmentService.create(any(CreateAppointmentRequest.class)))
                .thenReturn(buildResponse(AppointmentStatus.SCHEDULED));

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "patientId": "33333333-3333-3333-3333-333333333333",
                                  "doctorId": "11111111-1111-1111-1111-111111111111",
                                  "officeId": "44444444-4444-4444-4444-444444444444",
                                  "appointmentTypeId": "55555555-5555-5555-5555-555555555555",
                                  "startAt": "2025-05-10T09:00:00"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(appointmentId.toString()))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    @DisplayName("GET /api/appointments/{id} - debe retornar cita con 200")
    void findById_shouldReturn200() throws Exception {
        when(appointmentService.findById(appointmentId)).thenReturn(buildResponse(AppointmentStatus.SCHEDULED));

        mockMvc.perform(get("/api/appointments/{id}", appointmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(appointmentId.toString()))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    @DisplayName("GET /api/appointments - debe retornar lista con 200")
    void findAll_shouldReturn200() throws Exception {
        when(appointmentService.findAll()).thenReturn(List.of(buildResponse(AppointmentStatus.SCHEDULED)));

        mockMvc.perform(get("/api/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("SCHEDULED"));
    }

    @Test
    @DisplayName("PUT /api/appointments/{id}/confirm - debe confirmar cita y retornar 200")
    void confirm_shouldReturn200() throws Exception {
        when(appointmentService.confirm(appointmentId)).thenReturn(buildResponse(AppointmentStatus.CONFIRMED));

        mockMvc.perform(put("/api/appointments/{id}/confirm", appointmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("PUT /api/appointments/{id}/cancel - debe cancelar cita y retornar 200")
    void cancel_shouldReturn200() throws Exception {
        AppointmentResponse cancelled = new AppointmentResponse(
                appointmentId,
                new PatientSummaryResponse(patientId, "María López", "123456"),
                new DoctorSummaryResponse(doctorId, "Dr. Juan", "Cardiología"),
                new OfficeResponse(officeId, "Consultorio 1", "Piso 2", OfficeStatus.AVAILABLE),
                new AppointmentTypeSummaryResponse(typeId, "Consulta general", 30),
                LocalDateTime.of(2025, 5, 10, 9, 0),
                LocalDateTime.of(2025, 5, 10, 9, 30),
                AppointmentStatus.CANCELLED, "Paciente no puede asistir", null
        );

        when(appointmentService.cancel(eq(appointmentId), any(CancelAppointmentRequest.class))).thenReturn(cancelled);

        mockMvc.perform(put("/api/appointments/{id}/cancel", appointmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "cancellationReason": "Paciente no puede asistir" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancellationReason").value("Paciente no puede asistir"));
    }

    @Test
    @DisplayName("PUT /api/appointments/{id}/complete - con observaciones retorna 200")
    void complete_withObservations_shouldReturn200() throws Exception {
        AppointmentResponse completed = new AppointmentResponse(
                appointmentId,
                new PatientSummaryResponse(patientId, "María López", "123456"),
                new DoctorSummaryResponse(doctorId, "Dr. Juan", "Cardiología"),
                new OfficeResponse(officeId, "Consultorio 1", "Piso 2", OfficeStatus.AVAILABLE),
                new AppointmentTypeSummaryResponse(typeId, "Consulta general", 30),
                LocalDateTime.of(2025, 5, 10, 9, 0),
                LocalDateTime.of(2025, 5, 10, 9, 30),
                AppointmentStatus.COMPLETED, null, "Paciente mejoró"
        );

        when(appointmentService.complete(eq(appointmentId), eq("Paciente mejoró"))).thenReturn(completed);

        mockMvc.perform(put("/api/appointments/{id}/complete", appointmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "observations": "Paciente mejoró" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.observations").value("Paciente mejoró"));
    }

    @Test
    @DisplayName("PUT /api/appointments/{id}/complete - sin body retorna 200")
    void complete_withoutBody_shouldReturn200() throws Exception {
        when(appointmentService.complete(eq(appointmentId), eq(null)))
                .thenReturn(buildResponse(AppointmentStatus.COMPLETED));

        mockMvc.perform(put("/api/appointments/{id}/complete", appointmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(appointmentId.toString()));
    }

    @Test
    @DisplayName("PUT /api/appointments/{id}/no-show - debe marcar inasistencia y retornar 200")
    void markAsNoShow_shouldReturn200() throws Exception {
        when(appointmentService.markAsNoShow(appointmentId)).thenReturn(buildResponse(AppointmentStatus.NO_SHOW));

        mockMvc.perform(put("/api/appointments/{id}/no-show", appointmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NO_SHOW"));
    }
}
