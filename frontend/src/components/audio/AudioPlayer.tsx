import { FC, useEffect, useRef, useState } from 'react';
import { Card } from 'react-bootstrap';
import { AudioTrack } from '../../types/audio';
import { apiAudio } from '../../services/apiClient';
import { isAudioSource, isRemoteAudioPath } from '../../utils/audioSource';

export interface AudioPlayerProps {
  track: AudioTrack | null;
  position: number;
  isPlaying: boolean;
  onProgress: (position: number) => void;
  onPlay: () => void;
  onPause: () => void;
  onEnded: () => void;
}

export const AudioPlayer: FC<AudioPlayerProps> = ({
  track,
  position,
  isPlaying,
  onProgress,
  onPlay,
  onPause,
  onEnded,
}) => {
  const audioRef = useRef<HTMLAudioElement>(null);
  const positionRef = useRef(position);
  const ready = useRef(false);
  const [error, setError] = useState(() =>
    track && !isAudioSource(track.url) ? 'Unsupported audio source.' : '',
  );
  const [source, setSource] = useState(() =>
    track && isAudioSource(track.url) && !isRemoteAudioPath(track.url) ? track.url : '',
  );

  const remoteUrl = track?.url;
  useEffect(() => {
    if (!remoteUrl || !isRemoteAudioPath(remoteUrl)) return;
    let cancelled = false;
    let objectUrl: string | undefined;
    void apiAudio(remoteUrl)
      .then((blob) => {
        if (cancelled) return;
        objectUrl = URL.createObjectURL(blob);
        setSource(objectUrl);
      })
      .catch(() => {
        if (!cancelled) {
          setError('This audio could not be downloaded. Please try again.');
          onPause();
        }
      });
    return () => {
      cancelled = true;
      if (objectUrl) URL.revokeObjectURL(objectUrl);
    };
  }, [remoteUrl, onPause]);
  positionRef.current = position;

  useEffect(() => {
    const audio = audioRef.current;
    if (!audio || !source) return;
    let cancelled = false;
    if (isPlaying) {
      void audio.play().catch(() => {
        if (cancelled) return;
        setError('Playback could not start. Use the player controls to try again.');
        onPause();
      });
    } else audio.pause();
    return () => {
      cancelled = true;
    };
  }, [isPlaying, onPause, source]);

  useEffect(() => {
    const audio = audioRef.current;
    if (audio && ready.current && Math.abs(audio.currentTime - position) > 1.5)
      audio.currentTime = position;
  }, [position]);

  return (
    <Card className="player-card">
      <Card.Body>
        <p className="eyebrow">{track ? 'NOW SELECTED' : 'READY WHEN YOU ARE'}</p>
        <Card.Title>{track?.title ?? 'Find your next chapter.'}</Card.Title>
        <Card.Text>
          {track?.author ?? 'Select a story from your library to start listening.'}
        </Card.Text>
        {track && !source && !error && <p role="status">Loading audio…</p>}
        {track && (
          <>
            {/* Audio-only stories use native, keyboard-accessible playback controls. */}
            {/* eslint-disable-next-line jsx-a11y/media-has-caption */}
            <audio
              id="audio-player"
              ref={audioRef}
              src={source || undefined}
              controls
              preload="metadata"
              className="w-100"
              onLoadedMetadata={() => {
                const audio = audioRef.current;
                if (!audio) return;
                audio.currentTime = Math.min(
                  positionRef.current,
                  Number.isFinite(audio.duration) ? audio.duration : positionRef.current,
                );
                ready.current = true;
              }}
              onTimeUpdate={() => {
                if (ready.current && audioRef.current) onProgress(audioRef.current.currentTime);
              }}
              onPlay={() => {
                setError('');
                onPlay();
              }}
              onPause={onPause}
              onEnded={onEnded}
              onError={() => {
                setError(
                  'This audio could not be loaded. Check the file format or your connection.',
                );
                onPause();
              }}
              aria-label={`Audio player for ${track.title}`}
            />
            {error && (
              <p role="alert" className="text-warning mt-3 mb-0">
                {error}
              </p>
            )}
          </>
        )}
      </Card.Body>
    </Card>
  );
};
