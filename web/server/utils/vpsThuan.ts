/**
 * CHỈ SỐ VPS — hàm THUẦN (không đọc tệp, không chạy lệnh, không auto-import của Nuxt): nhận CHỮ đã đọc từ
 * /proc, `systemctl show`, hoặc JSON của PowerShell (Windows) và trả số. Lớp đọc thật ở server/utils/vpsMay.ts,
 * bộ lấy mẫu 2 giây ở server/plugins/chi-so-vps.ts. Test: tests/chi-so-vps.test.ts (đưa chữ mẫu vào).
 *
 * Quy ước: `null` = KHÔNG HỖ TRỢ trên hệ điều hành này (trang hiện "không hỗ trợ trên hệ điều hành này"),
 * khác với 0 (đo được, bằng 0). Tốc độ (byte/giây, gói/giây, trang/giây) tính từ HIỆU hai lần đọc bộ đếm
 * tích luỹ; bộ đếm tụt (máy khởi động lại, card mạng gắn lại) -> coi là 0 chứ không âm.
 */

// ---------------------------------------------------------------------------
//  Kiểu dữ liệu chung (Linux + Windows ra cùng một hình)
// ---------------------------------------------------------------------------

export interface VpsDia { diem: string, loai: string, tongMb: number, dungMb: number, pt: number }
export interface VpsDiaIo { ten: string, docBs: number, ghiBs: number }
/** `ao` = loopback / card ảo, không cộng vào tổng (laMangNoiBo). */
export interface VpsMang { ten: string, ao: boolean, vaoBs: number, raBs: number, goiVaoS: number, goiRaS: number }
export interface VpsTienTrinh {
  pid: number
  /** Tên thân thiện (Máy chủ game, Web Nuxt, MariaDB…) hoặc tên lệnh. */
  ten: string
  /** Tên lệnh gốc (comm / ProcessName). */
  lenh: string
  /** Đơn vị systemd (htbb-game.service…) nếu có. */
  donVi: string
  nguoi: string | null
  rssMb: number
  /** % của MỘT nhân (2 nhân bận hết = 200%), giống top. */
  cpu: number
}
export interface VpsDichVu {
  ten: string
  /** active | failed | inactive | activating | deactivating | reloading | khong_cai */
  trangThai: string
  /** Lúc vào trạng thái hiện tại (ms epoch), null = không rõ. */
  tu: number | null
}
export interface MauVps {
  t: number
  heDieuHanh: string
  may: string
  soNhan: number
  /** % toàn máy (0..100). */
  cpu: number | null
  cpuMoiNhan: number[] | null
  /** load average 1/5/15; null trên Windows. */
  tai: [number, number, number] | null
  uptime: number | null
  ram: { tongMb: number, dungMb: number, sanMb: number, bufCacheMb: number | null }
  swap: { tongMb: number, dungMb: number, vaoTrangS: number | null, raTrangS: number | null } | null
  dia: VpsDia[] | null
  diaIo: { docBs: number, ghiBs: number, thietBi: VpsDiaIo[] } | null
  mang: VpsMang[] | null
  tienTrinh: VpsTienTrinh[] | null
  dichVu: VpsDichVu[] | null
}

/** Điểm gọn cho biểu đồ 30 phút trong bộ nhớ (2 giây một điểm). */
export interface DiemVps {
  t: number
  cpu: number | null
  tai1: number | null
  ramDungMb: number
  ramSanMb: number
  swapDungMb: number | null
  docBs: number | null
  ghiBs: number | null
  vaoBs: number | null
  raBs: number | null
}

/** Đơn vị systemd được theo dõi (cố định — không bao giờ lấy từ request). */
export const DON_VI_VPS = [
  'htbb-game.service', 'htbb-login.service', 'htbb-web.service', 'htbb-hlv-ai.service',
  'htbb-staging-game.service', 'htbb-staging-login.service', 'mariadb.service', 'nginx.service',
] as const

const MB = 1024 * 1024
const tron = (v: number, n = 1) => Math.round(v * 10 ** n) / 10 ** n
const so = (s: string | undefined) => {
  const v = Number(s)
  return Number.isFinite(v) ? v : 0
}
/** Hiệu bộ đếm, không âm (bộ đếm tụt = 0). */
export const hieuDem = (moi: number, cu: number) => (moi >= cu ? moi - cu : 0)

