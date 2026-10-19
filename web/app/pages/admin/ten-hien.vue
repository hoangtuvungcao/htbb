<script setup lang="ts">
/**
 * Tên NPC & tên bản đồ người chơi thấy (bảng data_npc / data_map_ten trong CSDL game — nguồn duy nhất).
 * Ghi có nhật ký quản trị; game server tự nạp lại trong ≤ 30 giây (không cần khởi động lại).
 */
definePageMeta({ layout: 'admin', middleware: 'admin' })
const api = useAdminApi()
const toast = useToast()

interface DongNpc { type: number, ten: string, chuc_vu: string, pokemon: string, dex: number, ban_do: string, sua_boi: string, sua_luc: string | null }
interface DongMap { map_id: number, ten: string, vung: string, loai: string, mo_ta: string, sua_boi: string, sua_luc: string | null }
interface KetQua { coBang: boolean, npc: DongNpc[], map: DongMap[] }

const { data, status, error, refresh } = useAsyncData('admin-ten-hien', () => api<KetQua>('/api/admin/ten-hien'), { server: false })

const tab = ref<'npc' | 'map'>('npc')
const tim = ref('')
const nhap = reactive<Record<string, string>>({})
const nhapChucVu = reactive<Record<string, string>>({})
const dangLuu = reactive<Record<string, boolean>>({})
const idMoi = ref('')
const tenMoi = ref('')

const khop = (s: string) => s.toLocaleLowerCase('vi').includes(tim.value.trim().toLocaleLowerCase('vi'))
const dsNpc = computed(() => (data.value?.npc ?? []).filter(r => !tim.value || khop(`${r.type} ${r.ten} ${r.chuc_vu} ${r.pokemon}`)))
const dsMap = computed(() => (data.value?.map ?? []).filter(r => !tim.value || khop(`${r.map_id} ${r.ten} ${r.vung} ${r.mo_ta}`)))

function loiTen(ten: string): string | null {
  return kiemTenHien(ten)
}

async function luu(loai: 'npc' | 'map', id: number, ten: string, chucVu?: string) {
  const khoa = `${loai}:${id}`
  const l = loiTen(ten)
  if (l) {
    toast.add({ title: l, color: 'error', icon: 'i-lucide-circle-alert' })
    return
  }
  dangLuu[khoa] = true
  try {
    const kq = await api<{ message: string }>('/api/admin/ten-hien/sua', { method: 'POST', body: { loai, id, ten, chucVu } })
    toast.add({ title: kq.message, color: 'success', icon: 'i-lucide-check' })
    delete nhap[khoa]
    delete nhapChucVu[khoa]
    await refresh()
  } catch (e) {
    toast.add({ title: loiApi(e), color: 'error', icon: 'i-lucide-circle-alert' })
  } finally {
    dangLuu[khoa] = false
  }
}

async function themMap() {
  const id = Number(idMoi.value)
  if (!Number.isInteger(id) || id < 0) {
    toast.add({ title: 'Id bản đồ không hợp lệ.', color: 'error', icon: 'i-lucide-circle-alert' })
    return
  }
  await luu('map', id, tenMoi.value)
  idMoi.value = ''
  tenMoi.value = ''
}
</script>

