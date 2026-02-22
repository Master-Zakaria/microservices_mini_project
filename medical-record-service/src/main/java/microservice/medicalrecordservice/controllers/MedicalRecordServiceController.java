package microservice.medicalrecordservice.controllers;

import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import feign.FeignException;
import microservice.medicalrecordservice.dto.CreateMedicalRecordRequestDTO;
import microservice.medicalrecordservice.dto.CreateRecordEntryRequestDTO;
import microservice.medicalrecordservice.dto.MedicalRecordDTO;
import microservice.medicalrecordservice.dto.RecordEntryDTO;
import microservice.medicalrecordservice.exceptions.PatientServiceUnavailableException;
import microservice.medicalrecordservice.services.MedicalRecordService;

@RestController
@RequestMapping("/api/v1/medical-records")
public class MedicalRecordServiceController {
    private final MedicalRecordService medicalRecordService;

    public MedicalRecordServiceController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }

    @PostMapping
    public ResponseEntity<MedicalRecordDTO> createMedicalRecord(@RequestBody CreateMedicalRecordRequestDTO request) {
        try {
            MedicalRecordDTO created = medicalRecordService.createMedicalRecord(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (FeignException.NotFound e) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (PatientServiceUnavailableException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<MedicalRecordDTO> getByPatientId(@PathVariable Long patientId) {
        try {
            return ResponseEntity.ok(medicalRecordService.getMedicalRecordByPatientId(patientId));
        } catch (FeignException.NotFound e) {
            return ResponseEntity.notFound().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (PatientServiceUnavailableException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    @PostMapping("/patient/{patientId}/entries")
    public ResponseEntity<RecordEntryDTO> addEntry(@PathVariable Long patientId,
            @RequestBody CreateRecordEntryRequestDTO request) {
        try {
            RecordEntryDTO created = medicalRecordService.addEntryByPatientId(patientId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (PatientServiceUnavailableException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    @GetMapping("/{recordId}")
    public ResponseEntity<MedicalRecordDTO> getByRecordId(@PathVariable Long recordId) {
        try {
            return ResponseEntity.ok(medicalRecordService.getMedicalRecordById(recordId));
        } catch (FeignException.NotFound e) {
            return ResponseEntity.notFound().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (PatientServiceUnavailableException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
}
