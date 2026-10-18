<script setup lang="ts">
/**
 * Ma trận vai trò: vẽ thẳng từ shared/utils/quyenAdmin.ts — CHÍNH bảng mà server dùng để chặn
 * mọi /api/admin/** (tests/quyen-admin.test.ts bảo đảm mọi route đều có quy tắc). Không có số liệu
 * nào gõ tay ở đây nên trang không thể nói sai so với quyền thật.
 */
import type { QuyTacQuyen, VaiTroQuyen } from '#shared/utils/quyenAdmin'

definePageMeta({ layout: 'admin', middleware: 'admin' })

const me = useAdminMe()
const tim = ref('')
const boDau = (s: string) => s.normalize('NFD').replace(/[̀-ͯ]/g, '').replace(/đ/g, 'd').toLowerCase()

const nhom = computed(() => {
  const t = boDau(tim.value.trim())
  const m = new Map<string, QuyTacQuyen[]>()
  for (const r of QUY_TAC_QUYEN) {
    if (t && !boDau(`${r.nhom} ${r.viec} ${r.mau}`).includes(t)) continue
    m.set(r.nhom, [...(m.get(r.nhom) ?? []), r])
  }
  return [...m.entries()]
})

type Muc = 'ghi' | 'doc' | 'khong'
function muc(r: QuyTacQuyen, v: VaiTroQuyen): Muc {
  if (v === 'sieu_quan_tri' || r.ghi.includes(v)) return 'ghi'
  return r.doc.includes(v) ? 'doc' : 'khong'
}
const KIEU: Record<Muc, { icon: string, mau: string, nhan: string }> = {
  ghi: { icon: 'i-lucide-pencil', mau: 'text-success', nhan: 'Xem & thao tác' },
  doc: { icon: 'i-lucide-eye', mau: 'text-info', nhan: 'Chỉ xem' },
  khong: { icon: 'i-lucide-minus', mau: 'text-dimmed', nhan: 'Không được' },
}
</script>

<template>
  <AdminPage title="Ma trận vai trò">
    <template #right>
      <UButton v-if="me.duoc('/api/admin/phan-quyen', 'GET')" to="/admin/phan-quyen" icon="i-lucide-shield-check" label="Cấp vai trò" />
    </template>

    <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      <UCard v-for="v in DS_VAI_TRO_QUYEN" :key="v" :class="me.vaiTro.value === v ? 'ring-2 ring-primary' : ''">
        <div class="flex items-center justify-between gap-2">
          <b class="text-highlighted">{{ TEN_VAI_TRO_QUYEN[v] }}</b>
          <UBadge v-if="me.vaiTro.value === v" size="sm" color="primary" variant="subtle">Vai trò của bạn</UBadge>
        </div>
        <p class="mt-1 text-sm text-muted">{{ MO_TA_VAI_TRO_QUYEN[v] }}</p>
      </UCard>
    </div>

    <UAlert color="neutral" variant="subtle" icon="i-lucide-shield">
      <template #description>
        Quyền được kiểm ở <b>máy chủ</b> cho từng yêu cầu tới API quản trị — ẩn nút trên giao diện chỉ để gọn.
        Route mới chưa khai quy tắc thì <b>chỉ Siêu quản trị</b> gọi được (mặc định đóng), và bộ kiểm thử tự động báo đỏ.
        Mọi thao tác ghi đều vào Nhật ký quản trị kèm tên người làm và IP; sửa dữ liệu quan trọng lưu thêm bản trước/sau.
      </template>
    </UAlert>

    <div class="flex flex-wrap items-center gap-3">
      <UInput v-model="tim" icon="i-lucide-search" placeholder="Tìm chức năng…" aria-label="Tìm chức năng" class="w-full sm:w-72" />
      <span v-for="(k, ma) in KIEU" :key="ma" class="inline-flex items-center gap-1 text-sm text-muted">
        <UIcon :name="k.icon" class="size-4" :class="k.mau" /> {{ k.nhan }}
      </span>
    </div>

    <div class="overflow-x-auto rounded-lg border border-default">
      <table class="bang-wiki">
        <thead>
          <tr>
            <th scope="col">Chức năng</th>
            <th v-for="v in DS_VAI_TRO_QUYEN" :key="v" scope="col" class="text-center">{{ TEN_VAI_TRO_QUYEN[v] }}</th>
          </tr>
        </thead>
        <tbody>
          <template v-for="[ten, ds] in nhom" :key="ten">
            <tr>
              <th :colspan="DS_VAI_TRO_QUYEN.length + 1" scope="colgroup" class="!bg-elevated !text-highlighted">{{ ten }}</th>
            </tr>
            <tr v-for="r in ds" :key="r.mau">
              <td class="min-w-64">
                <span class="text-highlighted">{{ r.viec }}</span>
                <code class="mt-0.5 block text-xs text-dimmed">{{ r.mau }}</code>
              </td>
              <td v-for="v in DS_VAI_TRO_QUYEN" :key="v" class="text-center">
                <UIcon :name="KIEU[muc(r, v)].icon" class="size-4" :class="KIEU[muc(r, v)].mau" />
                <span class="sr-only">{{ KIEU[muc(r, v)].nhan }}</span>
              </td>
            </tr>
          </template>
          <tr v-if="!nhom.length">
            <td :colspan="DS_VAI_TRO_QUYEN.length + 1" class="py-8 text-center text-muted">Không có chức năng nào khớp.</td>
          </tr>
        </tbody>
      </table>
    </div>
  </AdminPage>
</template>
