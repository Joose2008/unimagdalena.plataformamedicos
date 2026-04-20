package unimag.plataformamedicos.api.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unimag.plataformamedicos.api.dtos.DoctorDtos.*;
import unimag.plataformamedicos.service.interfaces.DoctorService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @PostMapping
    public ResponseEntity<DoctorResponse> create(@RequestBody CreateDoctorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(doctorService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(doctorService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<DoctorResponse>> findAll() {
        return ResponseEntity.ok(doctorService.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<DoctorResponse> update(
            @PathVariable UUID id,
            @RequestBody UpdateDoctorRequest request) {
        return ResponseEntity.ok(doctorService.update(id, request));
    }

    @PatchMapping("/{id}/licence")
    public ResponseEntity<DoctorResponse> updateLicence(
            @PathVariable UUID id,
            @RequestBody UpdateDoctorLicenceRequest request) {
        return ResponseEntity.ok(doctorService.update(id, request));
    }
}
