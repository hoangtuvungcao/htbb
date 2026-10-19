<script setup lang="ts">
/**
 * Biểu tượng tiền tệ trong game (+ số): <TienTe loai="xu" :gt="1250000" />, <TienTe loai="luong_khoa" /> (chỉ biểu tượng).
 * Cùng hình với game (client_jar/tools/tao_icon_tien.py), di chuột / chạm = chú thích + số đầy đủ.
 */
import type { LoaiTien } from '#shared/utils/tienTe'

const props = withDefaults(defineProps<{
  loai: LoaiTien
  gt?: number | null
  /** số gọn (1,25 Tr) - số đầy đủ vẫn ở chú thích */
  gon?: boolean
  /** cỡ biểu tượng (px CSS) */
  co?: number
  /** hiện tên loại sau số ("1.000 xu") */
  ten?: boolean
}>(), { gt: null, gon: false, co: 18, ten: false })

const tt = computed(() => TIEN_TE[props.loai])
const chu = computed(() => (props.gt == null ? '' : props.gon ? soGon(props.gt) : soTien(props.gt)))
const goiY = computed(() => (props.gt == null ? tt.value.goiY : `${tt.value.ten}: ${soTien(props.gt)} — ${tt.value.goiY.split(': ')[1]}`))
</script>

<template>
  <span class="inline-flex items-center gap-1 align-middle whitespace-nowrap" :title="goiY">
    <img
      :src="`/tien/${loai}_32.png`"
      :srcset="`/tien/${loai}_32.png 1x, /tien/${loai}_64.png 2x`"
      :width="co"
      :height="co"
      :alt="tt.ten"
      class="inline-block shrink-0"
      loading="lazy"
      decoding="async"
    >
    <span v-if="chu" class="tabular-nums">{{ chu }}</span>
    <span v-if="ten" class="lowercase">{{ tt.ten }}</span>
    <span v-if="!chu && !ten" class="sr-only">{{ tt.ten }}</span>
  </span>
</template>
