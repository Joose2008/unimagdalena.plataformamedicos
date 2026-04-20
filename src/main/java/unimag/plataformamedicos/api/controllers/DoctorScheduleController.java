package unimag.plataformamedicos.api.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unimag.plataformamedicos.api.dtos.DoctorScheduleDtos.*;
import unimag.plataformamedicos.service.interfaces.DoctorScheduleService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/doctors/{doctorId}/schedules")
@RequiredArgsConstructor
public class DoctorScheduleController {

    private final DoctorScheduleService doctorScheduleService;

    @PostMapping
    public ResponseEntity<DoctorScheduleResponse> create(
            @PathVariable UUID doctorId,
            @RequestBody CreateDoctorScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(doctorScheduleService.create(doctorId, request));
    }

    @GetMapping
    public ResponseEntity<List<DoctorScheduleResponse>> findByDoctor(@PathVariable UUID doctorId) {
        return ResponseEntity.ok(doctorScheduleService.findByDoctor(doctorId));
    }
}
