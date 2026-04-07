package unimag.plataformamedicos.service.interfaces;

import unimag.plataformamedicos.api.dtos.ReportDtos.DoctorProductivityResponse;
import unimag.plataformamedicos.api.dtos.ReportDtos.NoShowPatientResponse;
import unimag.plataformamedicos.api.dtos.ReportDtos.OfficeOccupancyResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportService {
    List<OfficeOccupancyResponse> getOfficeOccupancy(LocalDateTime start, LocalDateTime end);
    List<DoctorProductivityResponse> getDoctorProductivity();
    List<NoShowPatientResponse> getNoShowPatients(LocalDateTime start, LocalDateTime end);
}
