/**
 * Số liệu cẩm nang: bản JSON đóng gói (shared/data/cam-nang*.json, dự phòng lúc build) với các mục có bảng SQL
 * đè bằng dữ liệu CSDL game lúc chạy (/api/cam-nang/du-lieu, server/utils/camNangSql.ts).
 *
 * Dùng ở <script setup>:  const { camNang: CAM_NANG, noiDung: CAM_NANG_ND } = await useCamNang()
 * (await để SSR render đúng số liệu CSDL; API lỗi / CSDL tắt -> vẫn render bằng JSON).
 */
interface DeCamNang { camNang?: Record<string, unknown>, noiDung?: Record<string, unknown>, nguon?: string[] }

/** Một lần gọi API cho mọi nơi dùng (cùng khoá useAsyncData). */
function deCamNang() {
  return useAsyncData<DeCamNang | null>('htbb-cam-nang-sql',
    () => $fetch<DeCamNang>('/api/cam-nang/du-lieu').catch(() => null), { default: () => null, dedupe: 'defer' })
}

export async function useCamNang(): Promise<{ camNang: CamNangData, noiDung: CamNangNoiDung }> {
  const { data } = await deCamNang()
  return {
    camNang: ghepCamNang(CAM_NANG, data.value?.camNang),
    noiDung: ghepCamNang(CAM_NANG_ND, data.value?.noiDung),
  }
}

/** Lịch 52 tuần theo CSDL game (data_su_kien_nam), dự phòng JSON — bản phản ứng, không cần await. */
export function useSuKienCamNang() {
  const { data } = deCamNang()
  return computed<SuKienTuan[]>(() => (data.value?.camNang?.suKien as SuKienTuan[] | undefined) ?? CAM_NANG.suKien)
}
