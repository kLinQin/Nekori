import { runDecorations } from './engine.ts';
import type { ProfileData, ReplaceColors } from './types.ts';
import { CHAPTER_TAG_NAME } from '../chapters/metadata.ts';
import type { ReaderHost, Unsubscribe } from '../protocol/reader-api.ts';

export interface ReplaceController {
  apply(profile: ProfileData, colors: ReplaceColors): void;
  dispose(): void;
}

function isProfile(value: unknown): value is ProfileData {
  return typeof value === 'object' && value !== null &&
    Array.isArray((value as ProfileData).fr) &&
    Array.isArray((value as ProfileData).hl);
}

function isColors(value: unknown): value is ReplaceColors {
  if (typeof value !== 'object' || value === null) return false;
  const keys = ['char', 'skill', 'place', 'quote', 'bracket', 'findActive', 'replaceText'];
  return keys.every((k) => typeof (value as Record<string, unknown>)[k] === 'string');
}

export function registerReplaceCommands(root: HTMLElement, host: ReaderHost): ReplaceController {
  let disposed = false;
  let observer: MutationObserver | null = null;
  let debounce = 0;
  let currentProfile: ProfileData = { fr: [], hl: [] };
  let currentColors: ReplaceColors | null = null;

  const applyNow = (): void => {
    if (disposed || !currentColors) return;
    runDecorations(root, currentProfile, currentColors);
  };

  const scheduleApply = (): void => {
    if (disposed) return;
    window.clearTimeout(debounce);
    debounce = window.setTimeout(applyNow, 400);
  };

  const startObserver = (): void => {
    if (observer) return;
    observer = new MutationObserver(() => {
      const chapters = root.querySelectorAll(CHAPTER_TAG_NAME);
      for (const chapter of Array.from(chapters)) {
        if (!chapter.querySelector('.nk-hl, .nk-quote, .nk-replace, .nk-bracket')) {
          scheduleApply();
          return;
        }
      }
    });
    observer.observe(root, { childList: true, subtree: true, characterData: true });
  };

  const unsubs: Unsubscribe[] = [
    host.onCommand('replace.emitTranslate', (payload) => {
      if (typeof payload !== 'object' || payload === null) return;
      const text = (payload as { text?: unknown }).text;
      if (typeof text !== 'string' || text.length === 0) return;
      host.emit('replace.translateSelection', { text });
    }),
    host.onCommand('replace.applyDecorations', (payload) => {
      if (disposed) return;
      if (typeof payload !== 'object' || payload === null) return;
      const p = payload as { profile?: unknown; colors?: unknown };
      if (!isProfile(p.profile) || !isColors(p.colors)) return;
      currentProfile = p.profile;
      currentColors = p.colors;
      applyNow();
    }),
  ];

  startObserver();

  return {
    apply(profile, colors) {
      currentProfile = profile;
      currentColors = colors;
      applyNow();
    },
    dispose() {
      disposed = true;
      observer?.disconnect();
      observer = null;
      window.clearTimeout(debounce);
      for (const unsub of unsubs) unsub();
    },
  };
}
