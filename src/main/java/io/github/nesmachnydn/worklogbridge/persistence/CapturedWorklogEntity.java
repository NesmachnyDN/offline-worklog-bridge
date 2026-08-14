package io.github.nesmachnydn.worklogbridge.persistence;

import io.github.nesmachnydn.worklogbridge.domain.CapturedWorklog;
import io.github.nesmachnydn.worklogbridge.domain.WorklogCategory;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(indexes = @Index(name = "idx_worklog_snapshot", columnList = "snapshot_id"))
public class CapturedWorklogEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "snapshot_id") private String snapshotId;
    private String sourceWorklogId;
    private String issueKey;
    private String issueTitle;
    private LocalDate workDate;
    private int minutes;
    @Enumerated(EnumType.STRING) private WorklogCategory category;
    protected CapturedWorklogEntity() {}
    public CapturedWorklogEntity(String snapshotId, CapturedWorklog worklog) {
        this.snapshotId = snapshotId; this.sourceWorklogId = worklog.sourceWorklogId(); this.issueKey = worklog.issueKey(); this.issueTitle = worklog.issueTitle(); this.workDate = worklog.workDate(); this.minutes = worklog.minutes(); this.category = worklog.category();
    }
    public CapturedWorklog toDomain() { return new CapturedWorklog(sourceWorklogId, issueKey, issueTitle, workDate, minutes, category); }
    public String getSnapshotId() { return snapshotId; }
    public LocalDate getWorkDate() { return workDate; }
}
