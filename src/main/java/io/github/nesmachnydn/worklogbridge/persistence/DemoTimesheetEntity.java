package io.github.nesmachnydn.worklogbridge.persistence;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_demo_destination_idempotency", columnNames = "idempotency_key"))
public class DemoTimesheetEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private LocalDate workDate;
    private int minutes;
    @Column(name = "entry_comment", length = 4000) private String comment;
    @Column(name = "idempotency_key") private String idempotencyKey;
    private String externalReference;
    protected DemoTimesheetEntity() {}
    public DemoTimesheetEntity(LocalDate workDate, int minutes, String comment, String idempotencyKey, String externalReference) {
        this.workDate = workDate; this.minutes = minutes; this.comment = comment; this.idempotencyKey = idempotencyKey; this.externalReference = externalReference;
    }
    public String getExternalReference() { return externalReference; }
}
