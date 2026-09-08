import type { LessonEditorCommand, LessonEditorRange, LessonSession } from './protocol';
import { MonacoWorkspaceService } from '../monaco/MonacoWorkspaceService';

export class LessonEditorController {
  private activeUri: string | null = null;
  private previousUri: string | null = null;

  constructor(private readonly service: MonacoWorkspaceService) {}

  lessonUri(): string | null { return this.activeUri; }

  enter(session: LessonSession): void {
    this.exit();
    const uri = `lesson://${session.lessonId}/${session.sessionId}`;
    this.previousUri = this.service.activeModelUri();
    this.activeUri = uri;
    this.service.mountEphemeralModel(uri, '', 'java', true);
    this.apply(session.commands);
  }

  apply(commands: LessonEditorCommand[]): void {
    if (!this.activeUri) return;
    commands.forEach(command => {
      if (command.type === 'SET_CODE') this.service.setEphemeralModelValue(this.activeUri!, command.code ?? '');
      if (command.type === 'HIGHLIGHT_RANGE' && command.range) this.service.setEphemeralDecorations(this.activeUri!, [command.range]);
      if (command.type === 'REVEAL_RANGE' && command.range) this.service.revealEphemeralRange(this.activeUri!, command.range);
      if (command.type === 'CLEAR_HIGHLIGHTS') this.service.clearEphemeralDecorations(this.activeUri!);
    });
  }

  exit(): void {
    if (!this.activeUri) return;
    const uri = this.activeUri;
    const previousUri = this.previousUri;
    this.activeUri = null;
    this.previousUri = null;
    this.service.disposeEphemeralModel(uri);
    if (previousUri) this.service.activate(previousUri);
  }
}