// ---------------------------------------------------------------------------
//  /proc/stat — CPU
// ---------------------------------------------------------------------------

export interface NhipCpu { ban: number, tong: number }
/** Mỗi dòng cpu / cpuN -> {ban, tong} (jiffies). Phần tử 0 = tổng máy, 1.. = từng nhân. */
export function docProcStat(chu: string): NhipCpu[] {
  const ra: NhipCpu[] = []
  for (const dong of chu.split('\n')) {
    if (!/^cpu\d*\s/.test(dong)) continue
    const c = dong.trim().split(/\s+/).slice(1).map(Number)
    // user nice system idle iowait irq softirq steal guest guest_nice — guest đã nằm trong user
    const ranh = (c[3] ?? 0) + (c[4] ?? 0)
    const tong = c.slice(0, 8).reduce((a, b) => a + (Number.isFinite(b) ? b : 0), 0)
    ra.push({ ban: tong - ranh, tong })
  }
  return ra
}

/** % bận giữa hai lần đọc (0..100), một số cho mỗi phần tử. */
export function phanTramCpu(cu: NhipCpu[], moi: NhipCpu[]): number[] {
  return moi.map((m, i) => {
    const c = cu[i]
    if (!c) return 0
    const dt = m.tong - c.tong
    if (dt <= 0) return 0
    return tron(Math.min(100, Math.max(0, ((m.ban - c.ban) / dt) * 100)))
  })
}

// ---------------------------------------------------------------------------
//  /proc/meminfo, /proc/vmstat, /proc/loadavg, /proc/uptime
// ---------------------------------------------------------------------------

export function docMeminfo(chu: string) {
  const kb: Record<string, number> = {}
  for (const dong of chu.split('\n')) {
    const m = dong.match(/^(\w+(?:\(\w+\))?):\s+(\d+)/)
    if (m) kb[m[1]!] = Number(m[2])
  }
  const mb = (k: string) => Math.round((kb[k] ?? 0) / 1024)
  const tongMb = mb('MemTotal')
  // MemAvailable có từ Linux 3.14; máy cũ hơn thì ước bằng free + buffers + cached.
  const sanMb = kb.MemAvailable !== undefined ? mb('MemAvailable') : mb('MemFree') + mb('Buffers') + mb('Cached')
  return {
    ram: { tongMb, dungMb: Math.max(0, tongMb - sanMb), sanMb, bufCacheMb: mb('Buffers') + mb('Cached') + mb('SReclaimable') },
    swap: { tongMb: mb('SwapTotal'), dungMb: Math.max(0, mb('SwapTotal') - mb('SwapFree')) },
  }
}

/** pswpin / pswpout (số TRANG đổi vào / ra, tích luỹ). */
export function docVmstat(chu: string): { vao: number, ra: number } {
  const vao = chu.match(/^pswpin\s+(\d+)/m)
  const ra = chu.match(/^pswpout\s+(\d+)/m)
  return { vao: vao ? Number(vao[1]) : 0, ra: ra ? Number(ra[1]) : 0 }
}

export function docLoadavg(chu: string): [number, number, number] | null {
  const c = chu.trim().split(/\s+/).slice(0, 3).map(Number)
  return c.length === 3 && c.every(Number.isFinite) ? [c[0]!, c[1]!, c[2]!] : null
}

export function docUptime(chu: string): number | null {
  const v = Number(chu.trim().split(/\s+/)[0])
  return Number.isFinite(v) ? Math.floor(v) : null
}

// ---------------------------------------------------------------------------
//  /proc/diskstats — đọc / ghi đĩa
// ---------------------------------------------------------------------------

/** Chỉ đĩa NGUYÊN (không phân vùng, loop, ram, dm-*, zram) để không đếm hai lần. */
export const laDiaNguyen = (ten: string) => /^(sd[a-z]+|vd[a-z]+|xvd[a-z]+|hd[a-z]+|nvme\d+n\d+|mmcblk\d+)$/.test(ten)

