const KEYWORDS = new Set([
  'abstract', 'assert', 'boolean', 'break', 'byte', 'case', 'catch', 'char', 'class',
  'const', 'continue', 'default', 'do', 'double', 'else', 'enum', 'extends', 'final',
  'finally', 'float', 'for', 'if', 'implements', 'import', 'instanceof', 'int',
  'interface', 'long', 'native', 'new', 'package', 'private', 'protected', 'public',
  'record', 'return', 'sealed', 'short', 'static', 'strictfp', 'super', 'switch',
  'synchronized', 'this', 'throw', 'throws', 'transient', 'try', 'var', 'void',
  'volatile', 'while', 'yield', 'permits', 'non-sealed', 'true', 'false', 'null'
]);

type JavaTokenKind = 'comment' | 'string' | 'keyword' | 'type' | 'number' | 'plain';

function tokenKind(value: string): JavaTokenKind {
  if (value.startsWith('//') || value.startsWith('/*')) return 'comment';
  if (value.startsWith('"') || value.startsWith("'")) return 'string';
  if (KEYWORDS.has(value)) return 'keyword';
  if (/^[A-Z][A-Za-z0-9_$]*$/.test(value)) return 'type';
  if (/^\d/.test(value)) return 'number';
  return 'plain';
}

function tokenizeExample(source: string): Array<{ value: string; kind: JavaTokenKind }> {
  const tokens: Array<{ value: string; kind: JavaTokenKind }> = [];
  const pattern = /\/\/[^\n]*|\/\*[\s\S]*?\*\/|"(?:\\.|[^"\\])*"|'(?:\\.|[^'\\])*'|\b\d+(?:\.\d+)?\b|[A-Za-z_$][\w$-]*/g;
  let cursor = 0;
  for (const match of source.matchAll(pattern)) {
    const value = match[0];
    const index = match.index ?? cursor;
    if (index > cursor) tokens.push({ value: source.slice(cursor, index), kind: 'plain' });
    tokens.push({ value, kind: tokenKind(value) });
    cursor = index + value.length;
  }
  if (cursor < source.length) tokens.push({ value: source.slice(cursor), kind: 'plain' });
  return tokens;
}

export function JavaExample({ source }: { source: string }) {
  return (
    <pre className="completion-example-code"><code>{tokenizeExample(source).map((token, index) => (
      <span key={`${index}-${token.value}`} className={`completion-code-${token.kind}`}>{token.value}</span>
    ))}</code></pre>
  );
}
