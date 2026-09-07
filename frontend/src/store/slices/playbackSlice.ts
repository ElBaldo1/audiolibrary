import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { PlaybackProgress } from '../../types/audio';

export interface PlaybackState {
  currentTrackId: string | null;
  isPlaying: boolean;
  position: number;
}

const initialState: PlaybackState = {
  currentTrackId: null,
  isPlaying: false,
  position: 0,
};

/**
 * Stores playback information to keep the audio player in sync.
 */
export const playbackSlice = createSlice({
  name: 'playback',
  initialState,
  reducers: {
    /**
     * Updates the active track identifier.
     */
    setCurrentTrack(state, action: PayloadAction<string | null>) {
      if (state.currentTrackId !== action.payload) state.position = 0;
      state.currentTrackId = action.payload;
      state.isPlaying = false;
    },
    /**
     * Marks whether the current track is playing.
     */
    setIsPlaying(state, action: PayloadAction<boolean>) {
      state.isPlaying = action.payload;
    },
    /**
     * Updates the playback position in seconds.
     */
    setPosition(state, action: PayloadAction<number>) {
      state.position = Number.isFinite(action.payload)
        ? Math.max(0, Math.floor(action.payload))
        : 0;
    },
    /**
     * Rehydrates playback state from a persisted snapshot.
     */
    hydrateFromProgress(state, action: PayloadAction<PlaybackProgress>) {
      state.currentTrackId = action.payload.trackId;
      state.position = action.payload.position;
      state.isPlaying = false;
    },
    /**
     * Resets playback to its initial value.
     */
    resetPlayback() {
      return initialState;
    },
  },
});

export const { hydrateFromProgress, resetPlayback, setCurrentTrack, setIsPlaying, setPosition } =
  playbackSlice.actions;

export default playbackSlice.reducer;
