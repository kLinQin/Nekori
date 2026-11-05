import { CHAPTER_TAG_NAME } from '../chapters/metadata.ts';
import type { FindReplaceEntry, HighlightEntry, HighlightType, ReplaceColors, ReplaceStore } from './types.ts';

const HIGHLIGHT_CLASS = 'nk-hl';
const HIGHLIGHT_TYPE_ATTR = 'data-nk-hl';
const REPLACE_CLASS = 'nk-replace';
const QUOTE_CLASS = 'nk-quote';
const QUOTE_INNER_CLASS = 'nk-quote-inner';
const BRACKET_CLASS = 'nk-bracket';
const ORIGINAL_ATTR = 'data-nk-original';

const QUOTE_PATTERNS: RegExp[] = [
  /\u201C[\s\S]{1,}?\u201D/g,
  /\u201E[\s\S]{1,}?\u201C/g,
  /(?<![\p{L}\p{N}])\u2018[\s\S]{1,}?\u2019(?![\p{L}\p{N}])/gu,
  /(?<![\p{L}\p{N}])\u2019[\s\S]{1,}?\u2019(?![\p{L}\p{N}])/gu,
  /(?<![\p{L}\p{N}])'[\s\S]{10,}?'(?![\p{L}\p{N}])/gu,
  /"[\s\S]{1,}?"/g,
  /\u00AB[\s\S]{1,}?\u00BB/g,
  /`[\s\S]{1,}?`/g,
];
const BRACKET_PATTERN = /\[[^[\]\n]{1,200}\]/g;

type Seg = {
  text: string;
  quoteClass: string | null;
  hlType: HighlightType | null;
  isReplace: boolean;
  originalText: string | null;
};

