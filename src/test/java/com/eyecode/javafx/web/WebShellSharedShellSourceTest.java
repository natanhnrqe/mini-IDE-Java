package com.eyecode.javafx.web;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebShellSharedShellSourceTest {
    @Test
    void workspaceOwnsOnePersistentMonacoHostAcrossProjectAndLearnModes() throws IOException {
        String workspace = Files.readString(Path.of("src/main/web/src/workspace/Workspace.tsx"));
        String learnWorkspace = Files.readString(Path.of("src/main/web/src/lessons/LearnWorkspace.tsx"));
        String styles = Files.readString(Path.of("src/main/web/src/styles.css"));

        assertTrue(workspace.contains("type AppMode = 'WELCOME' | 'PROJECT' | 'LEARN'"));
        assertEquals(1, occurrences(workspace, "<MonacoHost"));
        assertTrue(workspace.contains("<div className={`shell-workspace"));
        assertTrue(workspace.contains("learnMode ? <LearnWorkspace"));
        assertFalse(workspace.contains("LessonsPanel"));
        assertFalse(learnWorkspace.contains("MonacoHost"));
        assertTrue(workspace.contains("onClick={() => startLesson(selectedLearnLesson)}"));
        assertTrue(styles.contains(".workspace-empty > div:last-child, .workspace-empty > button"));
    }

    private static int occurrences(String text, String target) {
        return text.split(java.util.regex.Pattern.quote(target), -1).length - 1;
    }
}
