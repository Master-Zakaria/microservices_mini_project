package microservice.appointmentservice.services;

import org.springframework.stereotype.Service;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import microservice.appointmentservice.dto.PatientDTO;
import microservice.appointmentservice.exceptions.PatientServiceUnavailableException;
import microservice.appointmentservice.repositories.IPatientAPIRepository;

@Service
public class PatientLookupService {
    private final IPatientAPIRepository patientAPIRepository;

    public PatientLookupService(IPatientAPIRepository patientAPIRepository) {
        this.patientAPIRepository = patientAPIRepository;
    }

    @CircuitBreaker(name = "patientService", fallbackMethod = "getPatientByIdFallback")
    public PatientDTO getPatientById(Long patientId) {
        return patientAPIRepository.getPatientById(patientId);
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
