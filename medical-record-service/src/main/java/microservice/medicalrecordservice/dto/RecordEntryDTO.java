package microservice.medicalrecordservice.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import microservice.medicalrecordservice.models.RecordEntryType;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecordEntryDTO {
    private Long id;
    private Long recordId;
    private LocalDate date;
    private RecordEntryType type;
    private String content;
}
