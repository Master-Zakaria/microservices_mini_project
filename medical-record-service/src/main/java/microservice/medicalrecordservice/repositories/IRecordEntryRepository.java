package microservice.medicalrecordservice.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import microservice.medicalrecordservice.models.RecordEntry;

@Repository
public interface IRecordEntryRepository extends JpaRepository<RecordEntry, Long> {
    List<RecordEntry> findByRecordIdOrderByDateDescIdDesc(Long recordId);
}
