import { FC } from 'react';
import { Spinner } from 'react-bootstrap';

/**
 * Displays a centered spinner used when the application is performing a
 * background task.
 */
export const LoadingSpinner: FC = () => (
  <div
    className="d-flex justify-content-center align-items-center py-5"
    role="status"
    aria-live="polite"
  >
    <Spinner animation="border" role="presentation" className="me-2" />
    <span>Loading…</span>
  </div>
);
