<script setup lang="ts">
/** Trang chủ: hero, nút, thẻ tính năng, thứ tự 5 khởi đầu, banner trượt, cảnh phim. */
import AdminCmsChonAnh from '~/components/admin/cms/ChonAnh.vue'
import AdminTaikhoanXacNhan from '~/components/admin/taikhoan/XacNhan.vue'

definePageMeta({ layout: 'admin', middleware: 'admin' })
const me = useAdminMe()
const { f: fRef, luu, khoiPhuc, dangLuu, tuDb, coBang, daThayDoi } = await useCaiDatAdmin('trangChu')
const f = fRef as Ref<CmsTrangChu>
const chonAnh = useOverlay().create(AdminCmsChonAnh)
const xacNhan = useOverlay().create(AdminTaikhoanXacNhan)
const TEN_KD: Record<number, string> = { 0: 'Charmander', 1: 'Bulbasaur', 2: 'Eevee', 3: 'Squirtle', 4: 'Pichu' }
function doi<T>(ds: T[], i: number, d: number) {
  const j = i + d
  if (j < 0 || j >= ds.length) return
  const t = ds[i]!
  ds[i] = ds[j]!
  ds[j] = t
}
async function anhBanner(i: number) {
  const u = await chonAnh.open().result
  if (u) f.value.banner[i]!.anh = u
}
async function hoiKhoiPhuc() {
  if (await xacNhan.open({ title: 'Khôi phục trang chủ mặc định?', nut: 'Khôi phục', mau: 'warning' }).result) khoiPhuc()
}
</script>

