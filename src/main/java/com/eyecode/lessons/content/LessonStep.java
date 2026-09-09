package com.eyecode.lessons.content;

import java.util.List;
import java.util.HashSet;

public record LessonStep(String id, LessonStepType type, String title, String message,
                         List<LessonContentBlock> contentBlocks, List<LessonPresentation> presentations, LessonPractice practice) {
    public LessonStep {
        if (id == null || id.isBlank() || type == null || title == null || title.isBlank()
                || message == null || message.isBlank()) throw new IllegalArgumentException("Etapa de aula inválida");
        contentBlocks = contentBlocks == null ? List.of() : List.copyOf(contentBlocks);
        presentations = presentations == null ? List.of() : List.copyOf(presentations);
        if (presentations.isEmpty() || presentations.stream().map(LessonPresentation::id).count()
                != new HashSet<>(presentations.stream().map(LessonPresentation::id).toList()).size()) {
            throw new IllegalArgumentException("Apresentações de aula inválidas");
        }
    }
}
