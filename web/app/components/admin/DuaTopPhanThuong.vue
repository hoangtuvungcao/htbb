<script setup lang="ts">
/**
 * Cột "Phần thưởng" của hai bảng đua top: chuỗi thô + bản dịch dễ đọc (như cột chi
 * tiết của Giftcode), rồi các lựa chọn "người chơi tự chọn" gom theo "nhóm::nhãn con"
 * — đúng cách real/SuKienDuaTop.java đọc (nhóm có >1 nhãn con là menu 2 bước).
 */
import type { DuaTopNhomLuaChon } from '#shared/utils/duaTop'

defineProps<{ dong: { item: string, moTa: string[], luaChon: DuaTopNhomLuaChon[] } }>()
</script>

<template>
  <div class="space-y-2">
    <div v-if="dong.item.trim() !== ''">
      <code class="text-xs break-all text-highlighted">{{ dong.item.trim() }}</code>
      <ul v-if="dong.moTa.length" class="mt-1 list-disc space-y-0.5 pl-5 text-xs text-muted">
        <li v-for="(d, i) in dong.moTa" :key="i">
          {{ d }}
        </li>
      </ul>
    </div>
    <span v-else class="text-dimmed">—</span>

    <div v-if="dong.luaChon.length" class="text-xs text-muted">
      <strong class="text-toned">Người chơi tự chọn:</strong>
      <ul class="mt-1 list-disc space-y-0.5 pl-5">
        <li v-for="n in dong.luaChon" :key="n.ten">
          <template v-if="n.con.length === 1">
            {{ n.ten }} — <code>{{ n.con[0]!.chuoi }}</code>
          </template>
          <template v-else>
            {{ n.ten }} (chọn tiếp 1 trong {{ n.con.length }}):
            <ul class="mt-0.5 list-[circle] space-y-0.5 pl-5">
              <li v-for="(c, j) in n.con" :key="j">
                {{ c.nhan }} — <code>{{ c.chuoi }}</code>
              </li>
            </ul>
          </template>
        </li>
      </ul>
    </div>
  </div>
</template>
