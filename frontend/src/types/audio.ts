/**
 * Describes an audio track stored inside the library.
 */
export interface AudioTrack {
  id: string;
  title: string;
  author: string;
  description?: string;
  duration: number;
  url: string;
  uploadedAt: string;
  resumePosition?: number;
  lastPlayedAt?: string;
}

/**
 * Payload for persisting playback progress across devices.
 */
export interface PlaybackProgress {
  trackId: string | null;
  position: number;
  updatedAt: string;
}