function escapeRegex(value: string): string {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

function getReaderChapters(root: HTMLElement): Element[] {
  return Array.from(root.querySelectorAll(CHAPTER_TAG_NAME));
}

export function resetDecorations(root: HTMLElement): void {
  const chapters = getReaderChapters(root);
  for (const chapter of chapters) {
    // Unwrap replaces: gunakan data-original
    const replaces = chapter.querySelectorAll(`.${REPLACE_CLASS}`);
    for (const el of Array.from(replaces)) {
      const orig = el.getAttribute(ORIGINAL_ATTR);
      const text = orig !== null ? orig : (el.textContent ?? '');
      el.replaceWith(el.ownerDocument.createTextNode(text));
    }
    const others = chapter.querySelectorAll(`.${HIGHLIGHT_CLASS}, .${QUOTE_CLASS}, .${BRACKET_CLASS}`);
    for (const el of Array.from(others)) {
      el.replaceWith(el.ownerDocument.createTextNode(el.textContent ?? ''));
    }
    chapter.normalize();
  }
}

function acceptNode(node: Text): number {
  const parent = node.parentElement;
  if (!parent) return NodeFilter.FILTER_REJECT;
  const tag = parent.tagName;
  if (tag === 'SCRIPT' || tag === 'STYLE' || tag === 'NOSCRIPT') return NodeFilter.FILTER_REJECT;
  if (
    parent.classList.contains(HIGHLIGHT_CLASS) ||
    parent.classList.contains(REPLACE_CLASS) ||
    parent.classList.contains(QUOTE_CLASS) ||
    parent.classList.contains(BRACKET_CLASS)
  ) return NodeFilter.FILTER_REJECT;
  return NodeFilter.FILTER_ACCEPT;
}

function applyPattern(segs: Seg[], pattern: RegExp, quoteClass: string): Seg[] {
  const next: Seg[] = [];
  for (const seg of segs) {
    if (seg.quoteClass) { next.push(seg); continue; }
    pattern.lastIndex = 0;
    let last = 0;
    let matched = false;
    let m: RegExpExecArray | null;
    while ((m = pattern.exec(seg.text)) !== null) {
      matched = true;
      if (m.index > last) next.push({ ...seg, text: seg.text.slice(last, m.index) });
      next.push({ ...seg, text: m[0], quoteClass });
      last = m.index + m[0].length;
      if (m[0].length === 0) pattern.lastIndex++;
    }
    if (!matched) { next.push(seg); continue; }
    if (last < seg.text.length) next.push({ ...seg, text: seg.text.slice(last) });
  }
  return next;
}

function applyFR(segs: Seg[], rules: FindReplaceEntry[]): Seg[] {
  let current = segs;
  for (const r of rules) {
    const re = new RegExp(`\\b${escapeRegex(r.find)}\\b`, 'gi');
    const next: Seg[] = [];
    for (const seg of current) {
      if (seg.isReplace) { next.push(seg); continue; }
      re.lastIndex = 0;
      let last = 0;
      let matched = false;
      let m: RegExpExecArray | null;
      while ((m = re.exec(seg.text)) !== null) {
        matched = true;
        if (m.index > last) next.push({ ...seg, text: seg.text.slice(last, m.index) });
        next.push({ ...seg, text: r.replace, isReplace: true, originalText: m[0] });
        last = m.index + m[0].length;
        if (m[0].length === 0) re.lastIndex++;
      }
      if (!matched) { next.push(seg); continue; }
      if (last < seg.text.length) next.push({ ...seg, text: seg.text.slice(last) });
    }
    current = next;
  }
  return current;
}

function applyHL(segs: Seg[], rules: HighlightEntry[]): Seg[] {
  let current = segs;
  for (const r of rules) {
    const re = new RegExp(`\\b${escapeRegex(r.text)}\\b`, 'gi');
    const next: Seg[] = [];
    for (const seg of current) {
      if (seg.hlType) { next.push(seg); continue; }
      re.lastIndex = 0;
      let last = 0;
      let matched = false;
      let m: RegExpExecArray | null;
      while ((m = re.exec(seg.text)) !== null) {
        matched = true;
        if (m.index > last) next.push({ ...seg, text: seg.text.slice(last, m.index) });
        next.push({ ...seg, text: m[0], hlType: r.type });
        last = m.index + m[0].length;
        if (m[0].length === 0) re.lastIndex++;
      }
      if (!matched) { next.push(seg); continue; }
      if (last < seg.text.length) next.push({ ...seg, text: seg.text.slice(last) });
    }
    current = next;
  }
  return current;
}

function buildInline(document: Document, seg: Seg): Node {
  const needsSpan = seg.isReplace || seg.hlType !== null;
  if (!needsSpan) return document.createTextNode(seg.text);
  const span = document.createElement('span');
  if (seg.isReplace) {
    span.classList.add(REPLACE_CLASS);
    if (seg.originalText !== null) span.setAttribute(ORIGINAL_ATTR, seg.originalText);
  }
  if (seg.hlType) {
    span.classList.add(HIGHLIGHT_CLASS);
    span.setAttribute(HIGHLIGHT_TYPE_ATTR, seg.hlType);
  }
  span.textContent = seg.text;
  return span;
}

function processNode(document: Document, node: Text, fr: FindReplaceEntry[], hl: HighlightEntry[]): void {
  const raw = node.nodeValue ?? '';
  if (!raw.trim()) return;
  let segs: Seg[] = [{ text: raw, quoteClass: null, hlType: null, isReplace: false, originalText: null }];

  for (const pat of QUOTE_PATTERNS) segs = applyPattern(segs, pat, QUOTE_CLASS);
  segs = applyPattern(segs, BRACKET_PATTERN, BRACKET_CLASS);
  segs = applyFR(segs, fr);
  segs = applyHL(segs, hl);

  if (segs.length === 1 && !segs[0]!.hlType && !segs[0]!.isReplace && !segs[0]!.quoteClass) return;

  const frag = document.createDocumentFragment();
  let quoteWrapper: HTMLElement | null = null;
  let quoteClass: string | null = null;

  for (const seg of segs) {
    if (seg.quoteClass) {
      if (!quoteWrapper || quoteClass !== seg.quoteClass) {
        quoteWrapper = document.createElement('span');
        quoteWrapper.className = seg.quoteClass;
        quoteClass = seg.quoteClass;
        frag.appendChild(quoteWrapper);
      }
      quoteWrapper.appendChild(buildInline(document, seg));
    } else {
      quoteWrapper = null;
      quoteClass = null;
      frag.appendChild(buildInline(document, seg));
    }
  }
  node.parentNode?.replaceChild(frag, node);
}

export function runDecorations(root: HTMLElement, profile: ReplaceStore['list'][string], colors: ReplaceColors): void {
  resetDecorations(root);
  const frActive = profile.fr.filter((r) => r.enabled && r.find.trim()).sort((a, b) => b.find.length - a.find.length);
  const hlActive = profile.hl.filter((r) => r.enabled && r.text.trim()).sort((a, b) => b.text.length - a.text.length);
  if (frActive.length === 0 && hlActive.length === 0) return;

  for (const chapter of getReaderChapters(root)) {
    const walker = chapter.ownerDocument.createTreeWalker(chapter, NodeFilter.SHOW_TEXT, {
      acceptNode: (n) => acceptNode(n as Text),
    });
    const nodes: Text[] = [];
    let n = walker.nextNode();
    while (n) { nodes.push(n as Text); n = walker.nextNode(); }
    for (const node of nodes) processNode(chapter.ownerDocument, node, frActive, hlActive);
  }

  for (const chapter of getReaderChapters(root)) {
    for (const span of Array.from(chapter.querySelectorAll('.' + QUOTE_CLASS))) {
      if (span.querySelector('.' + QUOTE_INNER_CLASS)) continue;
      const text = span.textContent ?? '';
      if (!text) continue;
      let changed = false;
      const wrapper = span.ownerDocument.createElement('span');
      let last = 0;
      for (const pattern of QUOTE_PATTERNS) {
        pattern.lastIndex = 0;
        let m: RegExpExecArray | null;
        while ((m = pattern.exec(text)) !== null) {
          if (m.index <= 0) continue;
          if (m.index + m[0].length >= text.length) continue;
          if (m.index > last) {
            wrapper.appendChild(span.ownerDocument.createTextNode(text.slice(last, m.index)));
          }
          const inner = span.ownerDocument.createElement('span');
          inner.className = QUOTE_INNER_CLASS;
          inner.textContent = m[0];
          wrapper.appendChild(inner);
          last = m.index + m[0].length;
          changed = true;
          if (m[0].length === 0) pattern.lastIndex++;
        }
      }
      if (!changed) continue;
      if (last < text.length) {
        wrapper.appendChild(span.ownerDocument.createTextNode(text.slice(last)));
      }
      span.replaceChildren(...Array.from(wrapper.childNodes));
    }
  }

  // Apply warna via CSS vars di root, supaya CSS reader-replace.css bisa pakai var.
  const docRoot = root.ownerDocument.documentElement;
  docRoot.style.setProperty('--nk-color-char', colors.char);
  docRoot.style.setProperty('--nk-color-skill', colors.skill);
  docRoot.style.setProperty('--nk-color-place', colors.place);
  docRoot.style.setProperty('--nk-color-quote', colors.quote);
  docRoot.style.setProperty('--nk-color-bracket', colors.bracket);
  docRoot.style.setProperty('--nk-color-replace', colors.replaceText);
}
