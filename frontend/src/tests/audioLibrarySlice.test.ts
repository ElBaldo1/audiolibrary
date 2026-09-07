import reducer, {
  AudioLibraryState,
  fetchLibrary,
  setLibrary,
} from '../store/slices/audioLibrarySlice';
import { AudioTrack } from '../types/audio';

describe('audioLibrarySlice', () => {
  const track: AudioTrack = {
    id: '1',
    title: 'Test',
    author: 'Author',
    description: 'Desc',
    duration: 120,
    url: 'https://example.com/audio.mp3',
    uploadedAt: new Date().toISOString(),
  };

  it('handles initial state', () => {
    const initial = reducer(undefined, { type: 'unknown' });
    expect(initial).toEqual({ items: [], status: 'idle' });
  });

  it('sets library manually', () => {
    const state: AudioLibraryState = { items: [], status: 'idle' };
    const result = reducer(state, setLibrary([track]));
    expect(result.items).toHaveLength(1);
  });

  it('handles fetch success', () => {
    const state: AudioLibraryState = { items: [], status: 'idle' };
    const action = { type: fetchLibrary.fulfilled.type, payload: [track] };
    const result = reducer(state, action);
    expect(result.status).toBe('succeeded');
    expect(result.items).toEqual([track]);
  });

  it('handles fetch failure', () => {
    const state: AudioLibraryState = { items: [], status: 'idle' };
    const action = { type: fetchLibrary.rejected.type, error: { message: 'error' } };
    const result = reducer(state, action);
    expect(result.status).toBe('failed');
    expect(result.error).toBe('error');
  });
});

it('ignores stale catalog responses after the session is reset', () => {
  const state: AudioLibraryState = { items: [], status: 'idle' };
  const result = reducer(state, fetchLibrary.fulfilled([], 'previous-session'));
  expect(result).toEqual(state);
});
