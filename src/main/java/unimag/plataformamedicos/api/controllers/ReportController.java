package unimag.plataformamedicos.api.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MissingServletRequestParameterException;
import unimag.plataformamedicos.api.dtos.ReportDtos.*;
import unimag.plataformamedicos.service.interfaces.ReportService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/office-occupancy")
    public ResponseEntity<List<OfficeOccupancyResponse>> getOfficeOccupancy(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(reportService.getOfficeOccupancy(start, end));
    }

    @GetMapping("/doctor-productivity")
    public ResponseEntity<List<DoctorProductivityResponse>> getDoctorProductivity() {
        return ResponseEntity.ok(reportService.getDoctorProductivity());
    }

    @GetMapping("/no-show-patients")
    public ResponseEntity<List<NoShowPatientResponse>> getNoShowPatients(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(reportService.getNoShowPatients(start, end));
    }

    // Manejador para cuando faltan parámetros requeridos (devuelve 400 en lugar de 500)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleMissingParams(MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", "Parámetro requerido faltante: " + ex.getParameterName(),
                "message", ex.getMessage()
        ));
    }
}