/**
 * CHỈ SỐ VPS — lớp ĐỌC THẬT theo hệ điều hành (hàm thuần ở vpsThuan.ts, bộ lấy mẫu 2 giây ở plugins/chi-so-vps.ts).
 *
 * Linux  : /proc (stat, meminfo, vmstat, loadavg, uptime, diskstats, net/dev, mounts, <pid>/stat|cgroup|cmdline|status),
 *          fs.statfs cho dung lượng, `systemctl show -p … <đơn vị cố định>` (không cần root). Không cần quyền gì thêm.
 * Windows: Node (os.cpus() hiệu thời gian -> % từng nhân, os.totalmem/freemem, os.uptime, fs.statfs từng ổ C:..Z:)
 *          + MỘT kịch bản PowerShell CỐ ĐỊNH (KICH_BAN_PS) chạy nền tối đa 5 giây một lần, hết giờ 8 giây: mạng, đọc/ghi
 *          đĩa, pagefile, top tiến trình, dòng lệnh java/node, dịch vụ HTBB/MariaDB/nginx. Load average không có (null).
 * Khác   : như Windows nhưng không PowerShell.
 *
 * KHÔNG có tham số nào từ request đi vào đây: lệnh, đối số, đơn vị systemd đều là hằng số.
 */
import { execFile } from 'node:child_process'
import { readFile, readdir, statfs } from 'node:fs/promises'
import os from 'node:os'
import {
  type DemMang, type MauPs, type MauVps, type NhipCpu, type VpsDia, type VpsDichVu, type VpsTienTrinh,
  DON_VI_VPS, chonTienTrinh, docDiskstats, docJsonPowerShell, docLoadavg, docMeminfo, docMounts, docNetDev, docPasswd,
  docPidStat, docProcStat, docSystemctlShow, docUid, docUptime, docVmstat, donViTuCgroup, dungLuongDia, hieuDem, phanTramCpu,
  tenThanThien, tienTrinhWin, tocDoMang,
} from './vpsThuan'

const MB = 1024 * 1024
/** USER_HZ: 100 trên mọi bản Linux x86/ARM thông dụng (kernel cố định cho /proc, không theo CONFIG_HZ). */
const CLK_TCK = 100
const TRANG = 4096

const docChu = (p: string) => readFile(p, 'utf8')
const thu = async <T>(f: () => Promise<T>): Promise<T | null> => {
  try { return await f() }
  catch { return null }
}

function chay(lenh: string, doiSo: string[], hetGio: number): Promise<string> {
  return new Promise((res, rej) => {
    execFile(lenh, doiSo, { timeout: hetGio, windowsHide: true, maxBuffer: 4 * MB, env: { ...process.env, LC_ALL: 'C' } },
      (loi, ra) => (loi ? rej(loi) : res(String(ra))))
  })
}

// ---------------------------------------------------------------------------
//  Trạng thái giữa các lần đọc (bộ đếm tích luỹ -> tốc độ)
// ---------------------------------------------------------------------------

interface Truoc {
  luc: number
  cpu: NhipCpu[] | null
  swap: { vao: number, ra: number } | null
  dia: Map<string, { doc: number, ghi: number }> | null
  mang: Map<string, DemMang> | null
  pid: Map<number, number>
}
let truoc: Truoc | null = null

const cacheDia: { luc: number, ds: VpsDia[] | null } = { luc: 0, ds: null }
const cacheDv: { luc: number, ds: VpsDichVu[] | null, dangChay: boolean } = { luc: 0, ds: null, dangChay: false }
const cachePw: { luc: number, ds: Map<number, string> } = { luc: 0, ds: new Map() }
/** pid:starttime -> {cmdline, đơn vị, uid}: đọc MỘT lần cho mỗi tiến trình. */
const cacheTt = new Map<string, { dongLenh: string, donVi: string, uid: number | null, thay: number }>()

// ---------------------------------------------------------------------------
//  Linux
// ---------------------------------------------------------------------------

async function diaLinux(bayGio: number): Promise<VpsDia[] | null> {
  if (bayGio - cacheDia.luc < 15_000 && cacheDia.ds) return cacheDia.ds
  const mounts = await thu(() => docChu('/proc/mounts'))
  if (mounts === null) return null
  const ds: VpsDia[] = []
  for (const m of docMounts(mounts)) {
    const s = await thu(() => statfs(m.diem))
    if (s && s.blocks > 0) ds.push(dungLuongDia(m.diem, m.loai, s))
  }
  cacheDia.luc = bayGio
  cacheDia.ds = ds
  return ds
}

