/**
 * Chỉ số VPS (/admin/he-thong thẻ VPS): bộ đọc /proc, systemctl show, JSON PowerShell (Windows) với chữ MẪU,
 * gộp phút, ngưỡng cảnh báo, quyền /api/admin/chi-so-vps, và bảng tob_chi_so_vps khớp sql/htbb.sql (phần A + B).
 * Chạy: npm test.
 */
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import {
  BANG_VPS, COT_BANG_VPS, DON_VI_VPS, SQL_TAO_BANG_VPS, VongDem, chonTienTrinh, diemTuMau, docDiskstats, docJsonPowerShell,
  docLoadavg, docMeminfo, docMounts, docNetDev, docPasswd, docPidStat, docProcStat, docSystemctlShow, docUid, docUptime, docVmstat,
  donViTuCgroup, dungLuongDia, giaTriDongVps, gopPhut, laDiaNguyen, laMangNoiBo, phanTramCpu, tenThanThien, tienTrinhWin, tocDoMang,
  trangThaiDichVuWin, type MauVps, type VpsTienTrinh,
} from '../server/utils/vpsThuan.ts'
import { NGUONG_VPS, mucDia, mucRamSan, mucSwap, mucTai } from '../shared/utils/vpsNguong.ts'
import { kiemQuyenAdmin, timQuyTacQuyen } from '../shared/utils/quyenAdmin.ts'
import { phanDayDu, phanNangCap } from './htbbSql.ts'

// ---------------------------------------------------------------- /proc mẫu (VPS 2 nhân)
const STAT_1 = `cpu  1000 0 500 8000 100 0 0 0 0 0
cpu0 500 0 250 4000 50 0 0 0 0 0
cpu1 500 0 250 4000 50 0 0 0 0 0
intr 12345
ctxt 999
`
const STAT_2 = `cpu  1300 0 600 8500 100 0 0 0 0 0
cpu0 600 0 300 4400 50 0 0 0 0 0
cpu1 700 0 300 4100 50 0 0 0 0 0
`
const MEMINFO = `MemTotal:        4015360 kB
MemFree:          150000 kB
MemAvailable:     250880 kB
Buffers:           40960 kB
Cached:           512000 kB
SwapCached:        10240 kB
SwapTotal:       2097148 kB
SwapFree:         921600 kB
SReclaimable:      61440 kB
Hugepagesize:       2048 kB
`
const DISKSTATS = `   7       0 loop0 591 0 5092 27 0 0 0 0 0 15 27 0 0 0 0 0 0
 252       0 vda 1000 10 2048 300 500 20 4096 400 0 600 700 0 0 0 0 0 0
 252       1 vda1 900 10 2000 290 490 20 4000 390 0 590 690 0 0 0 0 0 0
 253       0 dm-0 1 0 8 0 0 0 0 0 0 0 0 0 0 0 0 0 0
 259       0 nvme0n1 10 0 100 1 20 0 200 2 0 3 3 0 0 0 0 0 0
`
const NETDEV_1 = `Inter-|   Receive                                                |  Transmit
 face |bytes    packets errs drop fifo frame compressed multicast|bytes    packets errs drop fifo colls carrier compressed
    lo: 5000 50    0    0    0     0          0         0 5000 50    0    0    0     0       0          0
  eth0: 1000000 1000 0 0 0 0 0 0 2000000 1500 0 0 0 0 0 0
`
const NETDEV_2 = `Inter-|   Receive                                                |  Transmit
 face |bytes    packets errs drop fifo frame compressed multicast|bytes    packets errs drop fifo colls carrier compressed
    lo: 7000 70    0    0    0     0          0         0 7000 70    0    0    0     0       0          0
  eth0: 1200000 1200 0 0 0 0 0 0 2100000 1600 0 0 0 0 0 0
`
const MOUNTS = `sysfs /sys sysfs rw,nosuid 0 0
proc /proc proc rw 0 0
/dev/vda1 / ext4 rw,relatime 0 0
tmpfs /run tmpfs rw 0 0
/dev/vdb1 /opt xfs rw 0 0
/dev/vda1 /var/lib/docker ext4 rw 0 0
/dev/loop3 /snap/core/1 squashfs ro 0 0
/dev/vdc1 /mnt/du\\040lieu ext4 rw 0 0
`