/** ten -> {doc, ghi} tính bằng BYTE (sector 512 byte, cố định trong kernel bất kể sector vật lý). */
export function docDiskstats(chu: string): Map<string, { doc: number, ghi: number }> {
  const ra = new Map<string, { doc: number, ghi: number }>()
  for (const dong of chu.split('\n')) {
    const c = dong.trim().split(/\s+/)
    if (c.length < 10 || !laDiaNguyen(c[2]!)) continue
    ra.set(c[2]!, { doc: so(c[5]) * 512, ghi: so(c[9]) * 512 })
  }
  return ra
}

// ---------------------------------------------------------------------------
//  /proc/net/dev — mạng
// ---------------------------------------------------------------------------

export interface DemMang { vaoB: number, vaoGoi: number, raB: number, raGoi: number }
export function docNetDev(chu: string): Map<string, DemMang> {
  const ra = new Map<string, DemMang>()
  for (const dong of chu.split('\n')) {
    const i = dong.indexOf(':')
    if (i < 0 || dong.includes('|')) continue
    const ten = dong.slice(0, i).trim()
    const c = dong.slice(i + 1).trim().split(/\s+/)
    if (!ten || c.length < 10) continue
    ra.set(ten, { vaoB: so(c[0]), vaoGoi: so(c[1]), raB: so(c[8]), raGoi: so(c[9]) })
  }
  return ra
}

/**
 * Card KHÔNG cộng vào tổng mạng: loopback, và card ảo / đường hầm mà gói của nó cũng đi qua card thật (cộng vào là đếm
 * hai lần): veth / docker / bridge, virbr, cni, flannel, tailscale, WireGuard, tun/tap, vEthernet (Hyper-V). Vẫn hiện trong bảng.
 */
export const laMangNoiBo = (ten: string) =>
  ten === 'lo' || /^(Loopback|veth|docker|br-|virbr|cni|flannel|tailscale|wg\d|tun\d|tap\d|vEthernet)/i.test(ten)

/** Tốc độ từng card giữa hai lần đọc. Bỏ card không còn ở lần đọc mới. Card thật trước, card ảo (`ao`) sau. */
export function tocDoMang(cu: Map<string, DemMang>, moi: Map<string, DemMang>, giay: number): VpsMang[] {
  const g = giay > 0 ? giay : 1
  const ra: VpsMang[] = []
  for (const [ten, m] of moi) {
    const c = cu.get(ten)
    if (!c) continue
    ra.push({
      ten,
      ao: laMangNoiBo(ten),
      vaoBs: Math.round(hieuDem(m.vaoB, c.vaoB) / g),
      raBs: Math.round(hieuDem(m.raB, c.raB) / g),
      goiVaoS: tron(hieuDem(m.vaoGoi, c.vaoGoi) / g),
      goiRaS: tron(hieuDem(m.raGoi, c.raGoi) / g),
    })
  }
  return ra.sort((a, b) => Number(laMangNoiBo(a.ten)) - Number(laMangNoiBo(b.ten)) || a.ten.localeCompare(b.ten))
}

// ---------------------------------------------------------------------------
//  /proc/mounts — chọn hệ tệp đáng xem dung lượng
// ---------------------------------------------------------------------------

const LOAI_HE_TEP = new Set(['ext2', 'ext3', 'ext4', 'xfs', 'btrfs', 'zfs', 'f2fs', 'jfs', 'reiserfs', 'vfat', 'exfat', 'ntfs', 'ntfs3', 'fuseblk'])

/** [{diem, loai, thietBi}] — hệ tệp thật, bỏ trùng thiết bị (bind mount), bỏ /snap, /boot/efi vẫn giữ. */
export function docMounts(chu: string): { diem: string, loai: string, thietBi: string }[] {
  const daCo = new Set<string>()
  const ra: { diem: string, loai: string, thietBi: string }[] = []
  for (const dong of chu.split('\n')) {
    const c = dong.trim().split(/\s+/)
    if (c.length < 3) continue
    const [thietBi, diemTho, loai] = c as [string, string, string]
    // /proc/mounts mã hoá dấu cách = \040
    const diem = diemTho.replace(/\\(\d{3})/g, (_, o: string) => String.fromCharCode(Number.parseInt(o, 8)))
    if (!LOAI_HE_TEP.has(loai) || diem.startsWith('/snap/') || daCo.has(thietBi)) continue
    daCo.add(thietBi)
    ra.push({ diem, loai, thietBi })
  }
  return ra
}