/** systemctl show chạy nền, tối đa 10 giây một lần; trả bản nhớ gần nhất (null = máy không có systemctl). */
function dichVuLinux(bayGio: number, uptime: number | null): VpsDichVu[] | null {
  if (!cacheDv.dangChay && bayGio - cacheDv.luc >= 10_000) {
    cacheDv.dangChay = true
    cacheDv.luc = bayGio
    chay('systemctl', ['show', '-p', 'Id,LoadState,ActiveState,ActiveEnterTimestampMonotonic,StateChangeTimestampMonotonic', '--', ...DON_VI_VPS], 3000)
      .then((ra) => { cacheDv.ds = docSystemctlShow(ra, uptime, Date.now()) })
      .catch(() => { cacheDv.ds = null })
      .finally(() => { cacheDv.dangChay = false })
  }
  return cacheDv.ds
}

async function tienTrinhLinux(bayGio: number, giay: number, cu: Map<number, number>, moi: Map<number, number>): Promise<VpsTienTrinh[] | null> {
  const pids = await thu(() => readdir('/proc'))
  if (!pids) return null
  if (bayGio - cachePw.luc > 600_000) {
    const pw = await thu(() => docChu('/etc/passwd'))
    if (pw) cachePw.ds = docPasswd(pw)
    cachePw.luc = bayGio
  }
  const ds: VpsTienTrinh[] = []
  await Promise.all(pids.filter(p => /^\d+$/.test(p)).map(async (p) => {
    const pid = Number(p)
    const st = await thu(() => docChu(`/proc/${pid}/stat`))
    const s = st ? docPidStat(st) : null
    if (!s || s.rssTrang <= 0) return
    moi.set(pid, s.cpu)
    const khoa = `${pid}:${s.batDau}`
    let meta = cacheTt.get(khoa)
    if (!meta) {
      const [cmd, cg, stt] = await Promise.all([
        thu(() => docChu(`/proc/${pid}/cmdline`)), thu(() => docChu(`/proc/${pid}/cgroup`)), thu(() => docChu(`/proc/${pid}/status`)),
      ])
      meta = { dongLenh: (cmd ?? '').replace(/\0/g, ' ').trim(), donVi: cg ? donViTuCgroup(cg) : '', uid: stt ? docUid(stt) : null, thay: bayGio }
      cacheTt.set(khoa, meta)
    }
    meta.thay = bayGio
    const c = cu.get(pid)
    ds.push({
      pid,
      ten: tenThanThien(s.comm, meta.dongLenh, meta.donVi) || s.comm,
      lenh: s.comm,
      donVi: meta.donVi,
      nguoi: meta.uid === null ? null : (cachePw.ds.get(meta.uid) ?? String(meta.uid)),
      rssMb: Math.round((s.rssTrang * TRANG / MB) * 10) / 10,
      cpu: c !== undefined && giay > 0 ? Math.round((hieuDem(s.cpu, c) / CLK_TCK / giay) * 1000) / 10 : 0,
    })
  }))
  // dọn cache tiến trình đã chết
  for (const [k, v] of cacheTt) if (bayGio - v.thay > 60_000) cacheTt.delete(k)
  return chonTienTrinh(ds)
}