test('/proc/stat: % CPU tổng + từng nhân giữa hai lần đọc', () => {
  const a = docProcStat(STAT_1)
  const b = docProcStat(STAT_2)
  assert.equal(a.length, 3)
  // tổng: bận 1500->1900 (+400), tổng 9600->10500 (+900) -> 44.4%
  // nhân 0: +150 / +550 = 27.3%; nhân 1: +250 / +350 = 71.4%
  assert.deepEqual(phanTramCpu(a, b), [44.4, 27.3, 71.4])
  assert.deepEqual(phanTramCpu(b, b), [0, 0, 0])
})

test('/proc/meminfo: RAM dùng = tổng - MemAvailable, cache, swap', () => {
  const m = docMeminfo(MEMINFO)
  assert.deepEqual(m.ram, { tongMb: 3921, dungMb: 3676, sanMb: 245, bufCacheMb: 40 + 500 + 60 })
  assert.deepEqual(m.swap, { tongMb: 2048, dungMb: 1148 })
  // kernel cũ không có MemAvailable
  assert.equal(docMeminfo('MemTotal: 1048576 kB\nMemFree: 102400 kB\nBuffers: 0 kB\nCached: 102400 kB\n').ram.sanMb, 200)
})

test('/proc/vmstat, loadavg, uptime', () => {
  assert.deepEqual(docVmstat('nr_free_pages 1\npswpin 1985480\npswpout 6260926\n'), { vao: 1985480, ra: 6260926 })
  assert.deepEqual(docVmstat('nr_free_pages 1\n'), { vao: 0, ra: 0 })
  assert.deepEqual(docLoadavg('0.52 1.07 3.20 2/311 12345\n'), [0.52, 1.07, 3.2])
  assert.equal(docLoadavg(''), null)
  assert.equal(docUptime('19959.19 128492.10\n'), 19959)
})

test('/proc/diskstats: chỉ đĩa nguyên, byte = sector x 512', () => {
  const d = docDiskstats(DISKSTATS)
  assert.deepEqual([...d.keys()], ['vda', 'nvme0n1'])
  assert.deepEqual(d.get('vda'), { doc: 2048 * 512, ghi: 4096 * 512 })
  for (const t of ['sda', 'vdb', 'xvda', 'nvme1n1', 'mmcblk0']) assert.ok(laDiaNguyen(t), t)
  for (const t of ['sda1', 'vda1', 'nvme0n1p2', 'loop0', 'dm-0', 'zram0', 'mmcblk0p1']) assert.ok(!laDiaNguyen(t), t)
})

test('/proc/net/dev: tốc độ từng card, loopback xếp cuối, bộ đếm tụt = 0', () => {
  const a = docNetDev(NETDEV_1)
  const b = docNetDev(NETDEV_2)
  assert.deepEqual(a.get('eth0'), { vaoB: 1000000, vaoGoi: 1000, raB: 2000000, raGoi: 1500 })
  const r = tocDoMang(a, b, 2)
  assert.deepEqual(r.map(x => x.ten), ['eth0', 'lo'])
  assert.deepEqual(r[0], { ten: 'eth0', ao: false, vaoBs: 100000, raBs: 50000, goiVaoS: 100, goiRaS: 50 })
  assert.equal(r[1]!.ao, true)
  assert.equal(tocDoMang(b, a, 2)[0]!.vaoBs, 0)
  for (const t of ['lo', 'veth1e7af97', 'docker0', 'br-78fcb890bfc8', 'tailscale0', 'wg0', 'vEthernet (WSL)']) assert.ok(laMangNoiBo(t), t)
  for (const t of ['eth0', 'ens3', 'enp62s0', 'Ethernet', 'Wi-Fi']) assert.ok(!laMangNoiBo(t), t)
})