/** Dung lượng từ kết quả statfs (bsize, blocks, bavail như fs.statfs). Dùng = tổng - trống (kể cả phần dự trữ root). */
export function dungLuongDia(diem: string, loai: string, s: { bsize: number, blocks: number, bfree: number, bavail: number }): VpsDia {
  const tong = s.blocks * s.bsize
  const dung = (s.blocks - s.bfree) * s.bsize
  // giống df: % = dùng / (dùng + còn cho người thường)
  const mau = dung + s.bavail * s.bsize
  return { diem, loai, tongMb: Math.round(tong / MB), dungMb: Math.round(dung / MB), pt: mau > 0 ? tron((dung / mau) * 100) : 0 }
}

// ---------------------------------------------------------------------------
//  /proc/<pid>/stat, cgroup, cmdline — tiến trình
// ---------------------------------------------------------------------------

/** comm, utime+stime (jiffies), rss (trang), starttime. comm có thể chứa dấu cách / ngoặc -> cắt theo ')' CUỐI. */
export function docPidStat(chu: string): { comm: string, cpu: number, rssTrang: number, batDau: number } | null {
  const a = chu.indexOf('(')
  const b = chu.lastIndexOf(')')
  if (a < 0 || b < a) return null
  const c = chu.slice(b + 2).trim().split(/\s+/)
  // sau ')' : [0]=state [11]=utime [12]=stime [19]=starttime [21]=rss
  if (c.length < 22) return null
  return { comm: chu.slice(a + 1, b), cpu: so(c[11]) + so(c[12]), rssTrang: so(c[21]), batDau: so(c[19]) }
}

/** Uid thật từ /proc/<pid>/status. */
export function docUid(chu: string): number | null {
  const m = chu.match(/^Uid:\s+(\d+)/m)
  return m ? Number(m[1]) : null
}

/** /etc/passwd -> uid -> tên. */
export function docPasswd(chu: string): Map<number, string> {
  const ra = new Map<number, string>()
  for (const dong of chu.split('\n')) {
    const c = dong.split(':')
    if (c.length >= 3 && /^\d+$/.test(c[2]!)) ra.set(Number(c[2]), c[0]!)
  }
  return ra
}

/** Đơn vị systemd từ /proc/<pid>/cgroup (cgroup v2 "0::/system.slice/htbb-game.service", v1 "1:name=systemd:/…"). */
export function donViTuCgroup(chu: string): string {
  // chỉ dịch vụ hệ thống (system.slice); phiên người dùng (user.slice/…/user@1000.service) không phải đơn vị cần xem
  const m = chu.match(/\/system\.slice\/(?:[\w@.-]+\.slice\/)?([\w@.-]+\.service)(?:\/|$)/m)
  return m ? m[1]! : ''
}

const TEN_DON_VI: Record<string, string> = {
  'htbb-game.service': 'Máy chủ game',
  'htbb-login.service': 'Máy chủ đăng nhập',
  'htbb-web.service': 'Web Nuxt',
  'htbb-hlv-ai.service': 'Bot AI (HLV AI)',
  'htbb-staging-game.service': 'Game staging',
  'htbb-staging-login.service': 'Đăng nhập staging',
  'mariadb.service': 'MariaDB',
  'mysql.service': 'MariaDB',
  'mysqld.service': 'MariaDB',
  'nginx.service': 'nginx',
}
export const tenDonViThanThien = (donVi: string) => TEN_DON_VI[donVi] ?? ''

/**
 * Tên thân thiện: ưu tiên đơn vị systemd (chắc chắn), rồi đoán theo dòng lệnh (máy dev / Windows / chạy tay):
 * thư mục làm việc staging, lớp DichVuHlvAi, jar đăng nhập, .output/server/index.mjs…
 */
