import { defineConfig } from 'astro/config';
import tailwind from '@tailwindcss/vite';

// Panel de administración de Aguacero
// Desplegado en GitHub Pages como https://dah3l.github.io/aguacero/admin/
export default defineConfig({
  site: 'https://dah3l.github.io',
  base: '/aguacero/admin',
  output: 'static',
  trailingSlash: 'ignore',
  vite: {
    plugins: [tailwind()],
  },
});
