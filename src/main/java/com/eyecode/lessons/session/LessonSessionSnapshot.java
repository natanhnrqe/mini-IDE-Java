package com.eyecode.lessons.session;

import com.eyecode.lessons.content.LessonStep;
import com.eyecode.lessons.content.LessonPresentation;

public record LessonSessionSnapshot(String sessionId, String lessonId, int currentStepIndex, int totalSteps,
                                    int currentPresentationIndex, LessonSessionState state, LessonStep step,
                                    LessonPresentation presentation, boolean canPrevious, boolean canNext) {
}