export function tenThanThien(lenh: string, dongLenh: string, donVi = ''): string {
  const dv = tenDonViThanThien(donVi)
  if (dv) return dv
  const d = dongLenh.replace(/\0/g, ' ').toLowerCase()
  const l = lenh.toLowerCase().replace(/\.exe$/, '')
  if (l === 'java' || l === 'javaw') {
    const staging = d.includes('htbb-staging')
    if (d.includes('dichvuhlvai') || d.includes('hlv-ai') || d.includes('hlv_ai')) return 'Bot AI (HLV AI)'
    if (/login/.test(d)) return staging ? 'Đăng nhập staging' : 'Máy chủ đăng nhập'
    if (d.includes('htbb') || d.includes('teamserver') || d.includes('server')) return staging ? 'Game staging' : 'Máy chủ game'
    return 'Java'
  }
  if (l === 'node') return d.includes('.output/server/index.mjs') || d.includes('.output\\server\\index.mjs') ? 'Web Nuxt' : 'Node.js'
  if (l === 'mariadbd' || l === 'mysqld' || l === 'mariadb') return 'MariaDB'
  if (l === 'nginx') return 'nginx'
  return ''
}

/**
 * Danh sách hiện: MỌI tiến trình có tên thân thiện cụ thể (game, web, bot, MariaDB, nginx…) + top theo RSS + top theo CPU,
 * tối đa `toiDa`, sắp theo RSS giảm dần. Bỏ tiến trình RSS 0 (luồng nhân).
 */
export function chonTienTrinh(ds: VpsTienTrinh[], toiDa = 25, topRss = 12, topCpu = 8): VpsTienTrinh[] {
  const that = ds.filter(p => p.rssMb > 0)
  const biet = new Set(Object.values(TEN_DON_VI).concat(['Bot AI (HLV AI)']))
  const chon = new Map<number, VpsTienTrinh>()
  for (const p of that) if (biet.has(p.ten)) chon.set(p.pid, p)
  for (const p of [...that].sort((a, b) => b.rssMb - a.rssMb).slice(0, topRss)) chon.set(p.pid, p)
  for (const p of [...that].sort((a, b) => b.cpu - a.cpu).slice(0, topCpu)) if (p.cpu > 0) chon.set(p.pid, p)
  return [...chon.values()].sort((a, b) => b.rssMb - a.rssMb).slice(0, toiDa)
}

// ---------------------------------------------------------------------------
//  systemctl show
// ---------------------------------------------------------------------------

/**
 * Chữ của `systemctl show -p Id,LoadState,ActiveState,ActiveEnterTimestampMonotonic,StateChangeTimestampMonotonic <đơn vị…>`
 * (các khối cách nhau dòng trống, thứ tự thuộc tính tuỳ systemd). Mốc thời gian dùng MONOTONIC (µs kể từ khởi động)
 * + uptime để khỏi phải đọc chuỗi ngày theo múi giờ của từng bản systemd.
 */
export function docSystemctlShow(chu: string, uptimeGiay: number | null, bayGio: number): VpsDichVu[] {
  const ra: VpsDichVu[] = []
  for (const khoi of chu.split(/\n\s*\n/)) {
    const p: Record<string, string> = {}
    for (const dong of khoi.split('\n')) {
      const i = dong.indexOf('=')
      if (i > 0) p[dong.slice(0, i).trim()] = dong.slice(i + 1).trim()
    }
    if (!p.Id) continue
    const khongCai = p.LoadState === 'not-found' || p.LoadState === 'masked'
    const trangThai = khongCai ? 'khong_cai' : (p.ActiveState || 'unknown')
    const mono = so(trangThai === 'active' ? p.ActiveEnterTimestampMonotonic : (p.StateChangeTimestampMonotonic || p.ActiveEnterTimestampMonotonic))
    const tu = !khongCai && mono > 0 && uptimeGiay !== null ? Math.round(bayGio - (uptimeGiay * 1000 - mono / 1000)) : null
    ra.push({ ten: p.Id, trangThai, tu })
  }
  return ra
}

// ---------------------------------------------------------------------------
//  Windows — JSON một lần chạy PowerShell (server/utils/vpsMay.ts, KICH_BAN_PS)
// ---------------------------------------------------------------------------

/** ConvertTo-Json trả object đơn thay vì mảng khi chỉ có một phần tử, và null khi rỗng. */
const mang = <T>(v: unknown): T[] => (Array.isArray(v) ? v as T[] : v == null ? [] : [v as T])

