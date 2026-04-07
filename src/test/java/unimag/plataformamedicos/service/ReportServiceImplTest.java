package unimag.plataformamedicos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import unimag.plataformamedicos.api.dtos.ReportDtos;
import unimag.plataformamedicos.api.dtos.query.DoctorAppointment;
import unimag.plataformamedicos.api.dtos.query.OfficeOccupancy;
import unimag.plataformamedicos.api.dtos.query.PatientCountStatus;
import unimag.plataformamedicos.domine.entities.*;
import unimag.plataformamedicos.domine.repositories.AppointmentRepository;
import unimag.plataformamedicos.service.impl.ReportServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock AppointmentRepository appointmentRepository;
    @InjectMocks ReportServiceImpl service;

    private Doctor doctor;
    private Office office;
    private Patient patient;
    private Specialty specialty;

    private final LocalDateTime START = LocalDateTime.now().minusDays(30);
    private final LocalDateTime END   = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        specialty = Specialty.builder().name("Medicina General").build();

        doctor = Doctor.builder()
                .name("Dr. Pérez")
                .licenceNumber("LIC-1")
                .email("perez@test.com")
                .active(true)
                .specialty(specialty)
                .build();

        office = Office.builder()
                .name("Consultorio 1")
                .location("Piso 1")
                .build();

        patient = Patient.builder()
                .name("Juan Pérez")
                .documentNumber("123456")
                .email("juan@test.com")
                .build();
    }

    // -----------------------------------------------------------------------
    // getOfficeOccupancy
    // -----------------------------------------------------------------------

    @Test
    void shouldReturnOfficeOccupancyWithPercentage() {
        // Rango de 60 minutos totales, 30 ocupados = 50%
        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end   = LocalDateTime.now();

        OfficeOccupancy occupancy = new OfficeOccupancy(office, 30L);
        when(appointmentRepository.sumOccupiedMinutesByOffice(start, end))
                .thenReturn(List.of(occupancy));

        List<ReportDtos.OfficeOccupancyResponse> result = service.getOfficeOccupancy(start, end);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Consultorio 1", result.get(0).officeName());
        assertEquals(30L, result.get(0).occupiedMinutes());
        assertEquals(50.0, result.get(0).occupancyPercentage(), 0.01);
    }

    @Test
    void shouldReturnZeroPercentageWhenNoOccupancy() {
        OfficeOccupancy occupancy = new OfficeOccupancy(office, 0L);
        when(appointmentRepository.sumOccupiedMinutesByOffice(any(), any()))
                .thenReturn(List.of(occupancy));

        List<ReportDtos.OfficeOccupancyResponse> result = service.getOfficeOccupancy(START, END);

        assertNotNull(result);
        assertEquals(0.0, result.get(0).occupancyPercentage(), 0.01);
    }

    @Test
    void shouldReturnEmptyListWhenNoOccupancyData() {
        when(appointmentRepository.sumOccupiedMinutesByOffice(any(), any()))
                .thenReturn(List.of());

        List<ReportDtos.OfficeOccupancyResponse> result = service.getOfficeOccupancy(START, END);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // -----------------------------------------------------------------------
    // getDoctorProductivity
    // -----------------------------------------------------------------------

    @Test
    void shouldReturnDoctorProductivityRanking() {
        DoctorAppointment productivity = new DoctorAppointment(doctor, 10L);
        when(appointmentRepository.rankDoctorByAppointment())
                .thenReturn(List.of(productivity));

        List<ReportDtos.DoctorProductivityResponse> result = service.getDoctorProductivity();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Dr. Pérez", result.get(0).doctorName());
        assertEquals("Medicina General", result.get(0).specialtyName());
        assertEquals(10L, result.get(0).completedAppointments());
    }

    @Test
    void shouldReturnEmptyListWhenNoCompletedAppointments() {
        when(appointmentRepository.rankDoctorByAppointment()).thenReturn(List.of());

        List<ReportDtos.DoctorProductivityResponse> result = service.getDoctorProductivity();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // -----------------------------------------------------------------------
    // getNoShowPatients
    // -----------------------------------------------------------------------

    @Test
    void shouldReturnNoShowPatientRanking() {
        PatientCountStatus noShow = new PatientCountStatus(patient, 3L);
        when(appointmentRepository.rankPatientByStatusNoShow(START, END))
                .thenReturn(List.of(noShow));

        List<ReportDtos.NoShowPatientResponse> result = service.getNoShowPatients(START, END);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Juan Pérez", result.get(0).patientName());
        assertEquals("123456", result.get(0).documentNumber());
        assertEquals(3L, result.get(0).noShowCount());
    }

    @Test
    void shouldReturnEmptyListWhenNoNoShows() {
        when(appointmentRepository.rankPatientByStatusNoShow(any(), any()))
                .thenReturn(List.of());

        List<ReportDtos.NoShowPatientResponse> result = service.getNoShowPatients(START, END);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldPassCorrectDateRangeToRepository() {
        when(appointmentRepository.rankPatientByStatusNoShow(START, END))
                .thenReturn(List.of());

        service.getNoShowPatients(START, END);

        verify(appointmentRepository).rankPatientByStatusNoShow(START, END);
    }
}
