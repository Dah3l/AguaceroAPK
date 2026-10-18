// Sesión del administrador guardada en localStorage.

const CLAVE_SESION = 'aguacero_sesion';
const CLAVE_TOKEN = 'aguacero_token';
const CLAVE_REPO = 'aguacero_repo';

export interface Sesion {
  usuario: string;
  nombre: string;
  esAdmin: boolean;
}

export function guardarSesion(sesion: Sesion): void {
  localStorage.setItem(CLAVE_SESION, JSON.stringify(sesion));
}

export function leerSesion(): Sesion | null {
  try {
    const crudo = localStorage.getItem(CLAVE_SESION);
    return crudo ? (JSON.parse(crudo) as Sesion) : null;
  } catch {
    return null;
  }
}

export function cerrarSesion(): void {
  localStorage.removeItem(CLAVE_SESION);
  localStorage.removeItem(CLAVE_TOKEN);
}

/** Token PAT con permisos de escritura sobre el repo (solo se guarda en el navegador). */
export function guardarToken(token: string): void {
  localStorage.setItem(CLAVE_TOKEN, token);
}

export function leerToken(): string {
  return localStorage.getItem(CLAVE_TOKEN) ?? '';
}

/** owner/repo/rama destino de los commits automáticos. */
export function guardarRepo(owner: string, repo: string, rama: string): void {
  localStorage.setItem(CLAVE_REPO, JSON.stringify({ owner, repo, rama }));
}

export function leerRepo(): { owner: string; repo: string; rama: string } {
  try {
    const crudo = localStorage.getItem(CLAVE_REPO);
    if (crudo) return JSON.parse(crudo);
  } catch {
    /* ignorar */
  }
  return { owner: 'dah3l', repo: 'aguacero', rama: 'main' };
}
