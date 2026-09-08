package com.eyecode.lessons.session;

import com.eyecode.lessons.content.LessonContentService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LessonSessionServiceTest {
    @Test void advancesAndClampsTheRepresentativeSession() {
        LessonSessionService service = new LessonSessionService(new LessonContentService());
        LessonSessionSnapshot first = service.start("java.fundamentals.variables.int");
        assertEquals("java.fundamentals.variables.int", first.lessonId());
        assertEquals(0, first.currentStepIndex());
        assertEquals(1, service.next(first.sessionId()).currentStepIndex());
        for (int index = 0; index < 4; index++) service.next(first.sessionId());
        assertEquals(5, service.next(first.sessionId()).currentStepIndex());
        assertEquals(5, service.next(first.sessionId()).currentStepIndex());
        assertEquals(4, service.previous(first.sessionId()).currentStepIndex());
        for (int index = 0; index < 4; index++) service.previous(first.sessionId());
        assertEquals(0, service.previous(first.sessionId()).currentStepIndex());
    }

    @Test void closePreventsFurtherNavigation() {
        LessonSessionService service = new LessonSessionService(new LessonContentService());
        LessonSessionSnapshot session = service.start("java.fundamentals.variables.int");
        assertEquals(LessonSessionState.CLOSED, service.close(session.sessionId()).state());
        assertThrows(IllegalArgumentException.class, () -> service.next(session.sessionId()));
    }
}
