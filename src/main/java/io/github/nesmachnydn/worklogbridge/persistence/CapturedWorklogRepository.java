package io.github.nesmachnydn.worklogbridge.persistence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface CapturedWorklogRepository extends JpaRepository<CapturedWorklogEntity, Long> { List<CapturedWorklogEntity> findBySnapshotIdOrderByWorkDateAscIdAsc(String snapshotId); }
