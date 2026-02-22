package microservice.appointmentservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import microservice.appointmentservice.dto.PatientDTO;
import java.time.LocalDate;
import java.time.LocalTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class AppointmentDTO {
    private Long id;

    private LocalDate date;

    private LocalTime time;

    private PatientDTO patient;

    private Long patientId;
}
