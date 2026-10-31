/**
 * Ngưỡng cảnh báo của trang Chỉ số VPS (/admin/he-thong, thẻ "VPS"). Nguồn duy nhất — trang và test dùng chung.
 * File thuần TypeScript, không import gì (Nuxt tự import ở app; test import trực tiếp).
 *
 *   RAM còn dùng được (MemAvailable) < 300 MB   -> đỏ
 *   swap đã dùng > 50% swap                      -> vàng
 *   hệ tệp đầy > 85%                             -> vàng (> 95% đỏ)
 *   load average 1 phút > số nhân x 1,5          -> vàng
 *   CPU toàn máy > 90%                           -> vàng
 */
export const NGUONG_VPS = {
  ramSanMb: 300,
  swapPt: 50,
  diaPt: 85,
  diaPtDo: 95,
  taiMoiNhan: 1.5,
  cpuPt: 90,
} as const

export type MucVps = '' | 'warn' | 'bad'

export const mucRamSan = (sanMb: number): MucVps => (sanMb < NGUONG_VPS.ramSanMb ? 'bad' : '')
export const mucSwap = (dungMb: number, tongMb: number): MucVps => (tongMb > 0 && (dungMb / tongMb) * 100 > NGUONG_VPS.swapPt ? 'warn' : '')
export const mucDia = (pt: number): MucVps => (pt > NGUONG_VPS.diaPtDo ? 'bad' : pt > NGUONG_VPS.diaPt ? 'warn' : '')
export const mucTai = (tai1: number, soNhan: number): MucVps => (soNhan > 0 && tai1 > soNhan * NGUONG_VPS.taiMoiNhan ? 'warn' : '')
export const mucCpu = (pt: number): MucVps => (pt > NGUONG_VPS.cpuPt ? 'warn' : '')
