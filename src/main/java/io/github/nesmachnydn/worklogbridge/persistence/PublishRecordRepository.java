package io.github.nesmachnydn.worklogbridge.persistence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface PublishRecordRepository extends JpaRepository<PublishRecordEntity, Long> {
    Optional<PublishRecordEntity> findByIdempotencyKey(String idempotencyKey);
    List<PublishRecordEntity> findTop50ByOrderByPublishedAtDesc();
}
