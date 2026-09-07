import { useCallback, useEffect } from 'react';
import { fetchLibrary } from '../store/slices/audioLibrarySlice';
import { useAppDispatch, useAppSelector } from '../store';

/**
 * Synchronises the Redux store with the remote audio catalog and returns the
 * relevant state slices for convenience.
 */
export const useAudioLibrary = () => {
  const dispatch = useAppDispatch();
  const tracks = useAppSelector((state) => state.audioLibrary.items);
  const status = useAppSelector((state) => state.audioLibrary.status);
  const error = useAppSelector((state) => state.audioLibrary.error);

  const refresh = useCallback(() => {
    void dispatch(fetchLibrary());
  }, [dispatch]);

  useEffect(() => {
    if (status === 'idle') {
      refresh();
    }
  }, [status, refresh]);

  return { tracks, status, error, refresh };
};
