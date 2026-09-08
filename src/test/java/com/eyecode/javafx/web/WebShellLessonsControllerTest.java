package com.eyecode.javafx.web;

import com.eyecode.lessons.catalog.ResourceLearningCatalog;
import com.eyecode.lessons.content.LessonContentService;
import com.eyecode.lessons.session.LessonSessionService;
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

    @Test void marksPrimitiveTypesAsExecutableWhenItsContentResourceExists() {
        Map<String, Object> payload = WebShellLessonsController.catalogPayload(new ResourceLearningCatalog());
        Map<String, Object> java = maps(payload.get("categories")).getFirst();
        Map<String, Object> fundamentals = maps(java.get("topics")).stream()
                .filter(topic -> "java.fundamentals".equals(topic.get("id"))).findFirst().orElseThrow();
        Map<String, Object> primitiveTypes = maps(fundamentals.get("lessons")).stream()
                .filter(lesson -> "java.fundamentals.variables.int".equals(lesson.get("id"))).findFirst().orElseThrow();

        assertEquals("Tipos Primitivos", primitiveTypes.get("title"));
        assertEquals(true, primitiveTypes.get("executable"));
    }

    @Test void preservesSessionStepAndCommandsInTheBridgePayload() {
        var sessions = new LessonSessionService(new LessonContentService());
        var started = sessions.start("java.fundamentals.variables.int");
        var snapshot = sessions.next(started.sessionId());
        Map<String, Object> payload = WebShellLessonsController.sessionPayload(snapshot);
        assertEquals(1, payload.get("currentStep"));
        assertEquals(0, payload.get("currentPresentation"));
        assertEquals("int", payload.get("presentationId"));
        assertEquals(6, payload.get("totalSteps"));
        assertEquals("HEADING", ((Map<?, ?>) ((List<?>) payload.get("contentBlocks")).getFirst()).get("type"));
        Map<?, ?> annotation = (Map<?, ?>) payload.get("annotation");
        assertEquals("int", annotation.get("title"));
        assertEquals(4, ((Map<?, ?>) annotation.get("range")).get("startLineNumber"));
        Map<?, ?> animate = (Map<?, ?>) ((List<?>) payload.get("commands")).get(2);
        assertEquals("ANIMATE_EDIT", animate.get("type"));
        assertTrue(((String) animate.get("replacementText")).contains("int age = 20;"));
        assertTrue(((String) animate.get("finalCode")).contains("int age = 20;"));
        assertEquals(18, animate.get("cadenceMillis"));
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> maps(Object value) { return (List<Map<String, Object>>) value; }
}
