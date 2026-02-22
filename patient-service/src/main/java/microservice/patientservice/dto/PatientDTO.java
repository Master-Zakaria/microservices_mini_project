package microservice.patientservice.dto;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class PatientDTO {
    private Long id;
    private String name;
    private String firstName;
    private LocalDate birthDate;
    private String contact;
}
