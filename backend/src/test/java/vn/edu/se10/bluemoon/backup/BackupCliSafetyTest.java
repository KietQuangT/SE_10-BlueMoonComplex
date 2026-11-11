package vn.edu.se10.bluemoon.backup;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BackupCliSafetyTest {
    @TempDir Path temp;

    private record Result(int exit, String output) {}

    private Result run(String script, String... args) throws Exception {
        Path root = Path.of("..").toAbsolutePath().normalize();
        var command = new java.util.ArrayList<>(List.of("bash", root.resolve("scripts").resolve(script).toString()));
        command.addAll(List.of(args));
        ProcessBuilder builder = new ProcessBuilder(command).redirectErrorStream(true);
        builder.environment().remove("BACKUP_CONF");
        Process process = builder.start();
        assertThat(process.waitFor(10, TimeUnit.SECONDS)).as("CLI must stop before external work").isTrue();
        return new Result(process.exitValue(), new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
    }

    private Path config(String text, String mode) throws IOException {
        Path file = temp.resolve("backup.conf");
        Files.writeString(file, text);
        Files.setPosixFilePermissions(file, PosixFilePermissions.fromString(mode));
        return file;
    }

    @Test
    void unknownArgumentMustNotEchoSensitiveValues() throws Exception {
        String secret = "canary-secret-93a4";
        for (String script : List.of("backup.sh", "backup-dispatch.sh", "restore-rehearsal.sh")) {
            Result result = run(script, secret);
            assertThat(result.exit()).as(script).isNotZero();
            assertThat(result.output()).as(script).doesNotContain(secret);
        }
    }

    @Test
    void dispatcherMustRejectReadableConfigBeforeSourcingIt() throws Exception {
        Path marker = temp.resolve("sourced");
        Path conf = config("touch '" + marker + "'\n", "rw-r--r--");
        Result result = run("backup-dispatch.sh", "--config", conf.toString());
        assertThat(result.exit()).isNotZero();
        assertThat(marker).as("insecure config must not execute").doesNotExist();
    }

    @Test
    void backupMustNotCreateDestinationOnAnOrdinaryUnmountedDirectory() throws Exception {
        Path destination = temp.resolve("mountpoint").resolve("backups");
        Files.createDirectory(destination.getParent());
        Path conf = config("COMPOSE_FILE=/unused\nCOMPOSE_PROJECT=test\nDB_NAME=test\nDB_USER=test\n"
                + "DB_PASSWORD=test\nBACKUP_DIR='" + destination + "'\nBACKUP_MOUNT_POINT='"
                + destination.getParent() + "'\nREQUIRE_ENCRYPTION_VERIFIED=false\n", "rw-------");
        Result result = run("backup.sh", "--config", conf.toString());
        assertThat(result.exit()).isNotZero();
        assertThat(destination).as("no fallback directory on an unmounted filesystem").doesNotExist();
    }
}
