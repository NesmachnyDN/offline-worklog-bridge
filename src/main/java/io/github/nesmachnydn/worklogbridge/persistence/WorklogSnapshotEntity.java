package io.github.nesmachnydn.worklogbridge.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.Instant;
import java.time.LocalDate;

@Entity
public class WorklogSnapshotEntity {
    @Id private String id;
    private LocalDate startDate;
    private LocalDate endDate;
    private Instant capturedAt;
    private String sourceName;
    protected WorklogSnapshotEntity() {}
    public WorklogSnapshotEntity(String id, LocalDate startDate, LocalDate endDate, Instant capturedAt, String sourceName) {
        this.id = id; this.startDate = startDate; this.endDate = endDate; this.capturedAt = capturedAt; this.sourceName = sourceName;
    }
    public String getId() { return id; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public Instant getCapturedAt() { return capturedAt; }
    public String getSourceName() { return sourceName; }
}
