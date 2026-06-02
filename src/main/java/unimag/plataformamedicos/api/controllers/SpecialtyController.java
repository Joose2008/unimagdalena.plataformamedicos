package unimag.plataformamedicos.api.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import unimag.plataformamedicos.api.dtos.SpecialtyDtos.*;
import unimag.plataformamedicos.service.interfaces.SpecialtyService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/specialties", "/especialidades"})
@RequiredArgsConstructor
public class SpecialtyController {

    private final SpecialtyService specialtyService;

    @PostMapping
    public ResponseEntity<SpecialtyResponse> create(@RequestBody CreateSpecialtyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(specialtyService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<SpecialtyResponse>> findAll() {
        return ResponseEntity.ok(specialtyService.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<SpecialtyResponse> update(
            @PathVariable UUID id,
            @RequestBody UpdateSpecialtyRequest request) {
        return ResponseEntity.ok(specialtyService.update(id, request));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        specialtyService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
