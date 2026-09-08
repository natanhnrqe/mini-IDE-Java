package com.eyecode.lessons.content;

public record LessonEditorCommand(LessonEditorCommandType type, String code, LessonEditorRange range) {
    public LessonEditorCommand {
        if (type == null) throw new IllegalArgumentException("Tipo de comando ausente");
        if (type == LessonEditorCommandType.SET_CODE && (code == null || code.isEmpty())) {
            throw new IllegalArgumentException("SET_CODE exige código");
        }
        if ((type == LessonEditorCommandType.HIGHLIGHT_RANGE || type == LessonEditorCommandType.REVEAL_RANGE)
                && range == null) throw new IllegalArgumentException("Comando de intervalo exige intervalo");
    }
}
