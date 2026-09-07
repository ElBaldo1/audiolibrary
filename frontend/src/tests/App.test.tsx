import { StrictMode } from 'react';
import { configureStore } from '@reduxjs/toolkit';
import { Provider } from 'react-redux';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import App from '../App';
import audioLibrary from '../store/slices/audioLibrarySlice';
import playback from '../store/slices/playbackSlice';
import { audioLibraryService } from '../services/audioLibraryService';
import { playbackService } from '../services/playbackService';

it('loads the library, restores progress under StrictMode and filters by author', async () => {
  vi.stubEnv('VITE_API_BASE_URL', '');
  localStorage.clear();
  sessionStorage.clear();
  const track = {
    id: 'story',
    title: 'The next chapter',
    author: 'A narrator',
    duration: 120,
    url: 'data:audio/mpeg;base64,SUQz',
    uploadedAt: '2026-01-01',
  };
  vi.spyOn(audioLibraryService, 'fetchLibrary').mockResolvedValue([track]);
  vi.spyOn(HTMLMediaElement.prototype, 'pause').mockImplementation(() => undefined);
  vi.spyOn(HTMLMediaElement.prototype, 'play').mockResolvedValue(undefined);
  playbackService.persist(
    { trackId: track.id, position: 42, updatedAt: new Date().toISOString() },
    'demo',
  );
  const store = configureStore({ reducer: { audioLibrary, playback } });
  render(
    <StrictMode>
      <Provider store={store}>
        <App />
      </Provider>
    </StrictMode>,
  );
  await waitFor(() => expect(store.getState().playback.position).toBe(42));
  expect(playbackService.readCache('demo').position).toBe(42);
  const user = userEvent.setup();
  const search = screen.getByRole('searchbox');
  await user.type(search, 'unmatched');
  expect(screen.getByText('No stories match your search.')).toBeInTheDocument();
  await user.clear(search);
  await user.type(search, 'narrator');
  expect(screen.getByRole('row', { name: /the next chapter/i })).toBeInTheDocument();
});
