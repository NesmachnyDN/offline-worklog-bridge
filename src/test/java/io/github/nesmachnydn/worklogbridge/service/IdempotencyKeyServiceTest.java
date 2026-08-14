package io.github.nesmachnydn.worklogbridge.service;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class IdempotencyKeyServiceTest {
    private final IdempotencyKeyService service = new IdempotencyKeyService();
    @Test void sameEntryProducesStableKey() {
        LocalDate day = LocalDate.of(2026, 8, 10);
        String first = service.forEntry("snapshot", day, 480, "DEV-101 — 8h");
        String second = service.forEntry("snapshot", day, 480, "DEV-101 — 8h");
        assertEquals(first, second); assertEquals(64, first.length());
    }
    @Test void changedCommentChangesKey() {
        LocalDate day = LocalDate.of(2026, 8, 10);
        assertNotEquals(service.forEntry("snapshot", day, 480, "A"), service.forEntry("snapshot", day, 480, "B"));
    }
}
