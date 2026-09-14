// utils/websocket.js
// Uni-app WebSocket wrapper with controlled reconnect.
class RiderWebSocket {
  constructor(url, options = {}) {
    this.url = url
    this.reconnectInterval = options.reconnectInterval || 5000
    this.onMessageCallback = options.onMessage || (() => {})
    this.onOpenCallback = options.onOpen || (() => {})
    this.onCloseCallback = options.onClose || (() => {})
    this.onErrorCallback = options.onError || (() => {})
    this.socket = null
    this.isOpen = false
    this.manualClose = false
    this.reconnectTimer = null
    this._connect()
  }

  _connect() {
    if (this.manualClose) return
    this.socket = uni.connectSocket({
      url: this.url,
      success: () => {
        console.log('WebSocket connecting...')
      },
      fail: (err) => {
        console.error('WebSocket connect failed', err)
        this._scheduleReconnect()
      }
    })

    this.socket.onOpen(() => {
      this.isOpen = true
      console.log('WebSocket opened')
      this.onOpenCallback()
    })

    this.socket.onMessage((res) => {
      try {
        const data = JSON.parse(res.data)
        this.onMessageCallback(data)
      } catch (e) {
        console.error('WebSocket message parse failed', e)
      }
    })

    this.socket.onError((err) => {
      this.isOpen = false
      console.error('WebSocket error', err)
      this.onErrorCallback(err)
    })

    this.socket.onClose(() => {
      this.isOpen = false
      console.log('WebSocket closed')
      this.onCloseCallback()
      this._scheduleReconnect()
    })
  }

  _scheduleReconnect() {
    if (this.manualClose || this.reconnectTimer) return
    this.reconnectTimer = setTimeout(() => {
      this.reconnectTimer = null
      this._connect()
    }, this.reconnectInterval)
  }

  send(data) {
    if (this.socket && this.isOpen) {
      this.socket.send({ data: JSON.stringify(data) })
      return true
    }
    return false
  }

  close() {
    this.manualClose = true
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
    if (this.socket) {
      this.socket.close()
      this.socket = null
    }
  }
}

export default RiderWebSocket
