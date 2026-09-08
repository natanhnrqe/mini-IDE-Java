package com.eyecode.lessons.content;

import com.eyecode.lessons.catalog.Json;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class LessonContentService {
    private static final String RESOURCE_PREFIX = "/learning/lessons/content/";

    public LessonContent load(String lessonId) {
        if (lessonId == null || lessonId.isBlank()) throw new IllegalArgumentException("ID de aula inválido");
        String resource = RESOURCE_PREFIX + lessonId + ".json";
        try (InputStream stream = LessonContentService.class.getResourceAsStream(resource)) {
            if (stream == null) throw new IllegalArgumentException("Conteúdo da aula não encontrado: " + lessonId);
            return parse(new String(stream.readAllBytes(), StandardCharsets.UTF_8), lessonId);
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível ler o conteúdo da aula", exception);
        }
    }

    public boolean hasContent(String lessonId) {
        return lessonId != null && LessonContentService.class.getResource(RESOURCE_PREFIX + lessonId + ".json") != null;
    }

    LessonContent parse(String json, String expectedId) {
        if (!(Json.parse(json) instanceof Map<?, ?> root)) throw new IllegalArgumentException("Conteúdo de aula deve ser um objeto JSON");
        String id = required(root, "id");
        if (!expectedId.equals(id)) throw new IllegalArgumentException("ID do conteúdo não corresponde à aula");
        int version = number(root, "version");
        List<LessonStep> steps = new ArrayList<>();
        for (Object value : array(root, "steps")) steps.add(step(object(value, "etapa")));
        return new LessonContent(id, version, required(root, "title"), steps);
    }

    private static LessonStep step(Map<?, ?> object) {
        LessonStepType type;
        try { type = LessonStepType.valueOf(required(object, "type")); }
        catch (IllegalArgumentException exception) { throw new IllegalArgumentException("Tipo de etapa inválido", exception); }
        List<LessonEditorCommand> commands = new ArrayList<>();
        for (Object value : array(object, "commands")) commands.add(command(object(value, "comando")));
        List<LessonContentBlock> contentBlocks = new ArrayList<>();
        if (object.get("contentBlocks") instanceof List<?> blocks) {
            for (Object value : blocks) contentBlocks.add(block(object(value, "bloco")));
        }
        LessonAnnotation annotation = object.get("annotation") == null ? null : annotation(object(object.get("annotation"), "anotação"));
        return new LessonStep(required(object, "id"), type, required(object, "title"), required(object, "message"), contentBlocks, annotation, commands);
    }

    private static LessonContentBlock block(Map<?, ?> object) {
        LessonContentBlockType type;
        try { type = LessonContentBlockType.valueOf(required(object, "type")); }
        catch (IllegalArgumentException exception) { throw new IllegalArgumentException("Tipo de bloco inválido", exception); }
        String text = optional(object, "text");
        String title = optional(object, "title");
        String language = optional(object, "language");
        String code = optional(object, "code");
        List<String> items = object.get("items") instanceof List<?> values
                ? values.stream().map(String::valueOf).toList() : List.of();
        return new LessonContentBlock(type, text, title, language, code, items);
    }

    private static LessonAnnotation annotation(Map<?, ?> object) {
        return new LessonAnnotation(required(object, "title"), required(object, "message"),
                range(object(object.get("range"), "intervalo da anotação")));
    }

    private static LessonEditorCommand command(Map<?, ?> object) {
        LessonEditorCommandType type;
        try { type = LessonEditorCommandType.valueOf(required(object, "type")); }
        catch (IllegalArgumentException exception) { throw new IllegalArgumentException("Tipo de comando inválido", exception); }
        String code = object.get("code") instanceof String value ? value : null;
        LessonEditorRange range = object.get("range") == null ? null : range(object(object.get("range"), "intervalo"));
        return new LessonEditorCommand(type, code, range);
    }

    private static LessonEditorRange range(Map<?, ?> object) {
        return new LessonEditorRange(number(object, "startLineNumber"), number(object, "startColumn"),
                number(object, "endLineNumber"), number(object, "endColumn"));
    }

    private static Map<?, ?> object(Object value, String kind) {
        if (value instanceof Map<?, ?> object) return object;
        throw new IllegalArgumentException("Era esperado um objeto de " + kind);
    }
    private static List<Object> array(Map<?, ?> object, String name) {
        if (object.get(name) instanceof List<?> list) return new ArrayList<>(list);
        throw new IllegalArgumentException("Era esperado um array: " + name);
    }
    private static String required(Map<?, ?> object, String name) {
        if (object.get(name) instanceof String value && !value.isBlank()) return value;
        throw new IllegalArgumentException("Campo ausente: " + name);
    }
    private static String optional(Map<?, ?> object, String name) {
        return object.get(name) instanceof String value && !value.isBlank() ? value : null;
    }
    private static int number(Map<?, ?> object, String name) {
        if (object.get(name) instanceof Number value) return value.intValue();
        throw new IllegalArgumentException("Campo numérico inválido: " + name);
    }
}
