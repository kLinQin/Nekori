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

const REPLACE_CLASS_NAME = 'nk-replace';
const ORIGINAL_ATTR_NAME = 'data-nk-original';

export function registerReplaceCommands(root: HTMLElement, host: ReaderHost): ReplaceController {
  let disposed = false;
  let observer: MutationObserver | null = null;
  let debounce = 0;
  let currentProfile: ProfileData = { fr: [], hl: [] };
  let currentColors: ReplaceColors | null = null;
  // Saat tap-swap teks, observer dimatikan sementara supaya runDecorations tidak re-apply
  // dan menghapus state swap.
  let suppressObserver = false;
  // Teks replace asli per span (diisi saat pertama kali swap), dipakai untuk balik lagi.
  const replaceTextMap = new WeakMap<HTMLElement, string>();

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
      if (suppressObserver) return;
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

  const onTap = (rawEvent: Event): void => {
    if (disposed) return;
    const event = rawEvent as MouseEvent;
    if (event.defaultPrevented || event.button !== 0) return;
    if (event.metaKey || event.ctrlKey || event.shiftKey || event.altKey) return;
    const target = event.target;
    const element = target instanceof Element ? target : target instanceof Node ? target.parentElement : null;
    const span = element ? element.closest<HTMLElement>('.' + REPLACE_CLASS_NAME) : null;
    if (!span || !root.contains(span)) return;
    const original = span.getAttribute(ORIGINAL_ATTR_NAME);
    if (original === null) return;
    const currentText = span.textContent ?? '';
    const storedReplace = replaceTextMap.get(span);
    // Kalau teks saat ini === original, berarti sedang menampilkan asli → balik ke replace.
    // Kalau belum pernah swap, simpan teks replace sekarang.
    if (currentText === original && storedReplace !== undefined) {
      span.textContent = storedReplace;
    } else {
      if (storedReplace === undefined) {
        replaceTextMap.set(span, currentText);
      }
      span.textContent = original;
    }
    event.preventDefault();
    event.stopImmediatePropagation();
  };

  // Capture + stopImmediatePropagation: hentikan handler lain (mis. anchor
  // fragment resolution) hanya saat target di dalam .nk-replace.
  root.addEventListener('click', onTap, true);

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
      root.removeEventListener('click', onTap, true);
      for (const unsub of unsubs) unsub();
    },
  };
}
