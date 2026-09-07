package com.eyecode.lessons.catalog;

import java.util.List;

public record LessonDescriptor(String id, String title, String description, String categoryId,
                               String topicId, LessonDifficulty difficulty, int estimatedMinutes,
                               List<String> concepts) {
    public LessonDescriptor {
        concepts = List.copyOf(concepts);
    }
}
