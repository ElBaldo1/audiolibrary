import { AudioTrack } from '../types/audio';
import { apiRequest, isRemoteMode } from './apiClient';
import { readLocalTracks, writeLocalTrack } from './localLibrary';

interface ApiTrack {
  idAudiolibro: number;
  titolo: string;
  autore?: string;
  descrizione: string;
  durata?: number;
  audio: string | null;
  mimeType?: string;
  dataInserimento: string;
  creatore: { username: string };
  ultimoAscolto: { secondi: number; data: string | null; updatedAt?: string | null };
}

const toTrack = (book: ApiTrack): AudioTrack => ({
  id: String(book.idAudiolibro),
  title: book.titolo,
  author: book.autore || book.creatore.username,
  description: book.descrizione,
  duration: book.durata ?? 0,
  url: book.audio
    ? `data:${book.mimeType || 'audio/mpeg'};base64,${book.audio}`
    : `/audiolibro/${book.idAudiolibro}/audio`,
  uploadedAt: book.dataInserimento,
  resumePosition: book.ultimoAscolto?.secondi ?? 0,
  lastPlayedAt: book.ultimoAscolto?.updatedAt ?? book.ultimoAscolto?.data ?? undefined,
});

const demo: AudioTrack = {
  id: 'demo-cosmos',
  title: 'A moment to listen',
  author: 'Sample audio',
  description: 'A short sample to try the player. Add your own stories to build your library.',
  duration: 12,
  url: 'https://samplelib.com/lib/preview/mp3/sample-12s.mp3',
  uploadedAt: '2026-01-01T00:00:00.000Z',
};

export const audioLibraryService = {
  async fetchLibrary(): Promise<AudioTrack[]> {
    if (isRemoteMode()) {
      const [owned, shared] = await Promise.all([
        apiRequest<ApiTrack[]>('/audiolibro/lista?metadataOnly=true', { tipo: 1 }),
        apiRequest<ApiTrack[]>('/audiolibro/lista?metadataOnly=true', { tipo: 2 }),
      ]);
      return Array.from(
        new Map([...owned, ...shared].map((book) => [book.idAudiolibro, toTrack(book)])).values(),
      );
    }
    const tracks = await readLocalTracks();
    // Import the previous demo cache once without overwriting newer IndexedDB records.
    let legacy: string | null = null;
    try {
      legacy = localStorage.getItem('audiolibrary.tracks');
    } catch (error) {
      console.warn('Legacy storage is unavailable.', error);
    }
    if (legacy) {
      let parsed: unknown;
      try {
        parsed = JSON.parse(legacy);
      } catch {
        parsed = [];
      }
      if (Array.isArray(parsed)) {
        for (const track of parsed) {
          if (isAudioTrack(track) && !tracks.some((item) => item.id === track.id)) {
            await writeLocalTrack(track);
            tracks.push(track);
          }
        }
      }
      localStorage.removeItem('audiolibrary.tracks');
    }
    return tracks.length ? tracks : [demo];
  },

  async uploadTrack(input: Omit<AudioTrack, 'id' | 'uploadedAt'>): Promise<AudioTrack> {
    if (isRemoteMode()) {
      const match = /^data:(audio\/[\w.+-]+);base64,(.+)$/.exec(input.url);
      if (!match) throw new Error('The audio file is not valid.');
      return toTrack(
        await apiRequest<ApiTrack>('/audiolibro/inserisci', {
          titolo: input.title,
          autore: input.author,
          descrizione: input.description ?? '',
          durata: Math.round(input.duration),
          copertina: '',
          mimeType: match[1],
          audio: match[2],
        }),
      );
    }
    const track = { ...input, id: crypto.randomUUID(), uploadedAt: new Date().toISOString() };
    await writeLocalTrack(track);
    return track;
  },
};

function isAudioTrack(value: unknown): value is AudioTrack {
  if (!value || typeof value !== 'object') return false;
  const track = value as Partial<AudioTrack>;
  return (
    typeof track.id === 'string' &&
    typeof track.title === 'string' &&
    typeof track.author === 'string' &&
    typeof track.url === 'string' &&
    /^(https?:\/\/|data:audio\/)/.test(track.url) &&
    typeof track.duration === 'number' &&
    Number.isFinite(track.duration) &&
    track.duration >= 0 &&
    typeof track.uploadedAt === 'string'
  );
}
