/**
 * Thao tác trên MỘT tài khoản ở trang Tài khoản.
 * Body: { viec, ...tham số } với viec ∈
 *   khoa | mo_khoa | kich_hoat | huy_kich_hoat | doi_mat_khau | xoa_tai_khoan | reset_trang
 * Trả về { loai: 'success' | 'warning', message, daXoa? }.
 */
export default defineAdminHandler(async (event, admin) => {
  const id = int(getRouterParam(event, 'id'))
  const body = await readBody<Record<string, unknown>>(event).catch(() => ({} as Record<string, unknown>))
  const viec = str(body?.viec)

  const tk = id > 0
    ? await q1<{ id: number, username: string, ban: number }>(dbAccount(),
        'SELECT id, username, ban FROM team_user WHERE id = ? LIMIT 1', [id])
    : null
  if (!tk) throw apiLoi(404, `Không tìm thấy tài khoản #${id}.`)

  const ten = String(tk.username)
  const laAdmin = laTaiKhoanAdmin(ten)
  const laChinhMinh = ten.toLowerCase() === admin.username.toLowerCase()

  switch (viec) {
    case 'khoa':
    case 'mo_khoa': {
      // Không cho khoá tài khoản admin — kể cả của chính mình. Đây là cái chốt duy nhất
      // ngăn việc tự nhốt mình ra ngoài, vì mở lại thì phải sửa thẳng trong database.
      if (laAdmin) {
        throw apiLoi(403, 'Không khoá được tài khoản quản trị. Gỡ tên khỏi ADMIN_ACCOUNTS trước nếu thật sự cần.')
      }
      const banMoi = viec === 'khoa' ? 1 : 0
      await exec(dbAccount(), 'UPDATE team_user SET ban = ? WHERE id = ?', [banMoi, id])
      await ghiNhatKy(event, viec === 'khoa' ? 'khoa_tai_khoan' : 'mo_tai_khoan', ten, `ban: ${Number(tk.ban)} -> ${banMoi}`)
      return {
        loai: 'success',
        message: viec === 'khoa'
          ? `Đã khoá tài khoản ${ten}. Người đang online vẫn chơi tiếp tới khi thoát ra.`
          : `Đã mở khoá tài khoản ${ten}.`,
      }
    }

    case 'kich_hoat': {
      if (!(await activateAccount(id, ten))) {
        throw apiLoi(422, `Không kích hoạt được tài khoản ${ten}.`)
      }
      await ghiNhatKy(event, 'kich_hoat', ten, 'cộng 30 ngày trong 5h_active')
      return { loai: 'success', message: `Đã cộng 30 ngày kích hoạt cho tài khoản ${ten}.` }
    }

    case 'huy_kich_hoat': {
      try {
        // Xoá theo cả hai cột: một số dòng cũ chỉ có userID đúng, một số chỉ có username
        // đúng (bảng này không có ràng buộc nào cả).
        const del = await exec(dbGame(), 'DELETE FROM `5h_active` WHERE username = ? OR userID = ?', [ten, id])
        await ghiNhatKy(event, 'huy_kich_hoat', ten, `xoá ${del.affectedRows} dòng khỏi 5h_active`)
        return { loai: 'success', message: `Đã huỷ kích hoạt ${ten} (${del.affectedRows} dòng).` }
      } catch (e: any) {
        throw apiLoi(422, `Không huỷ được: ${e?.message ?? e}`)
      }
    }

    case 'doi_mat_khau': {
      if (laAdmin && !laChinhMinh) {
        throw apiLoi(403, 'Không đổi được mật khẩu của một tài khoản quản trị khác.')
      }
      const mk = typeof body?.matkhau === 'string' ? body.matkhau : ''
      const mk2 = typeof body?.matkhau2 === 'string' ? body.matkhau2 : ''
      if (mk !== mk2) throw apiLoi(400, 'Hai lần nhập mật khẩu không giống nhau.')
      if (!validPassword(mk)) throw apiLoi(400, `Mật khẩu phải dài ${PASSWORD_MIN}–${PASSWORD_MAX} ký tự.`)
      try {
        // PASSWORD() để khớp đúng cách login server băm mật khẩu.
        await exec(dbAccount(), 'UPDATE team_user SET `password` = PASSWORD(?) WHERE id = ?', [mk, id])
      } catch (e: any) {
        throw apiLoi(422, 'Không đổi được mật khẩu. Nếu máy chủ chạy MySQL 8 thì hàm PASSWORD() đã bị gỡ '
          + `và cả login server lẫn trang này đều phải đổi cách băm. Lỗi: ${e?.message ?? e}`)
      }
      await ghiNhatKy(event, 'doi_mat_khau', ten, 'đặt mật khẩu mới (không ghi lại nội dung)')
      return { loai: 'success', message: `Đã đổi mật khẩu cho ${ten}.` }
    }

    case 'xoa_tai_khoan': {
      // XOÁ VĨNH VIỄN — không hoàn tác được. Chặn tài khoản quản trị giống hệt "khoá",
      // cộng thêm gõ đúng tên để xác nhận (giao diện chỉ bật nút, kiểm tra thật ở đây —
      // phòng khi ai đó POST thẳng không qua giao diện).
      if (laAdmin) {
        throw apiLoi(403, 'Không xoá được tài khoản quản trị. Gỡ tên khỏi ADMIN_ACCOUNTS trước nếu thật sự cần.')
      }
      if (str(body?.xac_nhan_ten) !== ten) {
        throw apiLoi(400, `Gõ đúng tên tài khoản "${ten}" để xác nhận xoá.`)
      }
      // Xoá từng nhân vật trước (đi qua bảng lệnh vì có thể đang online). Còn nhân vật nào
      // chưa xong thì tài khoản CHƯA bị xoá; bấm lại sẽ tiếp tục (mọi bước đều idempotent).
      const kq = await tkXoaHetNhanVat(id, admin.username)
      if (kq.soConLai > 0) {
        await ghiNhatKy(event, 'xoa_tai_khoan_tam_dung', ten,
          `còn ${kq.soConLai}/${kq.soBanDau} nhân vật chưa xoá xong: ${kq.chiTietLenh.join('; ')}`)
        return {
          loai: 'warning',
          message: `Còn ${kq.soConLai} nhân vật chưa xoá xong (${kq.chiTietLenh.join('; ')}) — có thể server game đang tắt hoặc chậm. `
            + 'Tài khoản CHƯA bị xoá; bấm "Xoá vĩnh viễn" lại để tiếp tục khi tất cả nhân vật đã xoá xong.',
        }
      }
      // Toàn bộ nhân vật đã xoá xong -> xoá 5h_active (giống huy_kich_hoat) rồi xoá thật tài
      // khoản. team_user ở DB riêng (account) mà server game không dùng tới ngoài lúc đăng
      // nhập, nên xoá thẳng ở đây an toàn — giống cách "khoá" đã làm.
      await exec(dbGame(), 'DELETE FROM `5h_active` WHERE username = ? OR userID = ?', [ten, id])
      await exec(dbAccount(), 'DELETE FROM team_user WHERE id = ?', [id])
      await ghiNhatKy(event, 'xoa_tai_khoan', ten, `đã xoá vĩnh viễn tài khoản (${kq.soBanDau} nhân vật đã xoá cùng lúc)`)
      return {
        loai: 'success',
        daXoa: true,
        message: `Đã xoá vĩnh viễn tài khoản ${ten} và toàn bộ ${kq.soBanDau} nhân vật của nó.`,
      }
    }

    case 'reset_trang': {
      // RESET VỀ TRẮNG: giống hệt xoa_tai_khoan (cùng đi qua bảng lệnh vì nhân vật có thể
      // đang online) nhưng KHÔNG đụng tới 5h_active/team_user — tài khoản đăng nhập giữ
      // nguyên, chỉ mất hết nhân vật, để đăng nhập lại là như vừa đăng ký.
      if (laAdmin) {
        throw apiLoi(403, 'Không reset được tài khoản quản trị. Gỡ tên khỏi ADMIN_ACCOUNTS trước nếu thật sự cần.')
      }
      if (str(body?.xac_nhan_ten) !== ten) {
        throw apiLoi(400, `Gõ đúng tên tài khoản "${ten}" để xác nhận reset.`)
      }
      const soNV = Number(await qv(dbGame(), 'SELECT COUNT(*) FROM tob_char WHERE userId = ?', [id]) ?? 0)
      if (soNV === 0) {
        return { loai: 'success', message: `Tài khoản ${ten} đã trắng sẵn, chưa có nhân vật nào.` }
      }
      const kq = await tkXoaHetNhanVat(id, admin.username)
      if (kq.soConLai > 0) {
        await ghiNhatKy(event, 'reset_tai_khoan_tam_dung', ten,
          `còn ${kq.soConLai}/${kq.soBanDau} nhân vật chưa xoá xong: ${kq.chiTietLenh.join('; ')}`)
        return {
          loai: 'warning',
          message: `Còn ${kq.soConLai} nhân vật chưa xoá xong (${kq.chiTietLenh.join('; ')}) — có thể server game đang tắt hoặc chậm. `
            + 'Tài khoản CHƯA reset xong; bấm "Reset về trắng" lại để tiếp tục khi tất cả nhân vật đã xoá xong.',
        }
      }
      await ghiNhatKy(event, 'reset_tai_khoan', ten, `đã xoá ${kq.soBanDau} nhân vật, đưa tài khoản về trắng`)
      return {
        loai: 'success',
        message: `Đã reset tài khoản ${ten} về trắng (đã xoá ${kq.soBanDau} nhân vật). `
          + 'Tài khoản vẫn đăng nhập được bình thường và có thể tạo nhân vật mới ngay.',
      }
    }

    default:
      throw apiLoi(400, 'Không hiểu thao tác.')
  }
})
