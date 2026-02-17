import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    uni(),
  ],
  server: {
    port: 5173,
    host: '0.0.0.0',
    strictPort: true, // Try next port if 5173 is busy, but we prefer 5173
    open: false // We use launch.json to open browser
  }
})
