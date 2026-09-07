import { fireEvent, render, screen } from '@testing-library/react';
import { AudioPlayer } from '../components/audio/AudioPlayer';

it('restores progress after metadata arrives and allows seeking back to zero', () => {
  vi.spyOn(HTMLMediaElement.prototype, 'pause').mockImplementation(() => undefined);
  const callbacks = { onProgress: vi.fn(), onPlay: vi.fn(), onPause: vi.fn(), onEnded: vi.fn() };
  const track = {
    id: '1',
    title: 'Story',
    author: 'Author',
    duration: 120,
    url: 'data:audio/mpeg;base64,SUQz',
    uploadedAt: '',
  };
  const { rerender } = render(
    <AudioPlayer track={track} position={42} isPlaying={false} {...callbacks} />,
  );
  const audio = screen.getByLabelText('Audio player for Story') as HTMLAudioElement;
  fireEvent.timeUpdate(audio);
  expect(callbacks.onProgress).not.toHaveBeenCalled();
  fireEvent.loadedMetadata(audio);
  expect(audio.currentTime).toBe(42);
  rerender(<AudioPlayer track={track} position={0} isPlaying={false} {...callbacks} />);
  expect(audio.currentTime).toBe(0);
});
