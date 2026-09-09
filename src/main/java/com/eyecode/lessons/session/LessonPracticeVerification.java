package com.eyecode.lessons.session;

import com.eyecode.lessons.practice.PracticeVerificationResult;

public record LessonPracticeVerification(LessonSessionSnapshot session,
                                         PracticeVerificationResult verification) {
}
