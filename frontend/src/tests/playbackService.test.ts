import { playbackService } from '../services/playbackService';
import { PlaybackProgress } from '../types/audio';

describe('playbackService', () => {
  beforeEach(() => {
    window.localStorage.clear();
    vi.stubEnv('VITE_API_BASE_URL', '');
  });

  it('returns an empty progress object when no cache exists', async () => {
    const progress = await playbackService.loadProgress('user');
    expect(progress).toEqual({ trackId: null, position: 0, updatedAt: expect.any(String) });
  });

  it('persists progress to local storage', async () => {
    const payload: PlaybackProgress = {
      trackId: '123',
      position: 42,
      updatedAt: new Date().toISOString(),
    };

    await playbackService.saveProgress('user', payload);
    const cached = playbackService.readCache('user');

    expect(cached).toEqual(payload);
  });
});

it('isolates progress between listeners', async () => {
  vi.stubEnv('VITE_API_BASE_URL', '');
  await playbackService.saveProgress('alice', {
    trackId: '1',
    position: 40,
    updatedAt: new Date().toISOString(),
  });
  expect(playbackService.readCache('bob').trackId).toBeNull();
});

it('keeps per-track positions when switching stories', async () => {
  vi.stubEnv('VITE_API_BASE_URL', '');
  await playbackService.saveProgress('listener', {
    trackId: 'first',
    position: 42,
    updatedAt: new Date().toISOString(),
  });
  await playbackService.saveProgress('listener', {
    trackId: 'second',
    position: 7,
    updatedAt: new Date().toISOString(),
  });
  expect(playbackService.readCache('listener', 'first').position).toBe(42);
  expect(playbackService.readCache('listener').position).toBe(7);
});

it('prefers newer remote progress over an older local position', async () => {
  vi.stubEnv('VITE_API_BASE_URL', '');
  await playbackService.saveProgress('listener', {
    trackId: 'story',
    position: 5,
    updatedAt: '2026-01-01T00:00:00Z',
  });
  const result = await playbackService.loadProgress('listener', [
    {
      id: 'story',
      title: 'Story',
      author: 'Test',
      duration: 100,
      url: '',
      uploadedAt: '',
      resumePosition: 30,
      lastPlayedAt: '2026-01-02T00:00:00Z',
    },
  ]);
  expect(result.position).toBe(30);
});
