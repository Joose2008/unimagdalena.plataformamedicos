package unimag.plataformamedicos.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unimag.plataformamedicos.api.dtos.ReportDtos;
import unimag.plataformamedicos.domine.repositories.AppointmentRepository;
import unimag.plataformamedicos.service.interfaces.ReportService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class ReportServiceImpl implements ReportService {

    private final AppointmentRepository appointmentRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ReportDtos.OfficeOccupancyResponse> getOfficeOccupancy(LocalDateTime start, LocalDateTime end) {
        Long totalMinutes = ChronoUnit.MINUTES.between(start, end);

        return appointmentRepository.sumOccupiedMinutesByOffice(start,end)
                .stream().map(oo -> new ReportDtos.OfficeOccupancyResponse(
                        oo.office().getName(),
                        oo.sumOccupiedMinutes(),
                        totalMinutes,
                        totalMinutes > 0 ? (oo.sumOccupiedMinutes()*100.0 / totalMinutes) : 0.0
                )).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportDtos.DoctorProductivityResponse> getDoctorProductivity() {
        return appointmentRepository.rankDoctorByAppointment().stream().map(
                pd -> new ReportDtos.DoctorProductivityResponse(
                        pd.doctor().getName(),
                        pd.doctor().getSpecialty().getName(),
                        pd.countCompletedAppointment()
                )).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportDtos.NoShowPatientResponse> getNoShowPatients(LocalDateTime start, LocalDateTime end) {
        return appointmentRepository.rankPatientByStatusNoShow(start,end).stream()
                .map(ns -> new ReportDtos.NoShowPatientResponse(
                        ns.patient().getName(),
                        ns.patient().getDocumentNumber(),
                        ns.countNoShow()
                )).toList();
    }
}
