// Publicación de los datos: regenera eventos.json/usuarios.json y hace commit
// automático al repo vía GitHub API. Muestra el aviso "Publicado ✓" al terminar.

import datosEmpaquetados from '../datos.json';
import type { DatosCartelera, Usuario } from './tipos';
import { ordenarEventos, refrescarMemoria } from './datos';
import { fileABase64, subirImagen, subirTexto } from './github';

/** Notifica al toast global del layout mediante un evento de ventana. */
function aviso(texto: string, tipo: 'exito' | 'error' | 'info'): void {
  window.dispatchEvent(new CustomEvent('aguacero-aviso', { detail: { texto, tipo } }));
}

export function anunciandoProgreso(mensaje: string): void {
  aviso(mensaje, 'info');
}

/** Regenera eventos.json (eventos ordenados por fecha/hora) y lo comitea. */
export async function publicarCartelera(
  cartelera: DatosCartelera,
  usuarios: Usuario[],
  mensajeCommit: string
): Promise<void> {
  const ordenada: DatosCartelera = {
    categorias: cartelera.categorias,
    eventos: ordenarEventos(cartelera.eventos),
  };
  const json = JSON.stringify(ordenada, null, 2) + '\n';
  await subirTexto('docs/eventos.json', json, mensajeCommit);
  refrescarMemoria(ordenada, usuarios);
  aviso('Publicado ✓', 'exito');
}

/** Comitea la lista de usuarios (con contraseñas hasheadas) a docs/usuarios.json. */
export async function publicarUsuarios(
  usuarios: Usuario[],
  mensajeCommit: string
): Promise<void> {
  const json = JSON.stringify(usuarios, null, 2) + '\n';
  await subirTexto('docs/usuarios.json', json, mensajeCommit);
  aviso('Publicado ✓', 'exito');
}

/**
 * Sube un flyer a docs/img/ con nombre único y devuelve la URL absoluta para
 * GitHub Pages (la APK la resuelve sin depender de rutas relativas).
 */
export async function subirFlyer(archivo: File, idEvento: string): Promise<string> {
  const base64 = await fileABase64(archivo);
  const extension =
    (archivo.name.split('.').pop() || 'jpg').toLowerCase().replace(/[^a-z0-9]/g, '') || 'jpg';
  const nombreUnico = `${idEvento}-${Date.now().toString(36)}.${extension}`;
  await subirImagen(`docs/img/${nombreUnico}`, base64, `Aguacero: flyer ${archivo.name}`);
  return `${datosEmpaquetados.sitioBase}/img/${nombreUnico}`;
}