test('/proc/mounts + statfs: hệ tệp thật, bỏ trùng thiết bị, giải mã \\040, % như df', () => {
  const m = docMounts(MOUNTS)
  assert.deepEqual(m.map(x => x.diem), ['/', '/opt', '/mnt/du lieu'])
  // 100 khối 1 MiB, trống 20, còn cho người thường 15 -> dùng 80 / (80 + 15) = 84.2%
  const d = dungLuongDia('/', 'ext4', { bsize: 1048576, blocks: 100, bfree: 20, bavail: 15 })
  assert.deepEqual(d, { diem: '/', loai: 'ext4', tongMb: 100, dungMb: 80, pt: 84.2 })
})

test('/proc/<pid>: stat (comm có dấu cách và ngoặc), status Uid, cgroup, passwd', () => {
  const s = docPidStat('1234 (java (htbb) x) S 1 1234 1234 0 -1 4194560 100 0 0 0 1500 250 0 0 20 0 45 0 98765 3000000000 262144 18446744073709551615 1 1 0 0 0')
  assert.deepEqual(s, { comm: 'java (htbb) x', cpu: 1750, rssTrang: 262144, batDau: 98765 })
  assert.equal(docPidStat('rác'), null)
  assert.equal(docUid('Name:\tjava\nUid:\t998\t998\t998\t998\nGid:\t998\n'), 998)
  assert.equal(donViTuCgroup('0::/system.slice/htbb-game.service\n'), 'htbb-game.service')
  assert.equal(donViTuCgroup('12:pids:/system.slice/mariadb.service\n1:name=systemd:/system.slice/mariadb.service\n'), 'mariadb.service')
  assert.equal(donViTuCgroup('0::/user.slice/user-1000.slice/session-3.scope\n'), '')
  assert.equal(donViTuCgroup('0::/user.slice/user-1000.slice/user@1000.service/app.slice/app-x.scope\n'), '')
  assert.equal(donViTuCgroup('0::/system.slice/system-getty.slice/getty@tty1.service\n'), 'getty@tty1.service')
  assert.equal(docPasswd('root:x:0:0:root:/root:/bin/bash\nhtbb:x:998:998::/opt/htbb:/usr/sbin/nologin\n').get(998), 'htbb')
})

test('tên thân thiện: đơn vị systemd trước, rồi dòng lệnh', () => {
  assert.equal(tenThanThien('java', '', 'htbb-game.service'), 'Máy chủ game')
  assert.equal(tenThanThien('java', '', 'htbb-staging-game.service'), 'Game staging')
  assert.equal(tenThanThien('mariadbd', '', 'mariadb.service'), 'MariaDB')
  assert.equal(tenThanThien('java', '/usr/bin/java -Xmx512m -cp /var/lib/htbb-hlv-ai/cls DichVuHlvAi 127.0.0.1 19129'), 'Bot AI (HLV AI)')
  assert.equal(tenThanThien('java', '/usr/bin/java -jar /opt/htbb-staging/server/loginServer/htbb-login.jar'), 'Đăng nhập staging')
  assert.equal(tenThanThien('java', '/usr/bin/java -Xmx1536m -jar /opt/htbb/server/htbb.jar'), 'Máy chủ game')
  assert.equal(tenThanThien('node', '/usr/bin/node /opt/htbb/web/.output/server/index.mjs'), 'Web Nuxt')
  assert.equal(tenThanThien('java.exe', 'C:\\htbb\\server\\java -jar htbb.jar'), 'Máy chủ game')
  assert.equal(tenThanThien('node.exe', 'node C:\\htbb\\web\\.output\\server\\index.mjs'), 'Web Nuxt')
  assert.equal(tenThanThien('nginx', 'nginx: worker process'), 'nginx')
  assert.equal(tenThanThien('bash', '-bash'), '')
})

