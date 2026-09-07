package com.eyecode.javafx.web;

import com.eyecode.lessons.catalog.LearningCatalog;
import com.eyecode.lessons.catalog.LearningCategory;
import com.eyecode.lessons.catalog.LearningRoadmapItem;
import com.eyecode.lessons.catalog.LearningRoadmapSection;
import com.eyecode.lessons.catalog.LearningTopic;
import com.eyecode.lessons.catalog.LessonDescriptor;
import com.eyecode.lessons.catalog.ResourceLearningCatalog;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class WebShellLessonsController {
    private final LearningCatalog catalog;
    private final String catalogError;

    public WebShellLessonsController(JavaFxWebShellSurface surface) {
        LearningCatalog loadedCatalog = null;
        String loadError = null;
        try {
            loadedCatalog = new ResourceLearningCatalog();
        } catch (RuntimeException exception) {
            loadError = exception.getMessage();
        }
        catalog = loadedCatalog;
        catalogError = loadError;
        surface.registerHandler("lessons", "catalog", this::catalog);
    }

    private WebShellEnvelope catalog(WebShellEnvelope request) {
        if (catalog == null) {
            return request.error(new WebShellError("LESSONS_CATALOG_UNAVAILABLE",
                    catalogError == null ? "O catálogo de aulas está indisponível" : catalogError, true));
        }
        return request.response(catalogPayload(catalog));
    }

    static Map<String, Object> catalogPayload(LearningCatalog catalog) {
        List<Map<String, Object>> categories = catalog.categories().stream()
                .map(category -> categoryPayload(catalog, category))
                .toList();
        return Map.of("categories", categories);
    }

    private static Map<String, Object> categoryPayload(LearningCatalog catalog, LearningCategory category) {
        Map<String, Object> payload = basePayload(category.id(), category.title(), category.description());
        payload.put("topics", catalog.topicsForCategory(category.id()).stream()
                .map(topic -> topicPayload(catalog, topic))
                .toList());
        return payload;
    }

    private static Map<String, Object> topicPayload(LearningCatalog catalog, LearningTopic topic) {
        Map<String, Object> payload = basePayload(topic.id(), topic.title(), topic.description());
        payload.put("categoryId", topic.categoryId());
        payload.put("roadmapSections", topic.roadmapSections().stream()
                .map(WebShellLessonsController::roadmapSectionPayload)
                .toList());
        payload.put("lessons", catalog.lessonsForTopic(topic.id()).stream()
                .map(WebShellLessonsController::lessonPayload)
                .toList());
        return payload;
    }

    private static Map<String, Object> roadmapSectionPayload(LearningRoadmapSection section) {
        Map<String, Object> payload = basePayload(section.id(), section.title(), "");
        payload.put("items", section.items().stream().map(WebShellLessonsController::roadmapItemPayload).toList());
        return payload;
    }

    private static Map<String, Object> roadmapItemPayload(LearningRoadmapItem item) {
        return basePayload(item.id(), item.title(), item.description());
    }

    private static Map<String, Object> lessonPayload(LessonDescriptor lesson) {
        Map<String, Object> payload = basePayload(lesson.id(), lesson.title(), lesson.description());
        payload.put("categoryId", lesson.categoryId());
        payload.put("topicId", lesson.topicId());
        payload.put("difficulty", lesson.difficulty().name());
        payload.put("estimatedMinutes", lesson.estimatedMinutes());
        payload.put("concepts", lesson.concepts());
        return payload;
    }

    private static Map<String, Object> basePayload(String id, String title, String description) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", id);
        payload.put("title", title);
        payload.put("description", description);
        return payload;
    }
}
