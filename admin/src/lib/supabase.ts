import { createClient } from '@supabase/supabase-js';

const URL = 'https://ucwixevfwqlsbzqopqrk.supabase.co';
const ANON_KEY = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVjd2l4ZXZmd3Fsc2J6cW9wcXJrIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTIzMTQ3MzUsImV4cCI6MjEwNzg5MDczNX0.b89wp1yibuNdX7f2A6DvtZ0vMo_j94aGFnVwz_XRgv4';

export const supabase = createClient(URL, ANON_KEY);

export interface Categoria {
  id: string;
  nombre: string;
  emoji: string;
  color: string;
}

export interface Evento {
  id: string;
  titulo: string;
  descripcion: string;
  fecha: string;
  hora: string;
  hora_apertura?: string;
  lugar: string;
  direccion: string;
  precio: string;
  contacto: string[];
  link_web?: string;
  imagen: string;
  categoria_id?: string;
  organizador: string;
  destacado: boolean;
  fecha_publicacion: string;
}

export function nuevoId(): string {
  return Date.now().toString(36) + Math.random().toString(36).slice(2, 8);
}
