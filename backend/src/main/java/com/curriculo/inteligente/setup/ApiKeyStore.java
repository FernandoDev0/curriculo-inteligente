package com.curriculo.inteligente.setup;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Set;

@Component
public class ApiKeyStore {

    public static final String ENV = "GEMINI_API_KEY";

    public Path file() {
        return Path.of(System.getProperty("user.home"), ".curriculo-inteligente", "gemini-api-key");
    }

    public String resolve() {
        String fromEnv = System.getenv(ENV);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv.trim();
        }
        Path path = file();
        if (!Files.isRegularFile(path)) {
            return null;
        }
        try {
            String value = Files.readString(path, StandardCharsets.UTF_8).trim();
            return value.isBlank() ? null : value;
        } catch (IOException ex) {
            return null;
        }
    }

    public String source() {
        String fromEnv = System.getenv(ENV);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return "ENV";
        }
        return Files.isRegularFile(file()) ? "FILE" : "NONE";
    }

    public void write(String apiKey) {
        try {
            Path path = file();
            Files.createDirectories(path.getParent());
            Files.writeString(path, apiKey.trim(), StandardCharsets.UTF_8);
            restrict(path);
        } catch (IOException ex) {
            throw new IllegalStateException("Não foi possível gravar a chave local.");
        }
    }

    public void deleteFile() {
        try {
            Files.deleteIfExists(file());
        } catch (IOException ex) {
            throw new IllegalStateException("Não foi possível remover o arquivo da chave.");
        }
    }

    private void restrict(Path path) throws IOException {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            String user = System.getProperty("user.name");
            run(new String[] {"icacls", path.toString(), "/inheritance:r"});
            run(new String[] {"icacls", path.toString(), "/grant:r", user + ":(R,W)"});
            return;
        }
        Set<PosixFilePermission> ownerOnly = EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);
        Files.setPosixFilePermissions(path, ownerOnly);
    }

    private void run(String[] command) throws IOException {
        try {
            Process process = new ProcessBuilder(command).start();
            if (process.waitFor() != 0) {
                throw new IOException("acl");
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException(ex);
        }
    }
}
