package com.eyecode.lessons.session;

import com.eyecode.lessons.content.LessonContent;

import java.util.UUID;

public final class LessonSession {
    private final String sessionId = UUID.randomUUID().toString();
    private final LessonContent content;
    private int currentStepIndex;
    private int currentPresentationIndex;
    private LessonSessionPhase phase = LessonSessionPhase.PRESENTATION;
    private boolean practiceCompleted;
    private LessonSessionState state = LessonSessionState.ACTIVE;

    LessonSession(LessonContent content) { this.content = content; }
    public String sessionId() { return sessionId; }
    public LessonContent content() { return content; }
    public int currentStepIndex() { return currentStepIndex; }
    public int currentPresentationIndex() { return currentPresentationIndex; }
    public LessonSessionState state() { return state; }
    public LessonSessionPhase phase() { return phase; }
    public boolean practiceCompleted() { return practiceCompleted; }
    void next() {
        if (currentPresentationIndex < content.steps().get(currentStepIndex).presentations().size() - 1) {
            currentPresentationIndex++;
        } else if (content.steps().get(currentStepIndex).practice() != null && phase == LessonSessionPhase.PRESENTATION) {
            phase = LessonSessionPhase.PRACTICE;
        } else if (phase == LessonSessionPhase.PRACTICE && !practiceCompleted) {
            return;
        } else if (currentStepIndex < content.steps().size() - 1) {
            currentStepIndex++;
            currentPresentationIndex = 0;
            phase = LessonSessionPhase.PRESENTATION;
        }
    }
    void previous() {
        if (phase == LessonSessionPhase.PRACTICE) {
            phase = LessonSessionPhase.PRESENTATION;
            practiceCompleted = false;
        } else if (currentPresentationIndex > 0) {
            currentPresentationIndex--;
        } else if (currentStepIndex > 0) {
            currentStepIndex--;
            currentPresentationIndex = content.steps().get(currentStepIndex).presentations().size() - 1;
        }
    }
    void completePractice() { if (phase != LessonSessionPhase.PRACTICE) throw new IllegalStateException("Prática não está ativa"); practiceCompleted = true; }
    void close() { state = LessonSessionState.CLOSED; }
}
