package unimag.plataformamedicos.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import unimag.plataformamedicos.api.dtos.DoctorScheduleDtos;
import unimag.plataformamedicos.domine.entities.Doctor;
import unimag.plataformamedicos.domine.repositories.DoctorRepository;
import unimag.plataformamedicos.domine.repositories.DoctorScheduleRepository;
import unimag.plataformamedicos.exception.ResourceNotFoundException;
import unimag.plataformamedicos.service.interfaces.DoctorScheduleService;
import unimag.plataformamedicos.service.mappers.DoctorScheduleMapper;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DoctorScheduleServiceImpl implements DoctorScheduleService {

    private final DoctorScheduleRepository doctorScheduleRepository;
    private final DoctorRepository doctorRepository;

    @Override
    @Transactional
    public DoctorScheduleDtos.DoctorScheduleResponse create(UUID doctorId, DoctorScheduleDtos.CreateDoctorScheduleRequest request) {

        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor with id %s not found".formatted(doctorId)));
        var doctorSchedule = DoctorScheduleMapper.toEntity(request, doctor);
        var  doctorScheduleSaved =  doctorScheduleRepository.save(doctorSchedule);
        return DoctorScheduleMapper.toResponse(doctorScheduleSaved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorScheduleDtos.DoctorScheduleResponse> findByDoctor(UUID doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor with id %s not found".formatted(doctorId)));

        return doctorScheduleRepository.findByDoctor(doctor).stream().map(DoctorScheduleMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public List<DoctorScheduleDtos.DoctorScheduleResponse> findDoctorScheduleByDoctorAndDayOfWeek(UUID doctorId, DayOfWeek dayOfWeek){
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor %s not found".formatted(doctorId)));

        return doctorScheduleRepository.findDoctorScheduleByDoctorAndDayOfWeek(doctor,dayOfWeek).stream().map(DoctorScheduleMapper::toResponse).toList();
    }
}