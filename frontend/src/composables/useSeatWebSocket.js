import { ref } from 'vue'
import { Client } from '@stomp/stompjs'

/**
 * Connects to STOMP WebSocket and subscribes to seat events for a showtime.
 * Support switching showtime via switchTopic() without reconnecting the client.
 *
 * @param {number} lichChieuId   - initial showtime to listen for (must be truthy)
 * @param {Function} onMessage  - called with each parsed JSON payload
 * @returns {{ connected: Ref<boolean>, switchTopic: Function, disconnect: Function }}
 */
export function useSeatWebSocket(lichChieuId, onMessage) {
  const connected = ref(false)
  let client = null
  let subscription = null
  let topicId = lichChieuId
  let isActive = false

  if (!lichChieuId) return { connected, switchTopic() {}, disconnect() {} }

  const wsUrl = `ws://${window.location.hostname}:8080/ws`

  client = new Client({
    brokerURL: wsUrl,
    reconnectDelay: 3000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    onConnect: () => {
      isActive = true
      connected.value = true
      // (re-)subscribe to the current topic after (re)connecting
      subscription = client.subscribe(`/topic/seats/${topicId}`, (msg) => {
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

  /**
   * Switch the subscribed showtime topic to a new lichChieuId.
   * Unsubscribes from the OLD topic before subscribing to the NEW one.
   */
  function switchTopic(newId) {
    if (!newId || newId === topicId) return false
    topicId = newId
    if (!isActive || !client || !client.connected) {
      // client will subscribe with the new topic on next onConnect
      return true
    }
    if (subscription) {
      try { subscription.unsubscribe() } catch {}
      subscription = null
    }
    subscription = client.subscribe(`/topic/seats/${topicId}`, (msg) => {
      try {
        const payload = JSON.parse(msg.body)
        if (onMessage) onMessage(payload)
      } catch { /* ignore malformed */ }
    })
    return true
  }

  function disconnect() {
    isActive = false
    if (subscription) { try { subscription.unsubscribe() } catch {} subscription = null }
    if (client) { try { client.deactivate() } catch {} client = null }
    connected.value = false
  }

  return { connected, switchTopic, disconnect }
}
