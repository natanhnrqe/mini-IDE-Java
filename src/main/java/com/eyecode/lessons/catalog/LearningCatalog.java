package com.eyecode.lessons.catalog;

import java.util.List;
import java.util.Optional;

public interface LearningCatalog {
    List<LearningCategory> categories();
    List<LearningTopic> topicsForCategory(String categoryId);
    List<LessonDescriptor> lessonsForTopic(String topicId);
    Optional<LearningTopic> topic(String topicId);
    Optional<LessonDescriptor> lesson(String lessonId);
}
