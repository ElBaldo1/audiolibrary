import { FC } from 'react';
import { Alert, Button } from 'react-bootstrap';

export interface ErrorBannerProps {
  message: string;
  onRetry?: () => void;
}

/**
 * Communicates a recoverable error and offers a retry action when applicable.
 */
export const ErrorBanner: FC<ErrorBannerProps> = ({ message, onRetry }) => (
  <Alert variant="danger" role="alert">
    <div className="d-flex justify-content-between align-items-center">
      <span>{message}</span>
      {onRetry && (
        <Button size="sm" variant="outline-light" className="ms-3" onClick={onRetry}>
          Retry
        </Button>
      )}
    </div>
  </Alert>
);
