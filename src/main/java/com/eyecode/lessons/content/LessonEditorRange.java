package com.eyecode.lessons.content;

public record LessonEditorRange(int startLineNumber, int startColumn, int endLineNumber, int endColumn) {
    public LessonEditorRange {
        if (startLineNumber < 1 || startColumn < 1 || endLineNumber < startLineNumber || endColumn < 1
                || (startLineNumber == endLineNumber && endColumn < startColumn)) {
            throw new IllegalArgumentException("Intervalo de editor inválido");
        }
    }
}
