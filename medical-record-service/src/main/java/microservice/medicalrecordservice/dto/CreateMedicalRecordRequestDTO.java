package microservice.medicalrecordservice.dto;

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
public class CreateMedicalRecordRequestDTO {
    private Long patientId;
    private String bloodType;
    private String allergies;
}
