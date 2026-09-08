package com.eyecode.lessons.content;

public record LessonAnnotation(String title, String message, LessonEditorRange range) {
    public LessonAnnotation {
        if (title == null || title.isBlank() || message == null || message.isBlank() || range == null) {
            throw new IllegalArgumentException("Anotação de aula inválida");
        }
    }
}
