package microservice.medicalrecordservice.exceptions;

public class PatientServiceUnavailableException extends RuntimeException {
    public PatientServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
