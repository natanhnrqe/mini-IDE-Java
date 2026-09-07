package com.eyecode.lessons.catalog;

import java.util.List;

public record LearningTopic(String id, String categoryId, String title, String description,
                            List<LearningRoadmapSection> roadmapSections) {
    public LearningTopic {
        roadmapSections = List.copyOf(roadmapSections);
    }
}
