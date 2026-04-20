package unimag.plataformamedicos.api.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unimag.plataformamedicos.api.dtos.ReportDtos.*;
import unimag.plataformamedicos.service.interfaces.ReportService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // GET /api/reports/office-occupancy?start=2025-04-01T00:00:00&end=2025-04-30T23:59:59
    @GetMapping("/office-occupancy")
    public ResponseEntity<List<OfficeOccupancyResponse>> getOfficeOccupancy(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(reportService.getOfficeOccupancy(start, end));
    }

    // GET /api/reports/doctor-productivity
    @GetMapping("/doctor-productivity")
    public ResponseEntity<List<DoctorProductivityResponse>> getDoctorProductivity() {
        return ResponseEntity.ok(reportService.getDoctorProductivity());
    }

    // GET /api/reports/no-show-patients?start=2025-04-01T00:00:00&end=2025-04-30T23:59:59
    @GetMapping("/no-show-patients")
    public ResponseEntity<List<NoShowPatientResponse>> getNoShowPatients(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(reportService.getNoShowPatients(start, end));
    }
}
