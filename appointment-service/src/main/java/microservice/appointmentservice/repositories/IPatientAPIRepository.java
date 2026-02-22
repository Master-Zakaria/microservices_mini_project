package microservice.appointmentservice.repositories;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import microservice.appointmentservice.dto.PatientDTO;

@FeignClient(name = "patient-service")
public interface IPatientAPIRepository {

    @GetMapping("/api/v1/patients/{patientId}")
    PatientDTO getPatientById(@PathVariable("patientId") Long id);
}
