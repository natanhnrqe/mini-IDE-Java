package com.eyecode.javafx.web;

import com.eyecode.lessons.catalog.LearningCatalog;
import com.eyecode.lessons.catalog.LearningCategory;
import com.eyecode.lessons.catalog.LearningRoadmapItem;
import com.eyecode.lessons.catalog.LearningRoadmapSection;
import com.eyecode.lessons.catalog.LearningTopic;
import com.eyecode.lessons.catalog.LessonDescriptor;
import com.eyecode.lessons.catalog.ResourceLearningCatalog;
import com.eyecode.lessons.content.LessonContentService;
import com.eyecode.lessons.content.LessonContentBlock;
import com.eyecode.lessons.content.LessonEditorCommand;
import com.eyecode.lessons.content.LessonEditorRange;
import com.eyecode.lessons.session.LessonSessionService;
import com.eyecode.lessons.session.LessonSessionSnapshot;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class WebShellLessonsController {
    private final LearningCatalog catalog;
    private final String catalogError;
    private final LessonContentService contentService;
    private final LessonSessionService sessionService;

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
        contentService = new LessonContentService();
        sessionService = new LessonSessionService(contentService);
        surface.registerHandler("lessons", "catalog", this::catalog);
        surface.registerHandler("lessons", "session/start", this::start);
        surface.registerHandler("lessons", "session/next", this::next);
        surface.registerHandler("lessons", "session/previous", this::previous);
        surface.registerHandler("lessons", "session/close", this::close);
    }

    public void closeActiveSession() {
        sessionService.closeActive();
    }

    private WebShellEnvelope catalog(WebShellEnvelope request) {
        if (catalog == null) {
            return request.error(new WebShellError("LESSONS_CATALOG_UNAVAILABLE",
                    catalogError == null ? "O catálogo de aulas está indisponível" : catalogError, true));
        }
        return request.response(catalogPayload(catalog, contentService));
    }

    static Map<String, Object> catalogPayload(LearningCatalog catalog) {
        return catalogPayload(catalog, new LessonContentService());
    }

    static Map<String, Object> catalogPayload(LearningCatalog catalog, LessonContentService contentService) {
        List<Map<String, Object>> categories = catalog.categories().stream()
                .map(category -> categoryPayload(catalog, contentService, category))
                .toList();
        return Map.of("categories", categories);
    }

    private static Map<String, Object> categoryPayload(LearningCatalog catalog, LessonContentService contentService, LearningCategory category) {
        Map<String, Object> payload = basePayload(category.id(), category.title(), category.description());
        payload.put("topics", catalog.topicsForCategory(category.id()).stream()
                .map(topic -> topicPayload(catalog, contentService, topic))
                .toList());
        return payload;
    }

    private static Map<String, Object> topicPayload(LearningCatalog catalog, LessonContentService contentService, LearningTopic topic) {
        Map<String, Object> payload = basePayload(topic.id(), topic.title(), topic.description());
        payload.put("categoryId", topic.categoryId());
        payload.put("roadmapSections", topic.roadmapSections().stream()
                .map(WebShellLessonsController::roadmapSectionPayload)
                .toList());
        payload.put("lessons", catalog.lessonsForTopic(topic.id()).stream()
                .map(lesson -> lessonPayload(contentService, lesson))
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

    private static Map<String, Object> lessonPayload(LessonContentService contentService, LessonDescriptor lesson) {
        Map<String, Object> payload = basePayload(lesson.id(), lesson.title(), lesson.description());
        payload.put("categoryId", lesson.categoryId());
        payload.put("topicId", lesson.topicId());
        payload.put("difficulty", lesson.difficulty().name());
        payload.put("estimatedMinutes", lesson.estimatedMinutes());
        payload.put("concepts", lesson.concepts());
        payload.put("executable", contentService.hasContent(lesson.id()));
        return payload;
    }

    private WebShellEnvelope start(WebShellEnvelope request) {
        return request.response(sessionPayload(sessionService.start(text(request, "lessonId"))));
    }

    private WebShellEnvelope next(WebShellEnvelope request) {
        return request.response(sessionPayload(sessionService.next(text(request, "sessionId"))));
    }

    private WebShellEnvelope previous(WebShellEnvelope request) {
        return request.response(sessionPayload(sessionService.previous(text(request, "sessionId"))));
    }

    private WebShellEnvelope close(WebShellEnvelope request) {
        return request.response(sessionPayload(sessionService.close(text(request, "sessionId"))));
    }

    static Map<String, Object> sessionPayload(LessonSessionSnapshot snapshot) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sessionId", snapshot.sessionId());
        payload.put("lessonId", snapshot.lessonId());
        payload.put("currentStep", snapshot.currentStepIndex());
        payload.put("totalSteps", snapshot.totalSteps());
        payload.put("state", snapshot.state().name());
        payload.put("title", snapshot.step().title());
        payload.put("message", snapshot.step().message());
        payload.put("canPrevious", snapshot.canPrevious());
        payload.put("canNext", snapshot.canNext());
        payload.put("contentBlocks", snapshot.step().contentBlocks().stream().map(WebShellLessonsController::blockPayload).toList());
        if (snapshot.step().annotation() != null) {
            payload.put("annotation", Map.of("title", snapshot.step().annotation().title(),
                    "message", snapshot.step().annotation().message(), "range", rangePayload(snapshot.step().annotation().range())));
        }
        payload.put("commands", snapshot.step().commands().stream().map(WebShellLessonsController::commandPayload).toList());
        return payload;
    }

    private static Map<String, Object> commandPayload(LessonEditorCommand command) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", command.type().name());
        if (command.code() != null) payload.put("code", command.code());
        if (command.range() != null) payload.put("range", rangePayload(command.range()));
        return payload;
    }

    private static Map<String, Object> blockPayload(LessonContentBlock block) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", block.type().name());
        if (block.text() != null) payload.put("text", block.text());
        if (block.title() != null) payload.put("title", block.title());
        if (block.language() != null) payload.put("language", block.language());
        if (block.code() != null) payload.put("code", block.code());
        if (!block.items().isEmpty()) payload.put("items", block.items());
        return payload;
    }

    private static Map<String, Object> rangePayload(LessonEditorRange range) {
        return Map.of("startLineNumber", range.startLineNumber(), "startColumn", range.startColumn(),
                "endLineNumber", range.endLineNumber(), "endColumn", range.endColumn());
    }

    private static String text(WebShellEnvelope request, String name) {
        Object value = request.payload().get(name);
        if (value instanceof String text && !text.isBlank()) return text;
        throw new IllegalArgumentException("Campo obrigatório ausente: " + name);
    }

    private static Map<String, Object> basePayload(String id, String title, String description) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", id);
        payload.put("title", title);
        payload.put("description", description);
        return payload;
    }
}
