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
        String lessonPanel = Files.readString(Path.of("src/main/web/src/lessons/LessonPanel.tsx"));
        String monaco = Files.readString(Path.of("src/main/web/src/monaco/MonacoWorkspaceService.ts"));
        String styles = Files.readString(Path.of("src/main/web/src/styles.css"));

        assertTrue(workspace.contains("type AppMode = 'WELCOME' | 'PROJECT' | 'LEARN'"));
        assertEquals(1, occurrences(workspace, "<MonacoHost"));
        assertTrue(workspace.contains("<div className={`shell-workspace"));
        assertTrue(workspace.contains("learnMode ? <LearnWorkspace"));
        assertFalse(workspace.contains("LessonsPanel"));
        assertFalse(learnWorkspace.contains("MonacoHost"));
        assertTrue(workspace.contains("onClick={() => startLesson(selectedLearnLesson)}"));
        assertTrue(styles.contains(".workspace-empty > div:last-child, .workspace-empty > button"));
        assertTrue(lessonPanel.contains("highlightLearningJavaSource"));
        assertTrue(monaco.contains("animateEphemeralEdit"));
        assertTrue(monaco.contains("model.applyEdits([edit])"));
        assertFalse(monaco.contains("executeEdits('eyecode.lesson.typing'"));
        assertTrue(monaco.contains("cancelLessonTyping()"));
        assertTrue(monaco.contains("this.ephemeralModels.get(uri)"));
        assertTrue(monaco.contains("uri.startsWith('lesson://')"));
        assertTrue(monaco.contains("this.editor?.getModel() !== model || model.uri.toString() !== uri"));
        assertTrue(monaco.contains("model.getValue() !== finalCode"));
        assertTrue(monaco.contains("character === '\\n' && !establishIndentation()"));
        assertTrue(monaco.contains("replacementText.charAt(end) === ' ' || replacementText.charAt(end) === '\\t'"));
        assertEquals(1, occurrences(monaco, "establishIndentation()"));
        assertFalse(monaco.contains("typeEphemeralCode"));
        assertFalse(monaco.contains("findIndex(command => command.type === 'ANIMATE_EDIT')"));
        assertFalse(monaco.contains("publishLessonAnimationDiagnostic"));
        assertFalse(workspace.contains("LessonAnimationTrace"));
        assertFalse(styles.contains("lesson-animation-trace"));
        assertTrue(workspace.contains("lessonPresentationReady"));
        assertTrue(workspace.contains("lessonSession && lessonPresentationReady && <LessonAnnotation"));
        assertTrue(styles.contains(".lesson-panel-content.learning-body pre code { font-size: 12px; }"));
    }

    private static int occurrences(String text, String target) {
        return text.split(java.util.regex.Pattern.quote(target), -1).length - 1;
    }
}
