package com.eyecode.lessons.catalog;

import java.util.List;

public record LearningRoadmapSection(String id, String title, List<LearningRoadmapItem> items) {
    public LearningRoadmapSection {
        items = List.copyOf(items);
    }
}
