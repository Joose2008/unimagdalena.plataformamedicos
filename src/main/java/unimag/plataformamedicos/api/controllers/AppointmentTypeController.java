package unimag.plataformamedicos.api.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unimag.plataformamedicos.api.dtos.AppointmentTypeDtos.*;
import unimag.plataformamedicos.service.interfaces.AppointmentTypeService;

import java.util.List;

@RestController
@RequestMapping("/api/appointment-types")
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
}
