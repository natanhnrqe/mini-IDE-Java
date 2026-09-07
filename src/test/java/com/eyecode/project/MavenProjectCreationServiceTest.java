package com.eyecode.project;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MavenProjectCreationServiceTest {

    @TempDir
    Path tempDir;

    private final MavenProjectCreationService service = new MavenProjectCreationService();

    @Test
    void createsAValidMavenWorkspace() throws Exception {
        MavenProjectCreationService.CreationResult result = service.create(
                new MavenProjectCreationService.CreationRequest("demo-app", tempDir.toString(), "dev.eyecode"));

        assertEquals(tempDir.resolve("demo-app"), result.projectRoot());
        assertTrue(Files.isDirectory(result.projectRoot().resolve("src/main/java")));
        assertTrue(Files.isDirectory(result.projectRoot().resolve("src/test/java")));
        assertTrue(Files.isDirectory(result.projectRoot().resolve("src/main/resources")));
        assertTrue(Files.exists(result.projectRoot().resolve(".gitignore")));
        String pom = Files.readString(result.projectRoot().resolve("pom.xml"));
        assertTrue(pom.contains("<groupId>dev.eyecode</groupId>"));
        assertTrue(pom.contains("<artifactId>demo-app</artifactId>"));
        assertTrue(pom.contains("<maven.compiler.release>21</maven.compiler.release>"));
        assertTrue(Files.readString(result.projectRoot().resolve("src/main/java/dev/eyecode/Main.java")).contains("package dev.eyecode;"));
    }

    @Test
    void rejectsTraversalAndExistingDestinations() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> service.create(
                new MavenProjectCreationService.CreationRequest("../escape", tempDir.toString(), "com.example")));
        Files.createDirectory(tempDir.resolve("existing"));
        assertThrows(java.io.IOException.class, () -> service.create(
                new MavenProjectCreationService.CreationRequest("existing", tempDir.toString(), "com.example")));
        assertFalse(Files.exists(tempDir.resolve("escape")));
    }

    @Test
    void defaultsTheGroupIdWithoutChangingWorkspaceLifecycle() throws Exception {
        MavenProjectCreationService.CreationResult result = service.create(
                new MavenProjectCreationService.CreationRequest("sample", tempDir.toString(), ""));

        assertEquals("com.example", result.groupId());
        assertFalse(Files.exists(result.projectRoot().resolve(".git")));
    }
}
