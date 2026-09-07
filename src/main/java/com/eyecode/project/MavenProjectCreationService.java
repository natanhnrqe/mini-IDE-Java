package com.eyecode.project;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MavenProjectCreationService {

    private static final String DEFAULT_GROUP_ID = "com.example";

    public CreationResult create(CreationRequest request) throws IOException {
        if (request == null) {
            throw new IllegalArgumentException("Project request is required");
        }
        String name = validateProjectName(request.name());
        String groupId = validateGroupId(request.groupId());
        Path location = validateLocation(request.location());
        Path projectRoot = location.resolve(name).normalize();
        if (!projectRoot.startsWith(location)) {
            throw new IllegalArgumentException("Project name must not escape the selected location");
        }

        Files.createDirectory(projectRoot);
        Files.createDirectories(projectRoot.resolve("src/main/java"));
        Files.createDirectories(projectRoot.resolve("src/test/java"));
        Files.createDirectories(projectRoot.resolve("src/main/resources"));
        Files.writeString(projectRoot.resolve("pom.xml"), pom(groupId, name), StandardCharsets.UTF_8);
        Files.writeString(projectRoot.resolve(".gitignore"), "target/\nout/\n*.class\n*.jar\n*.log\n.idea/\n.DS_Store\n", StandardCharsets.UTF_8);
        Path packageDirectory = projectRoot.resolve("src/main/java").resolve(groupId.replace('.', '/'));
        Files.createDirectories(packageDirectory);
        Files.writeString(packageDirectory.resolve("Main.java"), mainSource(groupId), StandardCharsets.UTF_8);
        return new CreationResult(projectRoot, name, groupId);
    }

    private String validateProjectName(String value) {
        String name = value == null ? "" : value.trim();
        if (!name.matches("[A-Za-z][A-Za-z0-9._-]*")) {
            throw new IllegalArgumentException("Project name must start with a letter and use only letters, numbers, dots, dashes, or underscores");
        }
        return name;
    }

    private String validateGroupId(String value) {
        String groupId = value == null || value.isBlank() ? DEFAULT_GROUP_ID : value.trim();
        if (!groupId.matches("[a-z][a-z0-9]*(\\.[a-z][a-z0-9]*)*")) {
            throw new IllegalArgumentException("Group ID must use lowercase identifiers separated by dots");
        }
        return groupId;
    }

    private Path validateLocation(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Project location is required");
        }
        Path location = Path.of(value).toAbsolutePath().normalize();
        if (!Files.isDirectory(location)) {
            throw new IllegalArgumentException("Project location is not an available directory");
        }
        return location;
    }

    private String pom(String groupId, String artifactId) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>%s</groupId>
                    <artifactId>%s</artifactId>
                    <version>1.0-SNAPSHOT</version>
                    <properties>
                        <maven.compiler.release>21</maven.compiler.release>
                        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                    </properties>
                </project>
                """.formatted(groupId, artifactId);
    }

    private String mainSource(String groupId) {
        return """
                package %s;

                public class Main {
                    public static void main(String[] args) {
                        System.out.println("Hello Maven!");
                    }
                }
                """.formatted(groupId);
    }

    public record CreationRequest(String name, String location, String groupId) {
    }

    public record CreationResult(Path projectRoot, String name, String groupId) {
    }
}
