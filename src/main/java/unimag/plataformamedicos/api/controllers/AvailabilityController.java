package unimag.plataformamedicos.api.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unimag.plataformamedicos.api.dtos.ReportDtos.AvailabilitySlotResponse;
import unimag.plataformamedicos.service.interfaces.AvailabilityService;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    // GET /api/availability/doctors/{doctorId}?date=2025-04-20&appointmentTypeId=...
    @GetMapping("/doctors/{doctorId}")
    public ResponseEntity<List<AvailabilitySlotResponse>> getAvailableSlots(
            @PathVariable UUID doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam UUID appointmentTypeId) {
        return ResponseEntity.ok(availabilityService.getAvailableSlots(doctorId, date, appointmentTypeId));
    }
}
