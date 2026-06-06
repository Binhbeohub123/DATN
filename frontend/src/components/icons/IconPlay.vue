<template>
  <svg
    :width="size"
    :height="size"
    viewBox="0 0 24 24"
    fill="none"
    stroke="none"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
    xmlns="http://www.w3.org/2000/svg"
  >
    <polygon fill="currentColor" stroke="none" points="5 3 19 12 5 21 5 3"/>
  </svg>
</template>

<script setup>
defineProps({ size: { type: Number, default: 24 } })
</script>
