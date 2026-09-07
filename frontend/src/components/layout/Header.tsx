import { FC } from 'react';
import { Container, Nav, Navbar } from 'react-bootstrap';

/**
 * Top navigation displaying the AudioLibrary branding and quick links.
 */
export const Header: FC = () => (
  <Navbar bg="dark" variant="dark" expand="lg" role="navigation" aria-label="Primary">
    <Container>
      <Navbar.Brand href="#main-content">AudioLibrary</Navbar.Brand>
      <Navbar.Toggle aria-controls="primary-navigation" />
      <Navbar.Collapse id="primary-navigation">
        <Nav className="me-auto">
          <Nav.Link href="#catalog">Catalog</Nav.Link>
          <Nav.Link href="#upload">Upload</Nav.Link>
        </Nav>
      </Navbar.Collapse>
    </Container>
  </Navbar>
);
