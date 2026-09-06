import { memo, useLayoutEffect, useMemo, useRef, useState } from 'react';
import type {
  CompletionDetailSection,
  CompletionPopupState,
  MonacoCompletionItem
} from './protocol';
import { JavaExample } from './JavaExample';

type Props = {
  state: CompletionPopupState;
  onSelect: (index: number) => void;
  onAccept: () => void;
};

function signature(item: MonacoCompletionItem): { name: string; suffix: string } {
  const value = item.signature || '';
  const index = value.indexOf(item.label);

  if (index >= 0) {
    return {
      name: item.label,
      suffix: value.slice(index + item.label.length)
    };
  }

  return {
    name: item.label,
    suffix: value && value !== item.label ? ` ${value}` : ''
  };
}

function iconName(kind: MonacoCompletionItem['kind']): string {
  if (kind === 'CONSTRUCTOR') return 'method';
  if (kind === 'RECORD' || kind === 'ENUM') return 'class';
  return kind.toLowerCase();
}

function iconUrl(kind: MonacoCompletionItem['kind']): string {
  const base = window.location.protocol === 'file:'
    ? './icons/completion'
    : '/icons/completion';

  return `${base}/${iconName(kind)}.svg`;
}

function highlightedLabel(item: MonacoCompletionItem) {
  const matches = new Set(item.matchIndices ?? []);

  return item.label.split('').map((character, index) =>
    matches.has(index)
      ? <mark key={index}>{character}</mark>
      : <span key={index}>{character}</span>
  );
}

/* -------------------------------------------------------------------------- */
/* Detail metadata fallback                                                    */
/* -------------------------------------------------------------------------- */

function splitParameters(source: string): string[] {
  const result: string[] = [];
  let depth = 0;
  let start = 0;

  for (let index = 0; index < source.length; index += 1) {
    const character = source[index];

    if (
      character === '<'
      || character === '('
      || character === '['
    ) {
      depth += 1;
      continue;
    }

    if (
      character === '>'
      || character === ')'
      || character === ']'
    ) {
      depth = Math.max(0, depth - 1);
      continue;
    }

    if (character === ',' && depth === 0) {
      result.push(source.slice(start, index).trim());
      start = index + 1;
    }
  }

  const last = source.slice(start).trim();

  if (last) {
    result.push(last);
  }

  return result;
}

function signatureParameters(item: MonacoCompletionItem): Array<{
  name: string;
  type?: string;
}> {
  const value = item.signature ?? '';

  const open = value.indexOf('(');
  const close = value.lastIndexOf(')');

  if (open < 0 || close <= open + 1) {
    return [];
  }

  const rawParameters = value.slice(open + 1, close).trim();

  if (!rawParameters) {
    return [];
  }

  return splitParameters(rawParameters).map(parameter => {
    const normalized = parameter.trim();

    const separator = normalized.lastIndexOf(' ');

    if (separator <= 0) {
      return {
        name: normalized
      };
    }

    return {
      type: normalized.slice(0, separator).trim(),
      name: normalized.slice(separator + 1).trim()
    };
  });
}

function fallbackDetailSections(item: MonacoCompletionItem): CompletionDetailSection[] {
  const methodLike = item.kind === 'METHOD' || item.kind === 'CONSTRUCTOR';

  if (!methodLike) {
    return [];
  }

  const sections: CompletionDetailSection[] = [];
  const parameters = signatureParameters(item);

  if (parameters.length > 0) {
    sections.push({
      title: 'Parameters',
      entries: parameters.map(parameter => ({
        name: parameter.name,
        type: parameter.type
      }))
    });
  }

  if (item.kind === 'METHOD' && item.returnType && item.returnType !== 'void') {
    sections.push({
      title: 'Returns',
      entries: [{
        name: item.returnType
      }]
    });
  }

  if (item.kind === 'CONSTRUCTOR') {
    sections.push({
      title: 'Creates',
      entries: [{
        name: item.owner || item.label
      }]
    });
  }

  return sections;
}

function DetailSection({ section }: { section: CompletionDetailSection }) {
  return (
    <section className="completion-detail-section">
      <h4>{section.title}</h4>

      <div className="completion-detail-entries">
        {section.entries.map((entry, index) => (
          <div
            key={`${section.title}-${entry.name}-${index}`}
            className="completion-detail-entry"
          >
            <code>
              {entry.type && (
                <span className="completion-detail-entry-type">
                  {entry.type}{' '}
                </span>
              )}

              {entry.name}
            </code>

            {entry.description && (
              <>
                <span className="completion-detail-separator">—</span>
                <span className="completion-detail-description">
                  {entry.description}
                </span>
              </>
            )}
          </div>
        ))}
      </div>
    </section>
  );
}

/* -------------------------------------------------------------------------- */
/* Row                                                                         */
/* -------------------------------------------------------------------------- */

type RowProps = {
  item: MonacoCompletionItem;
  index: number;
  selected: boolean;
  onSelect: (index: number) => void;
  onAccept: () => void;
};

