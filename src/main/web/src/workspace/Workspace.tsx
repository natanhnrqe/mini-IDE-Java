import { useCallback, useEffect, useRef, useState } from 'react';
import { bridge } from '../bridge/EyeCodeBridge';
import type { ShellBootstrap, WebShellEnvelope } from '../bridge/protocol';
import type { CompletionPopupState } from '../completion/protocol';
import { CompletionPopup } from '../completion/CompletionPopup';
import { EditorDiagnosticStrip } from '../diagnostics/EditorDiagnosticStrip';
import type { DiagnosticsViewState, WebDiagnostic } from '../diagnostics/protocol';
import type { DocumentPayload, DocumentSnapshot } from '../document/protocol';
import { LearningCard } from '../learning/LearningCard';
import type { LearningPopupState } from '../learning/protocol';
import { LearnWorkspace } from '../lessons/LearnWorkspace';
import { LessonAnnotation } from '../lessons/LessonAnnotation';
import { LessonEditorController } from '../lessons/LessonEditorController';
import { LessonPanel } from '../lessons/LessonPanel';
import type { LessonDescriptor, LessonSession } from '../lessons/protocol';
import { MonacoWorkspaceService } from '../monaco/MonacoWorkspaceService';
import { BottomPanel } from './BottomPanel';
import { EditorTabs } from './EditorTabs';
import { EyeCodeIcon } from './EyeCodeIcon';
import { MonacoHost } from './MonacoHost';
import { NewProjectDialog } from './NewProjectDialog';
import { NewJavaClassDialog } from './NewJavaClassDialog';
import { ProjectExplorer } from './ProjectExplorer';
import { StatusBar } from './StatusBar';
import { TopToolbar } from './TopToolbar';
import { WelcomeScreen } from './WelcomeScreen';
import type { ProjectNode, RunState, TerminalState, WorkspaceSnapshot } from './protocol';

type DocumentTab = Omit<DocumentSnapshot, 'content'>;
type BottomPanelId = 'run' | 'terminal' | 'output' | 'problems' | 'git';
type SidePanelId = 'project' | 'search' | 'documentation' | 'settings';
type AppMode = 'WELCOME' | 'PROJECT' | 'LEARN';
type ExplorerOperation = 'createFile' | 'createDirectory' | 'createJavaClass' | 'createPackage' | 'rename' | 'delete' | 'duplicate';
type ExplorerOperationResult = { path?: string; parent?: string; openFile?: boolean; ancestors?: string[] };

const emptyRunState: RunState = { running: false, rerunAvailable: false, configurations: [], selectedConfigurationId: '' };
const emptyTerminalState: TerminalState = { requested: false, running: false, workingDirectory: '' };

