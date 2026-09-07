export const isRemoteMode = () => Boolean(import.meta.env.VITE_API_BASE_URL?.trim());

export interface Session {
  jwtToken: string;
  utente: { username: string; nome: string; cognome: string };
}

const sessionKey = () => `audiolibrary.session:${import.meta.env.VITE_API_BASE_URL ?? 'demo'}`;

export function getSession(): Session | null {
  try {
    const value = JSON.parse(sessionStorage.getItem(sessionKey()) ?? 'null');
    return typeof value?.jwtToken === 'string' && typeof value?.utente?.username === 'string'
      ? value
      : null;
  } catch {
    return null;
  }
}

export function setSession(session: Session | null) {
  if (session) sessionStorage.setItem(sessionKey(), JSON.stringify(session));
  else sessionStorage.removeItem(sessionKey());
}

async function request(path: string, body: unknown, method = 'POST'): Promise<Response> {
  const token = getSession()?.jwtToken;
  const response = await fetch(`${import.meta.env.VITE_API_BASE_URL.replace(/\/$/, '')}${path}`, {
    method,
    credentials: 'omit',
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: method === 'GET' ? undefined : JSON.stringify(body),
    signal: AbortSignal.timeout(30000),
  });
  if (!response.ok) {
    if (response.status === 401 && path === '/utente/login') {
      throw new Error('Invalid username or password.');
    }
    if (response.status === 401 && token === getSession()?.jwtToken) {
      setSession(null);
      window.dispatchEvent(new Event('session-expired'));
      throw new Error('Your session has expired. Please sign in again.');
    }
    const text = await response.text();
    throw new Error(
      text && !text.startsWith('{') && text.length < 300
        ? text
        : `The server could not complete the request (${response.status}).`,
    );
  }
  if (token && token !== getSession()?.jwtToken) throw new Error('The account session changed.');
  return response;
}

export async function apiRequest<T>(path: string, body: unknown, method = 'POST'): Promise<T> {
  const response = await request(path, body, method);
  const text = await response.text();
  return text ? (JSON.parse(text) as T) : (undefined as T);
}

export async function apiAudio(path: string): Promise<Blob> {
  return (await request(path, undefined, 'GET')).blob();
}
