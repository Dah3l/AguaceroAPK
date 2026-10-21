// Lógica compartida del formulario de eventos por pasos (wizard).
// La usan nuevo.astro (vía FormEvento.astro) y editar.astro.
// Estructura esperada dentro del <form id="form-evento">:
//   - Indicador: elementos .wiz-ind con data-ind="1..N"
//   - Pasos: elementos .wiz-paso con data-paso="1..N"
//   - Botones: #btn-anterior, #btn-siguiente, #btn-guardar (en el último paso)
//   - Fechas: contenedor #lista-fechas con filas .fila-fecha (input name="fechas")
//             y botón #btn-add-fecha
//   - Resumen: contenedor #resumen (último paso)

const NOMBRES_PASOS = ['Info básica', 'Fechas y horarios', 'Lugar y contactos', 'Imagen', 'Revisar'];

function formatearFecha(iso: string): string {
  if (!iso) return '';
  const [y, m, d] = iso.split('-').map(Number);
  const f = new Date(y, (m ?? 1) - 1, d ?? 1);
  return f.toLocaleDateString('es', { weekday: 'short', day: 'numeric', month: 'short', year: 'numeric' });
}

function formatearHora(hhmm: string): string {
  if (!hhmm) return '';
  const [h, m] = hhmm.split(':').map(Number);
  const sufijo = h >= 12 ? 'PM' : 'AM';
  const h12 = h % 12 === 0 ? 12 : h % 12;
  return `${h12}:${String(m ?? 0).padStart(2, '0')} ${sufijo}`;
}

