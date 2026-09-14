// 环境配置
// =========
// 核心策略：
//   H5 开发阶段（dev server 端口 5173）→ 所有请求走 Vite 代理（相对路径 /api 前缀），
//     从根本上避开「跨域」和「后端 IP 硬编码不可达」两大问题；
//   其他场景（小程序 / App / H5 打包部署）→ 使用绝对地址（支持用户通过 localStorage.endpoint 手动覆盖）。

const DEFAULT_REMOTE = "10.187.87.194:8080"
const DEFAULT_LOCAL = "localhost:8080"

// 运行时判断：是否为浏览器/H5 环境
const isBrowser =
  typeof window !== 'undefined' && typeof document !== 'undefined'

// 运行时判断：是否为 dev server（当前页面端口 5173）
let isH5DevServer = false
let hostname = ''
try {
  if (typeof location !== 'undefined' && location && typeof location.hostname === 'string') {
    hostname = location.hostname
    const port = String(location.port || '')
    // Vite 默认 dev 端口是 5173；如果你改过 vite.config.js 的 server.port，这里同步改
    isH5DevServer = (port === '5173')
  }
} catch (e) { /* 非 H5 环境，忽略 */ }

// 允许用户在 DevTools 控制台强制指定后端地址（最高优先级）：
//   localStorage.setItem('endpoint', 'http://192.168.1.10:8080')
//   localStorage.removeItem('endpoint')
let userEndpoint = null
try {
  if (typeof localStorage !== 'undefined') {
    userEndpoint = localStorage.getItem('endpoint') || null
  }
} catch (e) { /* 小程序端无 localStorage，忽略 */ }

let baseUrl
let wsBaseUrl
// 是否走 Vite 代理（H5 dev 专属）。走代理时 request.js 会自动给 URL 加 /api 前缀，
// vite.config.js 把 /api/* rewrite 掉并转发到 http://localhost:8080
let useViteProxy = false

if (userEndpoint) {
  // 用户手动覆盖 → 绝对地址
  baseUrl = userEndpoint.replace(/\/$/, '')
  wsBaseUrl = baseUrl.replace(/^http/, 'ws')
  useViteProxy = false
} else if (isBrowser && isH5DevServer) {
  // H5 + Vite dev server → 相对路径走代理，避免跨域 & 避免 IP 写死不可达
  baseUrl = ''
  wsBaseUrl = ''
  useViteProxy = true
} else if (hostname === 'localhost' || hostname === '127.0.0.1') {
  // 非 5173 端口但本机打开（打包产物被本机静态服务加载）→ 回退本机
  baseUrl = "http://" + DEFAULT_LOCAL
  wsBaseUrl = "ws://" + DEFAULT_LOCAL
} else {
  // 其他场景：远端部署/教室环境 IP
  baseUrl = "http://" + DEFAULT_REMOTE
  wsBaseUrl = "ws://" + DEFAULT_REMOTE
}

export default {
  baseUrl,
  wsBaseUrl,
  useViteProxy,
  // 开发调试用：在控制台查看当前使用的地址
  endpoints: {
    remote: "http://" + DEFAULT_REMOTE,
    local: "http://" + DEFAULT_LOCAL
  }
}
