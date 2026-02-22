package microservice.medicalrecordservice.services;

import org.springframework.stereotype.Service;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import microservice.medicalrecordservice.clients.PatientClient;
import microservice.medicalrecordservice.dto.PatientDTO;
import microservice.medicalrecordservice.exceptions.PatientServiceUnavailableException;

@Service
public class PatientLookupService {
    private final PatientClient patientClient;

    public PatientLookupService(PatientClient patientClient) {
        this.patientClient = patientClient;
    }

    @CircuitBreaker(name = "patientService", fallbackMethod = "getPatientByIdFallback")
    public PatientDTO getPatientById(Long patientId) {
        return patientClient.getPatientById(patientId);
    }

    private PatientDTO getPatientByIdFallback(Long patientId, Throwable throwable) {
        if (throwable instanceof FeignException.NotFound) {
            throw (FeignException.NotFound) throwable;
        }
        throw new PatientServiceUnavailableException(
                "Patient service is unavailable while fetching patient id=" + patientId,
                throwable);
    }
}