const CompletionRow = memo(function CompletionRow({
  item,
  index,
  selected,
  onSelect,
  onAccept
}: RowProps) {
  const itemSignature = signature(item);

  return (
    <button
      type="button"
      className={`completion-row ${selected ? 'selected' : ''}`}
      role="option"
      aria-selected={selected}
      onMouseEnter={() => onSelect(index)}
      onMouseDown={event => event.preventDefault()}
      onClick={onAccept}
    >
      <img src={iconUrl(item.kind)} alt="" />

      <span className="completion-name">
        <strong>{highlightedLabel(item)}</strong>
        <span>{itemSignature.suffix}</span>
      </span>

      {item.returnType && (
        <span className="completion-return">
          {item.returnType}
        </span>
      )}

      {item.owner && (
        <span className="completion-owner">
          {item.owner}
        </span>
      )}
    </button>
  );
});

/* -------------------------------------------------------------------------- */
/* Popup                                                                       */
/* -------------------------------------------------------------------------- */

export function CompletionPopup({
  state,
  onSelect,
  onAccept
}: Props) {
  const popupRef = useRef<HTMLElement>(null);
  const listRef = useRef<HTMLDivElement>(null);
  const sessionRef = useRef(state.requestId);

  const [position, setPosition] = useState(state.anchor);
  const [placement, setPlacement] = useState<'above' | 'below' | null>(null);

  useLayoutEffect(() => {
    if (sessionRef.current === state.requestId) {
      return;
    }

    sessionRef.current = state.requestId;
    setPlacement(null);
  }, [state.requestId]);

  useLayoutEffect(() => {
    const popup = popupRef.current;

    if (!popup) {
      return;
    }

    const bounds = popup.getBoundingClientRect();

    const left = Math.min(
      Math.max(8, state.anchor.left),
      Math.max(8, window.innerWidth - bounds.width - 8)
    );

    if (placement === null) {
      setPlacement(
        state.anchor.top + bounds.height <= window.innerHeight - 8
          ? 'below'
          : 'above'
      );

      return;
    }

    const top = placement === 'below'
      ? Math.min(
          state.anchor.top,
          Math.max(8, window.innerHeight - bounds.height - 8)
        )
      : Math.max(8, state.anchor.top - bounds.height);

    setPosition({ left, top });
  }, [placement, state.anchor, state.items]);

  useLayoutEffect(() => {
    const list = listRef.current;
    const selected = list?.children[state.selectedIndex] as HTMLElement | undefined;

    if (!list || !selected) {
      return;
    }

    const visibleTop = list.scrollTop;
    const visibleBottom = visibleTop + list.clientHeight;

    const selectedTop = selected.offsetTop;
    const selectedBottom = selectedTop + selected.offsetHeight;

    if (selectedTop < visibleTop) {
      list.scrollTop = selectedTop;
    } else if (selectedBottom > visibleBottom) {
      list.scrollTop = selectedBottom - list.clientHeight;
    }
  }, [state.selectedIndex]);

  const selected = state.items[state.selectedIndex];
  const selectedSignature = selected ? signature(selected) : null;

  const detailSections = useMemo(() => {
    if (!selected) {
      return [];
    }

    if (selected.detailSections?.length) {
      return selected.detailSections;
    }

    return fallbackDetailSections(selected);
  }, [selected]);

  const exampleLabel = selected?.exampleLabel
    || (selected?.kind === 'KEYWORD' ? 'Syntax' : 'Example');

  return (
    <section
      ref={popupRef}
      className="completion-popup"
      style={{
        left: position.left,
        top: position.top
      }}
      role="listbox"
      aria-label="Completion suggestions"
    >
      <div
        ref={listRef}
        className="completion-list"
      >
        {state.items.map((item, index) => (
          <CompletionRow
            key={`${item.label}-${index}`}
            item={item}
            index={index}
            selected={index === state.selectedIndex}
            onSelect={onSelect}
            onAccept={onAccept}
          />
        ))}
      </div>

      {selected && selectedSignature && (
        <footer className="completion-details">
          <div className="completion-detail-header">
            <div className="completion-detail-title">
              {selected.returnType && (
                <span>{selected.returnType}</span>
              )}

              <strong>
                {selectedSignature.name}
                <span>{selectedSignature.suffix}</span>
              </strong>
            </div>

            {selected.owner && (
              <div className="completion-detail-owner">
                <img
                  src={iconUrl(selected.kind)}
                  alt=""
                />

                <span>{selected.owner}</span>
              </div>
            )}
          </div>

          {selected.documentation && (
            <p className="completion-detail-documentation">
              {selected.documentation}
            </p>
          )}

          {detailSections.length > 0 && (
            <div className="completion-detail-sections">
              {detailSections.slice(0, 2).map(section => (
                <DetailSection
                  key={section.title}
                  section={section}
                />
              ))}
            </div>
          )}

          {selected.example && (
            <div className="completion-example">
              <span>{exampleLabel}</span>
              <JavaExample source={selected.example} />
            </div>
          )}
        </footer>
      )}
    </section>
  );
}