export interface MauPs {
  luc: number
  mang: Map<string, DemMang>
  dia: { doc: number, ghi: number } | null
  pagefile: { tongMb: number, dungMb: number } | null
  trangS: { vao: number, ra: number } | null
  tienTrinh: { pid: number, lenh: string, wsB: number, cpuGiay: number, dongLenh: string }[]
  dichVu: { ten: string, trangThai: string }[]
}

/** Đọc JSON của kịch bản PowerShell. Thiếu phần nào thì phần đó rỗng/null — không ném lỗi. */
export function docJsonPowerShell(chu: string, luc: number): MauPs | null {
  let j: any
  try {
    j = JSON.parse(chu.replace(/^﻿/, '').trim())
  }
  catch { return null }
  if (!j || typeof j !== 'object') return null
  const mangMap = new Map<string, DemMang>()
  for (const n of mang<any>(j.net)) {
    if (!n?.Name) continue
    mangMap.set(String(n.Name), { vaoB: so(n.ReceivedBytes), raB: so(n.SentBytes), vaoGoi: so(n.ReceivedUnicastPackets), raGoi: so(n.SentUnicastPackets) })
  }
  const d = mang<any>(j.disk)[0]
  const pf = mang<any>(j.pf)
  const m = mang<any>(j.mem)[0]
  const cmd = new Map<number, string>()
  for (const c of mang<any>(j.cmd)) if (c?.ProcessId != null) cmd.set(Number(c.ProcessId), String(c.CommandLine ?? ''))
  return {
    luc,
    mang: mangMap,
    // Win32_PerfRawData_PerfDisk_PhysicalDisk _Total: DiskRead/WriteBytesPersec dạng thô = byte tích luỹ
    dia: d ? { doc: so(d.DiskReadBytesPersec), ghi: so(d.DiskWriteBytesPersec) } : null,
    // Win32_PageFileUsage: AllocatedBaseSize / CurrentUsage đơn vị MB, có thể nhiều tệp
    pagefile: pf.length ? { tongMb: pf.reduce((t, p) => t + so(p?.AllocatedBaseSize), 0), dungMb: pf.reduce((t, p) => t + so(p?.CurrentUsage), 0) } : null,
    // Win32_PerfFormattedData_PerfOS_Memory: đã là tốc độ (trang/giây)
    trangS: m ? { vao: so(m.PagesInputPersec), ra: so(m.PagesOutputPersec) } : null,
    tienTrinh: mang<any>(j.proc).filter(p => p?.Id != null).map(p => ({
      pid: Number(p.Id), lenh: String(p.ProcessName ?? ''), wsB: so(p.WS), cpuGiay: so(p.CPU), dongLenh: cmd.get(Number(p.Id)) ?? '',
    })),
    dichVu: mang<any>(j.svc).filter(s => s?.Name).map(s => ({ ten: String(s.Name), trangThai: trangThaiDichVuWin(s.Status) })),
  }
}

/** Get-Service Status (chuỗi hoặc số enum ServiceControllerStatus) -> trạng thái kiểu systemd. */
export function trangThaiDichVuWin(s: unknown): string {
  const t = String(s ?? '').toLowerCase()
  const bang: Record<string, string> = {
    running: 'active', 4: 'active', stopped: 'inactive', 1: 'inactive', startpending: 'activating', 2: 'activating',
    stoppending: 'deactivating', 3: 'deactivating', paused: 'inactive', 7: 'inactive', continuepending: 'activating', 5: 'activating', pausepending: 'deactivating', 6: 'deactivating',
  }
  return bang[t] ?? 'unknown'
}

/** CPU % mỗi tiến trình trên Windows: hiệu CPU giây (Get-Process .CPU) giữa hai lần chạy PowerShell. */
export function tienTrinhWin(cu: MauPs | null, moi: MauPs): VpsTienTrinh[] {
  const truoc = new Map((cu?.tienTrinh ?? []).map(p => [p.pid, p.cpuGiay]))
  const g = cu ? (moi.luc - cu.luc) / 1000 : 0
  return moi.tienTrinh.map(p => ({
    pid: p.pid,
    ten: tenThanThien(p.lenh, p.dongLenh) || p.lenh,
    lenh: p.lenh,
    donVi: '',
    nguoi: null,
    rssMb: tron(p.wsB / MB),
    cpu: g > 0 && truoc.has(p.pid) ? tron((hieuDem(p.cpuGiay, truoc.get(p.pid)!) / g) * 100) : 0,
  }))
}

