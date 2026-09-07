import { useEffect, useRef, useState } from 'react';
import { toast } from 'react-toastify';
import { playbackService } from '../services/playbackService';
import { useAppDispatch, useAppSelector } from '../store';
import { hydrateFromProgress } from '../store/slices/playbackSlice';
import { AudioTrack, PlaybackProgress } from '../types/audio';

export const usePlaybackSync = (userId: string, tracks: AudioTrack[], ready: boolean) => {
  const dispatch = useAppDispatch();
  const { currentTrackId, position, isPlaying } = useAppSelector((state) => state.playback);
  const [hydrated, setHydrated] = useState(false);
  const pending = useRef<PlaybackProgress | null>(null);
  const queue = useRef(Promise.resolve());

  useEffect(() => {
    if (!ready || hydrated) return;
    let cancelled = false;
    void playbackService.loadProgress(userId, tracks).then((progress) => {
      if (!cancelled) {
        dispatch(hydrateFromProgress(progress));
        setHydrated(true);
      }
    });
    return () => {
      cancelled = true;
    };
  }, [dispatch, userId, tracks, ready, hydrated]);

  useEffect(() => {
    if (!hydrated || !currentTrackId) return;
    const progress = { trackId: currentTrackId, position, updatedAt: new Date().toISOString() };
    pending.current = progress;
    try {
      playbackService.persist(progress, userId);
    } catch {
      toast.error('Browser storage is full or unavailable. Progress cannot be saved.', {
        toastId: 'storage',
      });
    }
  }, [hydrated, currentTrackId, position, userId]);

  useEffect(() => {
    if (!hydrated) return;
    const flush = () => {
      const progress = pending.current;
      if (!progress) return;
      pending.current = null;
      queue.current = queue.current
        .then(() => playbackService.saveProgress(userId, progress))
        .catch(() => {
          toast.error(
            'Progress could not be synced. Your position is still saved in this browser.',
            { toastId: 'sync' },
          );
        });
    };
    const onVisibility = () => {
      if (document.visibilityState === 'hidden') flush();
    };
    if (!isPlaying) flush();
    const timer = window.setInterval(flush, 5000);
    document.addEventListener('visibilitychange', onVisibility);
    return () => {
      window.clearInterval(timer);
      document.removeEventListener('visibilitychange', onVisibility);
      flush();
    };
  }, [hydrated, isPlaying, userId, currentTrackId]);

  return hydrated;
};
