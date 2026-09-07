import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit';
import { audioLibraryService } from '../../services/audioLibraryService';
import { AudioTrack } from '../../types/audio';

export interface AudioLibraryState {
  items: AudioTrack[];
  status: 'idle' | 'loading' | 'succeeded' | 'failed';
  error?: string;
  requestId?: string;
  uploadRequestId?: string;
  uploadStatus?: 'idle' | 'loading' | 'failed';
}

const initialState: AudioLibraryState = {
  items: [],
  status: 'idle',
};

/**
 * Requests the audio catalog from the backend or local cache.
 */
export const fetchLibrary = createAsyncThunk('audioLibrary/fetchLibrary', async () => {
  const tracks = await audioLibraryService.fetchLibrary();
  return tracks;
});

/**
 * Uploads a new track and returns the persisted entity.
 */
export const uploadTrack = createAsyncThunk(
  'audioLibrary/uploadTrack',
  async (payload: Omit<AudioTrack, 'id' | 'uploadedAt'>) => {
    const track = await audioLibraryService.uploadTrack(payload);
    return track;
  },
);

/**
 * Redux slice responsible for the audio catalog lifecycle.
 */
export const audioLibrarySlice = createSlice({
  name: 'audioLibrary',
  initialState,
  reducers: {
    clearLibrary() {
      return initialState;
    },
    /**
     * Replaces the entire audio catalog.
     */
    setLibrary(state, action: PayloadAction<AudioTrack[]>) {
      state.items = action.payload;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchLibrary.pending, (state, action) => {
        state.requestId = action.meta.requestId;
        state.status = 'loading';
        state.error = undefined;
      })
      .addCase(fetchLibrary.fulfilled, (state, action) => {
        if (state.requestId !== action.meta?.requestId) return;
        state.status = 'succeeded';
        state.items = action.payload;
      })
      .addCase(fetchLibrary.rejected, (state, action) => {
        if (state.requestId !== action.meta?.requestId) return;
        state.status = 'failed';
        state.error = action.error.message ?? 'Unable to load audio catalog.';
      })
      .addCase(uploadTrack.pending, (state, action) => {
        state.uploadRequestId = action.meta.requestId;
        state.uploadStatus = 'loading';
        state.error = undefined;
      })
      .addCase(uploadTrack.fulfilled, (state, action) => {
        if (state.uploadRequestId !== action.meta?.requestId) return;
        state.uploadStatus = 'idle';
        state.items.push(action.payload);
      })
      .addCase(uploadTrack.rejected, (state, action) => {
        if (state.uploadRequestId !== action.meta?.requestId) return;
        state.uploadStatus = 'failed';
        state.error = action.error.message ?? 'Unable to upload audio track.';
      });
  },
});

export const { setLibrary, clearLibrary } = audioLibrarySlice.actions;

export default audioLibrarySlice.reducer;
