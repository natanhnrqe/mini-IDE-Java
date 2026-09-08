package com.eyecode.lessons.content;

public record LessonEditorCommand(LessonEditorCommandType type, String code, String replacementText,
                                  LessonEditorRange range, String finalCode, Integer cadenceMillis) {
    public LessonEditorCommand {
        if (type == null) throw new IllegalArgumentException("Tipo de comando ausente");
        if (type == LessonEditorCommandType.SET_CODE && (code == null || code.isEmpty())) {
            throw new IllegalArgumentException("SET_CODE exige código");
        }
        if (type == LessonEditorCommandType.ANIMATE_EDIT
                && (replacementText == null || range == null || finalCode == null || finalCode.isEmpty()
                || cadenceMillis == null || cadenceMillis < 1)) {
            throw new IllegalArgumentException("ANIMATE_EDIT exige texto, intervalo, código final e cadência positiva");
        }
        if ((type == LessonEditorCommandType.HIGHLIGHT_RANGE || type == LessonEditorCommandType.REVEAL_RANGE)
                && range == null) throw new IllegalArgumentException("Comando de intervalo exige intervalo");
    }
}
