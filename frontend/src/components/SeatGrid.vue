<template>
  <div class="seat-grid-root">
    <div v-if="showScreen" class="seat-screen-box">&mdash; M&agrave;n h&igrave;nh &mdash;</div>
    <div class="seat-grid-scroll">
      <div class="seat-rows-wrap">
        <div v-for="row in rows" :key="row.label" class="seat-row">
          <slot name="row-left" :row="row">
            <span class="seat-row-label">{{ row.label }}</span>
          </slot>

          <div
            class="seat-cells"
            :style="{
              gridTemplateColumns: 'repeat(' + totalCols + ', ' + seatSize + 'px)',
              gap: seatGap + 'px'
            }"
          >
            <button
              v-for="seat in row.seats"
              :key="seatKeyFn(seat)"
              class="seat"
              :class="[seatTypeClass(seat), seatClassFn(seat)]"
              :style="{
                gridColumn: seat.soGhe,
                width: seatSize + 'px',
                height: seatSize + 'px',
                borderRadius: seatRadius + 'px'
              }"
              :disabled="disabledIds.has(seatIdFn(seat))"
              :title="seatTitleFn(seat)"
              @click="$emit('seat-click', seat)"
              @mousedown="$emit('seat-mousedown', seat)"
              @mouseenter="$emit('seat-mouseenter', seat)"
            >
              <slot name="seat-content" :seat="seat">
                {{ seat.soGheHienThi ?? seat.soGhe }}
              </slot>
            </button>
          </div>

          <slot name="row-right" :row="row">
            <span class="seat-row-label">{{ row.label }}</span>
          </slot>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  rows:          { type: Array, required: true },
  totalCols:     { type: Number, required: true },
  showScreen:    { type: Boolean, default: true },
  maxWidth:      { type: Number, default: 1630 },
  seatSize:      { type: Number, default: 36 },
  seatRadius:    { type: Number, default: 8 },
  seatGap:       { type: Number, default: 6 },
  rowGap:        { type: Number, default: 10 },
  disabledIds:   { type: Set, default: () => new Set() },
  seatKeyFn:     { type: Function, default: s => s.id },
  seatIdFn:      { type: Function, default: s => s.id },
  seatTitleFn:   { type: Function, default: () => '' },
  seatClassFn:   { type: Function, default: () => '' },
})

defineEmits(['seat-click', 'seat-mousedown', 'seat-mouseenter'])

function seatTypeClass(seat) {
  const t = (seat.loaiGhe || '').toLowerCase()
  if (t === 'trống') return 'seat--trong'
  if (t === 'vip') return 'seat--vip'
  if (t.includes('cặp') || t.includes('couple')) return 'seat--couple'
  return ''
}
</script>

<style scoped>
.seat-grid-root { width: 100%; max-width: v-bind('maxWidth + "px"'); margin: 0 auto; }

.seat-screen-box {
  text-align: center;
  font-size: 13px;
  font-weight: 600;
  color: rgba(148, 163, 184, 0.7);
  background: rgba(255, 255, 255, 0.04);
  border-radius: 6px;
  padding: 8px;
  margin-bottom: 20px;
}

.seat-grid-scroll { overflow-x: auto; padding-bottom: 16px; }
.seat-rows-wrap {
  display: flex;
  flex-direction: column;
  gap: v-bind('rowGap + "px"');
  width: max-content;
  margin: 0 auto;
}

.seat-row { display: flex; align-items: center; gap: 10px; }
.seat-row-label {
  width: 24px;
  text-align: center;
  font-size: 12px;
  font-weight: 800;
  flex-shrink: 0;
}

.seat-cells { display: grid; }

.seat {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(255, 255, 255, 0.08);
  font-size: 11px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.15s ease-out, box-shadow 0.2s ease-out;
}
.seat:hover:not(:disabled):not(.seat--trong) {
  transform: scale(1.12) translateY(-2px);
  border-color: #29bcea;
  color: #29bcea;
}
.seat:disabled { cursor: not-allowed; opacity: 0.5; }

.seat--vip {
  background: #C9A84C;
  border-color: #C9A84C;
  color: #ffffff;
}
.seat--vip:hover:not(:disabled) {
  background: #F5D17E;
  box-shadow: 0 0 8px rgba(201, 168, 76, 0.35);
}

.seat--couple {
  background: #ec4899;
  border-color: #db2777;
  color: #ffffff;
}
.seat--couple:hover:not(:disabled) {
  background: #f472b6;
}

.seat--trong {
  background: transparent;
  border-style: dashed;
  border-color: rgba(148, 163, 184, 0.55);
  color: transparent;
  cursor: default;
}
.seat--trong:hover:not(:disabled) {
  transform: none;
  border-color: rgba(148, 163, 184, 0.55);
}

.seat--booked {
  background: rgba(239, 68, 68, 0.2);
  border-color: rgba(239, 68, 68, 0.4);
  color: #ef4444;
  opacity: 0.7;
}

.seat--selected {
  background: #29bcea;
  border-color: #29bcea;
  color: #ffffff;
  transform: scale(1.06);
  box-shadow: 0 0 12px rgba(41, 188, 234, 0.30);
}
.seat--selected:hover:not(:disabled) {
  transform: scale(1.06);
  border-color: #29bcea;
  color: #ffffff;
  box-shadow: 0 0 12px rgba(41, 188, 234, 0.30);
}

.seat--locked {
  background: rgba(245, 158, 11, 0.25);
  border-color: rgba(245, 158, 11, 0.5);
  color: #f59e0b;
  cursor: not-allowed;
}

.seat--admin-selected {
  outline: 2px solid #60A5FA;
  outline-offset: 2px;
}
</style>