test('chọn tiến trình: luôn giữ dịch vụ biết tên (kể cả nginx), thêm top RAM / CPU, bỏ RSS 0', () => {
  const p = (pid: number, ten: string, lenh: string, rssMb: number, cpu = 0): VpsTienTrinh => ({ pid, ten, lenh, donVi: '', nguoi: 'x', rssMb, cpu })
  const ds = [
    p(1, 'nginx', 'nginx', 3), p(2, 'MariaDB', 'mariadbd', 300), p(3, 'Máy chủ game', 'java', 1200),
    ...Array.from({ length: 30 }, (_, i) => p(100 + i, 'rác', 'rac', 50 + i)),
    p(500, 'gzip', 'gzip', 1, 95), p(600, 'kworker', 'kworker', 0, 10),
  ]
  const r = chonTienTrinh(ds, 25, 5, 3)
  const pid = r.map(x => x.pid)
  for (const k of [1, 2, 3, 500]) assert.ok(pid.includes(k), `thiếu ${k}`)
  assert.ok(!pid.includes(600))
  assert.ok(r.length <= 25)
  assert.deepEqual(r.map(x => x.rssMb), [...r.map(x => x.rssMb)].sort((a, b) => b - a))
})

test('systemctl show: trạng thái, chưa cài, mốc "từ" theo monotonic + uptime', () => {
  const chu = `MainPID=0
Id=htbb-game.service
LoadState=loaded
ActiveState=active
ActiveEnterTimestampMonotonic=1000000000
StateChangeTimestampMonotonic=1000000000

Id=htbb-hlv-ai.service
LoadState=loaded
ActiveState=failed
ActiveEnterTimestampMonotonic=500000000
StateChangeTimestampMonotonic=1500000000

Id=nginx.service
LoadState=not-found
ActiveState=inactive
ActiveEnterTimestampMonotonic=0
StateChangeTimestampMonotonic=0
`
  const bayGio = 10_000_000
  // uptime 2000 giây: vào active ở giây 1000 -> 1000 giây trước
  assert.deepEqual(docSystemctlShow(chu, 2000, bayGio), [
    { ten: 'htbb-game.service', trangThai: 'active', tu: bayGio - 1000_000 },
    { ten: 'htbb-hlv-ai.service', trangThai: 'failed', tu: bayGio - 500_000 },
    { ten: 'nginx.service', trangThai: 'khong_cai', tu: null },
  ])
  assert.equal(docSystemctlShow(chu, null, bayGio)[0]!.tu, null)
  assert.ok(DON_VI_VPS.every(d => /^[\w-]+\.service$/.test(d)), 'đơn vị cố định, không ký tự lạ')
})

// ---------------------------------------------------------------- Windows: JSON của KICH_BAN_PS (PowerShell 5.1)
const PS_1 = '\uFEFF{"net":[{"Name":"Ethernet","ReceivedBytes":1000000,"SentBytes":500000,"ReceivedUnicastPackets":1000,"SentUnicastPackets":800}],'
  + '"disk":[{"DiskReadBytesPersec":"10000000","DiskWriteBytesPersec":"20000000"}],'
  + '"pf":{"Name":"C:\\\\pagefile.sys","AllocatedBaseSize":4096,"CurrentUsage":512},'
  + '"mem":[{"PagesInputPersec":3,"PagesOutputPersec":1}],'
  + '"proc":[{"Id":4321,"ProcessName":"java","WS":1073741824,"CPU":100.5},{"Id":77,"ProcessName":"node","WS":209715200,"CPU":20}],'
  + '"cmd":[{"ProcessId":4321,"CommandLine":"java -jar C:\\\\htbb\\\\server\\\\htbb.jar"},{"ProcessId":77,"CommandLine":"node C:\\\\htbb\\\\web\\\\.output\\\\server\\\\index.mjs"}],'
  + '"svc":[{"Name":"htbb-game","Status":"Running"},{"Name":"MariaDB","Status":4},{"Name":"nginx","Status":"Stopped"}]}'
const PS_2 = '{"net":{"Name":"Ethernet","ReceivedBytes":1500000,"SentBytes":600000,"ReceivedUnicastPackets":1500,"SentUnicastPackets":900},'
  + '"disk":[],"pf":[],"mem":[],"proc":[{"Id":4321,"ProcessName":"java","WS":1073741824,"CPU":103}],'
  + '"cmd":{"ProcessId":4321,"CommandLine":"java -jar C:\\\\htbb\\\\server\\\\htbb.jar"},"svc":null}'