export function inicializarWizard(form: HTMLFormElement) {
  const pasos = Array.from(form.querySelectorAll<HTMLElement>('.wiz-paso'));
  const indicadores = Array.from(form.querySelectorAll<HTMLElement>('.wiz-ind'));
  const btnAnterior = form.querySelector<HTMLButtonElement>('#btn-anterior');
  const btnSiguiente = form.querySelector<HTMLButtonElement>('#btn-siguiente');
  const btnGuardar = form.querySelector<HTMLButtonElement>('#btn-guardar');
  const total = pasos.length;
  let actual = 1;

  function mostrar(n: number) {
    actual = Math.min(Math.max(n, 1), total);
    pasos.forEach((p) => {
      p.style.display = Number(p.dataset.paso) === actual ? '' : 'none';
    });
    indicadores.forEach((ind) => {
      const k = Number(ind.dataset.ind);
      ind.classList.toggle('bg-aguacero', k === actual);
      ind.classList.toggle('text-white', k === actual);
      ind.classList.toggle('border-aguacero', k === actual);
      ind.classList.toggle('bg-emerald-500', k < actual);
      ind.classList.toggle('border-emerald-500', k < actual);
      ind.classList.toggle('text-slate-500', k > actual);
    });
    if (btnAnterior) btnAnterior.style.visibility = actual === 1 ? 'hidden' : 'visible';
    if (btnSiguiente) btnSiguiente.style.display = actual === total ? 'none' : '';
    if (btnGuardar) btnGuardar.style.display = actual === total ? '' : 'none';
    if (actual === total) construirResumen();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  function aviso(msg: string) {
    (window as any).toast?.(msg, 'error');
  }

  function validar(n: number): boolean {
    if (n === 1) {
      const titulo = form.querySelector<HTMLInputElement>('[name="titulo"]');
      if (!titulo?.value.trim()) { aviso('El título es obligatorio'); titulo?.focus(); return false; }
    }
    if (n === 2) {
      const fechas = Array.from(form.querySelectorAll<HTMLInputElement>('input[name="fechas"]'))
        .map((i) => i.value).filter(Boolean);
      if (fechas.length === 0) { aviso('Agrega al menos una fecha'); return false; }
      const hora = form.querySelector<HTMLInputElement>('[name="hora"]');
      if (!hora?.value) { aviso('La hora de inicio es obligatoria'); hora?.focus(); return false; }
    }
    if (n === 3) {
      const lugar = form.querySelector<HTMLInputElement>('[name="lugar"]');
      if (!lugar?.value.trim()) { aviso('El lugar es obligatorio'); lugar?.focus(); return false; }
    }
    return true;
  }

  function construirResumen() {
    const cont = form.querySelector<HTMLElement>('#resumen');
    if (!cont) return;
    const val = (name: string) =>
      (form.querySelector<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>(`[name="${name}"]`)?.value ?? '').trim();
    const fechas = Array.from(form.querySelectorAll<HTMLInputElement>('input[name="fechas"]'))
      .map((i) => i.value).filter(Boolean).sort();
    const contactos = val('contacto').split('\n').map((s) => s.trim()).filter(Boolean);
    const sel = form.querySelector<HTMLSelectElement>('[name="categoria_id"]');
    const categoria = sel ? sel.options[sel.selectedIndex]?.text : '';
    const horaIni = formatearHora(val('hora'));
    const horaFin = formatearHora(val('hora_fin'));
    const apertura = formatearHora(val('hora_apertura'));
    const destacado = form.querySelector<HTMLInputElement>('[name="destacado"]')?.checked;
    const publicado = form.querySelector<HTMLInputElement>('[name="publicado"]');
    const img = val('imagen');

    const fila = (etiqueta: string, valor: string) => valor
      ? `<div class="flex gap-2"><span class="font-medium text-slate-500 w-32 shrink-0">${etiqueta}</span><span class="text-slate-800">${valor}</span></div>`
      : '';

    cont.innerHTML = `
      <div class="space-y-2 text-sm">
        ${fila('Título', val('titulo'))}
        ${fila('Categoría', categoria && categoria !== 'Sin categoría' ? categoria : '')}
        ${fila('Fechas', fechas.map(formatearFecha).join(' · '))}
        ${fila('Horario', horaIni + (horaFin ? ` – ${horaFin}` : ''))}
        ${fila('Apertura', apertura)}
        ${fila('Lugar', val('lugar'))}
        ${fila('Dirección', val('direccion'))}
        ${fila('Precio', val('precio'))}
        ${fila('Organizador', val('organizador'))}
        ${fila('Contactos', contactos.join(', '))}
        ${fila('Correo', val('correo'))}
        ${fila('Link web', val('link_web'))}
        ${fila('Destacado', destacado ? '⭐ Sí' : 'No')}
        ${publicado ? fila('Publicado', publicado.checked ? '🚀 Sí' : 'No (borrador)') : ''}
      </div>
      ${img ? `<img src="${img}" class="mt-4 h-32 rounded-xl object-cover border border-slate-200" alt="Flyer" />` : ''}
      ${val('descripcion') ? `<p class="mt-3 text-sm text-slate-600 whitespace-pre-line">${val('descripcion')}</p>` : ''}
    `;
  }

  btnSiguiente?.addEventListener('click', () => {
    if (validar(actual)) mostrar(actual + 1);
  });
  btnAnterior?.addEventListener('click', () => mostrar(actual - 1));
  indicadores.forEach((ind) => {
    ind.addEventListener('click', () => {
      const destino = Number(ind.dataset.ind);
      if (destino < actual) mostrar(destino);
      else if (destino > actual && validar(actual)) mostrar(destino);
    });
  });

  // Enter no envía el formulario antes del último paso
  form.addEventListener('keydown', (ev) => {
    if ((ev as KeyboardEvent).key === 'Enter' && actual < total &&
        (ev.target as HTMLElement).tagName !== 'TEXTAREA') {
      ev.preventDefault();
    }
  });

  // ---- Fechas múltiples ----
  const lista = form.querySelector<HTMLElement>('#lista-fechas');
  const btnAdd = form.querySelector<HTMLButtonElement>('#btn-add-fecha');
  function refrescarBotonesQuitar() {
    const filas = lista?.querySelectorAll('.fila-fecha') ?? [];
    filas.forEach((f) => {
      const b = f.querySelector<HTMLButtonElement>('.btn-quitar-fecha');
      if (b) b.style.display = filas.length > 1 ? '' : 'none';
    });
  }
  lista?.addEventListener('click', (ev) => {
    const btn = (ev.target as HTMLElement).closest('.btn-quitar-fecha');
    if (!btn) return;
    btn.closest('.fila-fecha')?.remove();
    refrescarBotonesQuitar();
  });
  btnAdd?.addEventListener('click', () => {
    if (!lista) return;
    const fila = document.createElement('div');
    fila.className = 'fila-fecha flex items-center gap-2';
    fila.innerHTML = `
      <input type="date" name="fechas"
        class="flex-1 rounded-xl border border-slate-300 px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-aguacero" />
      <button type="button" class="btn-quitar-fecha rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-500 hover:bg-red-50 hover:text-red-600" title="Quitar fecha">✕</button>`;
    lista.appendChild(fila);
    refrescarBotonesQuitar();
  });
  refrescarBotonesQuitar();

  mostrar(1);
}

export { NOMBRES_PASOS, formatearFecha, formatearHora };
