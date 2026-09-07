import { FC } from 'react';
import { Container } from 'react-bootstrap';

/**
 * Application footer containing author attribution and helpful links.
 */
export const Footer: FC = () => (
  <footer className="bg-dark text-light py-4 mt-auto">
    <Container className="text-center">
      <small>
        &copy; {new Date().getFullYear()} Antonio Baldari · Built for the love of stories.
      </small>
    </Container>
  </footer>
);
