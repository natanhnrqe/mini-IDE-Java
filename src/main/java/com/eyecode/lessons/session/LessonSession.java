package com.eyecode.lessons.session;

import com.eyecode.lessons.content.LessonContent;

import java.util.UUID;

public final class LessonSession {
    private final String sessionId = UUID.randomUUID().toString();
    private final LessonContent content;
    private int currentStepIndex;
    private LessonSessionState state = LessonSessionState.ACTIVE;

    LessonSession(LessonContent content) { this.content = content; }
    public String sessionId() { return sessionId; }
    public LessonContent content() { return content; }
    public int currentStepIndex() { return currentStepIndex; }
    public LessonSessionState state() { return state; }
    void next() { if (currentStepIndex < content.steps().size() - 1) currentStepIndex++; }
    void previous() { if (currentStepIndex > 0) currentStepIndex--; }
    void close() { state = LessonSessionState.CLOSED; }
}
