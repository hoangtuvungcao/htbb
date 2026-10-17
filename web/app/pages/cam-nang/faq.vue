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
</script>

<template>
  <WikiShell slug="faq">
    <div v-for="n in nhom" :key="n.ten" class="[&+&]:mt-10">
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
    <p class="mt-10 text-sm text-muted">
      Chưa thấy câu trả lời? Xem <NuxtLink to="/download#support" class="lien-ket">kênh hỗ trợ</NuxtLink>
      hoặc <NuxtLink to="/dieu-khoan" class="lien-ket">Điều khoản</NuxtLink> · <NuxtLink to="/quyen-rieng-tu" class="lien-ket">Quyền riêng tư</NuxtLink>.
    </p>
  </WikiShell>
</template>
