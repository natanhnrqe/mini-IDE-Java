import { highlightLearningJavaHtml } from '../learning/highlightJava';
import type { LessonContentBlock, LessonSession } from './protocol';

type Props = { session: LessonSession; onPrevious(): void; onNext(): void; onExit(): void; busy?: boolean };

export function LessonPanel({ session, onPrevious, onNext, onExit, busy = false }: Props) {
  return <section className="bottom-panel lesson-panel" aria-label="Conteúdo da aula">
    <header className="bottom-tabs"><strong>Aula: {session.title}</strong><span>Parte {session.currentStep + 1} de {session.totalSteps}</span></header>
    <article className="bottom-panel-content lesson-panel-content">
      {session.contentBlocks.map((block, index) => <LessonBlock key={index} block={block} />)}
    </article>
    <footer className="lesson-panel-actions"><button type="button" onClick={onExit} disabled={busy}>Voltar ao início</button><div><button type="button" onClick={onPrevious} disabled={busy || !session.canPrevious}>Anterior</button><button type="button" className="primary-action" onClick={onNext} disabled={busy || !session.canNext}>Próximo</button></div></footer>
  </section>;
}

function LessonBlock({ block }: { block: LessonContentBlock }) {
  if (block.type === 'HEADING') return <h2>{block.text}</h2>;
  if (block.type === 'PARAGRAPH') return <p>{block.text}</p>;
  if (block.type === 'CODE') return <pre><code dangerouslySetInnerHTML={{ __html: highlightLearningJavaHtml(block.code ?? '') }} /></pre>;
  if (block.type === 'LIST') return <ul>{block.items?.map(item => <li key={item}>{item}</li>)}</ul>;
  return <aside className="lesson-callout"><strong>{block.title}</strong><p>{block.text}</p></aside>;
}

