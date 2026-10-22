<script setup lang="ts">
/**
 * Đồ họ & bộ trang bị (0.9.8): 18 bộ hệ + 3 bộ huy chương vùng. Dữ liệu: /api/do-trang-phuc (CSDL, rơi về
 * shared/data/danh-muc-do-trang-phuc.json) — sinh bởi tools/do_trang_phuc/sinh.py.
 */
import macDinh from '~~/shared/data/danh-muc-do-trang-phuc.json'
import { TEN_BAC, TEN_CHI_SO_BO, TEN_NGUON_DO, TEN_O, duongIcon, phanTram } from '~~/shared/utils/doTrangPhuc.mjs'

const { data } = await useFetch('/api/do-trang-phuc', { key: 'do-trang-phuc', default: () => macDinh })
const dm = computed(() => data.value ?? macDinh)

const boChon = ref('tat-ca')
const oChon = ref('tat-ca')
const bacChon = ref(-1)

const boItems = computed(() => [{ label: 'Mọi bộ', value: 'tat-ca' }, ...dm.value.bo.map(b => ({ label: b.ten, value: b.ma }))])
const oCo = computed(() => [...new Set(dm.value.do.map(d => d.o))])
const oItems = computed(() => [{ label: 'Mọi ô', value: 'tat-ca' }, ...oCo.value.map(o => ({ label: (TEN_O as Record<string, string>)[o] ?? o, value: o }))])
const bacItems = [{ label: 'Mọi bậc', value: -1 }, ...TEN_BAC.map((t: string, i: number) => ({ label: `${t[0]!.toUpperCase()}${t.slice(1)}`, value: i }))]

const tenBo = computed(() => Object.fromEntries(dm.value.bo.map(b => [b.ma, b.ten])))
const loc = computed(() => dm.value.do.filter(d =>
  (boChon.value === 'tat-ca' || d.maHo === boChon.value)
  && (oChon.value === 'tat-ca' || d.o === oChon.value)
  && (bacChon.value < 0 || d.bac === bacChon.value)))
const TOI_DA = 120
const hienDs = computed(() => loc.value.slice(0, TOI_DA))

const boHe = computed(() => dm.value.bo.filter(b => b.loai === 'he'))
const boVung = computed(() => dm.value.bo.filter(b => b.loai === 'vung'))
const soHe = computed(() => dm.value.do.filter(d => d.maHo.startsWith('he_')).length)
const soVung = computed(() => dm.value.do.length - soHe.value)

function chuThuong(t: { soMon: number, chiSo: string, phanNghin: number }, b: { he?: string, heKhac?: string }) {
  const cs = (TEN_CHI_SO_BO as Record<string, string>)[t.chiSo] ?? t.chiSo
  const he = t.chiSo === 'st_he' ? ` ${b.he}` : t.chiSo === 'khang_he_khac' ? ` ${b.heKhac}` : ''
  return `${t.soMon} món: +${phanTram(t.phanNghin)}% ${cs.replace(' khắc', '')}${he}`
}
const tenNguon = (n: string) => (TEN_NGUON_DO as Record<string, string>)[n] ?? n
const tenO = (o: string) => (TEN_O as Record<string, string>)[o] ?? o
</script>

