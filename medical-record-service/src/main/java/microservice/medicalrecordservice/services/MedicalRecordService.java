package microservice.medicalrecordservice.services;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import microservice.medicalrecordservice.dto.CreateMedicalRecordRequestDTO;
import microservice.medicalrecordservice.dto.CreateRecordEntryRequestDTO;
import microservice.medicalrecordservice.dto.MedicalRecordDTO;
import microservice.medicalrecordservice.dto.PatientDTO;
import microservice.medicalrecordservice.dto.RecordEntryDTO;
import microservice.medicalrecordservice.models.MedicalRecord;
import microservice.medicalrecordservice.models.RecordEntry;
import microservice.medicalrecordservice.repositories.IMedicalRecordRepository;
import microservice.medicalrecordservice.repositories.IRecordEntryRepository;

@Service
public class MedicalRecordService {
    private final IMedicalRecordRepository medicalRecordRepository;
    private final IRecordEntryRepository recordEntryRepository;
    private final PatientLookupService patientLookupService;

    public MedicalRecordService(IMedicalRecordRepository medicalRecordRepository,
            IRecordEntryRepository recordEntryRepository,
            PatientLookupService patientLookupService) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.recordEntryRepository = recordEntryRepository;
        this.patientLookupService = patientLookupService;
    }

    public MedicalRecordDTO createMedicalRecord(CreateMedicalRecordRequestDTO request) {
        if (request == null || request.getPatientId() == null) {
            throw new IllegalArgumentException("patientId is required");
        }

        patientLookupService.getPatientById(request.getPatientId());

        if (medicalRecordRepository.existsByPatientId(request.getPatientId())) {
            throw new IllegalStateException("Medical record already exists for patient");
        }

        MedicalRecord saved = medicalRecordRepository.save(MedicalRecord.builder()
                .patientId(request.getPatientId())
                .bloodType(request.getBloodType())
                .allergies(request.getAllergies())
                .build());

        PatientDTO patient = patientLookupService.getPatientById(saved.getPatientId());
        return toMedicalRecordDTO(saved, patient);
    }

    public MedicalRecordDTO getMedicalRecordByPatientId(Long patientId) {
        if (patientId == null) {
            throw new IllegalArgumentException("patientId is required");
        }

        PatientDTO patient = patientLookupService.getPatientById(patientId);
        MedicalRecord record = medicalRecordRepository.findByPatientId(patientId)
                .orElseThrow(() -> new NoSuchElementException("Medical record not found"));

        return toMedicalRecordDTO(record, patient);
    }

    public MedicalRecordDTO getMedicalRecordById(Long recordId) {
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new NoSuchElementException("Medical record not found"));
        PatientDTO patient = patientLookupService.getPatientById(record.getPatientId());
        return toMedicalRecordDTO(record, patient);
    }

    public RecordEntryDTO addEntryByPatientId(Long patientId, CreateRecordEntryRequestDTO request) {
        if (patientId == null) {
            throw new IllegalArgumentException("patientId is required");
        }
        if (request == null || request.getType() == null || request.getContent() == null || request.getContent().isBlank()) {
            throw new IllegalArgumentException("type and content are required");
        }

        MedicalRecord record = medicalRecordRepository.findByPatientId(patientId)
                .orElseThrow(() -> new NoSuchElementException("Medical record not found"));

        RecordEntry saved = recordEntryRepository.save(RecordEntry.builder()
                .recordId(record.getId())
                .date(request.getDate() != null ? request.getDate() : LocalDate.now())
                .type(request.getType())
                .content(request.getContent())
                .build());

        return toRecordEntryDTO(saved);
    }

    private MedicalRecordDTO toMedicalRecordDTO(MedicalRecord record, PatientDTO patient) {
        List<RecordEntryDTO> entries = recordEntryRepository.findByRecordIdOrderByDateDescIdDesc(record.getId())
                .stream()
                .map(this::toRecordEntryDTO)
                .toList();

        return MedicalRecordDTO.builder()
                .id(record.getId())
                .patientId(record.getPatientId())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .bloodType(record.getBloodType())
                .allergies(record.getAllergies())
                .patient(patient)
                .entries(entries)
                .build();
    }

    private RecordEntryDTO toRecordEntryDTO(RecordEntry entry) {
        return RecordEntryDTO.builder()
                .id(entry.getId())
                .recordId(entry.getRecordId())
                .date(entry.getDate())
                .type(entry.getType())
                .content(entry.getContent())
                .build();
    }
}
