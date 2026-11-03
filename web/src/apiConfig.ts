export const API_BASE = window.location.hostname === 'localhost'
  ? 'http://localhost:3000'
  : `http://${window.location.hostname}:3000`;

export function getAuthToken(): string | null {
  return sessionStorage.getItem('campusexam_token');
}

export function setAuthToken(token: string): void {
  sessionStorage.setItem('campusexam_token', token);
}

export function clearAuthToken(): void {
  sessionStorage.removeItem('campusexam_token');
}

export function authFetch(url: string, init?: RequestInit): Promise<Response> {
  const token = getAuthToken();
  const headers = new Headers(init?.headers || {});
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }
  return fetch(url, {
    ...init,
    headers
  });
}
