// Tipos de datos de Aguacero (compartidos con la APK mediante eventos.json)

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
  fecha: string;          // "2026-10-18"
  hora: string;           // "19:00"
  horaApertura?: string;  // opcional
  horaFin?: string;       // opcional, hora de culminación
  lugar: string;
  direccion: string;
  precio: string;         // texto libre
  contacto: string[];     // teléfonos
  correo?: string;        // correo de contacto
  fechas?: string[];      // todas las fechas del evento
  linkWeb?: string;       // opcional
  imagen: string;         // URL del flyer
  categoriaId: string;
  organizador?: string;   // opcional
  destacado: boolean;
  fechaPublicacion: string; // ISO
}

export interface DatosCartelera {
  categorias: Categoria[];
  eventos: Evento[];
}

export interface Usuario {
  usuario: string;
  nombre: string;
  hash: string;        // SHA-256 en hexadecimal
  esAdmin?: boolean;   // solo el primer usuario es admin
}

export interface AjustesRepo {
  owner: string;
  repo: string;
  rama: string;
}
