import { audioLibraryService } from '../services/audioLibraryService';
import { readLocalTracks } from '../services/localLibrary';

beforeEach(() => {
  vi.stubEnv('VITE_API_BASE_URL', '');
  localStorage.clear();
});
afterEach(() => vi.unstubAllGlobals());

it('stores audio larger than a typical localStorage quota in IndexedDB', async () => {
  const track = await audioLibraryService.uploadTrack({
    title: 'Large story',
    author: 'Test',
    duration: 12,
    url: 'data:audio/mpeg;base64,' + 'A'.repeat(6 * 1024 * 1024),
  });
  expect((await readLocalTracks()).find((item) => item.id === track.id)?.url).toBe(track.url);
  expect(localStorage.getItem('audiolibrary.tracks')).toBeNull();
});

it('does not turn server upload failures into local successes', async () => {
  vi.stubEnv('VITE_API_BASE_URL', 'http://localhost:8080');
  vi.stubGlobal(
    'fetch',
    vi.fn().mockResolvedValue(new Response('Service unavailable', { status: 503 })),
  );
  await expect(
    audioLibraryService.uploadTrack({
      title: 'Story',
      author: 'Test',
      duration: 12,
      url: 'data:audio/mpeg;base64,SUQz',
    }),
  ).rejects.toThrow('Service unavailable');
});

it('adapts the Java catalog contract and deduplicates shared books', async () => {
  vi.stubEnv('VITE_API_BASE_URL', 'http://localhost:8080');
  const book = {
    idAudiolibro: 42,
    titolo: 'Story',
    autore: 'Author',
    descrizione: '',
    durata: 30,
    audio: null,
    creatore: { username: 'listener' },
    dataInserimento: '2026-01-01',
    ultimoAscolto: { secondi: 12, updatedAt: '2026-01-02T12:00:00Z' },
  };
  const fetch = vi
    .fn()
    .mockImplementation(() => Promise.resolve(new Response(JSON.stringify([book]))));
  vi.stubGlobal('fetch', fetch);
  expect(await audioLibraryService.fetchLibrary()).toEqual([
    expect.objectContaining({
      id: '42',
      title: 'Story',
      author: 'Author',
      duration: 30,
      url: '/audiolibro/42/audio',
      resumePosition: 12,
    }),
  ]);
  expect(fetch).toHaveBeenCalledTimes(2);
});
