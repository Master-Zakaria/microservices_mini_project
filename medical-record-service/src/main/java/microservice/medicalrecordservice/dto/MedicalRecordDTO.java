package microservice.medicalrecordservice.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordDTO {
    private Long id;
    private Long patientId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String bloodType;
    private String allergies;
    private PatientDTO patient;
    private List<RecordEntryDTO> entries;
}
