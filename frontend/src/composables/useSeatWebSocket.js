import { ref, onUnmounted } from 'vue'
import { Client } from '@stomp/stompjs'

/**
 * Connects to STOMP WebSocket and subscribes to seat events for a showtime.
 * MUST be called inside onMounted() — requires a valid lichChieuId at call time.
 *
 * @param {number} lichChieuId   - showtime to listen for (must be truthy)
 * @param {Function} onMessage  - called with each parsed JSON payload
 * @returns {{ connected: Ref<boolean>, disconnect: Function }}
 */
export function useSeatWebSocket(lichChieuId, onMessage) {
  const connected = ref(false)
  let client = null
  let subscription = null

  if (!lichChieuId) return { connected, disconnect() {} }

  const wsUrl = `ws://${window.location.hostname}:8080/ws`

  client = new Client({
    brokerURL: wsUrl,
    reconnectDelay: 3000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    onConnect: () => {
      connected.value = true
      subscription = client.subscribe(`/topic/seats/${lichChieuId}`, (msg) => {
        try {
          const payload = JSON.parse(msg.body)
          if (onMessage) onMessage(payload)
        } catch { /* ignore malformed */ }
      })
    },
    onDisconnect: () => { connected.value = false },
    onStompError: () => { connected.value = false },
    onWebSocketClose: () => { connected.value = false },
  })

  client.activate()

  function disconnect() {
    if (subscription) { try { subscription.unsubscribe() } catch {} subscription = null }
    if (client) { try { client.deactivate() } catch {} client = null }
    connected.value = false
  }

  return { connected, disconnect }
}
