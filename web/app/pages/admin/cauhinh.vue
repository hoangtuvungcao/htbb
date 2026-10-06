<script setup lang="ts">
/**
 * Cấu hình server.
 * Ghi bảng kpah_db.cau_hinh; server game tự đọc lại mỗi 10 giây, không cần restart.
 */
definePageMeta({ layout: 'admin', middleware: 'admin' })

const api = useAdminApi()
const toast = useToast()

interface SuaLanCuoi { boi: string, luc: string }
interface DuLieu {
  loiDoc: string
  form: {
    drop: Record<string, string>
    expQuai: Record<string, string>
    tieu: {
      tieuMau: number[]
      doiTieuXuMau: number[]
      doiTieuLuongMau: number[]
      tieuHeSoChung: number
      tieuHeSoMau: number[]
      caoCap: Record<string, number>
    }
    chiemThanh: { gioChiemThanh: string, phutChiemThanh: string, cacThu: string[], ngayCuThe: string }
    lichHen: { tuKhoiDongLai: boolean, gomGiay: string, baoTruocGiay: string }
    expPhu: { expVantieu: boolean, luckyExp: boolean, expQua: boolean }
    khac: {
      maxCharPerAccount: string, adminAccounts: string, website: string, linkTaiGame: string,
      testCode: string, choPhepTaoTenGM: boolean, testMode: boolean
    }
  }
  suaLanCuoi: Record<string, SuaLanCuoi | null>
  cacThu: Record<string, string>
}

const { data, status, error, refresh } = await useAsyncData('admin-cauhinh', () => api<DuLieu>('/api/admin/cauhinh'), { server: false })

// Bản sao để sửa — nạp lại mỗi khi dữ liệu từ server về.
const f = ref<DuLieu['form'] | null>(null)
watch(data, (d) => {
  f.value = d ? structuredClone(toRaw(d.form)) : null
}, { immediate: true })

const TEN_MAU = ['Trắng', 'Xanh', 'Đỏ', 'Hoàn Mỹ', 'Vàng']
const TEN_MAU_DAY_DU = ['Trắng', 'Xanh', 'Đỏ', 'Hoàn Mỹ', 'Vàng', 'Tím']
const TEN_MAU_CAO_CAP = ['Hoàn Mỹ', 'Vàng', 'Tím']
const TEN_KHUNG_CAP = ['Cấp 50-59', 'Cấp 60-69', 'Cấp 70+']

// Chỉ tính Tím/tổng hiển thị cho dễ nhìn — server luôn tự tính lại và chặn tổng > 100%
// khi lưu, phần này không phải chốt an toàn.
function tinhTim(ds: (number | string)[] | undefined) {
  let tong = (ds ?? []).reduce<number>((t, v) => t + (Number.parseFloat(String(v)) || 0), 0)
  tong = Math.round(tong * 1000) / 1000
  const conLai = Math.round((100 - tong) * 1000) / 1000
  return { tong, tim: conLai < 0 ? 0 : conLai, vuot: conLai < 0 }
}
const timMoi = computed(() => tinhTim(f.value?.tieu.tieuMau))
const timDoiXu = computed(() => tinhTim(f.value?.tieu.doiTieuXuMau))
const timDoiLuong = computed(() => tinhTim(f.value?.tieu.doiTieuLuongMau))

const dangLuu = ref('')

async function luu(viec: string, body: Record<string, unknown>) {
  dangLuu.value = viec
  try {
    const r = await api<{ message: string }>('/api/admin/cauhinh/luu', { method: 'POST', body: { viec, ...body } })
    toast.add({ title: r.message, color: 'success', icon: 'i-lucide-check' })
    await refresh()
  } catch (e) {
    toast.add({ title: 'Chưa lưu', description: loiApi(e), color: 'error', icon: 'i-lucide-circle-x' })
  } finally {
    dangLuu.value = ''
  }
}

