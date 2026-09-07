import { useCallback, useEffect, useMemo, useState } from 'react';
import { Button, Col, Form, Row } from 'react-bootstrap';
import { toast } from 'react-toastify';
import { AppLayout } from './components/layout/AppLayout';
import { AudioList } from './components/audio/AudioList';
import { AudioPlayer } from './components/audio/AudioPlayer';
import { AudioUploadForm, AudioUploadFormValues } from './components/audio/AudioUploadForm';
import { ErrorBanner } from './components/feedback/ErrorBanner';
import { LoadingSpinner } from './components/feedback/LoadingSpinner';
import { useAudioLibrary } from './hooks/useAudioLibrary';
import { usePlaybackSync } from './hooks/usePlaybackSync';
import { useAppDispatch, useAppSelector } from './store';
import {
  resetPlayback,
  setCurrentTrack,
  setIsPlaying,
  setPosition,
} from './store/slices/playbackSlice';
import { clearLibrary, uploadTrack } from './store/slices/audioLibrarySlice';

import { AccountForm } from './components/auth/AccountForm';
import { apiRequest, getSession, isRemoteMode, Session, setSession } from './services/apiClient';
import { playbackService } from './services/playbackService';
import { readAudioFile, readDuration } from './utils/audioFile';

const Library = ({ userId }: { userId: string }) => {
  const dispatch = useAppDispatch();
  const { tracks, status, error, refresh } = useAudioLibrary();
  const playback = useAppSelector((state) => state.playback);
  const [isUploading, setIsUploading] = useState(false);

  const hydrated = usePlaybackSync(userId, tracks, status === 'succeeded');
  const [query, setQuery] = useState('');
  const filteredTracks = useMemo(
    () =>
      tracks.filter((track) =>
        `${track.title} ${track.author}`.toLowerCase().includes(query.toLowerCase()),
      ),
    [tracks, query],
  );
  const pause = useCallback(() => {
    dispatch(setIsPlaying(false));
  }, [dispatch]);

  const activeTrack = useMemo(
    () => tracks.find((track) => track.id === playback.currentTrackId) ?? null,
    [tracks, playback.currentTrackId],
  );

  useEffect(() => {
    const handleKeyboardShortcuts = (event: KeyboardEvent) => {
      if (
        event.altKey ||
        event.ctrlKey ||
        event.metaKey ||
        event.repeat ||
        (event.target instanceof HTMLElement &&
          event.target.closest('input, textarea, button, a, select, audio, [contenteditable]'))
      ) {
        return;
      }

      const audioElement = document.getElementById('audio-player') as HTMLAudioElement | null;
      if (!audioElement) {
        return;
      }

      if (event.code === 'Space') {
        event.preventDefault();
        if (audioElement.paused) {
          void audioElement
            .play()
            .catch(() => toast.error('Playback could not start. Try the player controls.'));
        } else {
          audioElement.pause();
        }
      }

      if (event.key.toLowerCase() === 'f') {
        event.preventDefault();
        audioElement.focus();
      }
    };

    window.addEventListener('keydown', handleKeyboardShortcuts);
    return () => window.removeEventListener('keydown', handleKeyboardShortcuts);
  }, []);

  const handleUpload = async ({
    title,
    author,
    description,
    duration,
    file,
  }: AudioUploadFormValues) => {
    if (!file) {
      return;
    }

    setIsUploading(true);

    try {
      const [dataUrl, actualDuration] = await Promise.all([
        readAudioFile(file),
        duration > 0 ? Promise.resolve(duration) : readDuration(file),
      ]);
      await dispatch(
        uploadTrack({
          title,
          author,
          description,
          duration: actualDuration,
          url: dataUrl,
        }),
      ).unwrap();
      toast.success('Audio uploaded successfully.');
    } catch (uploadError) {
      toast.error('Audio upload failed. Please try again.');
      throw uploadError;
    } finally {
      setIsUploading(false);
    }
  };

  const handleSelectTrack = (trackId: string) => {
    dispatch(setCurrentTrack(trackId));
    const track = tracks.find((item) => item.id === trackId);
    const cached = playbackService.readCache(userId, trackId);
    const remoteIsNewer =
      track?.lastPlayedAt && Date.parse(track.lastPlayedAt) > Date.parse(cached.updatedAt);
    dispatch(setPosition(remoteIsNewer ? (track?.resumePosition ?? 0) : cached.position));
    dispatch(setIsPlaying(true));
  };

  return (
    <>
      <section className="library-intro mb-5">
        <p className="eyebrow">A LITTLE SPACE FOR GREAT STORIES</p>
        <h1>
          Your next chapter
          <br />
          <span>starts here.</span>
        </h1>
        <p className="intro-copy">
          Keep your stories together. Press play. Pick up where you left off.
        </p>
        <span className="mode-badge">
          {isRemoteMode() ? 'Connected library' : 'Browser demo · stored on this device'}
        </span>
      </section>
      <Row className="gy-4">
        <Col xl={4} lg={5} md={12}>
          <section id="upload" className="surface" aria-labelledby="upload-heading">
            <h2 id="upload-heading" className="h4 mb-4">
              Upload a new audio story
            </h2>
            <AudioUploadForm onUpload={handleUpload} isSubmitting={isUploading} />
          </section>
        </Col>
        <Col xl={8} lg={7} md={12} id="catalog">
          <section className="surface" aria-labelledby="catalog-heading">
            <h2 id="catalog-heading" className="h4 mb-4">
              Library
            </h2>
            <Form.Control
              type="search"
              aria-label="Search by title or author"
              placeholder="Search by title or author…"
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              className="mb-3"
            />
            {(status === 'loading' || status === 'idle') && <LoadingSpinner />}
            {status === 'failed' && error && <ErrorBanner message={error} onRetry={refresh} />}
            {status === 'succeeded' && tracks.length > 0 && filteredTracks.length === 0 && (
              <p>No stories match your search.</p>
            )}
            {status === 'succeeded' &&
              (tracks.length ? (
                <AudioList
                  tracks={filteredTracks}
                  activeTrackId={playback.currentTrackId}
                  onSelect={(track) => {
                    if (hydrated) handleSelectTrack(track.id);
                  }}
                />
              ) : (
                <p className="text-muted">Upload your first audio story to populate the catalog.</p>
              ))}
          </section>
        </Col>
        <Col xs={12}>
          <section aria-labelledby="player-heading" className="mt-4">
            <h2 id="player-heading" className="h4 mb-3">
              Player
            </h2>
            <AudioPlayer
              key={activeTrack?.id ?? 'empty'}
              isPlaying={playback.isPlaying}
              track={activeTrack}
              position={playback.position}
              onProgress={(position) => dispatch(setPosition(position))}
              onPlay={() => dispatch(setIsPlaying(true))}
              onPause={pause}
              onEnded={() => {
                dispatch(setIsPlaying(false));
                dispatch(setPosition(0));
              }}
            />
          </section>
        </Col>
      </Row>
    </>
  );
};

