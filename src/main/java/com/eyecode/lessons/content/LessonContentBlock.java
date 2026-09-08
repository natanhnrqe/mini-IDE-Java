package com.eyecode.lessons.content;

import java.util.List;

public record LessonContentBlock(LessonContentBlockType type, String text, String title,
                                 String language, String code, List<String> items) {
    public LessonContentBlock {
        if (type == null) throw new IllegalArgumentException("Bloco de conteúdo inválido");
        items = items == null ? List.of() : List.copyOf(items);
        if ((type == LessonContentBlockType.HEADING || type == LessonContentBlockType.PARAGRAPH)
                && (text == null || text.isBlank())) throw new IllegalArgumentException("Texto do bloco obrigatório");
        if (type == LessonContentBlockType.CODE && (code == null || code.isBlank())) {
            throw new IllegalArgumentException("Código do bloco obrigatório");
        }
        if (type == LessonContentBlockType.LIST && items.isEmpty()) throw new IllegalArgumentException("Lista vazia");
        if (type == LessonContentBlockType.CALLOUT && (text == null || text.isBlank())) {
            throw new IllegalArgumentException("Texto do destaque obrigatório");
        }
    }
}
