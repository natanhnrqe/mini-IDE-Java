package com.eyecode.lessons.session;

import com.eyecode.lessons.content.LessonStep;
import com.eyecode.lessons.content.LessonPresentation;
import com.eyecode.lessons.content.LessonPractice;

public record LessonSessionSnapshot(String sessionId, String lessonId, int currentStepIndex, int totalSteps,
                                    int currentPresentationIndex, LessonSessionState state, LessonStep step,
                                    LessonPresentation presentation, LessonPractice practice, LessonSessionPhase phase,
                                    boolean practiceCompleted, boolean canPrevious, boolean canNext) {
}
