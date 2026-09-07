import { FC } from 'react';
import { Button, Table } from 'react-bootstrap';
import { AudioTrack } from '../../types/audio';
import { formatDuration } from '../../utils/formatters';

export interface AudioListProps {
  tracks: AudioTrack[];
  activeTrackId: string | null;
  onSelect: (track: AudioTrack) => void;
}

/**
 * Renders the available audio tracks in a compact, accessible table.
 */
export const AudioList: FC<AudioListProps> = ({ tracks, activeTrackId, onSelect }) => (
  <Table variant="dark" striped hover responsive className="mt-4" aria-label="Audio library">
    <thead>
      <tr>
        <th scope="col">Title</th>
        <th scope="col">Author</th>
        <th scope="col">Duration</th>
        <th scope="col" className="text-end">
          Actions
        </th>
      </tr>
    </thead>
    <tbody>
      {tracks.map((track) => (
        <tr key={track.id} className={track.id === activeTrackId ? 'table-active' : undefined}>
          <td>{track.title}</td>
          <td>{track.author}</td>
          <td>{formatDuration(track.duration)}</td>
          <td className="text-end">
            <Button
              size="sm"
              variant={track.id === activeTrackId ? 'success' : 'outline-light'}
              onClick={() => onSelect(track)}
              aria-pressed={track.id === activeTrackId}
            >
              {track.id === activeTrackId ? 'Play again' : 'Play'}
            </Button>
          </td>
        </tr>
      ))}
    </tbody>
  </Table>
);
