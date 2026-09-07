import { useEffect, useState } from 'react';
import { bridge } from '../bridge/EyeCodeBridge';
import type { LearningCategory, LearningTopic, LessonDescriptor, LessonsCatalog } from './protocol';

type View = { kind: 'home' } | { kind: 'category'; categoryId: string } | { kind: 'topic'; topicId: string } | { kind: 'lesson'; lessonId: string };
type Props = { active?: boolean; onBackToWelcome?: () => void };

export function LessonsPanel({ active = true, onBackToWelcome }: Props) {
  const [catalog, setCatalog] = useState<LessonsCatalog | null>(null);
  const [error, setError] = useState('');
  const [view, setView] = useState<View>({ kind: 'home' });

  useEffect(() => {
    if (!active || catalog || error) return;
    void bridge.request<LessonsCatalog>('lessons', 'catalog', {})
      .then(setCatalog)
      .catch(reason => setError(reason instanceof Error ? reason.message : 'Não foi possível carregar as aulas.'));
  }, [active, catalog, error]);

  const category = catalog && view.kind !== 'home' ? findCategory(catalog, view) : undefined;
  const topic = catalog && (view.kind === 'topic' || view.kind === 'lesson') ? findTopic(catalog, view) : undefined;
  const lesson = catalog && view.kind === 'lesson' ? findLesson(catalog, view.lessonId) : undefined;
  const heading = lesson?.title ?? topic?.title ?? category?.title ?? 'Aulas';
  const description = lesson?.description ?? topic?.description ?? category?.description ?? 'Construa uma base prática em Java com trilhas selecionadas.';

  return <section className={`lessons-panel${active ? ' is-active' : ''}`} aria-label="Aulas">
    <header className="lessons-header">
      <div>
        <div className="lessons-breadcrumb">
          {view.kind !== 'home' && <button type="button" onClick={() => setView({ kind: 'home' })}>Aulas</button>}
          {category && <><span>/</span><button type="button" onClick={() => setView({ kind: 'category', categoryId: category.id })}>{category.title}</button></>}
          {topic && <><span>/</span><button type="button" onClick={() => setView({ kind: 'topic', topicId: topic.id })}>{topic.title}</button></>}
          {lesson && <><span>/</span><strong>{lesson.title}</strong></>}
        </div>
        <h1>{heading}</h1>
        <p>{description}</p>
      </div>
      {onBackToWelcome && <button type="button" className="quiet-action" onClick={onBackToWelcome}>Voltar às boas-vindas</button>}
    </header>
    {error && <div className="lessons-message" role="alert">{error}</div>}
    {!catalog && !error && <div className="lessons-message">Carregando catálogo de aulas...</div>}
    {catalog && view.kind === 'home' && <Home catalog={catalog} onCategory={id => setView({ kind: 'category', categoryId: id })} />}
    {catalog && view.kind === 'category' && category && <Category category={category} onTopic={id => setView({ kind: 'topic', topicId: id })} />}
    {catalog && view.kind === 'topic' && topic && <Topic topic={topic} onLesson={id => setView({ kind: 'lesson', lessonId: id })} />}
    {catalog && view.kind === 'lesson' && lesson && <Lesson lesson={lesson} />}
  </section>;
}

function Home({ catalog, onCategory }: { catalog: LessonsCatalog; onCategory(id: string): void }) {
  return <div className="lessons-content"><section className="lessons-continue"><span>Continuar aprendendo</span><strong>Sua atividade de aprendizado aparecerá aqui quando as sessões de aula estiverem disponíveis.</strong></section><div className="lessons-category-grid">
    {catalog.categories.map(category => <button key={category.id} type="button" className="lessons-card" onClick={() => onCategory(category.id)}><strong>{category.title}</strong><span>{category.description}</span><small>{category.topics.length} tópicos</small></button>)}
  </div></div>;
}

function Category({ category, onTopic }: { category: LearningCategory; onTopic(id: string): void }) {
  return <div className="lessons-content lessons-topic-grid">{category.topics.map(topic => <button key={topic.id} type="button" className="lessons-card" onClick={() => onTopic(topic.id)}><strong>{topic.title}</strong><span>{topic.description}</span><small>{topic.roadmapSections.reduce((total, section) => total + section.items.length, 0)} itens da trilha</small></button>)}</div>;
}

function Topic({ topic, onLesson }: { topic: LearningTopic; onLesson(id: string): void }) {
  return <div className="lessons-content"><div className="lessons-roadmap">{topic.roadmapSections.map(section => <section key={section.id}><h2>{section.title}</h2><ul>{section.items.map(item => <li key={item.id}><strong>{item.title}</strong>{item.description && <span>{item.description}</span>}</li>)}</ul></section>)}</div>
    <section className="lessons-available"><h2>Aulas disponíveis</h2>{topic.lessons.length ? <div className="lessons-topic-grid">{topic.lessons.map(lesson => <button key={lesson.id} type="button" className="lessons-card" onClick={() => onLesson(lesson.id)}><strong>{lesson.title}</strong><span>{lesson.description}</span><small>{formatDifficulty(lesson.difficulty)} · {lesson.estimatedMinutes} min</small></button>)}</div> : <p>Os descritores de aula desta trilha estão sendo preparados.</p>}</section></div>;
}

function Lesson({ lesson }: { lesson: LessonDescriptor }) {
  return <div className="lessons-content"><article className="lesson-overview"><div className="lesson-meta"><span>{formatDifficulty(lesson.difficulty)}</span><span>{lesson.estimatedMinutes} min</span></div><h2>{lesson.title}</h2><p>{lesson.description}</p><h3>Conceitos</h3><div className="lesson-concepts">{lesson.concepts.map(concept => <span key={concept}>{concept}</span>)}</div><button type="button" className="primary-action" disabled>Iniciar aula</button><small>As sessões interativas de aula estarão disponíveis em uma fase futura.</small></article></div>;
}

function findCategory(catalog: LessonsCatalog, view: Exclude<View, { kind: 'home' }>) { return catalog.categories.find(category => category.id === (view.kind === 'category' ? view.categoryId : findTopic(catalog, view)?.categoryId)); }
function findTopic(catalog: LessonsCatalog, view: Extract<View, { kind: 'topic' | 'lesson' }>) { return catalog.categories.flatMap(category => category.topics).find(topic => topic.id === (view.kind === 'topic' ? view.topicId : findLesson(catalog, view.lessonId)?.topicId)); }
function findLesson(catalog: LessonsCatalog, lessonId: string) { return catalog.categories.flatMap(category => category.topics).flatMap(topic => topic.lessons).find(lesson => lesson.id === lessonId); }
function formatDifficulty(difficulty: string) { return ({ BEGINNER: 'Iniciante', INTERMEDIATE: 'Intermediário', ADVANCED: 'Avançado' })[difficulty] ?? difficulty; }
