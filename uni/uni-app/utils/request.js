// 引入后端基础地址
import config from "@/config/env.js"

// 当前正在使用的 baseUrl（支持运行时动态切换，如 failover 到 localhost）
let currentBaseUrl = config.baseUrl

/**
 * 切换 baseUrl（用于网络不可达时自动 failover 到本地）
 */
export function setBaseUrl(url) {
  currentBaseUrl = url
  console.info('[request] baseUrl 切换为:', url)
}

/**
 * 获取当前生效的 baseUrl
 */
export function getBaseUrl() {
  return currentBaseUrl
}

/**
 * 根据 env 配置，给业务层传入的 options.url 拼出最终请求地址
 *   - Vite dev 代理模式：baseUrl 为空，给 url 加 /api 前缀，由 vite server 代理转发
 *   - 绝对地址模式：直接 currentBaseUrl + options.url
 */
function buildFinalUrl(rawUrl) {
  const path = String(rawUrl || '')
  if (config.useViteProxy) {
    // 已经是 /api 开头就不加了，避免重复
    if (path.startsWith('/api/') || path === '/api') return path
    // 确保以 / 开头
    const normalized = path.startsWith('/') ? path : '/' + path
    return '/api' + normalized
  }
  return currentBaseUrl + path
}

// 统一请求方法：只用来获取后台数据
const request = (options) => {
  // 拼接完整接口地址
  const url = buildFinalUrl(options.url)
  // 自定义超时：不传则使用 15 秒；文件上传类可在 options 中覆盖更大值
  const timeout = options.timeout || 15000

  return new Promise((resolve, reject) => {
    uni.request({
      url: url,

      method: options.method || "GET",
      data: options.data || {},
      timeout: timeout,

      // 请求头，最简单通用格式
      header: {
        "Content-Type": "application/json",
        // 开发阶段 token 只用于标识当前登录用户，后端暂不做频繁校验。
        "Authorization": uni.getStorageSync("token") || ""
      },

      // 请求成功
      success: (res) => {
        // 200 = 接口通了，拿到后台数据
        if (res.statusCode === 200) {
          // 把后台数据返回给页面
          resolve(res.data)
        } else {
          // 其他错误提示
          const msg = (res.data && (res.data.msg || res.data.message)) || `请求失败 (${res.statusCode})`
          console.warn('[request fail] statusCode=' + res.statusCode, 'msg=' + msg, 'url=' + url)
          reject(res.data || new Error(msg))
        }
      },

      // 网络不通、后端没启动、超时
      fail: (err) => {
        const errMsg = (err && err.errMsg) || ''
        let tip = '无法连接后端'
        if (errMsg.indexOf('timeout') >= 0) {
          tip = '请求超时，请检查后端是否启动 (Vite 代理模式后端应为 localhost:8080)'
        }
        console.error('[request error]', tip, 'url=' + url, 'err=', err)
        reject(err || new Error(tip))
      }
    })
  })
}

export default request
