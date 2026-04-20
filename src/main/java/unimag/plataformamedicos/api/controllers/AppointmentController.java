package unimag.plataformamedicos.api.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unimag.plataformamedicos.api.dtos.AppointmentDtos.*;
import unimag.plataformamedicos.service.interfaces.AppointmentService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<AppointmentResponse> create(@RequestBody CreateAppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> findAll() {
        return ResponseEntity.ok(appointmentService.findAll());
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<AppointmentResponse> confirm(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.confirm(id));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponse> cancel(
            @PathVariable UUID id,
            @RequestBody CancelAppointmentRequest request) {
        return ResponseEntity.ok(appointmentService.cancel(id, request));
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<AppointmentResponse> complete(
            @PathVariable UUID id,
            @RequestBody(required = false) CompleteAppointmentRequest request) {
        String observations = request != null ? request.observations() : null;
        return ResponseEntity.ok(appointmentService.complete(id, observations));
    }

    @PutMapping("/{id}/no-show")
    public ResponseEntity<AppointmentResponse> markAsNoShow(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.markAsNoShow(id));
    }
}
