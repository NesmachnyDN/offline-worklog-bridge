package io.github.nesmachnydn.worklogbridge.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_publish_idempotency", columnNames = "idempotency_key"))
public class PublishRecordEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String snapshotId;
    private LocalDate workDate;
    private int minutes;
    @Column(name = "entry_comment", length = 4000) private String comment;
    @Column(name = "idempotency_key") private String idempotencyKey;
    private String destinationName;
    private String destinationReference;
    private Instant publishedAt;
    protected PublishRecordEntity() {}
    public PublishRecordEntity(String snapshotId, LocalDate workDate, int minutes, String comment, String idempotencyKey, String destinationName, String destinationReference, Instant publishedAt) {
        this.snapshotId = snapshotId; this.workDate = workDate; this.minutes = minutes; this.comment = comment; this.idempotencyKey = idempotencyKey; this.destinationName = destinationName; this.destinationReference = destinationReference; this.publishedAt = publishedAt;
    }
    public LocalDate getWorkDate() { return workDate; }
    public int getMinutes() { return minutes; }
    public String getComment() { return comment; }
    public String getDestinationReference() { return destinationReference; }
    public Instant getPublishedAt() { return publishedAt; }
}
