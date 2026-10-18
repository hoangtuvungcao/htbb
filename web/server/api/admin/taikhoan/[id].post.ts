/**
 * Thao tác trên MỘT tài khoản ở trang Tài khoản.
 * Body: { viec, ...tham số } với viec ∈
 *   khoa | mo_khoa | kich_hoat | huy_kich_hoat | doi_mat_khau | xoa_tai_khoan | reset_trang
 * Trả về { loai: 'success' | 'warning', message, daXoa? }.
 */
export default defineAdminHandler(async (event, admin) => {
  const id = int(getRouterParam(event, 'id'))
  const body = await bodyObj(event)
  const viec = str(body?.viec)
  if (id <= 0 || !Number.isSafeInteger(id)) throw apiLoi(404, 'Không tìm thấy tài khoản.')

  const tk = id > 0
    ? await q1<{ id: number, username: string, ban: number }>(dbAccount(),
        'SELECT id, username, ban FROM team_user WHERE id = ? LIMIT 1', [id])
    : null
  if (!tk) throw apiLoi(404, `Không tìm thấy tài khoản #${id}.`)

  const ten = String(tk.username)
  const laAdmin = laTaiKhoanAdmin(ten)
  const laChinhMinh = ten.toLowerCase() === admin.username.toLowerCase()

  // Xoá / reset (xoá mọi nhân vật) là thao tác không hoàn tác: chỉ siêu quản trị — cùng mức với
  // /api/admin/taikhoan/xoa-nhieu và /api/admin/nhanvat/*/xoa trong ma trận quyền.
  if ((viec === 'xoa_tai_khoan' || viec === 'reset_trang') && admin.vaiTro !== 'sieu_quan_tri') {
    throw apiLoi(403, 'Chỉ siêu quản trị được xoá / reset tài khoản.')
  }

  switch (viec) {
    case 'khoa':
    case 'mo_khoa': {
      // Không cho khoá tài khoản admin — kể cả của chính mình. Đây là cái chốt duy nhất
      // ngăn việc tự nhốt mình ra ngoài, vì mở lại thì phải sửa thẳng trong database.
      if (laAdmin || (viec === 'khoa' && (laChinhMinh || await ncLaQuanTri(ten)))) {
        throw apiLoi(403, 'Không khoá được tài khoản quản trị. Gỡ tên khỏi ADMIN_ACCOUNTS / thu vai trò trước nếu thật sự cần.')
      }
      const banMoi = viec === 'khoa' ? 1 : 0
      if (Number(tk.ban) === banMoi) {
        return { loai: 'success', message: banMoi ? `Tài khoản ${ten} đã bị khoá sẵn.` : `Tài khoản ${ten} không bị khoá.` }
      }
      const lyDo = chuoi(body?.ly_do, 'Lý do', 300) || 'Khoá từ trang Tài khoản'
      await exec(dbAccount(), 'UPDATE team_user SET ban = ? WHERE id = ?', [banMoi, id])
      // Đồng bộ lịch sử khoá với Trung tâm người chơi (web_khoa_tai_khoan): khoá ở đây = vĩnh viễn.
      if (await coBangCms('web_khoa_tai_khoan')) {
        await exec(dbGame(), "UPDATE web_khoa_tai_khoan SET mo_luc = NOW() WHERE username = ? AND loai = 'khoa' AND mo_luc IS NULL", [ten])
        if (banMoi) {
          await exec(dbGame(), "INSERT INTO web_khoa_tai_khoan (username, loai, ly_do, het_han, admin_user, tao_luc) VALUES (?, 'khoa', ?, NULL, ?, NOW())", [ten, lyDo, admin.username])
        }
      }
      await ghiThayDoi(event, viec === 'khoa' ? 'khoa_tai_khoan' : 'mo_tai_khoan', `tk:${ten}`, { ban: Number(tk.ban) }, { ban: banMoi, ...(banMoi ? { lyDo, hetHan: 'vĩnh viễn' } : {}) })
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
      await ghiThayDoi(event, 'kich_hoat', `tk:${ten}`, null, { congNgay: 30, bang: '5h_active' })
      return { loai: 'success', message: `Đã cộng 30 ngày kích hoạt cho tài khoản ${ten}.` }
    }

    case 'huy_kich_hoat': {
      try {
        // Xoá theo cả hai cột: một số dòng cũ chỉ có userID đúng, một số chỉ có username
        // đúng (bảng này không có ràng buộc nào cả).
        const truoc = await q(dbGame(), 'SELECT * FROM `5h_active` WHERE username = ? OR userID = ? LIMIT 20', [ten, id])
        const del = await exec(dbGame(), 'DELETE FROM `5h_active` WHERE username = ? OR userID = ?', [ten, id])
        await ghiThayDoi(event, 'huy_kich_hoat', `tk:${ten}`, truoc, { daXoa: del.affectedRows })
        return { loai: 'success', message: `Đã huỷ kích hoạt ${ten} (${del.affectedRows} dòng).` }
      } catch (e: any) {
        console.error('[taikhoan] huy_kich_hoat', e?.code ?? e)
        throw apiLoi(422, 'Không huỷ được kích hoạt (lỗi cơ sở dữ liệu — xem log máy chủ web).')
      }
    }

    case 'doi_mat_khau': {
      if (!laChinhMinh && (laAdmin || await ncLaQuanTri(ten))) {
        throw apiLoi(403, 'Không đổi được mật khẩu của một tài khoản quản trị khác.')
      }
      const mk = typeof body?.matkhau === 'string' ? body.matkhau.slice(0, 200) : ''
      const mk2 = typeof body?.matkhau2 === 'string' ? body.matkhau2.slice(0, 200) : ''
      if (mk !== mk2) throw apiLoi(400, 'Hai lần nhập mật khẩu không giống nhau.')
      if (!validPassword(mk)) throw apiLoi(400, `Mật khẩu phải dài ${PASSWORD_MIN}–${PASSWORD_MAX} ký tự.`)
      try {
        // PASSWORD() để khớp đúng cách login server băm mật khẩu.
        await exec(dbAccount(), 'UPDATE team_user SET `password` = PASSWORD(?) WHERE id = ?', [mk, id])
      } catch (e: any) {
        console.error('[taikhoan] doi_mat_khau', e?.code ?? e)
        throw apiLoi(422, 'Không đổi được mật khẩu. Nếu máy chủ chạy MySQL 8 thì hàm PASSWORD() đã bị gỡ '
          + 'và cả login server lẫn trang này đều phải đổi cách băm (chi tiết ở log máy chủ web).')
      }
      // Không ghi mật khẩu/hash vào nhật ký — chỉ ghi là đã đổi.
      await ghiThayDoi(event, 'doi_mat_khau', `tk:${ten}`, { matKhau: '(cũ — không lưu)' }, { matKhau: '(mới — không lưu)' })
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
        await ghiThayDoi(event, 'xoa_tai_khoan_tam_dung', `tk:${ten}`, { soNhanVat: kq.soBanDau }, { conLai: kq.soConLai, lenh: kq.chiTietLenh })
        return {
          loai: 'warning',
          message: `Còn ${kq.soConLai} Pokémon chưa xoá xong (${kq.chiTietLenh.join('; ')}) — có thể server game đang tắt hoặc chậm. `
            + 'Tài khoản CHƯA bị xoá; bấm "Xoá vĩnh viễn" lại để tiếp tục khi tất cả Pokémon đã xoá xong.',
        }
      }
      // Toàn bộ nhân vật đã xoá xong -> xoá 5h_active (giống huy_kich_hoat) rồi xoá thật tài
      // khoản. team_user ở DB riêng (account) mà server game không dùng tới ngoài lúc đăng
      // nhập, nên xoá thẳng ở đây an toàn — giống cách "khoá" đã làm.
      await exec(dbGame(), 'DELETE FROM `5h_active` WHERE username = ? OR userID = ?', [ten, id])
      await exec(dbAccount(), 'DELETE FROM team_user WHERE id = ?', [id])
      await ghiThayDoi(event, 'xoa_tai_khoan', `tk:${ten}`, { id, username: ten, ban: Number(tk.ban), soNhanVat: kq.soBanDau, lenh: kq.chiTietLenh }, null)
      return {
        loai: 'success',
        daXoa: true,
        message: `Đã xoá vĩnh viễn tài khoản ${ten} và toàn bộ ${kq.soBanDau} Pokémon của nó.`,
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
        return { loai: 'success', message: `Tài khoản ${ten} đã trắng sẵn, chưa có Pokémon nào.` }
      }
      const kq = await tkXoaHetNhanVat(id, admin.username)
      if (kq.soConLai > 0) {
        await ghiThayDoi(event, 'reset_tai_khoan_tam_dung', `tk:${ten}`, { soNhanVat: kq.soBanDau }, { conLai: kq.soConLai, lenh: kq.chiTietLenh })
        return {
          loai: 'warning',
          message: `Còn ${kq.soConLai} Pokémon chưa xoá xong (${kq.chiTietLenh.join('; ')}) — có thể server game đang tắt hoặc chậm. `
            + 'Tài khoản CHƯA reset xong; bấm "Reset về trắng" lại để tiếp tục khi tất cả Pokémon đã xoá xong.',
        }
      }
      await ghiThayDoi(event, 'reset_tai_khoan', `tk:${ten}`, { soNhanVat: kq.soBanDau }, { soNhanVat: 0, lenh: kq.chiTietLenh })
      return {
        loai: 'success',
        message: `Đã reset tài khoản ${ten} về trắng (đã xoá ${kq.soBanDau} Pokémon). `
          + 'Tài khoản vẫn đăng nhập được bình thường và có thể tạo Pokémon mới ngay.',
      }
    }

    default:
      throw apiLoi(400, 'Không hiểu thao tác.')
  }
})
