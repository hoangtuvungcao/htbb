/**
 * Bộ lấy mẫu VPS: mỗi 2 giây đọc một mẫu (vpsMay.ts), giữ 30 phút trong bộ nhớ (900 điểm) cho biểu đồ trực tiếp,
 * gộp mỗi phút thành một dòng `tob_chi_so_vps` (giữ 7 ngày, dọn mỗi giờ). Khởi động từ server/plugins/chi-so-vps.ts.
 * Tắt bằng HTBB_CHI_SO_VPS=0. Ghi CSDL lỗi (chưa có bảng, không có quyền CREATE) thì chỉ báo một lần, vẫn lấy mẫu.
 */
import os from 'node:os'
import { docMauVps } from './vpsMay'
import {
  BANG_VPS, COT_BANG_VPS, type DiemVps, type MauVps, NGAY_GIU_VPS, SQL_TAO_BANG_VPS, VongDem, diemTuMau, giaTriDongVps, gopPhut,
} from './vpsThuan'

export const CHU_KY_VPS_MS = 2000
const SO_DIEM = 900

const tt: {
  chay: boolean
  moiNhat: MauVps | null
  vong: VongDem<DiemVps>
  phut: number
  mauPhut: MauVps[]
  daTaoBang: boolean
  daBaoLoi: boolean
  donLuc: number
  batDau: number
} = { chay: false, moiNhat: null, vong: new VongDem<DiemVps>(SO_DIEM), phut: 0, mauPhut: [], daTaoBang: false, daBaoLoi: false, donLuc: 0, batDau: 0 }

export function vpsDangLayMau() { return tt.chay }
export function vpsMoiNhat() { return tt.moiNhat }
export function vpsVong() { return tt.vong.tatCa() }
export function vpsBatDauLuc() { return tt.batDau }

async function ghiPhut(phut: number, mau: MauVps[]) {
  const d = gopPhut(phut, mau, CHU_KY_VPS_MS / 1000)
  if (!d) return
  try {
    if (!tt.daTaoBang) {
      await exec(dbGame(), SQL_TAO_BANG_VPS)
      tt.daTaoBang = true
    }
    const cot = COT_BANG_VPS.map(c => `\`${c[0]}\``)
    const sua = cot.slice(2).map(c => `${c} = VALUES(${c})`).join(', ')
    await exec(dbGame(),
      `INSERT INTO \`${BANG_VPS}\` (${cot.join(', ')}) VALUES (FROM_UNIXTIME(?), ?, ${cot.slice(2).map(() => '?').join(', ')}) ON DUPLICATE KEY UPDATE ${sua}`,
      [phut, os.hostname().slice(0, 64), ...giaTriDongVps(d)])
    if (Date.now() - tt.donLuc > 3600_000) {
      tt.donLuc = Date.now()
      await exec(dbGame(), `DELETE FROM \`${BANG_VPS}\` WHERE luc < DATE_SUB(NOW(), INTERVAL ${NGAY_GIU_VPS} DAY)`)
    }
    tt.daBaoLoi = false
  }
  catch (e: any) {
    if (!tt.daBaoLoi) console.warn(`[chi-so-vps] chua ghi duoc ${BANG_VPS}: ${e?.code ?? e?.message ?? e}`)
    tt.daBaoLoi = true
  }
}

async function nhip() {
  const m = await docMauVps()
  tt.moiNhat = m
  tt.vong.them(diemTuMau(m))
  const phut = Math.floor(m.t / 60_000) * 60
  if (phut !== tt.phut) {
    // mẫu đầu tiên sau khởi động chỉ để có bộ đếm gốc (tốc độ = 0): không gộp phút dở đầu tiên
    if (tt.phut && tt.mauPhut.length) void ghiPhut(tt.phut, tt.mauPhut)
    tt.phut = phut
    tt.mauPhut = []
  }
  tt.mauPhut.push(m)
}

export function batDauLayMauVps() {
  if (tt.chay) return
  tt.chay = true
  tt.batDau = Date.now()
  const vong = async () => {
    const t0 = Date.now()
    try { await nhip() }
    catch (e: any) { console.warn('[chi-so-vps] loi lay mau:', e?.message ?? e) }
    const hen = setTimeout(vong, Math.max(200, CHU_KY_VPS_MS - (Date.now() - t0)))
    hen.unref?.()
  }
  void vong()
}
