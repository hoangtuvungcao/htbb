/**
 * NẠP TIỀN — CHẾ ĐỘ DEMO, phần chạm Nuxt (token, đọc gói / nhân vật CHỈ ĐỌC). Luật thuần ở napDemoThuan.ts.
 *
 * Đơn demo KHÔNG BAO GIỜ ghi vào database: kho trong bộ nhớ tiến trình (mất khi khởi động lại web).
 * Không có hàm nào ở đây gọi napTaoDon / napXacNhanThanhToan / tvCap — demo không cộng gì.
 */
import type { H3Event } from 'h3'
import type { DonDemo, GoiDemo } from './napDemoThuan'
import { DEMO_BANNER, GOI_DEMO, demoDuocXem, donDemoCongKhai, laMaDonDemo, taoKhoDemo } from './napDemoThuan'
import { GIO_HET_HAN_DON } from './napTienThuan'
import { TV_MA_GOI } from './thanhVienThuan'

const kho = taoKhoDemo()

export function napDemoKho() {
  return kho
}

export function napDemoBat(): boolean {
  return useHtbbConfig().nap.demo
}

/** Tên tài khoản người chơi trong token (không chạm DB) — chỉ để quyết định ẩn/hiện. */
export function napDemoTenNguoiXem(event: H3Event): string | null {
  return docToken(bearer(event), 'user')?.u ?? null
}

export function napDemoXem(username: string | null | undefined): boolean {
  const n = useHtbbConfig().nap
  return demoDuocXem(n.demo, n.demoChiAdmin, username, useHtbbConfig().admin.accounts)
}

/** Bắt buộc đăng nhập + được xem demo; người khác nhận đúng thông báo "đang tắt" như khi nạp tắt. */
export async function napDemoRequireUser(event: H3Event): Promise<{ id: number, username: string }> {
  const u = await requireUser(event)
  if (!napDemoXem(u.username)) throw apiLoi(403, 'Máy chủ này phi thương mại nên tính năng nạp tiền đã tắt.')
  return u
}

/** Thông tin hiển thị chung cho mọi phản hồi demo. Không có QR thật, không có link mở app ngân hàng. */
export function napDemoNganHang() {
  const n = useHtbbConfig().nap
  return { bin: n.bankBin, soTaiKhoan: n.demoSoTaiKhoan, chuTaiKhoan: n.demoTenTaiKhoan }
}

/** Gói hiển thị: đọc nap_goi nếu có (CHỈ ĐỌC), lỗi / chưa có bảng thì dùng danh sách minh hoạ. Không gồm Thành viên. */
export async function napDemoGoi(): Promise<GoiDemo[]> {
  try {
    if (await napCoBang()) {
      const ds = (await napDanhSachGoi(true)).filter(g => g.loai !== 'thanh_vien')
        .map(g => ({ ma: g.ma, ten: g.ten, loai: g.loai, gia_vnd: Number(g.gia_vnd), luong: Number(g.luong), mo_ta: g.mo_ta ?? '' }))
      if (ds.length) return ds
    }
  } catch { /* dùng danh sách minh hoạ */ }
  return GOI_DEMO
}

/** Gói Thành viên minh hoạ theo cấu hình hiện tại (giá mặc định 20.000 đ). */
export async function napDemoGoiThanhVien(): Promise<GoiDemo> {
  const c = await tvCauHinh().catch(() => null)
  return { ma: TV_MA_GOI, ten: 'Thành viên HTBB', loai: 'thanh_vien', gia_vnd: c?.gia ?? 20000, luong: 0, mo_ta: '' }
}

/** Pokémon của tài khoản (CHỈ ĐỌC). Chưa có / lỗi đọc -> một Pokémon minh hoạ để vẫn xem được trọn luồng. */
export async function napDemoNhanVat(userId: number): Promise<{ id: number, charname: string, lastLv: number }[]> {
  try {
    const ds = (await napNhanVat(userId)).map(n => ({ id: Number(n.id), charname: n.charname, lastLv: Number(n.lastLv) }))
    if (ds.length) return ds
  } catch { /* dùng Pokémon minh hoạ */ }
  return [{ id: 1, charname: 'Pokémon minh hoạ', lastLv: 1 }]
}

/** Danh mục /api/nap/goi khi demo (không ghi nap_vi, trần mặc định). */
export async function napDemoDanhMuc(userId: number) {
  return {
    goi: await napDemoGoi(),
    nhanVat: await napDemoNhanVat(userId),
    tran: { tuan: TRAN_TUAN_VND, thang: TRAN_THANG_VND, tuDat: 0, cho: 0, choLuc: null, daTuan: 0, daThang: 0 },
    khoaNap: false,
    noLuong: 0,
    mua: null,
    tyGia: LUONG_MOI_1000_VND,
    demo: true,
    banner: DEMO_BANNER,
  }
}

/** Một đơn demo của CHÍNH người xem (không phân biệt "không có" với "của người khác"). */
export function napDemoDonCuaToi(userId: number, maDon: string, loai?: string): DonDemo {
  const d = laMaDonDemo(maDon) ? kho.lay(maDon) : null
  if (!d || d.user_id !== userId || (loai && d.loai !== loai)) throw apiLoi(404, 'Không tìm thấy đơn này.', { code: 'not_found' })
  return d
}

/** Phản hồi chi tiết đơn demo: KHÔNG QR thật (qr rỗng -> trình duyệt vẽ hình giả + chữ DEMO), KHÔNG link mở app. */
export function napDemoChiTiet(d: DonDemo) {
  return {
    don: donDemoCongKhai(d),
    qr: '',
    nganHang: napDemoNganHang(),
    hetHanMs: d.taoMs + GIO_HET_HAN_DON * 3600_000,
    bayGioMs: Date.now(),
    moApp: [] as { ten: string, url: string }[],
    demo: true,
    banner: DEMO_BANNER,
  }
}

/** Ghi lại webhook gọi tới lúc đang demo (log tiến trình + bộ nhớ cho trang admin). Không ghi nhận tiền. */
export function napDemoGhiWebhook(event: H3Event, duongDan: string): void {
  const ip = ipKhach(event)
  kho.ghiWebhook(duongDan, ip)
  console.warn(`[htbb] DEMO: tu choi webhook ${duongDan} tu ${ip} - che do HTBB_NAP_DEMO=1 khong ghi nhan tien.`)
}
