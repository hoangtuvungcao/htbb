<script setup lang="ts">
/** FAQ: câu hỏi trong CSDL (sửa ở /admin/faq), chưa có thì dùng bộ mặc định (shared/utils/faqMacDinh.ts). */
const { data: db } = await useFetch<{ id: number, nhom: string, cauHoi: string, html: string }[] | null>('/api/faq', { key: 'htbb-faq', default: () => null })
interface CauHoi { label: string, html?: string, content?: string }
const nhom = computed<{ ten: string, hoi: CauHoi[] }[]>(() => {
  if (db.value?.length) {
    const ra: { ten: string, hoi: CauHoi[] }[] = []
    for (const r of db.value) {
      let g = ra.find(x => x.ten === r.nhom)
      if (!g) { g = { ten: r.nhom, hoi: [] }; ra.push(g) }
      g.hoi.push({ label: r.cauHoi, html: r.html })
    }
    return ra
  }
  return FAQ_MAC_DINH
})
// Danh sách nền tảng theo /admin/tai-game (thẻ {nen_tang} / {nen_tang_sap} trong câu trả lời).
const { tenDangCo, tenSapRaMat } = useBanTai()
function thay(s: string, laHtml: boolean): string {
  const esc = (x: string) => (laHtml ? x.replace(/&/g, '&amp;').replace(/</g, '&lt;') : x)
  return s.replaceAll('{nen_tang}', esc(tenDangCo.value))
    .replaceAll('{nen_tang_sap}', tenSapRaMat.value ? esc(` ${tenSapRaMat.value} sắp ra mắt.`) : '')
}
const nhomHien = computed(() => nhom.value.map(g => ({ ...g, hoi: g.hoi.map(h => ({ ...h, content: h.content ? thay(h.content, false) : h.content, html: h.html ? thay(h.html, true) : h.html })) })))
const { coZalo } = useHoTro()
</script>

<template>
  <WikiShell slug="faq">
    <div v-for="n in nhomHien" :key="n.ten" class="[&+&]:mt-10">
      <h2 class="section-title text-2xl text-highlighted">
        {{ n.ten }}
      </h2>
      <UAccordion :items="n.hoi" type="multiple" class="mt-3 rounded-2xl glass px-4" :ui="{ trigger: 'font-display text-base font-bold', body: 'text-muted' }">
        <template #body="{ item }">
          <div v-if="item.html" class="noi-dung-md" v-html="item.html" />
          <p v-else>
            {{ item.content }}
          </p>
        </template>
      </UAccordion>
    </div>
    <section v-if="coZalo" id="lien-he-ho-tro" class="mt-10 scroll-mt-24" aria-labelledby="lien-he-ho-tro-title">
      <h2 id="lien-he-ho-tro-title" class="section-title text-2xl text-highlighted">
        Liên hệ hỗ trợ
      </h2>
      <p class="mt-2 text-sm text-muted">
        Chưa thấy câu trả lời, gặp lỗi hoặc tài khoản bị khoá? Nhắn ban quản trị qua Zalo — ghi tên tài khoản, không gửi mật khẩu.
      </p>
      <SiteHoTroZalo kieu="the" class="mt-3" />
    </section>
    <p class="mt-10 text-sm text-muted">
      Chưa thấy câu trả lời? Xem <NuxtLink to="/download#support" class="lien-ket">kênh hỗ trợ</NuxtLink>
      hoặc <NuxtLink to="/dieu-khoan" class="lien-ket">Điều khoản</NuxtLink> · <NuxtLink to="/quyen-rieng-tu" class="lien-ket">Quyền riêng tư</NuxtLink>.
    </p>
  </WikiShell>
</template>
