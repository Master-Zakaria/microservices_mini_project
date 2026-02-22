package microservice.medicalrecordservice.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import microservice.medicalrecordservice.models.MedicalRecord;

@Repository
public interface IMedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    Optional<MedicalRecord> findByPatientId(Long patientId);

    boolean existsByPatientId(Long patientId);
}