async function mauLinux(bayGio: number): Promise<MauVps> {
  const [stat, mem, vm, la, up, ds, nd] = await Promise.all([
    thu(() => docChu('/proc/stat')), thu(() => docChu('/proc/meminfo')), thu(() => docChu('/proc/vmstat')),
    thu(() => docChu('/proc/loadavg')), thu(() => docChu('/proc/uptime')), thu(() => docChu('/proc/diskstats')), thu(() => docChu('/proc/net/dev')),
  ])
  const t = truoc
  const giay = t ? (bayGio - t.luc) / 1000 : 0
  const cpuMoi = stat ? docProcStat(stat) : null
  const cpuPt = cpuMoi && t?.cpu ? phanTramCpu(t.cpu, cpuMoi) : null
  const m = mem ? docMeminfo(mem) : null
  const sw = vm ? docVmstat(vm) : null
  const dMoi = ds ? docDiskstats(ds) : null
  const nMoi = nd ? docNetDev(nd) : null
  const uptime = up ? docUptime(up) : null
  const pidMoi = new Map<number, number>()
  const [dia, tienTrinh] = await Promise.all([diaLinux(bayGio), tienTrinhLinux(bayGio, giay, t?.pid ?? new Map(), pidMoi)])

  let diaIo: MauVps['diaIo'] = null
  if (dMoi && t?.dia && giay > 0) {
    const thietBi = [...dMoi].map(([ten, v]) => {
      const c = t.dia!.get(ten)
      return { ten, docBs: c ? Math.round(hieuDem(v.doc, c.doc) / giay) : 0, ghiBs: c ? Math.round(hieuDem(v.ghi, c.ghi) / giay) : 0 }
    })
    diaIo = { docBs: thietBi.reduce((a, b) => a + b.docBs, 0), ghiBs: thietBi.reduce((a, b) => a + b.ghiBs, 0), thietBi }
  }
  else if (dMoi) diaIo = { docBs: 0, ghiBs: 0, thietBi: [] }

  truoc = { luc: bayGio, cpu: cpuMoi, swap: sw, dia: dMoi, mang: nMoi, pid: pidMoi }
  const soNhan = cpuMoi ? Math.max(1, cpuMoi.length - 1) : os.cpus().length
  return {
    t: bayGio,
    heDieuHanh: 'linux',
    may: os.hostname(),
    soNhan,
    cpu: cpuPt ? cpuPt[0]! : null,
    cpuMoiNhan: cpuPt ? cpuPt.slice(1) : null,
    tai: la ? docLoadavg(la) : null,
    uptime,
    ram: m ? m.ram : { tongMb: Math.round(os.totalmem() / MB), dungMb: Math.round((os.totalmem() - os.freemem()) / MB), sanMb: Math.round(os.freemem() / MB), bufCacheMb: null },
    swap: m ? {
      ...m.swap,
      vaoTrangS: sw && t?.swap && giay > 0 ? Math.round((hieuDem(sw.vao, t.swap.vao) / giay) * 10) / 10 : (sw ? 0 : null),
      raTrangS: sw && t?.swap && giay > 0 ? Math.round((hieuDem(sw.ra, t.swap.ra) / giay) * 10) / 10 : (sw ? 0 : null),
    } : null,
    dia,
    diaIo,
    mang: nMoi ? (t?.mang && giay > 0 ? tocDoMang(t.mang, nMoi, giay) : tocDoMang(nMoi, nMoi, 1)) : null,
    tienTrinh,
    dichVu: dichVuLinux(bayGio, uptime),
  }
}

// ---------------------------------------------------------------------------
//  Windows (và hệ khác: phần Node)
// ---------------------------------------------------------------------------

/** Kịch bản CỐ ĐỊNH (không ghép chuỗi từ request). Mỗi phần lỗi thì phần đó rỗng (SilentlyContinue). */
export const KICH_BAN_PS = [
  "$ErrorActionPreference='SilentlyContinue'",
  '$r=@{}',
  '$r.net=@(Get-NetAdapterStatistics | Select-Object Name,ReceivedBytes,SentBytes,ReceivedUnicastPackets,SentUnicastPackets)',
  "$r.disk=@(Get-CimInstance Win32_PerfRawData_PerfDisk_PhysicalDisk -Filter \"Name='_Total'\" | Select-Object DiskReadBytesPersec,DiskWriteBytesPersec)",
  '$r.pf=@(Get-CimInstance Win32_PageFileUsage | Select-Object Name,AllocatedBaseSize,CurrentUsage)',
  '$r.mem=@(Get-CimInstance Win32_PerfFormattedData_PerfOS_Memory | Select-Object PagesInputPersec,PagesOutputPersec)',
  '$p=@(Get-Process | Sort-Object WS -Descending | Select-Object -First 30)',
  '$r.proc=@($p | Select-Object Id,ProcessName,WS,CPU)',
  "$f=(@($p | Where-Object { $_.ProcessName -in 'java','javaw','node' }) | ForEach-Object { 'ProcessId=' + $_.Id }) -join ' or '",
  'if($f){$r.cmd=@(Get-CimInstance Win32_Process -Filter $f | Select-Object ProcessId,CommandLine)}',
  "$r.svc=@(Get-Service -Name 'htbb-*','MariaDB*','MySQL*','nginx*' | Select-Object Name,@{n='Status';e={[string]$_.Status}})",
  '$r | ConvertTo-Json -Depth 3 -Compress',
].join('; ')

const ps: { moi: MauPs | null, cu: MauPs | null, luc: number, dangChay: boolean, hong: number } = { moi: null, cu: null, luc: 0, dangChay: false, hong: 0 }

function chayPowerShell(bayGio: number) {
  if (ps.dangChay || bayGio - ps.luc < 5000 || ps.hong >= 5) return
  ps.dangChay = true
  ps.luc = bayGio
  const ma = Buffer.from(KICH_BAN_PS, 'utf16le').toString('base64')
  chay('powershell.exe', ['-NoProfile', '-NonInteractive', '-ExecutionPolicy', 'Bypass', '-EncodedCommand', ma], 8000)
    .then((ra) => {
      const m = docJsonPowerShell(ra, Date.now())
      if (m) { ps.cu = ps.moi; ps.moi = m; ps.hong = 0 }
      else ps.hong++
    })
    .catch(() => { ps.hong++ })
    .finally(() => { ps.dangChay = false })
}