const App = () => {
  const dispatch = useAppDispatch();
  const [session, updateSession] = useState<Session | null>(getSession);
  const [signingOut, setSigningOut] = useState(false);
  const clear = useCallback(() => {
    setSession(null);
    updateSession(null);
    dispatch(clearLibrary());
    dispatch(resetPlayback());
  }, [dispatch]);
  useEffect(() => {
    window.addEventListener('session-expired', clear);
    return () => window.removeEventListener('session-expired', clear);
  }, [clear]);
  const signOut = async () => {
    setSigningOut(true);
    try {
      await apiRequest('/utente/logout', { jwtToken: session?.jwtToken });
      clear();
    } catch {
      toast.error('Could not sign out on the server. Please retry.');
    } finally {
      setSigningOut(false);
    }
  };
  return (
    <AppLayout>
      {isRemoteMode() && session && (
        <div className="d-flex justify-content-end align-items-center gap-3 mb-3">
          <span>{session.utente.username}</span>
          <Button variant="outline-light" disabled={signingOut} onClick={() => void signOut()}>
            Sign out
          </Button>
        </div>
      )}
      {isRemoteMode() && !session ? (
        <AccountForm onSignedIn={updateSession} />
      ) : (
        <Library
          key={session?.utente.username ?? 'demo'}
          userId={session?.utente.username ?? 'demo'}
        />
      )}
    </AppLayout>
  );
};

export default App;
