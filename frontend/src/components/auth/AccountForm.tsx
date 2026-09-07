import { FormEvent, useState } from 'react';
import { Alert, Button, Form } from 'react-bootstrap';
import { apiRequest, Session, setSession } from '../../services/apiClient';

export function AccountForm({ onSignedIn }: { onSignedIn: (session: Session) => void }) {
  const [register, setRegister] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const data = Object.fromEntries(new FormData(event.currentTarget));
    setBusy(true);
    setError('');
    try {
      if (register) await apiRequest('/utente/registrazione', data);
      const session = await apiRequest<Session>('/utente/login', {
        username: data.username,
        password: data.password,
      });
      setSession(session);
      onSignedIn(session);
    } catch (error) {
      setError(error instanceof Error ? error.message : 'Unable to sign in.');
    } finally {
      setBusy(false);
    }
  }
  return (
    <section className="account-panel surface mx-auto" aria-labelledby="account-title">
      <p className="eyebrow">YOUR PERSONAL LIBRARY</p>
      <h1 id="account-title">{register ? 'Start your next chapter.' : 'Welcome back.'}</h1>
      <p className="text-secondary">Sign in to access your stories and listening progress.</p>
      {error && (
        <Alert variant="danger" role="alert">
          {error}
        </Alert>
      )}
      <Form onSubmit={submit}>
        {register && (
          <>
            <Form.Group controlId="account-first" className="mb-3">
              <Form.Label>First name</Form.Label>
              <Form.Control name="nome" required maxLength={100} autoComplete="given-name" />
            </Form.Group>
            <Form.Group controlId="account-last" className="mb-3">
              <Form.Label>Last name</Form.Label>
              <Form.Control name="cognome" required maxLength={100} autoComplete="family-name" />
            </Form.Group>
            <Form.Group controlId="account-email" className="mb-3">
              <Form.Label>Email</Form.Label>
              <Form.Control
                name="email"
                type="email"
                required
                maxLength={255}
                autoComplete="email"
              />
            </Form.Group>
          </>
        )}
        <Form.Group controlId="account-username" className="mb-3">
          <Form.Label>Username</Form.Label>
          <Form.Control name="username" required maxLength={100} autoComplete="username" />
        </Form.Group>
        <Form.Group controlId="account-password" className="mb-4">
          <Form.Label>Password</Form.Label>
          <Form.Control
            name="password"
            type="password"
            required
            autoComplete={register ? 'new-password' : 'current-password'}
          />
          {register && (
            <Form.Text>
              At least 8 characters, including a letter, a number and one of @$!%*#?&.
            </Form.Text>
          )}
        </Form.Group>
        <Button type="submit" disabled={busy} className="w-100">
          {busy ? 'Please wait…' : register ? 'Create account' : 'Sign in'}
        </Button>
        <Button
          type="button"
          variant="link"
          className="w-100 mt-2"
          disabled={busy}
          onClick={() => {
            setRegister(!register);
            setError('');
          }}
        >
          {register ? 'Already have an account? Sign in' : 'New here? Create an account'}
        </Button>
      </Form>
    </section>
  );
}
