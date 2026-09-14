// 登录态工具：统一读写当前用户，避免每个页面各写一套缓存逻辑。

const DEFAULT_AVATAR = "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png"

// 保存登录结果：后端返回 token + user。
export function saveLogin(authData) {
  const user = normalizeUser(authData.user || {})
  uni.setStorageSync("token", authData.token || "")
  uni.setStorageSync("userInfo", user)
  uni.setStorageSync("userUid", user.uid)
  return user
}

// 获取当前用户：未登录时返回 null，由页面跳转到登录注册页。
export function getCurrentUser() {
  const cachedUser = uni.getStorageSync("userInfo") || {}
  return Object.keys(cachedUser).length ? normalizeUser(cachedUser) : null
}

// 获取当前用户ID，未登录返回 null
export function getCurrentUserId() {
  const user = getCurrentUser()
  return user?.uid || null
}

// 是否已经登录：有 token 就认为登录过。
export function isLoggedIn() {
  return !!uni.getStorageSync("token")
}

// 退出登录：只清理本地缓存，不影响数据库账号。
export function logout() {
  uni.removeStorageSync("token")
  uni.removeStorageSync("userInfo")
  uni.removeStorageSync("userUid")
}

// Frontend only stores uid; internal numeric database id is never exposed.
function normalizeUser(user) {
  const uid = String(user.uid || "")
  return {
    ...user,
    uid,
    username: user.username || user.nickname || "未设置昵称",
    nickname: user.nickname || user.username || "未设置昵称",
    avatar: user.avatar || DEFAULT_AVATAR
  }
}
