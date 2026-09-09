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

    @Test
    void practiceUsesTheExistingLessonModelWithoutStartingProfessorCommands() throws IOException {
        String workspace = Files.readString(Path.of("src/main/web/src/workspace/Workspace.tsx"));
        String controller = Files.readString(Path.of("src/main/web/src/lessons/LessonEditorController.ts"));
        String monaco = Files.readString(Path.of("src/main/web/src/monaco/MonacoWorkspaceService.ts"));

        assertTrue(monaco.contains("if ([...this.ephemeralModels.values()].includes(model)) return;"));
        assertTrue(monaco.indexOf("if ([...this.ephemeralModels.values()].includes(model)) return;")
                < monaco.indexOf("bridge.request<{ document: DocumentSnapshot }>('document', 'change'"));
        assertTrue(controller.contains("if (session.phase === 'PRACTICE') this.enterPractice(session.practice!.starterCode);\n    else this.apply(session.commands);"));
        assertFalse(workspace.contains("lessonEditor.enter(session);\n      if (session.phase === 'PRACTICE')"));
        assertTrue(workspace.contains("if (session.phase === 'PRACTICE') lessonEditor.enterPractice(session.practice!.starterCode);\n      else lessonEditor.apply(session.commands);"));
        assertTrue(controller.contains("this.cancelAnimation();\n    this.service.clearEphemeralDecorations(this.activeUri);\n    this.service.setEphemeralModelValue(this.activeUri, starterCode);\n    this.service.setEphemeralReadOnly(this.activeUri, false);\n    this.service.setLessonPracticeIntelligence(this.activeUri, true);\n    this.service.focus();"));
        assertTrue(controller.contains("this.service.setEphemeralReadOnly(uri, true);"));
        assertTrue(monaco.contains("focus(): void { this.editor?.focus(); }"));
    }

    @Test
    void practiceShowsItsInstructionAndEnablesOnlyEphemeralIntelligence() throws IOException {
        String panel = Files.readString(Path.of("src/main/web/src/lessons/LessonPanel.tsx"));
        String controller = Files.readString(Path.of("src/main/web/src/lessons/LessonEditorController.ts"));
        String monaco = Files.readString(Path.of("src/main/web/src/monaco/MonacoWorkspaceService.ts"));
        String completion = Files.readString(Path.of("src/main/java/com/eyecode/javafx/web/WebShellCompletionController.java"));
        String learning = Files.readString(Path.of("src/main/java/com/eyecode/javafx/web/WebShellLearningController.java"));

        assertTrue(panel.contains("const practice = session.phase === 'PRACTICE' ? session.practice : undefined;"));
        assertTrue(panel.contains("<h2>Sua vez</h2><p>{practice.instruction}</p>"));
        assertTrue(controller.contains("this.service.setLessonPracticeIntelligence(uri, false);"));
        assertTrue(controller.contains("this.service.setLessonPracticeIntelligence(this.activeUri, true);"));
        assertTrue(monaco.contains("private readonly lessonPracticeUris = new Set<string>();"));
        assertTrue(monaco.contains("uri.startsWith('lesson://') && !lessonPractice"));
        assertTrue(monaco.contains("lessonPractice,"));
        assertTrue(monaco.contains("lessonPractice: this.lessonPracticeUris.has(target.uri),"));
        assertTrue(monaco.contains("if ([...this.ephemeralModels.values()].includes(model)) return;"));
        assertTrue(completion.contains("boolean lessonPractice = isLessonPracticeRequest(message.payload(), modelId);"));
        assertTrue(completion.contains("new EditorDocument(session == null ? null : session.getFile(), content)"));
        assertTrue(completion.contains("uri.startsWith(\"lesson://\") && Boolean.TRUE.equals(payload.get(\"lessonPractice\"))"));
        assertTrue(learning.contains("boolean lessonPractice = isLessonPracticeRequest(message.payload(), uri);"));
        assertTrue(learning.contains("new EditorDocument(session == null ? null : session.getFile(), content)"));
        assertTrue(learning.contains("uri.startsWith(\"lesson://\") && Boolean.TRUE.equals(payload.get(\"lessonPractice\"))"));
    }

    @Test
    void practiceVerificationUsesCurrentEphemeralSourceAndAuthoritativeSessionState() throws IOException {
        String workspace = Files.readString(Path.of("src/main/web/src/workspace/Workspace.tsx"));
        String controller = Files.readString(Path.of("src/main/web/src/lessons/LessonEditorController.ts"));
        String panel = Files.readString(Path.of("src/main/web/src/lessons/LessonPanel.tsx"));
        String lessons = Files.readString(Path.of("src/main/java/com/eyecode/javafx/web/WebShellLessonsController.java"));

        assertTrue(controller.contains("practiceSource(): string | null"));
        assertTrue(controller.contains("this.service.ephemeralModelValue(this.activeUri)"));
        assertTrue(workspace.contains("lessonEditor.practiceSource()"));
        assertTrue(workspace.contains("'lessons', 'session/verify'"));
        assertTrue(workspace.contains("sessionId, practiceId, source"));
        assertTrue(workspace.contains("lessonEditor.practiceSource() !== source"));
        assertTrue(workspace.contains("response.session.phase !== 'PRACTICE'"));
        assertTrue(panel.contains("onClick={onVerify}"));
        assertTrue(panel.contains("{verifying ? 'Verificando...' : 'Verificar'}"));
        assertTrue(panel.contains("{verification.message}"));
        assertTrue(lessons.contains("surface.registerHandler(\"lessons\", \"session/verify\", this::verify)"));
        assertTrue(lessons.contains("sessionService.verifyPractice(sessionId, practiceId, source, validator)"));
        assertTrue(lessons.contains("\"verification\", Map.of(\"status\""));
    }

    private static int occurrences(String text, String target) {
        return text.split(java.util.regex.Pattern.quote(target), -1).length - 1;
    }
}
