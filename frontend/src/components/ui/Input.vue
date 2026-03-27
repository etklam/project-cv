<script setup>
defineProps({
  modelValue: {
    type: [String, Number],
    default: "",
  },
  placeholder: {
    type: String,
    default: "",
  },
});

const emit = defineEmits(["update:modelValue"]);
</script>

<template>
  <input
    class="ui-input"
    :value="modelValue"
    :placeholder="placeholder"
    @input="emit('update:modelValue', $event.target.value)"
  />
</template>

<style scoped>
.ui-input {
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  padding: 8px 10px;
  width: 100%;
}
</style>