test('Windows: đọc JSON PowerShell (mảng / object đơn / null / BOM), tốc độ, CPU tiến trình', () => {
  assert.equal(docJsonPowerShell('không phải json', 0), null)
  const a = docJsonPowerShell(PS_1, 1000)!
  assert.deepEqual(a.mang.get('Ethernet'), { vaoB: 1000000, raB: 500000, vaoGoi: 1000, raGoi: 800 })
  assert.deepEqual(a.dia, { doc: 10000000, ghi: 20000000 })
  assert.deepEqual(a.pagefile, { tongMb: 4096, dungMb: 512 })
  assert.deepEqual(a.trangS, { vao: 3, ra: 1 })
  assert.deepEqual(a.dichVu, [{ ten: 'htbb-game', trangThai: 'active' }, { ten: 'MariaDB', trangThai: 'active' }, { ten: 'nginx', trangThai: 'inactive' }])
  const b = docJsonPowerShell(PS_2, 6000)!
  assert.equal(b.dia, null)
  assert.equal(b.pagefile, null)
  assert.deepEqual(b.dichVu, [])
  assert.deepEqual(tocDoMang(a.mang, b.mang, 5)[0], { ten: 'Ethernet', ao: false, vaoBs: 100000, raBs: 20000, goiVaoS: 100, goiRaS: 20 })
  const tt = tienTrinhWin(a, b)
  assert.deepEqual(tt[0], { pid: 4321, ten: 'Máy chủ game', lenh: 'java', donVi: '', nguoi: null, rssMb: 1024, cpu: 50 })
  const dau = tienTrinhWin(null, a)
  assert.equal(dau[1]!.ten, 'Web Nuxt')
  assert.equal(dau[1]!.cpu, 0)
  assert.equal(trangThaiDichVuWin('StartPending'), 'activating')
  assert.equal(trangThaiDichVuWin(undefined), 'unknown')
})

// ---------------------------------------------------------------- gộp phút, vòng đệm
function mau(t: number, o: Partial<MauVps> = {}): MauVps {
  return {
    t, heDieuHanh: 'linux', may: 'vps', soNhan: 2, cpu: 10, cpuMoiNhan: [10, 10], tai: [1, 1, 1], uptime: 100,
    ram: { tongMb: 4000, dungMb: 3000, sanMb: 1000, bufCacheMb: 500 },
    swap: { tongMb: 2048, dungMb: 100, vaoTrangS: 0, raTrangS: 2 },
    dia: [{ diem: '/', loai: 'ext4', tongMb: 100, dungMb: 50, pt: 50 }],
    diaIo: { docBs: 1000, ghiBs: 2000, thietBi: [] },
    mang: [{ ten: 'eth0', ao: false, vaoBs: 100, raBs: 200, goiVaoS: 1, goiRaS: 2 }, { ten: 'lo', ao: true, vaoBs: 99999, raBs: 99999, goiVaoS: 9, goiRaS: 9 }],
    tienTrinh: [], dichVu: [], ...o,
  }
}

