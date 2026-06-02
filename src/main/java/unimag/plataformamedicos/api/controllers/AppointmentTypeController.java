package unimag.plataformamedicos.api.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unimag.plataformamedicos.api.dtos.AppointmentTypeDtos.*;
import unimag.plataformamedicos.service.interfaces.AppointmentTypeService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/appointment-types", "/tipos-cita"})
@RequiredArgsConstructor
public class AppointmentTypeController {

    private final AppointmentTypeService appointmentTypeService;

    @PostMapping
    public ResponseEntity<AppointmentTypeResponse> create(@RequestBody CreateAppointmentTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentTypeService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<AppointmentTypeResponse>> findAll() {
        return ResponseEntity.ok(appointmentTypeService.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentTypeResponse> update(
            @PathVariable UUID id,
            @RequestBody UpdateAppointmentTypeRequest request) {
        return ResponseEntity.ok(appointmentTypeService.update(id, request));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        appointmentTypeService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
