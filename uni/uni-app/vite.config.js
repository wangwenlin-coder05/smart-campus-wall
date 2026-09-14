import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'

// Vite 配置：
//   - dev 模式下通过 server.proxy 把 /api/* 与 /ws/* 代理到后端 localhost:8080，
//     解决浏览器跨域 & 避免前端硬编码 IP 导致不可达问题。
//   - H5 打包后由 Nginx / Spring Boot 静态目录提供，不走本代理。
export default defineConfig({
  plugins: [uni()],
  server: {
    port: 5173,
    host: '0.0.0.0',
    proxy: {
      // HTTP 接口代理：前端写 /api/auth/login → 转发到 http://localhost:8080/auth/login
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      },
      // WebSocket 代理：前端写 ws://当前host:5173/ws/xxx → 转发到 ws://localhost:8080/ws/xxx
      '/ws': {
        target: 'ws://localhost:8080',
        changeOrigin: true,
        ws: true
      }
    }
  }
})
