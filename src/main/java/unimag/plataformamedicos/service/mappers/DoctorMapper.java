package unimag.plataformamedicos.service.mappers;

import unimag.plataformamedicos.api.dtos.DoctorDtos;
import unimag.plataformamedicos.domine.entities.Doctor;
import unimag.plataformamedicos.domine.entities.Specialty;

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

    public static Doctor toEntity(DoctorDtos.CreateDoctorRequest request, Specialty specialty) {
        return Doctor.builder()
                .name(request.name())
                .licenceNumber(request.licenceNumber())
                .email(request.email())
                .specialty(specialty)
                .build();
    }

    public static void patch(Doctor doctor, DoctorDtos.UpdateDoctorRequest request, Specialty specialty) {
        if (request.name() != null) {
            doctor.setName(request.name());
        }
        if (request.email() != null) {
            doctor.setEmail(request.email());
        }
        if (specialty != null) {
            doctor.setSpecialty(specialty);
        }
        if (request.active() != null) {
            doctor.setActive(request.active());
        }
    }
}