export function Workspace() {
  const service = useRef(new MonacoWorkspaceService()).current;
  const lessonEditor = useRef(new LessonEditorController(service)).current;
  const selectCompletion = useRef((index: number) => service.selectCompletion(index)).current;
  const acceptCompletion = useRef(() => service.acceptSelectedCompletion()).current;
  const [documents, setDocuments] = useState<DocumentTab[]>([]);
  const [activeUri, setActiveUri] = useState<string | null>(null);
  const [connected, setConnected] = useState(false);
  const [bootstrap, setBootstrap] = useState<ShellBootstrap | null>(null);
  const [message, setMessage] = useState('');
  const [completion, setCompletion] = useState<CompletionPopupState | null>(null);
  const [diagnostics, setDiagnostics] = useState<DiagnosticsViewState | null>(null);
  const [learning, setLearning] = useState<LearningPopupState | null>(null);
  const [workspace, setWorkspace] = useState<WorkspaceSnapshot>({ recentProjects: [] });
  const [childrenByPath, setChildrenByPath] = useState<Record<string, ProjectNode[]>>({});
  const childrenCache = useRef<Record<string, ProjectNode[]>>({});
  const pendingChildren = useRef(new Map<string, Promise<void>>());
  const [treeChangedPath, setTreeChangedPath] = useState<string | undefined>(undefined);
  const [treeRefreshRevision, setTreeRefreshRevision] = useState(0);
  const [runState, setRunState] = useState<RunState>(emptyRunState);
  const [runOutput, setRunOutput] = useState<string[]>([]);
  const [terminalState, setTerminalState] = useState<TerminalState>(emptyTerminalState);
  const [bottomPanel, setBottomPanel] = useState<BottomPanelId>('terminal');
  const [sidePanel, setSidePanel] = useState<SidePanelId>('project');
  const [caret, setCaret] = useState({ line: 1, column: 1 });
  const [newProjectOpen, setNewProjectOpen] = useState(false);
  const [newJavaClassOpen, setNewJavaClassOpen] = useState(false);
  const [mode, setMode] = useState<AppMode>('WELCOME');
  const [selectedLearnLesson, setSelectedLearnLesson] = useState<LessonDescriptor | null>(null);
  const [selectedLearnRoadmapItemId, setSelectedLearnRoadmapItemId] = useState<string | null>(null);
  const [learnPath, setLearnPath] = useState<string[]>(['Java', 'Fundamentos', 'Tipos Primitivos']);
  const [lessonSession, setLessonSession] = useState<LessonSession | null>(null);
  const lessonSessionRef = useRef<LessonSession | null>(null);
  const lessonRequest = useRef(0);
  const [lessonBusy, setLessonBusy] = useState(false);

  useEffect(() => () => service.dispose(), [service]);

  const updateDocument = useCallback((document: DocumentSnapshot) => {
    const tab: DocumentTab = { ...document };
    delete (tab as Partial<DocumentSnapshot>).content;
    setDocuments(items => {
      const index = items.findIndex(item => item.uri === tab.uri);
      if (index < 0) return [...items, tab];
      const next = [...items];
      next[index] = tab;
      return next;
    });
  }, []);

  const replaceChildren = useCallback((update: (current: Record<string, ProjectNode[]>) => Record<string, ProjectNode[]>) => {
    const next = update(childrenCache.current);
    childrenCache.current = next;
    setChildrenByPath(next);
  }, []);

  const loadChildren = useCallback((path: string, force = false): Promise<void> => {
    if (!force && childrenCache.current[path] !== undefined) return Promise.resolve();
    const pending = pendingChildren.current.get(path);
    if (pending) return pending;
    const request = bridge.request<{ parent: string; children: ProjectNode[] }>('workspace', 'children', { path })
      .then(response => replaceChildren(current => ({ ...current, [response.parent]: response.children })))
      .catch(error => { setMessage(formatError(error)); throw error; })
      .finally(() => pendingChildren.current.delete(path));
    pendingChildren.current.set(path, request);
    return request;
  }, [replaceChildren]);

  const refreshWorkspace = useCallback(async () => {
    try {
      const snapshot = await bridge.request<WorkspaceSnapshot>('workspace', 'snapshot', {});
      setWorkspace(snapshot);
      if (snapshot.project) void loadChildren(snapshot.project.root.path);
    } catch (error) { setMessage(formatError(error)); }
  }, [loadChildren]);

  useEffect(() => {
    service.setDocumentChangeHandler(document => { updateDocument(document); if (document.dirty) setMessage(''); });
    service.setCaretPositionHandler(setCaret);
    service.setErrorHandler(setMessage);
    service.setCompletionStateHandler(setCompletion);
    service.setDiagnosticsStateHandler(setDiagnostics);
    service.setLearningStateHandler(setLearning);
    return () => {
      service.setCaretPositionHandler(null);
      service.setCompletionStateHandler(null);
      service.setDiagnosticsStateHandler(null);
      service.setLearningStateHandler(null);
    };
  }, [service, updateDocument]);

  useEffect(() => {
    const unsubscribe = bridge.subscribe((event: WebShellEnvelope) => {
      if (event.channel === 'shell' && event.name === 'bootstrap') {
        setBootstrap(event.payload as ShellBootstrap);
        setConnected(true);
        void refreshWorkspace();
        void refreshRunState();
        void refreshTerminalState();
      }
      if (event.channel === 'workspace' && event.name === 'changed') {
        const next = event.payload as WorkspaceSnapshot;
        setWorkspace(next);
        childrenCache.current = {};
        pendingChildren.current.clear();
        setChildrenByPath({});
        setTreeChangedPath(undefined);
        setTreeRefreshRevision(0);
        service.clearDiagnostics();
      }
      if (event.channel === 'workspace' && event.name === 'reset') {
        const session = lessonSessionRef.current;
        if (session) {
          void bridge.request('lessons', 'session/close', { sessionId: session.sessionId });
          lessonEditor.exit();
          lessonSessionRef.current = null;
          setLessonSession(null);
        }
        service.resetWorkspace();
        setDocuments([]);
        setActiveUri(null);
        setCompletion(null);
        setLearning(null);
        setDiagnostics(null);
        setMessage('');
        setMode('WELCOME');
      }
      if (event.channel === 'workspace' && event.name === 'treeChanged') {
        const parent = String((event.payload as { parent?: string }).parent ?? '');
        if (parent) {
          setTreeChangedPath(parent);
          setTreeRefreshRevision(current => current + 1);
        }
      }
      if (event.channel === 'run' && event.name === 'state') setRunState(event.payload as RunState);
      if (event.channel === 'run' && event.name === 'output') {
        const payload = event.payload as { line?: string; error?: boolean; clear?: boolean };
        if (payload.clear) {
          setRunOutput([]);
          setBottomPanel('run');
          return;
        }
        const line = payload.line;
        if (line) {
          setRunOutput(lines => [...lines, payload.error ? `[stderr] ${line}` : line]);
          setBottomPanel('run');
        }
      }
      if (event.channel === 'terminal' && event.name === 'state') setTerminalState(event.payload as TerminalState);
      if (event.channel !== 'document') return;
      const payload = event.payload as DocumentPayload;
      if (event.name === 'closed') {
        const uri = String(payload.uri ?? '');
        service.close(uri);
        setDocuments(items => items.filter(item => item.uri !== uri));
        setActiveUri(current => current === uri ? null : current);
        return;
      }
      if (event.name === 'activeChanged') {
        const uri = String(payload.uri ?? '');
        setActiveUri(uri);
        service.activate(uri);
        return;
      }
      if (event.name === 'reidentified') {
        const previousUri = String(payload.previousUri ?? '');
        const document = payload.document;
        if (!document?.uri || !previousUri) return;
        service.reidentify(previousUri, document);
        updateDocument(document);
        setDocuments(items => items.filter(item => item.uri !== previousUri));
        setActiveUri(current => current === previousUri ? document.uri : current);
        return;
      }
      const document = event.name === 'saved' || event.name === 'saveFailed' ? payload.document : payload as DocumentSnapshot;
      if (!document?.uri) return;
      if (event.name === 'opened' && document.kind === 'documentation') {
        updateDocument(document);
        return;
      }
      if (service.apply(document, event.name === 'opened' || event.name === 'externalChanged')) updateDocument(document);
      if (event.name === 'saveFailed') setMessage('Could not save the document');
    });
    bridge.emit('shell', 'ready', {});
    return unsubscribe;
  }, [lessonEditor, loadChildren, refreshWorkspace, service, updateDocument]);

  useEffect(() => {
    const region = document.querySelector<HTMLElement>('.editor-region');
    if (!region) return;
    const reportLayout = () => {
      const rect = region.getBoundingClientRect();
      bridge.emit('document', 'layout', {
        x: rect.left,
        y: rect.top,
        width: rect.width,
        height: rect.height,
      });
      service.layout();
    };
    const observer = new ResizeObserver(reportLayout);
    observer.observe(region);
    reportLayout();
    window.addEventListener('resize', reportLayout);
    return () => {
      observer.disconnect();
      window.removeEventListener('resize', reportLayout);
    };
  }, [activeUri, bottomPanel, sidePanel, documents.length, mode, service]);

  useEffect(() => {
    const initialFile = bootstrap?.initialFile;
    if (initialFile) void bridge.request('document', 'open', { path: initialFile }).catch(error => setMessage(formatError(error)));
  }, [bootstrap]);

  async function refreshRunState() {
    try { setRunState(await bridge.request<RunState>('run', 'state', {})); }
    catch (error) { setMessage(formatError(error)); }
  }

  async function refreshTerminalState() {
    try { setTerminalState(await bridge.request<TerminalState>('terminal', 'state', {})); }
    catch (error) { setMessage(formatError(error)); }
  }

  async function openProject(path?: string) {
    try {
      const snapshot = await bridge.request<WorkspaceSnapshot>('workspace', 'openProject', path ? { path } : {}, { timeoutMs: null });
      if (snapshot.project) {
        setWorkspace(snapshot);
        childrenCache.current = {};
        pendingChildren.current.clear();
        setChildrenByPath({});
        setTreeChangedPath(undefined);
        setTreeRefreshRevision(0);
        service.clearDiagnostics();
        setMode('PROJECT');
      }
      setMessage('');
    } catch (error) { setMessage(formatError(error)); }
  }

  async function chooseProjectLocation(): Promise<string | undefined> {
    const response = await bridge.request<{ path?: string; cancelled?: boolean }>('workspace', 'chooseDirectory', {}, { timeoutMs: null });
    return response.path;
  }

  async function createProject(request: { name: string; location: string; groupId: string }) {
    const snapshot = await bridge.request<WorkspaceSnapshot>('workspace', 'createProject', request, { timeoutMs: null });
    if (snapshot.project) {
      setWorkspace(snapshot);
      childrenCache.current = {};
      pendingChildren.current.clear();
      setChildrenByPath({});
      setTreeChangedPath(undefined);
      setTreeRefreshRevision(0);
      service.clearDiagnostics();
      setMode('PROJECT');
      setNewProjectOpen(false);
      setMessage('');
    }
  }

  async function newDocument() {
    try { await bridge.request('document', 'new', {}); setMessage(''); }
    catch (error) { setMessage(formatError(error)); }
  }

  async function createJavaClass(name: string) {
    const root = workspace.project?.root.path;
    if (!root) return;
    const result = await operateProject('createJavaClass', root, name);
    if (result.path && result.openFile) await openFile(result.path);
    setNewJavaClassOpen(false);
  }

  async function openFile(path: string) {
    try { await bridge.request('workspace', 'openFile', { path }); setMessage(''); }
    catch (error) { setMessage(formatError(error)); }
  }

  async function refreshProject(paths: string[]): Promise<string[]> {
    try {
      const snapshot = await bridge.request<WorkspaceSnapshot>('workspace', 'refresh', { paths });
      setWorkspace(snapshot);
      const validPaths = snapshot.validPaths ?? [];
      setTreeChangedPath(validPaths[0]);
      setTreeRefreshRevision(current => current + 1);
      return validPaths;
    } catch (error) {
      setMessage(formatError(error));
      return [];
    }
  }

  async function operateProject(operation: ExplorerOperation, target: string, name?: string): Promise<ExplorerOperationResult> {
    try {
      const result = await bridge.request<ExplorerOperationResult>('workspace', operation, {
        target,
        ...(name ? { name } : {}),
      });
      if (result.parent) void loadChildren(result.parent, true);
      setMessage('');
      return result;
    } catch (error) {
      setMessage(formatError(error));
      throw error;
    }
  }

  async function activate(uri: string): Promise<boolean> {
    try { await bridge.request('document', 'activate', { uri }); return true; }
    catch (error) { setMessage(formatError(error)); return false; }
  }

  async function navigateProblem(uri: string, diagnostic: WebDiagnostic) {
    if (await activate(uri)) service.revealDiagnostic(uri, diagnostic);
  }
  async function close(uri: string) {
    try { await bridge.request('document', 'close', { uri }); }
    catch (error) { setMessage(formatError(error)); }
  }

  async function run(name: 'run' | 'rerun' | 'stop') {
    try { if (name === 'run') setRunOutput([]); await bridge.request('run', name, {}); }
    catch (error) { setMessage(formatError(error)); }
  }

  async function terminal(name: 'show' | 'restart' | 'stop') {
    try {
      await bridge.request('terminal', name, {});
      setMessage('');
    } catch (error) { setMessage(formatError(error)); }
  }

  function selectBottomPanel(id: BottomPanelId) {
    setBottomPanel(id);
    if (id === 'terminal' && !terminalState.running) void terminal('show');
  }

  async function selectConfiguration(id: string) {
    try { await bridge.request('run', 'selectConfiguration', { id }); }
    catch (error) { setMessage(formatError(error)); }
  }

  async function windowAction(action: 'windowMinimize' | 'windowToggleMaximize' | 'windowClose') {
    try { await bridge.request('native', action, {}); }
    catch (error) { setMessage(formatError(error)); }
  }

  function openLessons() {
    if (mode !== 'LEARN') {
      service.clearActiveModel();
    }
    setMode('LEARN');
  }

  async function leaveProject() {
    if (lessonSessionRef.current) await closeLesson();
    service.clearActiveModel();
    setMode('WELCOME');
  }

  function selectSidePanel(id: SidePanelId) {
    if (lessonSession) void closeLesson();
    setSidePanel(id);
  }

  async function startLesson(lesson: LessonDescriptor) {
    const requestId = ++lessonRequest.current;
    setLessonBusy(true);
    try {
      const session = await bridge.request<LessonSession>('lessons', 'session/start', { lessonId: lesson.id });
      if (requestId !== lessonRequest.current) return;
      lessonEditor.enter(session);
      lessonSessionRef.current = session;
      setLessonSession(session);
      setMessage('');
    } catch (error) { if (requestId === lessonRequest.current) setMessage(formatError(error)); }
    finally { if (requestId === lessonRequest.current) setLessonBusy(false); }
  }

  async function changeLessonStep(action: 'next' | 'previous') {
    const current = lessonSessionRef.current;
    if (!current || lessonBusy) return;
    const requestId = ++lessonRequest.current;
    setLessonBusy(true);
    try {
      const session = await bridge.request<LessonSession>('lessons', `session/${action}`, { sessionId: current.sessionId });
      if (requestId !== lessonRequest.current || lessonSessionRef.current?.sessionId !== session.sessionId) return;
      lessonEditor.apply(session.commands);
      lessonSessionRef.current = session;
      setLessonSession(session);
    } catch (error) { if (requestId === lessonRequest.current) setMessage(formatError(error)); }
    finally { if (requestId === lessonRequest.current) setLessonBusy(false); }
  }

  async function closeLesson() {
    const session = lessonSessionRef.current;
    if (!session) return;
    ++lessonRequest.current;
    setLessonBusy(false);
    try { await bridge.request('lessons', 'session/close', { sessionId: session.sessionId }); }
    catch (error) { setMessage(formatError(error)); }
    finally { lessonEditor.exit(); lessonSessionRef.current = null; setLessonSession(null); }
  }

  async function leaveLearn() {
    await closeLesson();
    setMode('WELCOME');
  }

  const selectLearnLesson = useCallback((lesson: LessonDescriptor | null, path: string[], roadmapItemId: string | null) => {
    setSelectedLearnLesson(lesson);
    setLearnPath(path);
    setSelectedLearnRoadmapItemId(roadmapItemId);
  }, []);

  const activeDocument = documents.find(document => document.uri === activeUri);
  const activeEditorDocument = activeDocument?.kind === 'documentation' ? undefined : activeDocument;
  const projectMode = mode === 'PROJECT';
  const learnMode = mode === 'LEARN';
  const toolbar = <TopToolbar projectName={projectMode ? workspace.project?.name : undefined} projectPath={projectMode ? workspace.project?.path : undefined} recentProjects={workspace.recentProjects} runState={runState}
    onNewProject={() => setNewProjectOpen(true)} onOpenProject={() => void openProject()} onNewFile={() => void newDocument()}
    onOpenRecentProject={path => void openProject(path)} onWelcome={() => void leaveProject()} onRun={() => void run('run')} onRerun={() => void run('rerun')}
    onStop={() => void run('stop')} onSelectConfiguration={id => void selectConfiguration(id)}
    onOpenSearch={() => setSidePanel('search')} onOpenSettings={() => setSidePanel('settings')}
    onWindowAction={action => void windowAction(action)} />;
  return <main className="app-shell">
    {toolbar}
    <div className={`shell-workspace${mode === 'WELCOME' ? ' is-welcome' : ''}`} aria-hidden={mode === 'WELCOME'}>
      {projectMode ? <nav className="activity-bar" aria-label="Workspace views">
        {(['project', 'search', 'documentation', 'settings'] as SidePanelId[]).map(id => <button key={id}
          type="button" className={sidePanel === id ? 'is-active' : ''} onClick={() => selectSidePanel(id)} aria-label={id}><EyeCodeIcon name={sideIcon(id)} /></button>)}
      </nav> : <nav className="activity-bar learn-activity-bar" aria-hidden="true" />}
      <aside className="side-panel">
        {learnMode ? <LearnWorkspace selectedRoadmapItemId={selectedLearnRoadmapItemId} onLessonSelected={selectLearnLesson} /> : sidePanel === 'project' ? <ProjectExplorer project={workspace.project} childrenByPath={childrenByPath}
          reveal={workspace.reveal} treeChangedPath={treeChangedPath} treeRefreshRevision={treeRefreshRevision} onLoadChildren={loadChildren} onOpenFile={openFile}
          onRefresh={refreshProject} onOperation={operateProject} onOpenProject={() => void openProject()} onNewFile={() => void newDocument()} /> : <section className="auxiliary-panel">
          <header className="panel-heading"><span>{sideTitle(sidePanel)}</span></header>
          <div className="toolwindow-placeholder"><strong>{sideTitle(sidePanel)}</strong>
            <span>This shell view is composed and ready for its dedicated service integration.</span></div>
        </section>}
      </aside>
      <section className="main-workspace">
        <div className="editor-stack">
          {projectMode ? <EditorTabs documents={documents} activeUri={activeUri} onActivate={uri => void activate(uri)} onClose={uri => void close(uri)} /> : <header className="document-tabs learn-editor-tabs">{learnPath.join(' / ')}</header>}
          <section className="editor-region">
            {projectMode && !documents.length && <div className="workspace-empty"><div className="empty-mark">EC</div><strong>Start coding</strong>
              <span>Open a file from Project panel or create something new.</span><div><button type="button" className="primary-action" onClick={() => setNewJavaClassOpen(true)}>New Java Class</button></div></div>}
            {learnMode && !lessonSession && <div className="workspace-empty"><div className="empty-mark">EC</div><strong>{selectedLearnLesson?.title ?? 'Tipos Primitivos'}</strong><span>Inicie a aula para carregar o exemplo no editor.</span>{selectedLearnLesson?.executable && <button type="button" className="primary-action" onClick={() => startLesson(selectedLearnLesson)}>Iniciar aula</button>}</div>}
            <MonacoHost service={service} />
            {projectMode && <EditorDiagnosticStrip state={diagnostics} onNavigate={navigateProblem} />}
          </section>
        </div>
      </section>
      {projectMode ? <BottomPanel active={bottomPanel} output={runOutput} terminalState={terminalState}
        diagnostics={diagnostics} documents={documents} onSelect={selectBottomPanel}
        onNavigateProblem={(uri, diagnostic) => void navigateProblem(uri, diagnostic)} /> : lessonSession ? <LessonPanel session={lessonSession} onPrevious={() => void changeLessonStep('previous')}
          onNext={() => void changeLessonStep('next')} onExit={() => void leaveLearn()} busy={lessonBusy} /> : <section className="bottom-panel lesson-preview-panel"><header className="bottom-tabs"><strong>Aula</strong></header><div className="bottom-panel-content"><strong>{selectedLearnLesson?.title ?? 'Tipos Primitivos'}</strong><p>{selectedLearnLesson?.description ?? 'Selecione uma aula no roteiro para começar.'}</p></div></section>}
    </div>
    {projectMode ? <StatusBar activeUri={activeEditorDocument?.uri} displayName={activeEditorDocument?.displayName}
      projectRoot={workspace.project?.root.path} projectName={workspace.project?.name} caret={caret} message={message} />
      : <div className="shell-status-spacer" />}
    {mode === 'WELCOME' && <section className="welcome-mode"><WelcomeScreen recentProjects={workspace.recentProjects} onNewProject={() => setNewProjectOpen(true)} onOpenProject={() => void openProject()}
      onOpenRecentProject={path => void openProject(path)} onLessons={openLessons} /></section>}
    <div className="overlay-root">
      {completion && <CompletionPopup state={completion} onSelect={selectCompletion} onAccept={acceptCompletion} />}
      {learning && <LearningCard state={learning} onNavigate={identifier => service.navigateLearning(identifier)}
        onAction={action => service.openLearningAction(action)} onHover={hovered => service.setLearningHovered(hovered)} />}
      {learnMode && lessonSession && <LessonAnnotation service={service} lessonUri={lessonEditor.lessonUri()} annotation={lessonSession.annotation} />}
      {newProjectOpen && <NewProjectDialog onCancel={() => setNewProjectOpen(false)} onBrowse={chooseProjectLocation} onCreate={createProject} />}
      {newJavaClassOpen && <NewJavaClassDialog onCancel={() => setNewJavaClassOpen(false)} onCreate={createJavaClass} />}
    </div>
  </main>;
}

function sideIcon(id: SidePanelId): string {
  return ({ project: 'project', search: 'search', documentation: 'markdown', settings: 'settings' })[id];
}

function sideTitle(id: SidePanelId): string {
  return ({ project: 'Project', search: 'Search', documentation: 'Documentation', settings: 'Settings' })[id];
}

function formatError(error: unknown): string {
  if (error && typeof error === 'object' && 'code' in error && 'message' in error) {
    const value = error as { code: string; message: string };
    return `${value.code}: ${value.message}`;
  }
  return error instanceof Error ? error.message : String(error);
}