<template>
  <AdminPage title="Tên NPC & bản đồ">
    <template #right>
      <UButton icon="i-lucide-refresh-cw" color="neutral" variant="ghost" :loading="status === 'pending'" aria-label="Tải lại" @click="refresh()" />
    </template>

    <div>
      <h1 class="text-2xl font-bold text-highlighted">Tên NPC & bản đồ</h1>
      <p class="mt-1 text-sm text-muted">
        Tên người chơi thấy, lưu trong CSDL game (bảng <code>data_npc</code>, <code>data_map_ten</code>). Tên phải có dấu tiếng Việt
        (trừ danh từ riêng Pokémon như "Pewter"), không dùng tên cũ của bản game gốc. Mỗi lần lưu có ghi
        <NuxtLink to="/admin/nhatky" class="text-primary hover:underline">nhật ký</NuxtLink>.
      </p>
      <p class="mt-1 text-sm text-muted">
        <strong>Hiệu lực:</strong> máy chủ game tự nạp lại trong khoảng 30 giây, không cần khởi động lại. Tên bản đồ hiện ở lần
        vào bản đồ kế tiếp (và ở /admin/online). Bản sao theo khu (ví dụ 2424 của Bến tàu) tự lấy tên "Bến tàu phía bắc"; thêm
        dòng riêng nếu muốn tên khác. Tên NPC tĩnh còn một bản sao trong client: đổi ở đây cần phát hành lại client mới thấy trong game.
      </p>
    </div>

    <UAlert v-if="error && !data" color="error" variant="subtle" icon="i-lucide-circle-x" title="Không tải được" :description="loiApi(error)"
      :actions="[{ label: 'Thử lại', color: 'error', variant: 'outline', onClick: () => refresh() }]" />
    <UAlert v-else-if="data && !data.coBang" color="warning" variant="subtle" icon="i-lucide-triangle-alert"
      title="CSDL game chưa có bảng tên" description="Chạy sql/pokemon_ten_hien.sql trên CSDL game (chạy lại được, giữ tên đã sửa)." />
    <div v-else-if="!data && status === 'pending'" class="space-y-2">
      <USkeleton v-for="i in 6" :key="i" class="h-9 w-full" />
    </div>
    <template v-else-if="data">
      <div class="flex flex-wrap items-center gap-2">
        <UButton :color="tab === 'npc' ? 'primary' : 'neutral'" :variant="tab === 'npc' ? 'solid' : 'outline'" size="sm" :label="`NPC (${data.npc.length})`" @click="tab = 'npc'" />
        <UButton :color="tab === 'map' ? 'primary' : 'neutral'" :variant="tab === 'map' ? 'solid' : 'outline'" size="sm" :label="`Bản đồ (${data.map.length})`" @click="tab = 'map'" />
        <UInput v-model="tim" icon="i-lucide-search" placeholder="Tìm theo id, tên…" size="sm" class="w-full sm:w-64" />
      </div>

      <UCard v-if="tab === 'npc'">
        <div class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead class="text-left text-muted">
              <tr><th class="p-2">Type</th><th class="p-2">Tên</th><th class="p-2">Chức vụ</th><th class="p-2">Pokémon</th><th class="p-2">Bản đồ (cMap)</th><th class="p-2">Sửa lần cuối</th><th class="p-2" /></tr>
            </thead>
            <tbody>
              <tr v-for="r in dsNpc" :key="r.type" class="border-t border-default align-top">
                <td class="p-2 tabular-nums">{{ r.type }}</td>
                <td class="p-2"><UInput :model-value="nhap[`npc:${r.type}`] ?? r.ten" size="sm" class="min-w-48" :aria-label="`Tên NPC ${r.type}`" @update:model-value="v => nhap[`npc:${r.type}`] = String(v)" /></td>
                <td class="p-2"><UInput :model-value="nhapChucVu[`npc:${r.type}`] ?? r.chuc_vu" size="sm" class="min-w-40" :aria-label="`Chức vụ NPC ${r.type}`" @update:model-value="v => nhapChucVu[`npc:${r.type}`] = String(v)" /></td>
                <td class="p-2">{{ r.pokemon }}</td>
                <td class="p-2 text-xs text-muted">{{ r.ban_do || '—' }}</td>
                <td class="p-2 text-xs text-muted">{{ r.sua_boi ? `${r.sua_boi} · ${r.sua_luc}` : '—' }}</td>
                <td class="p-2">
                  <UButton size="sm" label="Lưu" :loading="dangLuu[`npc:${r.type}`]"
                    :disabled="(nhap[`npc:${r.type}`] ?? r.ten) === r.ten && (nhapChucVu[`npc:${r.type}`] ?? r.chuc_vu) === r.chuc_vu"
                    @click="luu('npc', r.type, nhap[`npc:${r.type}`] ?? r.ten, nhapChucVu[`npc:${r.type}`] ?? r.chuc_vu)" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </UCard>

      <UCard v-else>
        <div class="mb-3 flex flex-wrap items-end gap-2">
          <UInput v-model="idMoi" size="sm" placeholder="Id bản đồ" class="w-28" aria-label="Id bản đồ mới" />
          <UInput v-model="tenMoi" size="sm" placeholder="Tên riêng (vd cho bản sao theo khu)" class="w-full sm:w-72" aria-label="Tên bản đồ mới" />
          <UButton size="sm" icon="i-lucide-plus" label="Thêm / đặt tên" :disabled="!idMoi || !tenMoi" @click="themMap()" />
        </div>
        <div class="overflow-x-auto">
          <table class="w-full text-sm">
            <thead class="text-left text-muted">
              <tr><th class="p-2">Id</th><th class="p-2">Tên</th><th class="p-2">Vùng</th><th class="p-2">Loại</th><th class="p-2">Sửa lần cuối</th><th class="p-2" /></tr>
            </thead>
            <tbody>
              <tr v-for="r in dsMap" :key="r.map_id" class="border-t border-default align-top">
                <td class="p-2 tabular-nums">{{ r.map_id }}</td>
                <td class="p-2"><UInput :model-value="nhap[`map:${r.map_id}`] ?? r.ten" size="sm" class="min-w-48" :aria-label="`Tên bản đồ ${r.map_id}`" @update:model-value="v => nhap[`map:${r.map_id}`] = String(v)" /></td>
                <td class="p-2">{{ r.vung || '—' }}</td>
                <td class="p-2 text-xs text-muted">{{ r.mo_ta || r.loai || '—' }}</td>
                <td class="p-2 text-xs text-muted">{{ r.sua_boi ? `${r.sua_boi} · ${r.sua_luc}` : '—' }}</td>
                <td class="p-2">
                  <UButton size="sm" label="Lưu" :loading="dangLuu[`map:${r.map_id}`]" :disabled="(nhap[`map:${r.map_id}`] ?? r.ten) === r.ten"
                    @click="luu('map', r.map_id, nhap[`map:${r.map_id}`] ?? r.ten)" />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </UCard>
    </template>
  </AdminPage>
</template>
