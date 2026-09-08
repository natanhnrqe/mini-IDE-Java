package com.eyecode.lessons.session;

import com.eyecode.lessons.content.LessonStep;

public record LessonSessionSnapshot(String sessionId, String lessonId, int currentStepIndex, int totalSteps,
                                    LessonSessionState state, LessonStep step, boolean canPrevious, boolean canNext) {
}
