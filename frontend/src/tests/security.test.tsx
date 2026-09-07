import { render, screen } from '@testing-library/react';
import { AudioList } from '../components/audio/AudioList';
import { AudioPlayer } from '../components/audio/AudioPlayer';
import { ErrorBanner } from '../components/feedback/ErrorBanner';
import { isAudioSource } from '../utils/audioSource';
import { apiRequest, setSession } from '../services/apiClient';

const payload = '<img src=x onerror=alert(document.domain)>';

it('renders untrusted titles, authors and API errors as text, never markup', () => {
  const { container } = render(
    <>
      <AudioList
        tracks={[
          {
            id: '1',
            title: payload,
            author: '<svg onload=alert(1)>',
            duration: 10,
            url: '',
            uploadedAt: '',
          },
        ]}
        activeTrackId={null}
        onSelect={vi.fn()}
      />
      <ErrorBanner message={'<script>alert(1)</script>'} />
    </>,
  );
  expect(screen.getByText(payload)).toBeInTheDocument();
  expect(screen.getByText('<script>alert(1)</script>')).toBeInTheDocument();
  expect(container.querySelector('img, svg, script, [onerror], [onload]')).toBeNull();
});

it.each([
  'javascript:alert(1)',
  'data:text/html,<script>alert(1)</script>',
  'data:image/svg+xml,<svg/>',
  '//attacker.example/audio',
  'https://user:secret@example.com/audio',
  '/audiolibro/../utente/logout',
])('rejects an unsupported or ambiguous media source: %s', (source) => {
  expect(isAudioSource(source)).toBe(false);
});

it('does not assign malicious cached media URLs to the audio element', () => {
  render(
    <AudioPlayer
      track={{
        id: '1',
        title: payload,
        author: 'Test',
        duration: 1,
        url: 'javascript:alert(1)',
        uploadedAt: '',
      }}
      position={0}
      isPlaying={false}
      onPlay={vi.fn()}
      onPause={vi.fn()}
      onProgress={vi.fn()}
      onEnded={vi.fn()}
    />,
  );
  expect(screen.getByLabelText(`Audio player for ${payload}`)).not.toHaveAttribute('src');
  expect(screen.getByRole('alert')).toHaveTextContent('Unsupported audio source.');
});

it('sends an explicit Bearer token and omits browser cookies', async () => {
  vi.stubEnv('VITE_API_BASE_URL', 'https://api.example.com');
  setSession({
    jwtToken: 'test-token',
    utente: { username: 'test', nome: 'Test', cognome: 'User' },
  });
  const fetch = vi.fn().mockResolvedValue(new Response('{}'));
  vi.stubGlobal('fetch', fetch);
  try {
    await apiRequest('/audiolibro/lista', { tipo: 1 });
    expect(fetch).toHaveBeenCalledWith(
      'https://api.example.com/audiolibro/lista',
      expect.objectContaining({
        credentials: 'omit',
        headers: { 'Content-Type': 'application/json', Authorization: 'Bearer test-token' },
      }),
    );
  } finally {
    setSession(null);
    vi.unstubAllGlobals();
  }
});
