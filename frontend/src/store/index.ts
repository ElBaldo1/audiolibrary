import { configureStore } from '@reduxjs/toolkit';
import { TypedUseSelectorHook, useDispatch, useSelector } from 'react-redux';
import audioLibraryReducer from './slices/audioLibrarySlice';
import playbackReducer from './slices/playbackSlice';

/**
 * Configures the root Redux store for the AudioLibrary application.
 */
export const store = configureStore({
  reducer: {
    audioLibrary: audioLibraryReducer,
    playback: playbackReducer,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;

/**
 * Strongly typed wrapper around the Redux dispatch function.
 */
export const useAppDispatch = () => useDispatch<AppDispatch>();

/**
 * Strongly typed selector helper to access Redux state.
 */
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector;
