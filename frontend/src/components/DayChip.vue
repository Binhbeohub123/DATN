<script setup>
defineProps({
  num:    { type: Number, required: true },
  mo:     { type: Number, required: true },
  dow:    { type: String, required: true },
  active: { type: Boolean, default: false },
  static: { type: Boolean, default: false },
})
defineEmits(['select'])
</script>

<template>
  <component
    :is="static ? 'div' : 'button'"
    :type="static ? undefined : 'button'"
    class="day-chip"
    :class="{ 'day-chip--active': active }"
    @click="static ? undefined : $emit('select')"
  >
    <span class="day-chip__num">{{ num }}/{{ mo }}</span>
    <span class="day-chip__dow">{{ dow }}</span>
  </component>
</template>

<style scoped>
.day-chip {
  flex-shrink: 0;
  width: 56px;
  min-height: 0;
  padding: 10px 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  border-radius: 12px;
  border: 1px solid var(--glass-border, rgba(255,255,255,0.08));
  background: var(--glass-bg, rgba(255,255,255,0.04));
  color: var(--text-secondary, #94a3b8);
  font-family: var(--font-ui, 'Inter', sans-serif);
  cursor: pointer;
  transition: all 0.2s;
}
.day-chip:hover {
  border-color: var(--electric, #29bcea);
  color: var(--electric, #29bcea);
  background: var(--glass-bg-heavy, rgba(255,255,255,0.08));
}
.day-chip--active {
  background: var(--electric, #29bcea);
  border-color: var(--electric, #29bcea);
  color: var(--on-accent, #ffffff);
}
.day-chip--active:hover { color: var(--on-accent, #ffffff); }
.day-chip__num { font-size: 18px; font-weight: 900; line-height: 1; font-family: inherit; }
.day-chip__dow { font-size: 10px; font-weight: 700; text-transform: uppercase; font-family: inherit; }
</style>