// ---------------------------------------------------------------------------
//  Cửa sổ 30 phút + gộp phút
// ---------------------------------------------------------------------------

export function diemTuMau(m: MauVps): DiemVps {
  const tongMang = (k: 'vaoBs' | 'raBs') => (m.mang ? m.mang.filter(n => !n.ao).reduce((t, n) => t + n[k], 0) : null)
  return {
    t: m.t,
    cpu: m.cpu,
    tai1: m.tai ? m.tai[0] : null,
    ramDungMb: m.ram.dungMb,
    ramSanMb: m.ram.sanMb,
    swapDungMb: m.swap ? m.swap.dungMb : null,
    docBs: m.diaIo ? m.diaIo.docBs : null,
    ghiBs: m.diaIo ? m.diaIo.ghiBs : null,
    vaoBs: tongMang('vaoBs'),
    raBs: tongMang('raBs'),
  }
}

/** Vòng đệm cố định (mặc định 900 điểm = 30 phút x 2 giây). */
export class VongDem<T> {
  private ds: T[] = []
  readonly toiDa: number
  constructor(toiDa: number) { this.toiDa = toiDa }
  them(v: T) {
    this.ds.push(v)
    if (this.ds.length > this.toiDa) this.ds.splice(0, this.ds.length - this.toiDa)
  }

  tatCa(): T[] { return this.ds.slice() }
  get length() { return this.ds.length }
}

/** Một dòng tob_chi_so_vps (gộp các mẫu trong cùng một phút). */
export interface DongPhutVps {
  phut: number
  soMau: number
  cpuTb: number | null
  cpuMax: number | null
  tai1: number | null
  ramTongMb: number
  ramDungMb: number
  ramSanMinMb: number
  swapTongMb: number | null
  swapDungMb: number | null
  swapVaoS: number | null
  swapRaS: number | null
  /** byte đọc / ghi đĩa và byte mạng vào / ra (không tính loopback) TRONG phút này. */
  diaDocB: number | null
  diaGhiB: number | null
  mangVaoB: number | null
  mangRaB: number | null
  mangGoiVaoS: number | null
  mangGoiRaS: number | null
  diaMaxPt: number | null
}

/**
 * Gộp các mẫu của MỘT phút. `giayMoiMau` = khoảng lấy mẫu (2 giây) để đổi tốc độ thành tổng byte trong phút.
 * CPU / tải / swap-rate: trung bình; RAM dùng, swap dùng, % đĩa đầy nhất: lớn nhất; RAM còn: nhỏ nhất.
 */
