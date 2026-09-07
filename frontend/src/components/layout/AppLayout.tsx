import { FC, PropsWithChildren } from 'react';
import { Container } from 'react-bootstrap';
import { Footer } from './Footer';
import { Header } from './Header';

/**
 * Provides the shared page structure including navigation, layout spacing, and
 * accessibility helpers.
 */
export const AppLayout: FC<PropsWithChildren> = ({ children }) => (
  <div className="d-flex flex-column min-vh-100">
    <a className="visually-hidden-focusable" href="#main-content">
      Skip to main content
    </a>
    <Header />
    <Container as="main" id="main-content" className="flex-grow-1 py-5">
      {children}
    </Container>
    <Footer />
  </div>
);
