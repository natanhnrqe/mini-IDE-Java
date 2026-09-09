package com.eyecode.lessons.content;

public record LessonPractice(String id, String instruction, String starterCode) {
    public LessonPractice {
        if (id == null || id.isBlank() || instruction == null || instruction.isBlank()
                || starterCode == null || starterCode.isBlank()) {
            throw new IllegalArgumentException("Prática de aula inválida");
        }
    }
}