export function gopPhut(phut: number, mau: MauVps[], giayMoiMau: number): DongPhutVps | null {
  if (!mau.length) return null
  const tb = (ds: (number | null)[]) => {
    const v = ds.filter((x): x is number => x !== null)
    return v.length ? tron(v.reduce((a, b) => a + b, 0) / v.length, 2) : null
  }
  const lon = (ds: (number | null)[]) => {
    const v = ds.filter((x): x is number => x !== null)
    return v.length ? Math.max(...v) : null
  }
  const tong = (ds: (number | null)[]) => {
    const v = ds.filter((x): x is number => x !== null)
    return v.length ? Math.round(v.reduce((a, b) => a + b, 0) * giayMoiMau) : null
  }
  const mangNgoai = (m: MauVps) => (m.mang ? m.mang.filter(n => !n.ao) : null)
  const cong = (m: MauVps, k: 'vaoBs' | 'raBs' | 'goiVaoS' | 'goiRaS') => { const n = mangNgoai(m); return n ? n.reduce((t, x) => t + x[k], 0) : null }
  const cuoi = mau.at(-1)!
  return {
    phut,
    soMau: mau.length,
    cpuTb: tb(mau.map(m => m.cpu)),
    cpuMax: lon(mau.map(m => m.cpu)),
    tai1: tb(mau.map(m => (m.tai ? m.tai[0] : null))),
    ramTongMb: cuoi.ram.tongMb,
    ramDungMb: lon(mau.map(m => m.ram.dungMb)) ?? 0,
    ramSanMinMb: Math.min(...mau.map(m => m.ram.sanMb)),
    swapTongMb: cuoi.swap ? cuoi.swap.tongMb : null,
    swapDungMb: lon(mau.map(m => (m.swap ? m.swap.dungMb : null))),
    swapVaoS: tb(mau.map(m => m.swap?.vaoTrangS ?? null)),
    swapRaS: tb(mau.map(m => m.swap?.raTrangS ?? null)),
    diaDocB: tong(mau.map(m => m.diaIo?.docBs ?? null)),
    diaGhiB: tong(mau.map(m => m.diaIo?.ghiBs ?? null)),
    mangVaoB: tong(mau.map(m => cong(m, 'vaoBs'))),
    mangRaB: tong(mau.map(m => cong(m, 'raBs'))),
    mangGoiVaoS: tb(mau.map(m => cong(m, 'goiVaoS'))),
    mangGoiRaS: tb(mau.map(m => cong(m, 'goiRaS'))),
    diaMaxPt: lon(mau.map(m => (m.dia?.length ? Math.max(...m.dia.map(d => d.pt)) : null))),
  }
}

// ---------------------------------------------------------------------------
//  Bảng tob_chi_so_vps (một dòng mỗi phút mỗi máy, giữ 7 ngày)
// ---------------------------------------------------------------------------

export const BANG_VPS = 'tob_chi_so_vps'
export const NGAY_GIU_VPS = 7

/** [cột, kiểu] — cùng thứ tự với sql/htbb.sql (phần A + khối them_chi_so_vps.sql ở phần B; test so khớp). */
export const COT_BANG_VPS: [string, string][] = [
  ['luc', 'DATETIME NOT NULL'],
  ['may', "VARCHAR(64) NOT NULL DEFAULT ''"],
  ['so_mau', 'SMALLINT NOT NULL DEFAULT 0'],
  ['cpu_tb', 'FLOAT NULL'],
  ['cpu_max', 'FLOAT NULL'],
  ['tai_1', 'FLOAT NULL'],
  ['ram_tong_mb', 'INT NOT NULL DEFAULT 0'],
  ['ram_dung_mb', 'INT NOT NULL DEFAULT 0'],
  ['ram_san_min_mb', 'INT NOT NULL DEFAULT 0'],
  ['swap_tong_mb', 'INT NULL'],
  ['swap_dung_mb', 'INT NULL'],
  ['swap_vao_s', 'FLOAT NULL'],
  ['swap_ra_s', 'FLOAT NULL'],
  ['dia_doc_b', 'BIGINT NULL'],
  ['dia_ghi_b', 'BIGINT NULL'],
  ['mang_vao_b', 'BIGINT NULL'],
  ['mang_ra_b', 'BIGINT NULL'],
  ['mang_goi_vao_s', 'FLOAT NULL'],
  ['mang_goi_ra_s', 'FLOAT NULL'],
  ['dia_max_pt', 'FLOAT NULL'],
]

export const SQL_TAO_BANG_VPS = `CREATE TABLE IF NOT EXISTS \`${BANG_VPS}\` (${COT_BANG_VPS.map(([c, k]) => `\`${c}\` ${k}`).join(', ')}, `
  + 'PRIMARY KEY (`may`, `luc`), KEY `k_luc` (`luc`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4'

/** Giá trị theo thứ tự cột (trừ luc, may) cho INSERT. */
export function giaTriDongVps(d: DongPhutVps): (number | null)[] {
  return [d.soMau, d.cpuTb, d.cpuMax, d.tai1, d.ramTongMb, d.ramDungMb, d.ramSanMinMb, d.swapTongMb, d.swapDungMb, d.swapVaoS,
    d.swapRaS, d.diaDocB, d.diaGhiB, d.mangVaoB, d.mangRaB, d.mangGoiVaoS, d.mangGoiRaS, d.diaMaxPt]
}
