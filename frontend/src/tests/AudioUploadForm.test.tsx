import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AudioUploadForm } from '../components/audio/AudioUploadForm';

const createFile = () => new File(['dummy'], 'track.mp3', { type: 'audio/mpeg' });

describe('AudioUploadForm', () => {
  it('submits valid data to the callback', async () => {
    const user = userEvent.setup();
    const onUpload = vi.fn().mockResolvedValue(undefined);

    render(<AudioUploadForm onUpload={onUpload} />);

    await user.type(screen.getByLabelText(/title/i), 'Demo track');
    await user.type(screen.getByLabelText(/author/i), 'Antonio Baldari');
    await user.type(screen.getByLabelText(/description/i), 'Sample description');
    await user.type(screen.getByLabelText(/duration/i), '120');
    await user.upload(screen.getByLabelText(/audio file/i), createFile());

    await user.click(screen.getByRole('button', { name: /upload audio/i }));

    expect(onUpload).toHaveBeenCalledTimes(1);
    expect(onUpload.mock.calls[0][0]).toMatchObject({
      title: 'Demo track',
      author: 'Antonio Baldari',
      description: 'Sample description',
      duration: 120,
    });
  });

  it('shows an error when no file is provided', async () => {
    const user = userEvent.setup();
    const onUpload = vi.fn().mockResolvedValue(undefined);

    render(<AudioUploadForm onUpload={onUpload} />);

    await user.type(screen.getByLabelText(/title/i), 'Demo track');
    await user.click(screen.getByRole('button', { name: /upload audio/i }));

    expect(onUpload).not.toHaveBeenCalled();
    expect(screen.getByRole('alert')).toHaveTextContent(/please select an audio file/i);
  });
});
