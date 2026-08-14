package io.github.nesmachnydn.worklogbridge.service;

import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HexFormat;

@Service
public class IdempotencyKeyService {
    public String forEntry(String snapshotId, LocalDate date, int minutes, String comment) {
        String canonical = snapshotId + "\n" + date + "\n" + minutes + "\n" + comment;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required by the Java runtime", e);
        }
    }
}
