package com.eyecode.lessons.content;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LessonContentServiceTest {
    private final LessonContentService service = new LessonContentService();

    @Test void loadsTheRepresentativeLesson() {
        LessonContent content = service.load("java.fundamentals.variables.int");
        assertEquals("java.fundamentals.variables.int", content.id());
        assertEquals(6, content.steps().size());
        assertEquals("Tipos Primitivos", content.title());
        assertEquals(LessonContentBlockType.HEADING, content.steps().get(1).contentBlocks().getFirst().type());
        assertEquals("int", content.steps().get(1).annotation().title());
    }

    @Test void rejectsUnknownMalformedAndInvalidRanges() {
        assertThrows(IllegalArgumentException.class, () -> service.load("lesson.missing"));
        assertThrows(IllegalArgumentException.class, () -> service.parse("{}", "lesson"));
        String malformedRange = "{\"id\":\"lesson\",\"version\":1,\"title\":\"Aula\",\"steps\":[{\"id\":\"one\",\"type\":\"DEMO\",\"title\":\"Passo\",\"message\":\"Texto\",\"commands\":[{\"type\":\"HIGHLIGHT_RANGE\",\"range\":{\"startLineNumber\":1,\"startColumn\":4,\"endLineNumber\":1,\"endColumn\":2}}]}]}";
        assertThrows(IllegalArgumentException.class, () -> service.parse(malformedRange, "lesson"));
        String invalidBlock = "{\"id\":\"lesson\",\"version\":1,\"title\":\"Aula\",\"steps\":[{\"id\":\"one\",\"type\":\"DEMO\",\"title\":\"Passo\",\"message\":\"Texto\",\"contentBlocks\":[{\"type\":\"CODE\"}],\"commands\":[]}]}";
        assertThrows(IllegalArgumentException.class, () -> service.parse(invalidBlock, "lesson"));
    }

    @Test void rejectsDuplicateStepIds() {
        String duplicate = "{\"id\":\"lesson\",\"version\":1,\"title\":\"Aula\",\"steps\":["
                + "{\"id\":\"one\",\"type\":\"DEMO\",\"title\":\"Um\",\"message\":\"Texto\",\"commands\":[]},"
                + "{\"id\":\"one\",\"type\":\"DEMO\",\"title\":\"Dois\",\"message\":\"Texto\",\"commands\":[]}] }";
        assertThrows(IllegalArgumentException.class, () -> service.parse(duplicate, "lesson"));
    }
}