<template>
  <AdminPage title="Trang chủ">
    <template #right>
      <UBadge v-if="daThayDoi" color="warning" variant="subtle">Chưa lưu</UBadge>
      <UButton to="/" target="_blank" color="neutral" variant="ghost" icon="i-lucide-external-link" label="Xem trang chủ" />
      <UButton v-if="tuDb" color="neutral" variant="outline" icon="i-lucide-rotate-ccw" label="Mặc định" :disabled="!me.duocGhiNoiDung.value" @click="hoiKhoiPhuc" />
      <UButton icon="i-lucide-save" label="Lưu" :loading="dangLuu" :disabled="!me.duocGhiNoiDung.value" @click="luu" />
    </template>
    <AdminCmsChuaCoBang v-if="!coBang" bang="web_cai_dat" />

    <UCard>
      <template #header><b>Hero (đầu trang)</b></template>
      <div class="grid gap-4 md:grid-cols-2">
        <UFormField label="Nhãn nhỏ"><UInput v-model="f.hero.nhan" class="w-full" /></UFormField>
        <span />
        <UFormField label="Dòng tiêu đề 1"><UInput v-model="f.hero.dong1" class="w-full" /></UFormField>
        <UFormField label="Dòng tiêu đề 2 (chữ vàng)"><UInput v-model="f.hero.dong2" class="w-full" /></UFormField>
        <UFormField label="Mô tả (hộp thoại)" class="md:col-span-2"><UTextarea v-model="f.hero.moTa" :rows="3" class="w-full" /></UFormField>
      </div>
      <p class="mt-4 text-sm font-medium">Nút</p>
      <div v-for="(n, i) in f.hero.nut" :key="i" class="mt-2 grid gap-2 sm:grid-cols-[1fr_1fr_9rem_auto]">
        <UInput v-model="n.label" placeholder="Chữ trên nút" />
        <UInput v-model="n.to" placeholder="/download hoặc https://..." />
        <USelect v-model="n.kieu" :items="[{ label: 'Nút đỏ (chính)', value: 'chinh' }, { label: 'Nút vàng', value: 'phu' }]" />
        <UButton color="error" variant="ghost" icon="i-lucide-trash-2" aria-label="Xoá nút" @click="f.hero.nut.splice(i, 1)" />
      </div>
      <UButton v-if="f.hero.nut.length < 3" class="mt-2" size="sm" color="neutral" variant="outline" icon="i-lucide-plus" label="Thêm nút" @click="f.hero.nut.push({ label: 'Nút mới', to: '/', kieu: 'phu' })" />
    </UCard>

    <UCard>
      <template #header>
        <div class="flex items-center justify-between">
          <b>Banner trượt (trên mục tính năng)</b>
          <UButton size="sm" icon="i-lucide-plus" label="Thêm banner" @click="f.banner.push({ anh: '', tieuDe: 'Banner mới', moTa: '', to: '/news', hien: true })" />
        </div>
      </template>
      <p v-if="!f.banner.length" class="text-sm text-muted">Chưa có banner — khối này ẩn trên trang chủ.</p>
      <div v-for="(b, i) in f.banner" :key="i" class="grid gap-3 border-b border-default py-3 last:border-0 md:grid-cols-[10rem_1fr_auto]">
        <button type="button" class="grid aspect-[21/9] place-items-center overflow-hidden rounded-lg border border-dashed border-default bg-elevated text-xs text-muted" @click="anhBanner(i)">
          <img v-if="b.anh" :src="b.anh" alt="" class="size-full object-cover">
          <span v-else>Chọn ảnh</span>
        </button>
        <div class="grid gap-2 sm:grid-cols-2">
          <UInput v-model="b.tieuDe" placeholder="Tiêu đề" />
          <UInput v-model="b.to" placeholder="Liên kết" />
          <UInput v-model="b.moTa" placeholder="Mô tả ngắn" class="sm:col-span-2" />
        </div>
        <div class="flex items-start gap-1">
          <USwitch v-model="b.hien" aria-label="Hiện" />
          <UButton size="xs" color="neutral" variant="ghost" icon="i-lucide-arrow-up" aria-label="Lên" @click="doi(f.banner, i, -1)" />
          <UButton size="xs" color="neutral" variant="ghost" icon="i-lucide-arrow-down" aria-label="Xuống" @click="doi(f.banner, i, 1)" />
          <UButton size="xs" color="error" variant="ghost" icon="i-lucide-trash-2" aria-label="Xoá" @click="f.banner.splice(i, 1)" />
        </div>
      </div>
      <USwitch v-model="f.hienCanhPhim" label="Hiện cảnh phim Pokémon chạy qua đồng cỏ" class="mt-3" />
    </UCard>

    <UCard>
      <template #header>
        <div class="flex items-center justify-between">
          <b>Thẻ tính năng</b>
          <UButton size="sm" icon="i-lucide-plus" label="Thêm thẻ" @click="f.tinhNang.push({ icon: 'i-lucide-star', t: 'Tính năng mới', m: '', to: '/' })" />
        </div>
      </template>
      <UFormField label="Tiêu đề mục"><UInput v-model="f.tinhNangTieuDe" class="w-full" /></UFormField>
      <div v-for="(t, i) in f.tinhNang" :key="i" class="mt-3 grid gap-2 rounded-lg border border-default p-2 md:grid-cols-[12rem_1fr_1fr_auto]">
        <UInput v-model="t.icon" placeholder="i-lucide-..." :icon="t.icon" />
        <UInput v-model="t.t" placeholder="Tiêu đề" />
        <UInput v-model="t.to" placeholder="Liên kết" />
        <div class="flex gap-1">
          <UButton size="xs" color="neutral" variant="ghost" icon="i-lucide-arrow-up" aria-label="Lên" @click="doi(f.tinhNang, i, -1)" />
          <UButton size="xs" color="neutral" variant="ghost" icon="i-lucide-arrow-down" aria-label="Xuống" @click="doi(f.tinhNang, i, 1)" />
          <UButton size="xs" color="error" variant="ghost" icon="i-lucide-trash-2" aria-label="Xoá" @click="f.tinhNang.splice(i, 1)" />
        </div>
        <UTextarea v-model="t.m" :rows="1" placeholder="Mô tả" class="md:col-span-4" />
      </div>
    </UCard>

    <UCard>
      <template #header><b>Thứ tự 5 Pokémon khởi đầu</b></template>
      <ol class="flex flex-wrap gap-2">
        <li v-for="(c, i) in f.khoiDauThuTu" :key="c" class="flex items-center gap-1 rounded-full border border-default py-1 pl-1 pr-2">
          <PokeAvatar :dex="DEX_KHOI_DAU[c as 0]" class="size-7" />
          <span class="text-sm font-semibold">{{ TEN_KD[c] }}</span>
          <UButton size="xs" color="neutral" variant="ghost" icon="i-lucide-chevron-left" aria-label="Lên trước" @click="doi(f.khoiDauThuTu, i, -1)" />
          <UButton size="xs" color="neutral" variant="ghost" icon="i-lucide-chevron-right" aria-label="Ra sau" @click="doi(f.khoiDauThuTu, i, 1)" />
        </li>
      </ol>
    </UCard>
  </AdminPage>
</template>
