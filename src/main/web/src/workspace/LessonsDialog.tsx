type Props = { onClose(): void };

export function LessonsDialog({ onClose }: Props) {
  return <div className="lessons-backdrop" onPointerDown={onClose}>
    <section className="lessons-dialog" onPointerDown={event => event.stopPropagation()} role="dialog" aria-modal="true" aria-labelledby="lessons-title">
      <strong id="lessons-title">Lessons</strong><p>Lessons will be available in a future EyeCode update.</p><button type="button" onClick={onClose}>Close</button>
    </section>
  </div>;
}