function luuTieu() {
  const t = f.value!.tieu
  const body: Record<string, unknown> = { tieuHeSoChung: t.tieuHeSoChung }
  t.tieuMau.forEach((v, i) => { body[`tieuMau${i}`] = v })
  t.doiTieuXuMau.forEach((v, i) => { body[`doiTieuXuMau${i}`] = v })
  t.doiTieuLuongMau.forEach((v, i) => { body[`doiTieuLuongMau${i}`] = v })
  t.tieuHeSoMau.forEach((v, i) => { body[`tieuHeSoMau${i}`] = v })
  Object.assign(body, t.caoCap)
  return luu('luu_tieu', body)
}

function doiThu(ma: string, bat: boolean | 'indeterminate') {
  const ds = f.value!.chiemThanh.cacThu.filter(x => x !== ma)
  if (bat === true) ds.push(ma)
  f.value!.chiemThanh.cacThu = ds
}
</script>

<template>
  <AdminPage title="Cấu hình server">
    <template #right>
      <UButton icon="i-lucide-refresh-cw" color="neutral" variant="ghost" :loading="status === 'pending'" @click="refresh()">
        Tải lại
      </UButton>
    </template>

    <div>
      <h1 class="text-2xl font-bold text-highlighted">
        Cấu hình server
      </h1>
      <p class="mt-1 text-muted">
        32 setting nạp lại được lúc đang chạy — sửa xong, server tự áp dụng trong tối đa 10 giây, không cần restart.
      </p>
    </div>

    <UAlert v-if="data?.loiDoc" color="error" variant="subtle" icon="i-lucide-circle-x" :title="`Lỗi đọc dữ liệu: ${data.loiDoc}`" />

    <UAlert color="warning" variant="subtle" icon="i-lucide-info">
      <template #description>
        Đây <strong>không phải toàn bộ</strong> <code>server.ini</code> — chỉ 32 khoá đã có sẵn cơ chế nạp lại
        (xem <code>src/server/CauHinh.java</code>). Các khoá kết nối CSDL, cổng mạng, đường dẫn máy... vẫn phải
        sửa trực tiếp <code>server.ini</code> trên VPS rồi khởi động lại — không nằm ở đây vì bản thân chúng là
        thứ server cần để kết nối được tới CSDL này.
      </template>
    </UAlert>

    <UAlert
      v-if="error && !f"
      color="error"
      variant="subtle"
      icon="i-lucide-circle-x"
      title="Không tải được cấu hình"
      :description="loiApi(error)"
    />

    <div v-else-if="!f" class="grid gap-4 md:grid-cols-2">
      <USkeleton v-for="i in 4" :key="i" class="h-48 rounded-xl" />
    </div>

    <template v-else>
      <!-- ============ Tỷ lệ rơi đồ ============ -->
      <UCard>
        <template #header>
          <h3 class="font-semibold text-highlighted">
            Tỷ lệ rơi đồ khi train quái
          </h3>
          <p class="text-sm text-muted">
            Phần trăm so với tỷ lệ gốc. 100 = như cũ, 0 = tắt hẳn.
          </p>
        </template>
        <form class="space-y-4" @submit.prevent="luu('luu_drop', f.drop)">
          <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <UFormField label="Xu (%)">
              <UInput v-model="f.drop.tileXu" type="number" min="0" max="100" class="w-full" />
            </UFormField>
            <UFormField label="Lượng (%)">
              <UInput v-model="f.drop.tileLuong" type="number" min="0" max="100" class="w-full" />
            </UFormField>
            <UFormField label="Đồ sự kiện (%)">
              <UInput v-model="f.drop.tileItemSuKien" type="number" min="0" max="100" class="w-full" />
            </UFormField>
            <UFormField label="Bình thuốc (%)">
              <UInput v-model="f.drop.tilePotion" type="number" min="0" max="100" class="w-full" />
            </UFormField>
            <UFormField label="Ngọc (%)">
              <UInput v-model="f.drop.tileNgoc" type="number" min="0" max="100" class="w-full" />
            </UFormField>
            <UFormField label="Đồ thường (%)">
              <UInput v-model="f.drop.tileItem" type="number" min="0" max="100" class="w-full" />
            </UFormField>
            <UFormField label="Chia số xu rơi ra" help="Số xu rơi ra = xu gốc chia cho số này. Càng lớn càng ít.">
              <UInput v-model="f.drop.chiaXuRoi" type="number" min="1" class="w-full" />
            </UFormField>
          </div>
          <div class="flex flex-wrap items-center justify-between gap-3">
            <AdminCauHinhSuaBoi :moc="data?.suaLanCuoi.drop" />
            <UButton type="submit" icon="i-lucide-save" :loading="dangLuu === 'luu_drop'">
              Lưu tỷ lệ rơi đồ
            </UButton>
          </div>
        </form>
      </UCard>

      <!-- ============ EXP quái ============ -->
      <UCard>
        <template #header>
          <h3 class="font-semibold text-highlighted">
            Tỉ lệ EXP quái thường
          </h3>
          <p class="text-sm text-muted">
            Phần trăm so với EXP gốc, tính theo <strong>cấp NHÂN VẬT</strong> đang nhận EXP
            (không phải cấp quái) — dù nhân vật ≥50 đi train ở quái cấp nào cũng bị giảm. Áp dụng cho cả Boss.
            100 = như gốc, 0 = tắt hẳn. Xem <code>Monster.apDungGiamExpTheoCapNV()</code>.
          </p>
        </template>
        <form class="space-y-4" @submit.prevent="luu('luu_exp_quai', f.expQuai)">
          <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <UFormField label="Nhân vật cấp ≥ 50 (%)">
              <UInput v-model="f.expQuai.tileExpLv50" type="number" min="0" max="100" class="w-full" />
            </UFormField>
            <UFormField label="Nhân vật cấp < 50 (%)">
              <UInput v-model="f.expQuai.tileExpDuoiLv50" type="number" min="0" max="100" class="w-full" />
            </UFormField>
          </div>
          <div class="flex flex-wrap items-center justify-between gap-3">
            <AdminCauHinhSuaBoi :moc="data?.suaLanCuoi.expQuai" />
            <UButton type="submit" icon="i-lucide-save" :loading="dangLuu === 'luu_exp_quai'">
              Lưu tỉ lệ EXP quái
            </UButton>
          </div>
        </form>
      </UCard>

      <!-- ============ Vận Tiêu ============ -->
      <UCard>
        <template #header>
          <h3 class="font-semibold text-highlighted">
            Tỉ lệ Vận Tiêu
          </h3>
          <p class="text-sm text-muted">
            Khi nhận một con tiêu (Vận Tiêu) mới, server random ra 1 trong 6 màu theo % ở
            phần 1 — Trắng phổ biến nhất, Tím hiếm nhất. Khi TRẢ tiêu thành công, người chơi nhận EXP theo
            phần 2 (và phần 3 nếu là Hoàn Mỹ/Vàng/Tím từ cấp 50). Phần 4 là tỉ lệ RIÊNG khi ĐỔI tiêu
            (reroll trả phí xu/lượng) — độc lập với phần 1. Xem <code>Char.doInitMonter()</code>,
            <code>Char.doChangeTieu()</code>, <code>CharSuKien.doResponseTieu()</code>.
          </p>
        </template>
        <form class="space-y-8" @submit.prevent="luuTieu">
          <section class="space-y-3">
            <h4 class="font-semibold text-highlighted">
              1. Tỉ lệ ra màu khi nhận tiêu mới
            </h4>
            <p class="text-sm text-muted">
              Nhập % cho 5 màu đầu (được nhập số lẻ, vd <code>0.001</code> để ra tỉ lệ cực
              hiếm) — <strong>Tím không nhập, tự động = phần trăm còn lại</strong> sau khi trừ 5 màu kia.
            </p>
            <div class="grid gap-4 sm:grid-cols-3 lg:grid-cols-6">
              <UFormField v-for="(ten, i) in TEN_MAU" :key="ten" :label="`${ten} (%)`">
                <UInput v-model="f.tieu.tieuMau[i]" type="number" min="0" max="100" step="0.001" class="w-full" />
              </UFormField>
              <UFormField label="Tím (%) — tự tính">
                <UInput :model-value="String(timMoi.tim)" readonly variant="soft" class="w-full" />
              </UFormField>
            </div>
            <p class="text-sm text-muted">
              Tổng Trắng→Vàng: <strong class="num text-highlighted">{{ timMoi.tong }}</strong>%
              — còn lại cho Tím: <strong class="num text-highlighted">{{ timMoi.tim }}</strong>%.
              <span v-if="timMoi.vuot" class="text-error">Tổng 5 màu đã vượt 100% — Tím sẽ về 0, lưu lại sẽ báo lỗi.</span>
            </p>
          </section>

          <section class="space-y-3">
            <h4 class="font-semibold text-highlighted">
              2. Hệ số EXP khi trả tiêu thành công
            </h4>
            <p class="text-sm text-muted">
              Hệ số dạng "x lần" — 1 = giữ nguyên gốc, 2 = gấp đôi. Hai hệ số NHÂN VỚI
              NHAU: EXP cuối = EXP gốc × hệ số chung × hệ số theo màu. Chỉ áp dụng khi cấp mở tiêu dưới 50,
              hoặc màu Trắng/Xanh/Đỏ (Hoàn Mỹ/Vàng/Tím từ cấp 50 dùng công thức riêng ở phần 3).
            </p>
            <UFormField label="Hệ số chung (x...)" class="max-w-52">
              <UInput v-model="f.tieu.tieuHeSoChung" type="number" min="0" max="10" step="0.01" class="w-full" />
            </UFormField>
            <div class="grid gap-4 sm:grid-cols-3 lg:grid-cols-6">
              <UFormField v-for="(ten, i) in TEN_MAU_DAY_DU" :key="ten" :label="`Hệ số ${ten} (x...)`">
                <UInput v-model="f.tieu.tieuHeSoMau[i]" type="number" min="0" max="100" step="0.01" class="w-full" />
              </UFormField>
            </div>
          </section>

          <section class="space-y-3">
            <h4 class="font-semibold text-highlighted">
              3. % EXP cần để lên cấp (riêng Hoàn Mỹ/Vàng/Tím, từ cấp 50)
            </h4>
            <p class="text-sm text-muted">
              Vd 9.0 = trả tiêu xong được cộng thêm 9.0% lượng EXP cần để lên cấp kế
              tiếp. Chỉ 3 màu hiếm mới dùng bảng này; Trắng/Xanh/Đỏ luôn dùng công thức ở phần 2.
            </p>
            <div class="max-w-2xl overflow-x-auto rounded-lg border border-default">
              <table class="w-full text-sm">
                <thead class="bg-elevated/50 text-left text-muted">
                  <tr>
                    <th class="px-3 py-2" />
                    <th v-for="ten in TEN_MAU_CAO_CAP" :key="ten" class="px-3 py-2 font-medium">
                      {{ ten }} (%)
                    </th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(tenKhung, khung) in TEN_KHUNG_CAP" :key="tenKhung" class="border-t border-default">
                    <td class="px-3 py-2 whitespace-nowrap text-toned">
                      {{ tenKhung }}
                    </td>
                    <td v-for="(tenMau, mau) in TEN_MAU_CAO_CAP" :key="tenMau" class="px-3 py-2">
                      <UInput v-model="f.tieu.caoCap[`tieuPhanTramCaoCap${khung}_${mau}`]" type="number" min="0" max="100" step="0.1" class="w-24" />
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>

          <section class="space-y-3">
            <h4 class="font-semibold text-highlighted">
              4. Tỉ lệ ra màu khi ĐỔI tiêu (reroll trả phí)
            </h4>
            <p class="text-sm text-muted">
              Bảng RIÊNG, độc lập với phần 1 — dùng khi người chơi trả phí để đổi màu
              con tiêu đang cầm. Có 2 giá riêng theo cách trả phí (xu rẻ hơn, lượng đắt hơn nhưng tỉ lệ
              màu hiếm cao hơn). Cùng quy tắc nhập: 5 màu đầu nhập %, Tím tự tính phần còn lại.
            </p>

            <p class="text-sm font-semibold text-toned">
              Đổi bằng Xu:
            </p>
            <div class="grid gap-4 sm:grid-cols-3 lg:grid-cols-6">
              <UFormField v-for="(ten, i) in TEN_MAU" :key="ten" :label="`${ten} (%)`">
                <UInput v-model="f.tieu.doiTieuXuMau[i]" type="number" min="0" max="100" step="0.001" class="w-full" />
              </UFormField>
              <UFormField label="Tím (%) — tự tính">
                <UInput :model-value="String(timDoiXu.tim)" readonly variant="soft" class="w-full" />
              </UFormField>
            </div>
            <p class="text-sm text-muted">
              Tổng Trắng→Vàng: <strong class="num text-highlighted">{{ timDoiXu.tong }}</strong>%
              — còn lại cho Tím: <strong class="num text-highlighted">{{ timDoiXu.tim }}</strong>%.
              <span v-if="timDoiXu.vuot" class="text-error">Tổng 5 màu đã vượt 100% — Tím sẽ về 0, lưu lại sẽ báo lỗi.</span>
            </p>

            <p class="pt-2 text-sm font-semibold text-toned">
              Đổi bằng Lượng:
            </p>
            <div class="grid gap-4 sm:grid-cols-3 lg:grid-cols-6">
              <UFormField v-for="(ten, i) in TEN_MAU" :key="ten" :label="`${ten} (%)`">
                <UInput v-model="f.tieu.doiTieuLuongMau[i]" type="number" min="0" max="100" step="0.001" class="w-full" />
              </UFormField>
              <UFormField label="Tím (%) — tự tính">
                <UInput :model-value="String(timDoiLuong.tim)" readonly variant="soft" class="w-full" />
              </UFormField>
            </div>
            <p class="text-sm text-muted">
              Tổng Trắng→Vàng: <strong class="num text-highlighted">{{ timDoiLuong.tong }}</strong>%
              — còn lại cho Tím: <strong class="num text-highlighted">{{ timDoiLuong.tim }}</strong>%.
              <span v-if="timDoiLuong.vuot" class="text-error">Tổng 5 màu đã vượt 100% — Tím sẽ về 0, lưu lại sẽ báo lỗi.</span>
            </p>
          </section>

          <div class="flex flex-wrap items-center justify-between gap-3">
            <AdminCauHinhSuaBoi :moc="data?.suaLanCuoi.tieu" />
            <UButton type="submit" icon="i-lucide-save" :loading="dangLuu === 'luu_tieu'">
              Lưu tỉ lệ Vận Tiêu
            </UButton>
          </div>
        </form>
      </UCard>

      <!-- ============ Chiếm thành ============ -->
      <UCard>
        <template #header>
          <h3 class="font-semibold text-highlighted">
            Giờ chiếm thành
          </h3>
          <p class="text-sm text-muted">
            Để trống giờ = dùng lịch chuẩn 21h thứ 3 và thứ 7. Sự kiện World Cup nay bật/tắt
            ở trang <ULink to="/admin/sukien" class="text-primary">Sự kiện</ULink>, không còn ở đây nữa.
          </p>
        </template>
        <form class="space-y-4" @submit.prevent="luu('luu_chiemthanh', f.chiemThanh)">
          <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <UFormField label="Giờ (0-23, để trống = mặc định)">
              <UInput v-model="f.chiemThanh.gioChiemThanh" placeholder="mặc định" class="w-full" />
            </UFormField>
            <UFormField label="Phút (0-59)">
              <UInput v-model="f.chiemThanh.phutChiemThanh" type="number" min="0" max="59" class="w-full" />
            </UFormField>
          </div>
          <UFormField label="Lặp lại vào các thứ trong tuần (không chọn thứ nào = mọi ngày)">
            <div class="mt-1 flex flex-wrap gap-x-5 gap-y-2">
              <UCheckbox
                v-for="(ten, ma) in data?.cacThu"
                :key="ma"
                :label="ten"
                :model-value="f.chiemThanh.cacThu.includes(String(ma))"
                @update:model-value="v => doiThu(String(ma), v)"
              />
            </div>
          </UFormField>
          <UFormField label="Hoặc một ngày cụ thể (yyyy-MM-dd)" help="Điền vào đây thì bỏ qua các thứ chọn ở trên, chỉ chạy đúng 1 lần." class="max-w-80">
            <UInput v-model="f.chiemThanh.ngayCuThe" placeholder="2026-10-25" class="w-full" />
          </UFormField>
          <div class="flex flex-wrap items-center justify-between gap-3">
            <AdminCauHinhSuaBoi :moc="data?.suaLanCuoi.chiemThanh" />
            <UButton type="submit" icon="i-lucide-save" :loading="dangLuu === 'luu_chiemthanh'">
              Lưu giờ chiếm thành
            </UButton>
          </div>
        </form>
      </UCard>

      <!-- ============ Lịch hẹn sự kiện ============ -->
      <UCard>
        <template #header>
          <h3 class="font-semibold text-highlighted">
            Lịch hẹn sự kiện tự khởi động lại
          </h3>
          <p class="text-sm text-muted">
            Khi bật/tắt sự kiện ở trang <ULink to="/admin/sukien" class="text-primary">Sự kiện</ULink> cần khởi động lại, server tự
            gom các thay đổi rồi tự restart — không cần SSH. Cấu hình ở đây quyết định cách gom đó.
          </p>
        </template>
        <form class="space-y-4" @submit.prevent="luu('luu_lichhen', f.lichHen)">
          <div class="grid items-end gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <USwitch v-model="f.lichHen.tuKhoiDongLai" label="Tự khởi động lại" class="pb-2" />
            <UFormField label="Gom thay đổi trong (giây, 30-3600)">
              <UInput v-model="f.lichHen.gomGiay" type="number" min="30" max="3600" class="w-full" />
            </UFormField>
            <UFormField label="Báo trước (giây, 10-600)">
              <UInput v-model="f.lichHen.baoTruocGiay" type="number" min="10" max="600" class="w-full" />
            </UFormField>
          </div>
          <div class="flex flex-wrap items-center justify-between gap-3">
            <AdminCauHinhSuaBoi :moc="data?.suaLanCuoi.lichHen" />
            <UButton type="submit" icon="i-lucide-save" :loading="dangLuu === 'luu_lichhen'">
              Lưu lịch hẹn sự kiện
            </UButton>
          </div>
        </form>
      </UCard>

      <!-- ============ EXP phụ ============ -->
      <UCard>
        <template #header>
          <h3 class="font-semibold text-highlighted">
            Thưởng EXP phụ
          </h3>
          <p class="text-sm text-muted">
            Trước 11/09/2026 nằm trong <code>server.ini</code> (<code>sv.expvantieu</code>,
            <code>sv.Luckyexp</code>, <code>sv.ExpQua</code>), phải SSH sửa tay + khởi động lại. Nay sửa ở đây,
            server tự áp dụng trong tối đa 10 giây, không cần restart, không cần đụng tới <code>server.ini</code> nữa.
          </p>
        </template>
        <form class="space-y-4" @submit.prevent="luu('luu_exp_phu', f.expPhu)">
          <div class="flex flex-wrap gap-x-8 gap-y-3">
            <USwitch v-model="f.expPhu.expVantieu" label="EXP giao tiêu cá nhân" />
            <USwitch v-model="f.expPhu.luckyExp" label="EXP trong Rương May Mắn (Bạc & Vàng)" />
            <USwitch v-model="f.expPhu.expQua" label="EXP cây ăn quả" />
          </div>
          <UAlert color="warning" variant="subtle" icon="i-lucide-triangle-alert">
            <template #description>
              <strong>Tắt "EXP cây ăn quả" còn khoá luôn nút "Tăng tốc" cho MỌI cây ăn quả</strong>
              (kể cả cây ra đá quý/thuốc, không riêng cây EXP) — hiệu ứng phụ có sẵn trong mã nguồn
              (<code>FruitTemplate.canTangToc</code>), không phải do đợt chuyển đổi này gây ra.
            </template>
          </UAlert>
          <div class="flex flex-wrap items-center justify-between gap-3">
            <AdminCauHinhSuaBoi :moc="data?.suaLanCuoi.expPhu" />
            <UButton type="submit" icon="i-lucide-save" :loading="dangLuu === 'luu_exp_phu'">
              Lưu
            </UButton>
          </div>
        </form>
      </UCard>

      <!-- ============ Khác ============ -->
      <UCard>
        <template #header>
          <h3 class="font-semibold text-highlighted">
            Khác
          </h3>
        </template>
        <form class="space-y-4" @submit.prevent="luu('luu_khac', f.khac)">
          <div class="grid gap-4 sm:grid-cols-2">
            <UFormField label="Số nhân vật tối đa / tài khoản (1-3)">
              <UInput v-model="f.khac.maxCharPerAccount" type="number" min="1" max="3" class="w-full" />
            </UFormField>
            <UFormField
              label="Tài khoản quản trị trong game (phân cách bằng dấu phẩy)"
              help="Đây là quyền GM trong game (chat lệnh, bảng setting...) — khác hẳn KPAH_ADMIN_ACCOUNTS (danh sách được vào trang quản trị web này), hai danh sách không liên quan nhau."
            >
              <UInput v-model="f.khac.adminAccounts" maxlength="255" class="w-full" />
            </UFormField>
            <UFormField label="Website">
              <UInput v-model="f.khac.website" maxlength="255" class="w-full" />
            </UFormField>
            <UFormField label="Link tải game">
              <UInput v-model="f.khac.linkTaiGame" maxlength="255" class="w-full" />
            </UFormField>
            <UFormField label="Mã test">
              <UInput v-model="f.khac.testCode" maxlength="50" class="w-full" />
            </UFormField>
          </div>
          <UAlert color="warning" variant="subtle" icon="i-lucide-triangle-alert">
            <template #description>
              <strong>Hai cờ dưới đây chỉ nên bật tạm thời rồi tắt lại ngay</strong> — để quên trên server thật
              là lỗ hổng (cho phép tạo nhân vật trùng tên GM / mã test cấp xu-lượng miễn phí).
            </template>
          </UAlert>
          <div class="flex flex-wrap gap-x-8 gap-y-3">
            <USwitch v-model="f.khac.choPhepTaoTenGM" color="error" label="Cho phép tạo tên trùng GM" />
            <USwitch v-model="f.khac.testMode" color="error" label="Bật testMode" />
          </div>
          <div class="flex flex-wrap items-center justify-between gap-3">
            <AdminCauHinhSuaBoi :moc="data?.suaLanCuoi.khac" />
            <UButton type="submit" icon="i-lucide-save" :loading="dangLuu === 'luu_khac'">
              Lưu
            </UButton>
          </div>
        </form>
      </UCard>
    </template>
  </AdminPage>
</template>