let cpuNodeCu: NhipCpu[] | null = null
function cpuNode(): NhipCpu[] {
  const ds = os.cpus().map((c) => {
    const tong = c.times.user + c.times.nice + c.times.sys + c.times.idle + c.times.irq
    return { ban: tong - c.times.idle, tong }
  })
  const tong = ds.reduce((a, b) => ({ ban: a.ban + b.ban, tong: a.tong + b.tong }), { ban: 0, tong: 0 })
  return [tong, ...ds]
}

const cacheO: { luc: number, o: string[] } = { luc: 0, o: [] }
async function diaWindows(bayGio: number): Promise<VpsDia[] | null> {
  if (bayGio - cacheDia.luc < 15_000 && cacheDia.ds) return cacheDia.ds
  if (bayGio - cacheO.luc > 60_000) {
    const co: string[] = []
    for (const c of 'CDEFGHIJKLMNOPQRSTUVWXYZ') if (await thu(() => statfs(`${c}:\\`))) co.push(`${c}:\\`)
    cacheO.o = co
    cacheO.luc = bayGio
  }
  const ds: VpsDia[] = []
  for (const o of cacheO.o) {
    const s = await thu(() => statfs(o))
    if (s && s.blocks > 0) ds.push(dungLuongDia(o, '', s))
  }
  cacheDia.luc = bayGio
  cacheDia.ds = ds
  return ds
}

async function mauNode(bayGio: number, laWindows: boolean): Promise<MauVps> {
  const cpuMoi = cpuNode()
  const pt = cpuNodeCu ? phanTramCpu(cpuNodeCu, cpuMoi) : null
  cpuNodeCu = cpuMoi
  if (laWindows) chayPowerShell(bayGio)
  const moi = laWindows ? ps.moi : null
  const cu = laWindows ? ps.cu : null
  const g = moi && cu ? (moi.luc - cu.luc) / 1000 : 0
  const tong = os.totalmem()
  const trong = os.freemem()
  const dichVu: VpsDichVu[] | null = moi
    ? DON_VI_VPS.map((d) => {
      const goc = d.replace(/\.service$/, '')
      const s = moi.dichVu.find(x => x.ten.toLowerCase() === goc || (goc === 'mariadb' && /^(mariadb|mysql)/i.test(x.ten)) || (goc === 'nginx' && /^nginx/i.test(x.ten)))
      return { ten: d, trangThai: s ? s.trangThai : 'khong_cai', tu: null }
    })
    : null
  return {
    t: bayGio,
    heDieuHanh: process.platform,
    may: os.hostname(),
    soNhan: os.cpus().length,
    cpu: pt ? pt[0]! : null,
    cpuMoiNhan: pt ? pt.slice(1) : null,
    tai: null,
    uptime: Math.floor(os.uptime()),
    ram: { tongMb: Math.round(tong / MB), dungMb: Math.round((tong - trong) / MB), sanMb: Math.round(trong / MB), bufCacheMb: null },
    swap: moi?.pagefile ? { tongMb: moi.pagefile.tongMb, dungMb: moi.pagefile.dungMb, vaoTrangS: moi.trangS?.vao ?? null, raTrangS: moi.trangS?.ra ?? null } : null,
    dia: await diaWindows(bayGio).catch(() => null),
    diaIo: moi?.dia ? {
      docBs: cu?.dia && g > 0 ? Math.round(hieuDem(moi.dia.doc, cu.dia.doc) / g) : 0,
      ghiBs: cu?.dia && g > 0 ? Math.round(hieuDem(moi.dia.ghi, cu.dia.ghi) / g) : 0,
      thietBi: [],
    } : null,
    mang: moi && moi.mang.size ? (cu && g > 0 ? tocDoMang(cu.mang, moi.mang, g) : tocDoMang(moi.mang, moi.mang, 1)) : null,
    tienTrinh: moi ? chonTienTrinh(tienTrinhWin(cu, moi)) : null,
    dichVu,
  }
}

/** Một mẫu VPS (gọi mỗi 2 giây từ plugin). Không ném lỗi: phần nào hỏng thì null. */
export async function docMauVps(bayGio = Date.now()): Promise<MauVps> {
  if (process.platform === 'linux') return mauLinux(bayGio)
  return mauNode(bayGio, process.platform === 'win32')
}
