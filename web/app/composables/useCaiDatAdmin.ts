/**
 * Sửa một khối cài đặt CMS ('site' | 'trangChu' | 'menu' | 'taiGame') trong khu quản trị:
 *   const { f, luu, khoiPhuc, dangLuu, tuDb, coBang } = await useCaiDatAdmin('trangChu')
 * `f` là bản sao sâu, sửa thoải mái; Lưu gửi cả khối (server nắn theo khuôn mặc định).
 */
export async function useCaiDatAdmin<K extends KhoaCms>(khoa: K) {
  const api = useAdminApi()
  const toast = useToast()
  const f = ref(structuredClone(CMS_MAC_DINH[khoa])) as Ref<CmsCongKhai[K]>
  const tuDb = ref(false)
  const coBang = ref(true)
  const dangLuu = ref(false)
  const daThayDoi = ref(false)
  async function tai() {
    try {
      const r = await api<{ coBang: boolean, giaTri: CmsCongKhai, tuDb: Record<string, boolean> }>('/api/admin/cms/cai-dat')
      f.value = structuredClone(r.giaTri[khoa])
      tuDb.value = !!r.tuDb[khoa]
      coBang.value = r.coBang
      nextTick(() => { daThayDoi.value = false })
    }
    catch (e) { toast.add({ title: loiApi(e), color: 'error' }) }
  }
  watch(f, () => { daThayDoi.value = true }, { deep: true })
  async function luu() {
    dangLuu.value = true
    try {
      const r = await api<{ giaTri: CmsCongKhai[K] }>('/api/admin/cms/cai-dat/luu', { method: 'POST', body: { khoa, giaTri: f.value } })
      f.value = structuredClone(r.giaTri)
      tuDb.value = true
      nextTick(() => { daThayDoi.value = false })
      toast.add({ title: 'Đã lưu — trang công khai cập nhật trong vài giây', color: 'success' })
    }
    catch (e) { toast.add({ title: loiApi(e), color: 'error' }) }
    finally { dangLuu.value = false }
  }
  async function khoiPhuc() {
    try {
      await api('/api/admin/cms/cai-dat/khoi-phuc', { method: 'POST', body: { khoa } })
      await tai()
      toast.add({ title: 'Đã khôi phục mặc định', color: 'success' })
    }
    catch (e) { toast.add({ title: loiApi(e), color: 'error' }) }
  }
  if (import.meta.client) await tai()
  else onMounted(tai)
  return { f, luu, khoiPhuc, dangLuu, tuDb, coBang, daThayDoi, tai }
}
