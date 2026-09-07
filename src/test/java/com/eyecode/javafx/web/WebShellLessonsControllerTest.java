package com.eyecode.javafx.web;

import com.eyecode.lessons.catalog.ResourceLearningCatalog;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebShellLessonsControllerTest {
    @Test void serializesCategoriesTopicsAndLessonDescriptors() {
        Map<String, Object> payload = WebShellLessonsController.catalogPayload(new ResourceLearningCatalog());
        List<Map<String, Object>> categories = maps(payload.get("categories"));
        assertEquals(4, categories.size());
        Map<String, Object> java = categories.getFirst();
        assertEquals("java", java.get("id"));
        Map<String, Object> strings = maps(java.get("topics")).stream()
                .filter(topic -> "java.strings".equals(topic.get("id"))).findFirst().orElseThrow();
        Map<String, Object> lesson = maps(strings.get("lessons")).stream()
                .filter(item -> "java.strings.manipulation".equals(item.get("id"))).findFirst().orElseThrow();
        assertEquals("BEGINNER", lesson.get("difficulty"));
        assertTrue(((List<?>) lesson.get("concepts")).contains("substring"));
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> maps(Object value) { return (List<Map<String, Object>>) value; }
}
