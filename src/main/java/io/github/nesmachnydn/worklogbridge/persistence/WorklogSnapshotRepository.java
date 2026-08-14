package io.github.nesmachnydn.worklogbridge.persistence;
import org.springframework.data.jpa.repository.JpaRepository;
public interface WorklogSnapshotRepository extends JpaRepository<WorklogSnapshotEntity, String> { WorklogSnapshotEntity findTopByOrderByCapturedAtDesc(); }
