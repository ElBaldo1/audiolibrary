import { AudioTrack, PlaybackProgress } from '../types/audio';
import { apiRequest, getSession, isRemoteMode } from './apiClient';

const key = (userId: string, trackId = 'latest') =>
  `audiolibrary.playback:${import.meta.env.VITE_API_BASE_URL || 'demo'}:${userId}:${trackId}`;
const empty = (): PlaybackProgress => ({
  trackId: null,
  position: 0,
  updatedAt: new Date(0).toISOString(),
});

export const playbackService = {
  async loadProgress(userId: string, tracks: AudioTrack[] = []): Promise<PlaybackProgress> {
    const cached = this.readCache(userId);
    const candidates = [
      cached,
      ...tracks
        .filter((track) => track.lastPlayedAt)
        .map((track) => ({
          trackId: track.id,
          position: track.resumePosition ?? 0,
          updatedAt: track.lastPlayedAt!,
        })),
    ].filter((progress) => !tracks.length || tracks.some((track) => track.id === progress.trackId));
    return (
      candidates.sort((a, b) => Date.parse(b.updatedAt) - Date.parse(a.updatedAt))[0] ?? empty()
    );
  },

  async saveProgress(userId: string, progress: PlaybackProgress): Promise<void> {
    this.persist(progress, userId);
    if (isRemoteMode() && progress.trackId && getSession()?.utente.username === userId) {
      await apiRequest('/audiolibro/ascolta?metadataOnly=true', {
        idAudiolibro: Number(progress.trackId),
        secondi: Math.floor(progress.position),
      });
    }
  },

  persist(progress: PlaybackProgress, userId: string): void {
    localStorage.setItem(key(userId), JSON.stringify(progress));
    if (progress.trackId)
      localStorage.setItem(key(userId, progress.trackId), JSON.stringify(progress));
  },

  readCache(userId: string, trackId?: string): PlaybackProgress {
    try {
      const progress = JSON.parse(localStorage.getItem(key(userId, trackId)) ?? 'null');
      if (
        progress &&
        (typeof progress.trackId === 'string' || progress.trackId === null) &&
        Number.isFinite(progress.position) &&
        progress.position >= 0 &&
        typeof progress.updatedAt === 'string' &&
        Number.isFinite(Date.parse(progress.updatedAt))
      ) {
        return progress;
      }
    } catch (error) {
      console.warn('Playback storage could not be read.', error);
    }
    return empty();
  },
};
