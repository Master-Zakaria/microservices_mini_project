package microservice.patientservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import microservice.patientservice.models.Patient;

@Repository
public interface IPatientRepository extends JpaRepository<Patient, Long> {
}
