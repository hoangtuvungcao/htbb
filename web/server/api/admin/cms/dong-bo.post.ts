/**
 * "Đồng bộ dữ liệu từ tài liệu game": chạy 3 script sinh dữ liệu (cần repo HTBB trên cùng máy,
 * đặt HTBB_DIR hoặc để ../HTBB), rồi nhập lịch sự kiện mới vào CSDL.
 *
 * Lưu ý: JSON trong shared/data/ được ĐÓNG GÓI lúc build — số liệu cẩm nang/Pokédex mới chỉ hiện
 * sau khi build lại website. Lịch sự kiện (CSDL) có hiệu lực ngay. Chỉ siêu quản trị (xem vaiTro.ts).
 */
import { execFile } from 'node:child_process'
import { existsSync } from 'node:fs'
import { join, resolve } from 'node:path'
import { promisify } from 'node:util'

const chay = promisify(execFile)

export default defineAdminHandler(async (event) => {
  const web = resolve((process.env.HTBB_WEB_SRC_DIR ?? process.env.KPAH_WEB_SRC_DIR) || process.cwd()) // TUONG-THICH-TEN-CU
  const htbb = resolve(web, process.env.HTBB_DIR ?? '..')
  const ketQua: { buoc: string, ok: boolean, log: string }[] = []
  if (!existsSync(join(web, 'scripts/dong-bo-cam-nang.mjs'))) {
    throw apiLoi(409, `Không thấy thư mục mã nguồn website (${web}/scripts). Đặt HTBB_WEB_SRC_DIR trỏ tới thư mục HTBB-Web.`)
  }
  if (!existsSync(join(htbb, 'docs/pokemon'))) {
    throw apiLoi(409, `Không thấy repo game ở ${htbb} (đặt HTBB_DIR).`)
  }
  for (const s of ['sinh-du-lieu-pokemon.mjs', 'sinh-anim-khoi-dau.mjs', 'dong-bo-cam-nang.mjs']) {
    try {
      const { stdout, stderr } = await chay(process.execPath, [join(web, 'scripts', s)], { cwd: web, env: { ...process.env, HTBB_DIR: htbb }, timeout: 180_000, maxBuffer: 4 << 20 })
      ketQua.push({ buoc: s, ok: true, log: `${stdout}${stderr}`.trim().slice(-2000) })
    }
    catch (e: any) {
      ketQua.push({ buoc: s, ok: false, log: String(e?.stderr || e?.message || e).slice(-2000) })
    }
  }
  await ghiNhatKy(event, 'cms_dong_bo', 'du_lieu_game', ketQua.map(k => `${k.buoc}:${k.ok ? 'ok' : 'loi'}`).join(', '))
  return { ok: ketQua.every(k => k.ok), ketQua, canBuild: true }
})
