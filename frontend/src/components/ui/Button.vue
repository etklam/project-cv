<script setup>
defineProps({
  type: {
    type: String,
    default: "button",
  },
});
</script>

<template>
  <button class="ui-button" :type="type"><slot /></button>
</template>

<style scoped>
.ui-button {
  border: 1px solid #0f172a;
  background: #0f172a;
  color: #fff;
  border-radius: 10px;
  padding: 8px 14px;
  cursor: pointer;
}
</style>
