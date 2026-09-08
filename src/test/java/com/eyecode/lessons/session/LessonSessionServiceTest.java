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
        LessonSessionSnapshot integersInt = service.next(first.sessionId());
        assertEquals(1, integersInt.currentStepIndex());
        assertEquals(0, integersInt.currentPresentationIndex());
        assertEquals("int", integersInt.presentation().id());
        LessonSessionSnapshot integersLong = service.next(first.sessionId());
        assertEquals(1, integersLong.currentStepIndex());
        assertEquals(1, integersLong.currentPresentationIndex());
        assertEquals("long", integersLong.presentation().id());
        LessonSessionSnapshot decimalsFloat = service.next(first.sessionId());
        assertEquals(2, decimalsFloat.currentStepIndex());
        assertEquals("float", decimalsFloat.presentation().id());
        assertEquals("long", service.previous(first.sessionId()).presentation().id());
    }

    @Test void closePreventsFurtherNavigation() {
        LessonSessionService service = new LessonSessionService(new LessonContentService());
        LessonSessionSnapshot session = service.start("java.fundamentals.variables.int");
        assertEquals(LessonSessionState.CLOSED, service.close(session.sessionId()).state());
        assertThrows(IllegalArgumentException.class, () -> service.next(session.sessionId()));
    }
}
