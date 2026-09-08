import type { LessonEditorCommand, LessonEditorRange, LessonSession } from './protocol';
import { MonacoWorkspaceService } from '../monaco/MonacoWorkspaceService';

export class LessonEditorController {
  private activeUri: string | null = null;
  private previousUri: string | null = null;
  private commandGeneration = 0;
  private presentationReadyHandler: ((ready: boolean) => void) | null = null;

  constructor(private readonly service: MonacoWorkspaceService) {}

  lessonUri(): string | null { return this.activeUri; }

  setPresentationReadyHandler(handler: ((ready: boolean) => void) | null): void {
    this.presentationReadyHandler = handler;
  }

  cancelAnimation(): void {
    this.commandGeneration++;
    this.service.cancelLessonTyping();
    this.presentationReadyHandler?.(false);
  }

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
    this.cancelAnimation();
    const uri = this.activeUri;
    const generation = this.commandGeneration;
    void this.executeCommands(uri, commands, generation);
  }

  exit(): void {
    this.cancelAnimation();
    if (!this.activeUri) return;
    const uri = this.activeUri;
    const previousUri = this.previousUri;
    this.activeUri = null;
    this.previousUri = null;
    this.service.disposeEphemeralModel(uri);
    if (previousUri) this.service.activate(previousUri);
  }

  private async executeCommands(uri: string, commands: LessonEditorCommand[], generation: number): Promise<void> {
    for (const command of commands) {
      if (generation !== this.commandGeneration || this.activeUri !== uri) return;
      if (command.type === 'SET_CODE') this.service.setEphemeralModelValue(uri, command.code ?? '');
      if (command.type === 'ANIMATE_EDIT') {
        const finished = await this.service.animateEphemeralEdit(uri, command.range!, command.replacementText!, command.finalCode!, command.cadenceMillis!);
        if (!finished) return;
      }
      if (command.type === 'HIGHLIGHT_RANGE' && command.range) this.service.setEphemeralDecorations(uri, [command.range]);
      if (command.type === 'REVEAL_RANGE' && command.range) this.service.revealEphemeralRange(uri, command.range);
      if (command.type === 'CLEAR_HIGHLIGHTS') this.service.clearEphemeralDecorations(uri);
    }
    if (generation === this.commandGeneration && this.activeUri === uri) this.presentationReadyHandler?.(true);
  }
}
