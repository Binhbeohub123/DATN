<template>
  <svg
    :width="size"
    :height="size"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
    xmlns="http://www.w3.org/2000/svg"
  >
    <path d="M19 12H5"/>
    <path d="M12 19l-7-7 7-7"/>
  </svg>
</template>

<script setup>
defineProps({ size: { type: Number, default: 24 } })
</script>
