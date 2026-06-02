package unimag.plataformamedicos.api.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import unimag.plataformamedicos.api.dtos.OfficeDtos.*;
import unimag.plataformamedicos.service.interfaces.OfficeService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/offices")
@RequiredArgsConstructor
public class OfficeController {

    private final OfficeService officeService;

    @PostMapping
    public ResponseEntity<OfficeResponse> create(@RequestBody CreateOfficeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(officeService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<OfficeResponse>> findAll() {
        return ResponseEntity.ok(officeService.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<OfficeResponse> update(
            @PathVariable UUID id,
            @RequestBody UpdateOfficeRequest request) {
        return ResponseEntity.ok(officeService.update(id, request));
    }

    // Manejador para errores de deserialización (ej. valor de enum inválido)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return Map.of("error", "Solicitud mal formada: " + ex.getMessage());
    }
}