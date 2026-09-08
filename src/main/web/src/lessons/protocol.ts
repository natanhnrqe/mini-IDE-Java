export type LessonDifficulty = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';

export type LearningRoadmapItem = { id: string; title: string; description: string };
export type LearningRoadmapSection = { id: string; title: string; description: string; items: LearningRoadmapItem[] };
export type LessonDescriptor = {
  id: string;
  title: string;
  description: string;
  categoryId: string;
  topicId: string;
  difficulty: LessonDifficulty;
  estimatedMinutes: number;
  concepts: string[];
  executable: boolean;
};
export type LearningTopic = {
  id: string;
  title: string;
  description: string;
  categoryId: string;
  roadmapSections: LearningRoadmapSection[];
  lessons: LessonDescriptor[];
};
export type LearningCategory = { id: string; title: string; description: string; topics: LearningTopic[] };
export type LessonsCatalog = { categories: LearningCategory[] };
export type LessonEditorRange = { startLineNumber: number; startColumn: number; endLineNumber: number; endColumn: number };
export type LessonEditorCommand = { type: 'SET_CODE' | 'HIGHLIGHT_RANGE' | 'REVEAL_RANGE' | 'CLEAR_HIGHLIGHTS'; code?: string; range?: LessonEditorRange };
export type LessonContentBlock = { type: 'HEADING' | 'PARAGRAPH' | 'CODE' | 'LIST' | 'CALLOUT'; text?: string; title?: string; language?: string; code?: string; items?: string[] };
export type LessonAnnotation = { title: string; message: string; range: LessonEditorRange };
export type LessonSession = {
  sessionId: string;
  lessonId: string;
  currentStep: number;
  totalSteps: number;
  state: 'ACTIVE' | 'CLOSED';
  title: string;
  message: string;
  contentBlocks: LessonContentBlock[];
  annotation?: LessonAnnotation;
  canPrevious: boolean;
  canNext: boolean;
  commands: LessonEditorCommand[];
};
