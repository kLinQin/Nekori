export type HighlightType = 'char' | 'skill' | 'place';

export interface FindReplaceEntry {
  find: string;
  replace: string;
  enabled: boolean;
}

export interface HighlightEntry {
  text: string;
  type: HighlightType;
  enabled: boolean;
}

export interface ProfileData {
  fr: FindReplaceEntry[];
  hl: HighlightEntry[];
}

export interface ReplaceColors {
  char: string;
  skill: string;
  place: string;
  quote: string;
  bracket: string;
  findActive: string;
  replaceText: string;
}

export interface ReplaceStore {
  active: string;
  list: Record<string, ProfileData>;
}

export const DEFAULT_COLORS: ReplaceColors = {
  char: '#FB7185',
  skill: '#E879F9',
  place: '#5EEAD4',
  quote: '#FDBA74',
  bracket: '#818CF8',
  findActive: '#ef5350',
  replaceText: '#38BDF8',
};

export const DEFAULT_PROFILE_NAME = 'default';

export function emptyStore(): ReplaceStore {
  return {
    active: DEFAULT_PROFILE_NAME,
    list: { [DEFAULT_PROFILE_NAME]: { fr: [], hl: [] } },
  };
}

export function emptyColors(): ReplaceColors {
  return { ...DEFAULT_COLORS };
}

export function ensureProfile(store: ReplaceStore, name: string): ProfileData {
  const existing = store.list[name];
  if (existing) return existing;
  const created: ProfileData = { fr: [], hl: [] };
  store.list[name] = created;
  return created;
}
