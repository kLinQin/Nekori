import type { ReaderHost, Unsubscribe } from '../protocol/reader-api.ts';

export function registerSelectionWatch(root: HTMLElement, host: ReaderHost): Unsubscribe {
  let debounce = 0;
  let lastText = '';

  const emit = (text: string): void => {
    if (text === lastText) return;
    lastText = text;
    host.emit('replace.selectionChanged', { text });
  };

  const update = (): void => {
    const sel = root.ownerDocument.getSelection();
    if (!sel || sel.isCollapsed || sel.rangeCount === 0) {
      emit('');
      return;
    }
    const text = sel.toString().trim();
    if (text.length < 2) {
      emit('');
      return;
    }
    const anchor = sel.anchorNode;
    if (!anchor || (!root.contains(anchor) && !root.contains(anchor.parentNode))) {
      emit('');
      return;
    }
    emit(text);
  };

  const onChange = (): void => {
    window.clearTimeout(debounce);
    debounce = window.setTimeout(update, 250);
  };

  root.ownerDocument.addEventListener('selectionchange', onChange);

  return () => {
    window.clearTimeout(debounce);
    root.ownerDocument.removeEventListener('selectionchange', onChange);
  };
}
