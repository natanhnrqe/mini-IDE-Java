package com.eyecode.lessons.content;

import java.util.List;

public record LessonStep(String id, LessonStepType type, String title, String message,
                         List<LessonContentBlock> contentBlocks, LessonAnnotation annotation,
                         List<LessonEditorCommand> commands) {
    public LessonStep {
        if (id == null || id.isBlank() || type == null || title == null || title.isBlank()
                || message == null || message.isBlank()) throw new IllegalArgumentException("Etapa de aula inválida");
        contentBlocks = contentBlocks == null ? List.of() : List.copyOf(contentBlocks);
        commands = List.copyOf(commands);
    }
}
