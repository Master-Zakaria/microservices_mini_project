package microservice.patientservice.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import microservice.patientservice.dto.PatientDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import microservice.patientservice.services.PatientService;
import microservice.patientservice.models.Patient;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

@RestController
@RequestMapping("/api/v1/patients")
public class PatientServiceController {

    private final PatientService patientService;

    public PatientServiceController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping("")
    public ResponseEntity<PatientDTO> createPatient(@RequestBody PatientDTO patientDTO) {
        Patient patient = patientService.createPatient(patientDTO);
        return ResponseEntity.ok(
                PatientDTO.builder()
                        .id(patient.getId())
                        .name(patient.getName())
                        .firstName(patient.getFirstName())
                        .birthDate(patient.getBirthDate())
                        .contact(patient.getContact())
                        .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientDTO> getPatientById(@PathVariable Long id) {
        try {
            Patient patient = patientService.getPatientById(id);
            return ResponseEntity.ok(
                    PatientDTO.builder()
                            .id(patient.getId())
                            .name(patient.getName())
                            .firstName(patient.getFirstName())
                            .birthDate(patient.getBirthDate())
                            .contact(patient.getContact())
                            .build());
        } catch (Exception e) {
            if (e.getMessage().equals("Patient not found"))
                return ResponseEntity.notFound().build();
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientDTO> updatePatient(@PathVariable Long id, @RequestBody PatientDTO patientDTO) {
        Patient patient = patientService.updatePatient(id, patientDTO);
        return ResponseEntity.ok(
                PatientDTO.builder()
                        .id(patient.getId())
                        .name(patient.getName())
                        .firstName(patient.getFirstName())
                        .birthDate(patient.getBirthDate())
                        .contact(patient.getContact())
                        .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("")
    public ResponseEntity<List<PatientDTO>> getAllPatients() {
        List<Patient> patients = patientService.getAllPatients();
        return ResponseEntity.ok(
                patients.stream()
                        .map(patient -> PatientDTO.builder()
                                .id(patient.getId())
                                .name(patient.getName())
                                .firstName(patient.getFirstName())
                                .birthDate(patient.getBirthDate())
                                .contact(patient.getContact())
                                .build())
                        .collect(Collectors.toList()));
    }
}
