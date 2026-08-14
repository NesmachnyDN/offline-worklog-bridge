package io.github.nesmachnydn.worklogbridge.persistence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface DemoTimesheetRepository extends JpaRepository<DemoTimesheetEntity, Long> { Optional<DemoTimesheetEntity> findByIdempotencyKey(String idempotencyKey); }
