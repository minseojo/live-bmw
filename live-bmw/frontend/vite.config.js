import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // 캐싱 비활성화
    force: true,
    // HMR 설정
    hmr: {
      overlay: true
    }
  },
  build: {
    // 빌드 시 캐시 무효화
    rollupOptions: {
      output: {
        manualChunks: undefined
      }
    }
  },
  // 캐시 비활성화
  cacheDir: null,
  // 환경 변수로 캐시 무효화
  define: {
    __BUILD_TIME__: JSON.stringify(Date.now())
  }
})