<template>
  <WikiShell slug="do-moi">
    <p class="van">
      {{ dm.do.length }} món đồ họ mới: {{ soHe }} món trong 18 <strong>bộ hệ</strong> (6 ô khăn quàng, đai, băng đô, vòng cổ,
      lông vũ, ruy băng ở 5 bậc) và {{ soVung }} món trong 3 <strong>bộ huy chương vùng</strong> Kanto, Johto, Hoenn (huy hiệu,
      găng, phiến đá và đồ cầm tay của cả 5 dòng ở mọi bậc). Mỗi món có icon pixel riêng; độ hiếm (Thường → Huyền thoại)
      vẫn quay ngẫu nhiên khi món rơi như mọi trang bị.
    </p>

    <WikiCallout kieu="vi-sao" title="Không mạnh hơn, chỉ khác hướng">
      Mỗi món đồ họ là <strong>bản sao chỉ số</strong> của món gốc cùng ô, cùng bậc. Cái khác là tên, hình và
      <strong>thưởng bộ</strong>: thưởng bộ hệ cộng vào sát thương hệ / kháng hệ (chung trần 20% / 60%), thưởng bộ vùng cộng chung
      trần thưởng bộ 5% và chí mạng bộ 3% với bộ cùng bậc. Đồ họ <strong>thay</strong> món gốc khi rơi từ quái, boss hoặc chế tạo —
      số món rơi mỗi giờ không đổi, nên không có thêm nguồn đồ nào.
    </WikiCallout>

    <WikiSection id="bo-he" title="18 bộ hệ" lead="Mặc 2 / 4 / 6 món cùng bộ (bậc nào cũng tính).">
      <div class="bang-cuon">
        <table class="bang-wiki">
          <thead>
            <tr>
              <th>Bộ</th><th>Hệ</th><th>Từ → đến</th><th>Thưởng</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="b in boHe" :key="b.ma">
              <td class="font-semibold text-highlighted">
                {{ b.ten }}
              </td>
              <td>{{ b.he }}</td>
              <td class="whitespace-nowrap text-sm">
                {{ (b as any).pokemon ? `${(b as any).pokemon[0]} → ${(b as any).pokemon.at(-1)}` : '—' }}
              </td>
              <td class="text-sm">
                <span v-for="t in b.thuong" :key="t.soMon + t.chiSo" class="block">{{ chuThuong(t, b) }}</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </WikiSection>

    <WikiSection id="bo-vung" title="Bộ huy chương vùng" lead="Huy hiệu, găng, phiến đá và đồ cầm tay mang tên huy hiệu Gym, Trưởng Gym và địa danh.">
      <ul class="grid gap-3 sm:grid-cols-3">
        <li v-for="b in boVung" :key="b.ma" class="the-so">
          <b class="!text-xl">{{ b.ten }}</b>
          <span class="block">Bậc {{ TEN_BAC[(b as any).bacTu] }} → {{ TEN_BAC[(b as any).bacDen] }}</span>
          <span v-for="t in b.thuong" :key="t.soMon + t.chiSo" class="block">{{ chuThuong(t, b) }}</span>
        </li>
      </ul>
    </WikiSection>

    <WikiSection id="danh-sach" title="Danh sách đồ" :lead="`${loc.length} món khớp bộ lọc.`">
      <div class="grid gap-3 sm:grid-cols-3">
        <USelect v-model="boChon" :items="boItems" class="w-full" aria-label="Lọc theo bộ" />
        <USelect v-model="oChon" :items="oItems" class="w-full" aria-label="Lọc theo ô" />
        <USelect v-model="bacChon" :items="bacItems" class="w-full" aria-label="Lọc theo bậc" />
      </div>
      <ul class="mt-5 grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-3">
        <li v-for="d in hienDs" :key="d.id" class="the-go flex gap-3 p-3">
          <span class="grid size-14 shrink-0 place-items-center rounded-2xl bg-screen">
            <img :src="duongIcon(d.icon)" width="16" height="16" :alt="d.ten" loading="lazy" class="pixelated size-12">
          </span>
          <span class="min-w-0">
            <strong class="block truncate font-display text-highlighted">{{ d.ten }}</strong>
            <span class="block text-xs text-muted">{{ tenO(d.o) }} · bậc {{ TEN_BAC[d.bac] }} · {{ tenBo[d.maHo] }}</span>
            <span class="mt-1 flex flex-wrap gap-1">
              <UBadge v-for="n in d.nguon" :key="n" color="neutral" variant="subtle" size="sm">{{ tenNguon(n) }}</UBadge>
            </span>
          </span>
        </li>
      </ul>
      <p v-if="loc.length > TOI_DA" class="mt-3 text-sm text-muted">
        Đang hiện {{ TOI_DA }} / {{ loc.length }} món — chọn bộ, ô hoặc bậc để xem hết.
      </p>
    </WikiSection>

    <WikiSection id="nguon" title="Lấy ở đâu">
      <ul class="list-disc space-y-1 pl-5 text-sm">
        <li><strong>Quái cùng hệ</strong>: một phần món rơi ở 6 ô bộ hệ (đúng bậc có bộ) đổi thành món bộ hệ theo hệ của Pokémon hoang dã bị hạ.</li>
        <li><strong>Quái trong vùng</strong>: một phần huy hiệu, găng, phiến đá, đồ cầm tay rơi đổi thành món bộ huy chương của bậc.</li>
        <li><strong>Boss</strong>: món rơi ưu tiên bộ hệ theo hệ của boss.</li>
        <li><strong>Chế tạo ở Machamp</strong>: vòng cổ theo hệ đã chọn, phiến đá ra món bộ vùng.</li>
      </ul>
      <p class="mt-3 text-sm text-muted">
        Đồ khoá khi mặc như mọi trang bị; chưa mặc thì giao dịch được. Xem thêm
        <NuxtLink to="/cam-nang/trang-bi" class="lien-ket">Trang bị & nâng cấp</NuxtLink> và
        <NuxtLink to="/cam-nang/trang-phuc" class="lien-ket">Trang phục & trang trí</NuxtLink>.
      </p>
    </WikiSection>
  </WikiShell>
</template>
