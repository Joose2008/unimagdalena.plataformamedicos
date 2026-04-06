package unimag.plataformamedicos.service.mappers;

import unimag.plataformamedicos.api.dtos.DoctorDtos;
import unimag.plataformamedicos.domine.entities.Doctor;

public class DoctorMapper {

    public static DoctorDtos.DoctorResponse toResponse(Doctor doctor) {
        return new DoctorDtos.DoctorResponse(
                doctor.getId(),
                doctor.getName(),
                doctor.getLicenceNumber(),
                doctor.getEmail(),
                doctor.getActive(),
                SpecialtyMapper.toResponse(doctor.getSpecialty())
        );
    }

    public static DoctorDtos.DoctorSummaryResponse toSummaryResponse(Doctor doctor) {
        return new DoctorDtos.DoctorSummaryResponse(
                doctor.getId(),
                doctor.getName(),
                doctor.getSpecialty().getName()
        );
    }

    public static Doctor toEntity(DoctorDtos.CreateDoctorRequest request) {
        return Doctor.builder()
                .name(request.name())
                .licenceNumber(request.licenceNumber())
                .email(request.email())
                .build();
    }

    public static void patch(Doctor doctor, DoctorDtos.UpdateDoctorRequest request) {
        if (request.name() != null) {
            doctor.setName(request.name());
        }
        if (request.email() != null) {
            doctor.setEmail(request.email());
        }
        if (request.active() != null) {
            doctor.setActive(request.active());
        }
    }
}