test('gộp phút: trung bình / lớn nhất / nhỏ nhất, byte trong phút, bỏ loopback; điểm biểu đồ', () => {
  const d = gopPhut(60, [mau(0), mau(2000, { cpu: 30, ram: { tongMb: 4000, dungMb: 3500, sanMb: 400, bufCacheMb: 1 }, dia: [{ diem: '/', loai: 'ext4', tongMb: 100, dungMb: 90, pt: 90 }] })], 2)!
  assert.equal(d.soMau, 2)
  assert.equal(d.cpuTb, 20)
  assert.equal(d.cpuMax, 30)
  assert.equal(d.ramDungMb, 3500)
  assert.equal(d.ramSanMinMb, 400)
  assert.equal(d.mangVaoB, 400) // (100 + 100) x 2 giây, không tính lo
  assert.equal(d.diaGhiB, 8000)
  assert.equal(d.diaMaxPt, 90)
  assert.equal(giaTriDongVps(d).length, COT_BANG_VPS.length - 2)
  assert.equal(gopPhut(0, [], 2), null)
  const w = gopPhut(0, [mau(0, { tai: null, swap: null, diaIo: null, mang: null, dia: null })], 2)!
  assert.equal(w.tai1, null)
  assert.equal(w.swapDungMb, null)
  assert.equal(w.mangVaoB, null)
  assert.deepEqual(diemTuMau(mau(5)), { t: 5, cpu: 10, tai1: 1, ramDungMb: 3000, ramSanMb: 1000, swapDungMb: 100, docBs: 1000, ghiBs: 2000, vaoBs: 100, raBs: 200 })
  const v = new VongDem<number>(3)
  for (let i = 0; i < 5; i++) v.them(i)
  assert.deepEqual(v.tatCa(), [2, 3, 4])
})

test('ngưỡng cảnh báo: RAM < 300 MB, swap > 50%, đĩa > 85%, tải > nhân x 1,5', () => {
  assert.equal(NGUONG_VPS.ramSanMb, 300)
  assert.equal(mucRamSan(299), 'bad')
  assert.equal(mucRamSan(300), '')
  assert.equal(mucSwap(1025, 2048), 'warn')
  assert.equal(mucSwap(1024, 2048), '')
  assert.equal(mucSwap(0, 0), '')
  assert.equal(mucDia(85), '')
  assert.equal(mucDia(85.1), 'warn')
  assert.equal(mucDia(96), 'bad')
  assert.equal(mucTai(3.01, 2), 'warn')
  assert.equal(mucTai(3, 2), '')
})

test('quyền: /api/admin/chi-so-vps chỉ Siêu quản trị + Chỉ xem đọc, không ai ghi', () => {
  for (const p of ['/api/admin/chi-so-vps', '/api/admin/chi-so-vps/lich-su']) {
    assert.equal(timQuyTacQuyen(p)?.mau, '/api/admin/chi-so-vps/**')
    assert.equal(kiemQuyenAdmin(p, 'GET', 'sieu_quan_tri'), true)
    assert.equal(kiemQuyenAdmin(p, 'GET', 'xem'), true)
    assert.equal(kiemQuyenAdmin(p, 'GET', 'ho_tro'), false)
    assert.equal(kiemQuyenAdmin(p, 'GET', 'bien_tap'), false)
    for (const v of ['xem', 'ho_tro', 'bien_tap'] as const) assert.equal(kiemQuyenAdmin(p, 'POST', v), false)
  }
  for (const f of ['index.get.ts', 'lich-su.get.ts']) {
    const src = readFileSync(new URL(`../server/api/admin/chi-so-vps/${f}`, import.meta.url), 'utf8')
    assert.match(src, /export default defineAdminHandler\(/)
    assert.doesNotMatch(src, /child_process|execFile|spawn|exec\(/, `${f} không được chạy lệnh`)
  }
})

test('bảng tob_chi_so_vps: web tạo đúng như sql/htbb.sql phần B, phần A cùng cột', () => {
  const b = phanNangCap()
  assert.ok(b.includes('-- >>> them_chi_so_vps.sql'))
  assert.ok(b.includes(`${SQL_TAO_BANG_VPS};`), 'phần B phải chứa nguyên văn SQL_TAO_BANG_VPS')
  const a = phanDayDu()
  const i = a.indexOf(`CREATE TABLE IF NOT EXISTS \`${BANG_VPS}\` (`)
  assert.ok(i > a.indexOf('USE `htbb_db`;'), 'phần A có bảng trong htbb_db')
  const ddl = a.slice(i, a.indexOf(';', i))
  const cot = [...ddl.matchAll(/^\s+`(\w+)` /gm)].map(m => m[1])
  assert.deepEqual(cot, COT_BANG_VPS.map(c => c[0]))
  assert.match(ddl, /PRIMARY KEY \(`may`,`luc`\)/)
})
