package vn.edu.se10.bluemoon.backup;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RestoreArtifactGuardTest {
    @TempDir Path temporaryDirectory;

    private Path bundle() throws Exception {
        Path bundle = Files.createDirectory(temporaryDirectory.resolve("bundle"));
        byte[] dump = "-- MySQL dump 8.4\n-- Dump completed\n".getBytes(StandardCharsets.UTF_8);
        Files.write(bundle.resolve("dump.sql"), dump);
        String checksum = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(dump));
        Files.writeString(bundle.resolve("manifest.json"), "{\"sha256\":\"" + checksum
                + "\",\"table_count\":0,\"total_row_count\":0}");
        return bundle;
    }

    private ProcessBuilder restore(Path bundle, String target) {
        ProcessBuilder process = new ProcessBuilder("bash", "../scripts/restore-rehearsal.sh",
                "--bundle", bundle.toString(), "--target-db", target,
                "--target-user", "fixture", "--target-password", "fixture-password",
                "--evidence-out", temporaryDirectory.resolve("evidence.json").toString(), "--skip-cleanup");
        process.redirectErrorStream(true);
        process.environment().put("MOCK_RESTORE_MODE", "true");
        process.environment().put("BACKUP_LOCK_FILE", temporaryDirectory.resolve("lock").toString());
        return process;
    }

    @Test
    void incompleteBundleMustNotProduceSuccessfulRestoreEvidence() throws Exception {
        Process process = restore(bundle(), "bluemoon_restore_s13_fixture").start();
        process.getInputStream().readAllBytes();
        assertNotEquals(0, process.waitFor(), "bundle without COMPLETED must be rejected");
        assertFalse(Files.exists(temporaryDirectory.resolve("evidence.json")));
    }

    @Test
    void targetNameContainingSqlSyntaxMustBeRejectedBeforeEvidence() throws Exception {
        Path bundle = bundle();
        Files.writeString(bundle.resolve("COMPLETED"), "");
        Process process = restore(bundle, "bluemoon_restore_s13_x' OR '1'='1").start();
        process.getInputStream().readAllBytes();
        assertNotEquals(0, process.waitFor(), "target name must be a safe schema identifier");
        assertFalse(Files.exists(temporaryDirectory.resolve("evidence.json")));
    }

    @Test
    void unknownOptionDoesNotEchoSecret() throws Exception {
        String canary = "fixture-secret-canary";
        ProcessBuilder builder = new ProcessBuilder("bash", "../scripts/restore-rehearsal.sh", "--" + canary);
        builder.redirectErrorStream(true);
        Process process = builder.start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        assertNotEquals(0, process.waitFor());
        assertFalse(output.contains(canary));
    }
}
