package com.eyecode.lessons.content;

import java.util.List;

public record LessonPresentation(String id, List<LessonEditorCommand> commands, LessonAnnotation annotation) {
    public LessonPresentation {
        if (id == null || id.isBlank() || commands == null) throw new IllegalArgumentException("Apresentação de aula inválida");
        commands = List.copyOf(commands);
    }
}
