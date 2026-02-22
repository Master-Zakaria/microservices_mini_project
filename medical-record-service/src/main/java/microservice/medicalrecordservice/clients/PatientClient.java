package microservice.medicalrecordservice.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import microservice.medicalrecordservice.dto.PatientDTO;

@FeignClient(name = "patient-service")
public interface PatientClient {

    @GetMapping("/api/v1/patients/{patientId}")
    PatientDTO getPatientById(@PathVariable("patientId") Long patientId);
}
