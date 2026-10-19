<script setup lang="ts">
/** Chiêu thức theo dòng — tên + hệ chiêu từ pokedex.json (bang_chieu.csv / ChieuPokemon.java). */
const HO_TRO = new Set(['Màn khói', 'Vũ điệu kiếm', 'Tự vệ', 'Sinh trưởng', 'Mong ước', 'Nghỉ ngơi', 'Phước lành hồi sinh', 'Chia sẻ nỗi đau', 'Phá mai', 'Rút vào mai', 'Nhanh nhẹn', 'Nạp điện'])

const dong = POKEDEX.khoiDau.map((k) => {
  const dau = loaiTheoDex(k.dex)!
  const cacDang = [...(dau.tienHoa?.buoc ?? []).map(b => b.dex), ...(dau.tienHoa?.nhanh ?? []).map(b => b.dex)]
    .map(d => loaiTheoDex(d)!).filter(Boolean)
  return { ...k, dau, cacDang: cacDang.length ? cacDang : [dau], chieu: dau.chieu ?? [] }
})
const chon = ref(dong[0]!.dex)
const d = computed(() => dong.find(x => x.dex === chon.value)!)
const dangChon = ref<number | null>(null)
watch(chon, () => { dangChon.value = null })
const loaiDang = computed(() => d.value.cacDang.find(l => l.dex === dangChon.value) ?? d.value.cacDang[d.value.cacDang.length - 1]!)
const coStab = (he: number) => loaiDang.value.he.includes(he)
</script>

<template>
  <WikiShell slug="chieu-thuc">
    <p class="van">
      Mỗi dòng khởi đầu có một bộ chiêu riêng. Mỗi lần lên cấp bạn nhận <strong>1 điểm kỹ năng</strong> để học hoặc nâng chiêu
      (tăng % sức mạnh). Gán chiêu vào thanh phím tắt (phím 1, 3, 5, 7, 9) và bấm <strong>phím 0</strong> để đổi bảng chiêu.
    </p>

    <WikiSection id="bang-chieu" title="Chiêu theo dòng">
      <div class="flex flex-wrap gap-2" role="tablist" aria-label="Chọn dòng Pokémon">
        <UButton
          v-for="x in dong"
          :key="x.dex"
          role="tab"
          :aria-selected="chon === x.dex"
          :color="chon === x.dex ? 'primary' : 'neutral'"
          :variant="chon === x.dex ? 'solid' : 'outline'"
          @click="chon = x.dex"
        >
          <PokeAvatar :dex="x.dex" class="size-6" /> {{ x.dau.ten }}
        </UButton>
      </div>

      <div class="mt-4 flex flex-wrap items-center gap-2 text-sm">
        <span class="font-semibold text-muted">Tính STAB theo dạng:</span>
        <button
          v-for="l in d.cacDang"
          :key="l.dex"
          type="button"
          class="flex items-center gap-1.5 rounded-full border px-2 py-1 transition"
          :class="loaiDang.dex === l.dex ? 'border-primary bg-primary/10 font-bold text-highlighted' : 'border-default text-toned hover:border-primary'"
          :aria-pressed="loaiDang.dex === l.dex"
          @click="dangChon = l.dex"
        >
          <PokeAvatar :dex="l.dex" class="size-5" /> {{ l.ten }}
        </button>
      </div>

      <div class="bang-cuon mt-4">
        <table class="bang-wiki">
          <caption>Bảng chiêu dòng {{ d.dau.ten }} — STAB tính cho {{ loaiDang.ten }} ({{ loaiDang.he.map(tenHe).join('/') }})</caption>
          <thead>
            <tr>
              <th class="so">
                #
              </th><th>Chiêu</th><th>Hệ</th><th>Loại</th><th>STAB ×{{ (POKEDEX.stabPhanTram / 100).toLocaleString('vi-VN') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="c in d.chieu" :key="c.so">
              <td class="so text-dimmed">
                {{ c.so + 1 }}
              </td>
              <td class="font-semibold text-highlighted">
                <span class="inline-flex items-center gap-2">
                  <img :src="`/chieu/${d.class}-${c.so}.png`" alt="" width="28" height="28" class="size-7 shrink-0" loading="lazy">
                  {{ c.ten }}
                </span>
              </td>
              <td><PokeHeBadge :he="c.he" size="sm" /></td>
              <td>{{ HO_TRO.has(c.ten) ? 'Hỗ trợ / tăng sức' : c.so === 0 ? 'Đánh thường' : 'Tấn công' }}</td>
              <td>
                <UBadge v-if="!HO_TRO.has(c.ten) && coStab(c.he)" color="success" variant="subtle" size="sm" icon="i-lucide-check">
                  Có
                </UBadge>
                <span v-else class="text-dimmed">—</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </WikiSection>

    <WikiSection id="meo" title="Mẹo dùng chiêu">
      <ul class="cham">
        <li>Ưu tiên nâng một chiêu tấn công <strong>cùng hệ</strong> (STAB) làm đòn chủ lực, và một chiêu <strong>khác hệ</strong> để đánh quái kháng hệ của bạn — ví dụ Charmander có Vuốt rồng (Rồng) khi gặp quái hệ Nước/Đá.</li>
        <li>Chiêu hỗ trợ (tăng công, tăng thủ, hồi máu) rất đáng giá khi đi đội và đánh boss.</li>
        <li>Eevee giữ nguyên bảng chiêu sau khi tiến hoá, nhưng hệ đổi theo nhánh — nên chiêu được STAB cũng thay đổi.</li>
        <li>Xem hệ của quái trước khi đánh trong <NuxtLink to="/pokedex" class="lien-ket">Pokédex</NuxtLink>; hệ số khắc hệ đi theo hệ của <strong>chiêu</strong>, không phải hệ của bạn.</li>
      </ul>
    </WikiSection>
  </WikiShell>
</template>
