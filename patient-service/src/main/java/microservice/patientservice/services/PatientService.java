package microservice.patientservice.services;

import org.springframework.stereotype.Service;
import microservice.patientservice.repositories.IPatientRepository;
import microservice.patientservice.models.Patient;
import microservice.patientservice.dto.PatientDTO;
import java.util.List;

@Service
public class PatientService {
    private final IPatientRepository patientRepository;

    public PatientService(IPatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public Patient createPatient(PatientDTO patientDTO) {
        Patient patient = Patient.builder()
                .name(patientDTO.getName())
                .firstName(patientDTO.getFirstName())
                .birthDate(patientDTO.getBirthDate())
                .contact(patientDTO.getContact())
                .build();
        return patientRepository.save(patient);
    }

    public Patient getPatientById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
    }

    public Patient updatePatient(Long id, PatientDTO patientDTO) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        patient.setName(patientDTO.getName());
        patient.setFirstName(patientDTO.getFirstName());
        patient.setBirthDate(patientDTO.getBirthDate());
        patient.setContact(patientDTO.getContact());
        return patientRepository.save(patient);
    }

    public void deletePatient(Long id) {
        patientRepository.deleteById(id);
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }
}
