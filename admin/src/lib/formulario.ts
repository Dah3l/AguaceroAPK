// Utilidades del formulario de eventos: contactos dinámicos, lectura y validación.

export interface DatosFormularioEvento {
  titulo: string;
  descripcion: string;
  fecha: string;
  hora: string;
  horaApertura: string;
  lugar: string;
  direccion: string;
  precio: string;
  contacto: string[];
  linkWeb: string;
  imagen: string;
  categoriaId: string;
  organizador: string;
}

function valor(id: string): string {
  return ((document.getElementById(id) as HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement)?.value ?? '').trim();
}

/** Crea una fila de teléfono con botón para quitarla. */
export function anadirCampoContacto(telefono = ''): void {
  const lista = document.getElementById('lista-contactos');
  if (!lista) return;
  const fila = document.createElement('div');
  fila.className = 'flex gap-2';
  const entrada = document.createElement('input');
  entrada.type = 'tel';
  entrada.placeholder = '+53 58335927';
  entrada.value = telefono;
  entrada.className =
    'flex-1 rounded-lg border border-slate-300 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-aguacero';
  const botonQuitar = document.createElement('button');
  botonQuitar.type = 'button';
  botonQuitar.textContent = '✕';
  botonQuitar.title = 'Quitar teléfono';
  botonQuitar.className =
    'rounded-lg border border-slate-300 px-3 text-slate-500 hover:bg-red-50 hover:text-red-600';
  botonQuitar.addEventListener('click', () => fila.remove());
  fila.append(entrada, botonQuitar);
  lista.append(fila);
}

/** Conecta el botón "Añadir teléfono". */
export function montarGestionContactos(): void {
  document.getElementById('btn-anadir-contacto')?.addEventListener('click', () => anadirCampoContacto());
}

/** Rellena la lista de contactos (usado al editar). */
export function cargarContactos(telefonos: string[]): void {
  const lista = document.getElementById('lista-contactos');
  if (!lista) return;
  lista.innerHTML = '';
  telefonos.forEach((t) => anadirCampoContacto(t));
  if (telefonos.length === 0) anadirCampoContacto('');
}

/** Lee todos los campos del formulario. */
export function leerFormulario(): DatosFormularioEvento {
  const contactos = Array.from(
    document.querySelectorAll('#lista-contactos input[type="tel"]') as NodeListOf<HTMLInputElement>
  )
    .map((i) => i.value.trim())
    .filter(Boolean);

  const archivoSeleccionado = (document.getElementById('imagen-archivo') as HTMLInputElement)?.files?.[0];
  // Si no se sube archivo, se conserva/usa la URL escrita.
  const imagen = archivoSeleccionado ? '' : valor('imagen-url');

  return {
    titulo: valor('titulo'),
    descripcion: ((document.getElementById('descripcion') as HTMLTextAreaElement)?.value ?? '').trim(),
    fecha: valor('fecha'),
    hora: valor('hora'),
    horaApertura: valor('hora-apertura'),
    lugar: valor('lugar'),
    direccion: valor('direccion'),
    precio: valor('precio'),
    contacto: contactos,
    linkWeb: valor('link-web'),
    imagen,
    categoriaId: valor('categoria'),
    organizador: valor('organizador'),
  };
}

/** Valida los obligatorios; devuelve false y pinta el error si falla. */
export function validar(parrafoError: HTMLElement): boolean {
  const datos = leerFormulario();
  const faltantes: string[] = [];
  if (!datos.titulo) faltantes.push('Título');
  if (!datos.descripcion) faltantes.push('Descripción');
  if (!datos.fecha) faltantes.push('Fecha');
  if (!datos.hora) faltantes.push('Hora');
  if (!datos.lugar) faltantes.push('Lugar');
  if (!datos.direccion) faltantes.push('Dirección');
  if (!datos.categoriaId) faltantes.push('Categoría');

  if (faltantes.length > 0) {
    parrafoError.textContent = `Faltan campos obligatorios: ${faltantes.join(', ')}.`;
    parrafoError.classList.remove('hidden');
    return false;
  }
  return true;
}

/** Muestra la vista previa de la imagen actual. */
export function mostrarVistaPrevia(url: string): void {
  const imagen = document.getElementById('vista-imagen') as HTMLImageElement | null;
  const campoUrl = document.getElementById('imagen-url') as HTMLInputElement | null;
  if (!imagen) return;
  if (campoUrl && !campoUrl.value) campoUrl.value = url;
  if (url) {
    imagen.src = url;
    imagen.classList.remove('hidden');
  } else {
    imagen.classList.add('hidden');
  }
}
