// Acceso a los datos de la cartelera: primero el repo publicado (GitHub API con
// caché de 60 s para evitar rate-limit), y como respaldo el JSON empaquetado en el build.

import datosEmpaquetados from '../datos.json';
import type { Categoria, DatosCartelera, Evento, Usuario } from './tipos';
import { nuevoId, sha256 } from './hash';
import { leerRepo } from './sesion';

const CLAVE_CACHA = 'aguacero_cachadatos';
const DURACION_CACHA_MS = 60_000;

interface CachéLocal {
  guardadoEn: number;
  cartelera: DatosCartelera;
  usuarios: Usuario[];
}

let memoria: CachéLocal | null = null;

function empaquetadoComoCartelera(): DatosCartelera {
  const crudo = JSON.stringify(datosEmpaquetados).replace(
    /__USUARIOS_SEED__/g,
    JSON.stringify(datosEmpaquetados.usuariosDemo ?? [])
  );
  return JSON.parse(crudo) as DatosCartelera;
}

async function leerRutaEnRepo(ruta: string): Promise<string | null> {
  const { owner, repo, rama } = leerRepo();
  const url = `https://api.github.com/repos/${owner}/${repo}/contents/${ruta}?ref=${rama}`;
  try {
    const respuesta = await fetch(url, {
      headers: { Accept: 'application/vnd.github.raw+json' },
    });
    if (!respuesta.ok) return null;
    return await respuesta.text();
  } catch {
    return null;
  }
}

async function cargarDesdeRemoto(): Promise<CachéLocal | null> {
  const txtCartelera = await leerRutaEnRepo('docs/eventos.json');
  if (!txtCartelera) return null;
  let cartelera: DatosCartelera;
  try {
    cartelera = JSON.parse(txtCartelera) as DatosCartelera;
  } catch {
    return null;
  }
  let usuarios: Usuario[] = [];
  const txtUsuarios = await leerRutaEnRepo('docs/usuarios.json');
  if (txtUsuarios) {
    try {
      usuarios = JSON.parse(txtUsuarios) as Usuario[];
    } catch {
      usuarios = [];
    }
  }
  // Si aún no hay usuarios publicados, se arranca con los de demostración
  // (conversión a hash hecha aquí, nunca en el JSON del repo).
  if (usuarios.length === 0) {
    usuarios = await usuariosSemilla();
  }
  return { guardadoEn: Date.now(), cartelera, usuarios };
}

export async function usuariosSemilla(): Promise<Usuario[]> {
  const lista: Usuario[] = [];
  for (const demo of datosEmpaquetados.usuariosDemo ?? []) {
    lista.push({
      usuario: demo.usuario,
      nombre: demo.nombre,
      hash: await sha256(demo.clave),
      esAdmin: lista.length === 0,
    });
  }
  return lista;
}

/** Carga cartelera + usuarios con caché en memoria y localStorage. */
export async function cargarDatos(): Promise<{ cartelera: DatosCartelera; usuarios: Usuario[] }> {
  if (memoria && Date.now() - memoria.guardadoEn < DURACION_CACHA_MS) {
    return { cartelera: memoria.cartelera, usuarios: memoria.usuarios };
  }
  // Intento 1: lectura directa de GitHub Pages (barata, sin API)
  let remoto = await cargarDesdePaginaPublicada();
  // Intento 2: Contents API (permite ver cambios sin esperar el despliegue)
  if (!remoto) remoto = await cargarDesdeRemoto();

  if (remoto) {
    memoria = remoto;
    try {
      localStorage.setItem(CLAVE_CACHA, JSON.stringify(remoto));
    } catch {
      /* almacenamiento lleno: ignorar */
    }
    return { cartelera: remoto.cartelera, usuarios: remoto.usuarios };
  }
  // Intento 3: caché local previa
  const crudo = typeof localStorage !== 'undefined' ? localStorage.getItem(CLAVE_CACHA) : null;
  if (crudo) {
    const antigua = JSON.parse(crudo) as CachéLocal;
    memoria = antigua;
    return { cartelera: antigua.cartelera, usuarios: antigua.usuarios };
  }
  // Intento 4: datos empaquetados en el build
  const semilla = empaquetadoComoCartelera();
  const usuarios = await usuariosSemilla();
  memoria = { guardadoEn: 0, cartelera: semilla, usuarios };
  return { cartelera: semilla, usuarios };
}

async function cargarDesdePaginaPublicada(): Promise<CachéLocal | null> {
  const base = datosEmpaquetados.sitioBase;
  try {
    const [rCartelera, rUsuarios] = await Promise.all([
      fetch(`${base}/eventos.json`, { cache: 'no-store' }),
      fetch(`${base}/usuarios.json`, { cache: 'no-store' }).catch(() => null),
    ]);
    if (!rCartelera.ok) return null;
    const cartelera = (await rCartelera.json()) as DatosCartelera;
    let usuarios: Usuario[] = [];
    if (rUsuarios && rUsuarios.ok) {
      usuarios = (await rUsuarios.json()) as Usuario[];
    }
    if (usuarios.length === 0) usuarios = await usuariosSemilla();
    return { guardadoEn: Date.now(), cartelera, usuarios };
  } catch {
    return null;
  }
}

/** Invalida la caché tras un commit propio para reflejar lo recién publicado. */
export function refrescarMemoria(cartelera: DatosCartelera, usuarios: Usuario[]): void {
  memoria = { guardadoEn: Date.now(), cartelera, usuarios };
  try {
    localStorage.setItem(CLAVE_CACHA, JSON.stringify(memoria));
  } catch {
    /* ignorar */
  }
}

export function ordenarEventos(eventos: Evento[]): Evento[] {
  return [...eventos].sort((a, b) =>
    `${a.fecha}T${a.hora || '00:00'}`.localeCompare(`${b.fecha}T${b.hora || '00:00'}`)
  );
}

export function slugId(nombre: string, existentes: Categoria[]): string {
  const base = nombre
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '');
  let id = base || nuevoId().slice(0, 8);
  let n = 1;
  while (existentes.some((c) => c.id === id)) {
    id = `${base}-${++n}`;
  }
  return id;
}
