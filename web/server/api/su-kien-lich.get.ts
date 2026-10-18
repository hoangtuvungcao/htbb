/** Lịch sự kiện 52 tuần trong CSDL; null = chưa nhập -> trang dùng dữ liệu sinh từ su_kien_nam.csv. */
export default defineEventHandler(async () => quaDem('lich', 15, async () => {
  if (!(await coBangCms('web_su_kien_lich'))) return null
  try {
    const rows = await q<Record<string, any>>(dbGame(), 'SELECT * FROM web_su_kien_lich ORDER BY tuan')
    if (!rows.length) return null
    return rows.map(r => ({
      tuan: Number(r.tuan), thang: Number(r.thang), ten1: String(r.ten1), ten2: String(r.ten2), phan: String(r.phan), loai: String(r.loai),
      coChe: String(r.co_che), soNgay: Number(r.so_ngay), phanThuong: String(r.phan_thuong), mocGiuChan: String(r.moc),
    }))
  }
  catch { return null }
}))
