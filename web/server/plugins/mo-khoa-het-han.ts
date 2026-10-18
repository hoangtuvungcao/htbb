/**
 * Mỗi 5 phút: mở khoá tài khoản có hạn khoá đã qua (web_khoa_tai_khoan.het_han <= bây giờ).
 * Hai CSDL tách nhau (htbb_db / account) nên làm hai bước, không dùng UPDATE...JOIN.
 */
export default defineNitroPlugin(() => {
  if (import.meta.prerender) return
  const chay = async () => {
    try {
      if (!(await coBang(dbGame(), 'web_khoa_tai_khoan'))) return
      const het = await q<{ id: number, username: string }>(dbGame(),
        "SELECT id, username FROM web_khoa_tai_khoan WHERE loai = 'khoa' AND mo_luc IS NULL AND het_han IS NOT NULL AND het_han <= NOW() LIMIT 100")
      for (const r of het) {
        // Còn lệnh khoá khác (vĩnh viễn hoặc chưa hết hạn) thì không mở.
        const con = await qv(dbGame(),
          "SELECT id FROM web_khoa_tai_khoan WHERE username = ? AND loai = 'khoa' AND mo_luc IS NULL AND id <> ? AND (het_han IS NULL OR het_han > NOW()) LIMIT 1", [r.username, r.id])
        if (!con) await exec(dbAccount(), 'UPDATE team_user SET ban = 0 WHERE username = ?', [r.username])
        await exec(dbGame(), 'UPDATE web_khoa_tai_khoan SET mo_luc = NOW() WHERE id = ?', [r.id])
        console.log(`[htbb] Hết hạn khoá: mở ${r.username}`)
      }
    }
    catch { /* CSDL chưa sẵn sàng: thử lại lần sau */ }
  }
  setTimeout(chay, 20_000)
  setInterval(chay, 5 * 60_000).unref?.()
})
