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
