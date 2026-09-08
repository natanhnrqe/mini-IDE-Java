import { useEffect, useState } from 'react';
import { bridge } from '../bridge/EyeCodeBridge';
import { EyeCodeIcon } from '../workspace/EyeCodeIcon';
import type { LearningTopic, LessonDescriptor, LessonsCatalog } from './protocol';

type Props = {
  selectedRoadmapItemId: string | null;
  onLessonSelected(lesson: LessonDescriptor | null, path: string[], roadmapItemId: string | null): void;
};

export function LearnWorkspace({ selectedRoadmapItemId, onLessonSelected }: Props) {
  const [catalog, setCatalog] = useState<LessonsCatalog | null>(null);
  const [error, setError] = useState('');
  const [expandedTopics, setExpandedTopics] = useState<Set<string>>(() => new Set(['java.fundamentals']));

  useEffect(() => {
    void bridge.request<LessonsCatalog>('lessons', 'catalog', {})
      .then(next => {
        setCatalog(next);
        const lesson = allLessons(next).find(item => item.executable) ?? null;
        const topic = lesson ? findTopic(next, lesson.topicId) : null;
        const roadmapItem = lesson && topic ? findRoadmapItem(topic, lesson.title) : null;
        onLessonSelected(lesson, lesson && topic ? [next.categories.find(category => category.id === topic.categoryId)?.title ?? 'Java', topic.title, lesson.title] : ['Java'], roadmapItem?.id ?? null);
      })
      .catch(reason => setError(reason instanceof Error ? reason.message : 'Não foi possível carregar as aulas.'));
  }, [onLessonSelected]);

  const java = catalog?.categories.find(category => category.id === 'java');

  function toggle(topicId: string) {
    setExpandedTopics(current => {
      const next = new Set(current);
      if (next.has(topicId)) next.delete(topicId);
      else next.add(topicId);
      return next;
    });
  }

  function selectTopic(topic: LearningTopic, title?: string, roadmapItemId: string | null = null) {
    const lesson = topic.lessons.find(item => item.executable) ?? topic.lessons[0] ?? null;
    onLessonSelected(lesson, [java?.title ?? 'Java', topic.title, title ?? lesson?.title ?? topic.title], roadmapItemId);
  }

  return <section className="project-explorer learn-roadmap" aria-label="Roteiro de conhecimento Java">
    <header className="panel-heading"><span>Java</span></header>
    <div className="project-tree" role="tree">
      {java?.topics.map(topic => {
        const expanded = expandedTopics.has(topic.id);
        const items = topic.roadmapSections.flatMap(section => section.items);
        return <div key={topic.id} className="tree-node" role="treeitem" aria-expanded={expanded}>
          <button type="button" className="tree-row tree-directory" onClick={() => { toggle(topic.id); selectTopic(topic); }} style={{ paddingLeft: '8px' }}>
            <span className={`tree-chevron${expanded ? ' is-open' : ''}`}>›</span><EyeCodeIcon name={expanded ? 'folderOpen' : 'folder'} className="tree-icon" /><span className="tree-label">{topic.title}</span>
          </button>
          {expanded && items.map(item => <div key={item.id} className="tree-node" role="treeitem">
            <button type="button" className={`tree-row tree-file${selectedRoadmapItemId === item.id ? ' is-selected' : ''}`} onClick={() => selectTopic(topic, item.title, item.id)} style={{ paddingLeft: '38px' }}>
              <span className="tree-chevron" /><EyeCodeIcon name="file" className="tree-icon" /><span className="tree-label">{item.title}</span>
            </button>
          </div>)}
        </div>;
      })}
    </div>
    {error && <div className="learn-roadmap-error" role="alert">{error}</div>}
  </section>;
}

function allLessons(catalog: LessonsCatalog): LessonDescriptor[] {
  return catalog.categories.flatMap(category => category.topics).flatMap(topic => topic.lessons);
}

function findTopic(catalog: LessonsCatalog, topicId: string): LearningTopic | null {
  return catalog.categories.flatMap(category => category.topics).find(topic => topic.id === topicId) ?? null;
}

function findRoadmapItem(topic: LearningTopic, title: string) {
  return topic.roadmapSections.flatMap(section => section.items).find(item => item.title === title) ?? null;
}
