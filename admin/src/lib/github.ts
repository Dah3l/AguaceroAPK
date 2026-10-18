// Commits automáticos al repo vía GitHub REST API (Contents API).

import { leerRepo, leerToken } from './sesion';

interface ArchivoExistente {
  sha: string;
}

async function cabeceras(): HeadersInit {
  return {
    Authorization: `Bearer ${leerToken()}`,
    Accept: 'application/vnd.github+json',
    'X-GitHub-Api-Version': '2022-11-28',
  };
}

/** Obtiene el SHA actual de un archivo (o null si no existe todavía). */
export async function shaDeArchivo(ruta: string): Promise<string | null> {
  const { owner, repo, rama } = leerRepo();
  const url = `https://api.github.com/repos/${owner}/${repo}/contents/${ruta}?ref=${rama}`;
  const respuesta = await fetch(url, { headers: await cabeceras() });
  if (respuesta.status === 404) return null;
  if (!respuesta.ok) throw new Error(`GitHub API (${respuesta.status}) al leer ${ruta}`);
  const datos = (await respuesta.json()) as ArchivoExistente;
  return datos.sha;
}

/** Crea o actualiza un archivo con contenido UTF-8. */
export async function subirTexto(ruta: string, contenido: string, mensaje: string): Promise<void> {
  await subirArchivo(ruta, contenido, mensaje, false);
}

/** Sube una imagen (flyer) en base64. */
export async function subirImagen(ruta: string, base64: string, mensaje: string): Promise<void> {
  await subirArchivo(ruta, base64, mensaje, true);
}

async function subirArchivo(
  ruta: string,
  contenido: string,
  mensaje: string,
  esBase64: boolean
): Promise<void> {
  const { owner, repo, rama } = leerRepo();
  const shaActual = await shaDeArchivo(ruta);
  const url = `https://api.github.com/repos/${owner}/${repo}/contents/${ruta}`;
  const cuerpo: Record<string, unknown> = {
    message,
    content: esBase64 ? contenido : btoa(unescape(encodeURIComponent(contenido))),
    branch: rama,
  };
  if (shaActual) cuerpo.sha = shaActual;

  const respuesta = await fetch(url, {
    method: 'PUT',
    headers: { ...(await cabeceras()), 'Content-Type': 'application/json' },
    body: JSON.stringify(cuerpo),
  });
  if (!respuesta.ok) {
    const texto = await respuesta.text();
    throw new Error(`No se pudo publicar ${ruta}: ${respuesta.status} ${texto.slice(0, 200)}`);
  }
}

/** Convierte un File a base64 plano (sin prefijo data URL). */
export function fileABase64(archivo: File): Promise<string> {
  return new Promise((resolver, rechazar) => {
    const lector = new FileReader();
    lector.onload = () => {
      const resultado = lector.result as string;
      resolver(resultado.split(',')[1] ?? '');
    };
    lector.onerror = () => rechazar(new Error('No se pudo leer el archivo'));
    lector.readAsDataURL(archivo);
  });
}
