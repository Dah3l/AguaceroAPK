import { defineConfig } from 'astro/config';
import tailwind from '@tailwindcss/vite';

// Panel de administración de Aguacero
// Desplegado en GitHub Pages como https://dah3l.github.io/AguaceroAPK/
export default defineConfig({
  site: 'https://dah3l.github.io',
  base: '/AguaceroAPK',
  output: 'static',
  trailingSlash: 'ignore',
  vite: {
    plugins: [tailwind()],
  },
});
