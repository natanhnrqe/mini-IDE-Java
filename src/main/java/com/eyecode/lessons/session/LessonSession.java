package com.eyecode.lessons.session;

import com.eyecode.lessons.content.LessonContent;

import java.util.UUID;

public final class LessonSession {
    private final String sessionId = UUID.randomUUID().toString();
    private final LessonContent content;
    private int currentStepIndex;
    private int currentPresentationIndex;
    private LessonSessionState state = LessonSessionState.ACTIVE;

    LessonSession(LessonContent content) { this.content = content; }
    public String sessionId() { return sessionId; }
    public LessonContent content() { return content; }
    public int currentStepIndex() { return currentStepIndex; }
    public int currentPresentationIndex() { return currentPresentationIndex; }
    public LessonSessionState state() { return state; }
    void next() {
        if (currentPresentationIndex < content.steps().get(currentStepIndex).presentations().size() - 1) {
            currentPresentationIndex++;
        } else if (currentStepIndex < content.steps().size() - 1) {
            currentStepIndex++;
            currentPresentationIndex = 0;
        }
    }
    void previous() {
        if (currentPresentationIndex > 0) {
            currentPresentationIndex--;
        } else if (currentStepIndex > 0) {
            currentStepIndex--;
            currentPresentationIndex = content.steps().get(currentStepIndex).presentations().size() - 1;
        }
    }
    void close() { state = LessonSessionState.CLOSED; }
}
