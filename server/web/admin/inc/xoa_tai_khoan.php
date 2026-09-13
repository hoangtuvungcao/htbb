<?php
/** Xoa nhieu tai khoan theo cung quy trinh xoa nhan vat cua server game. */
function idsTaiKhoanDaChon($raw): array
{
    if (!is_array($raw) || count($raw) > 50) {
        throw new InvalidArgumentException('Chọn từ 1 đến 50 tài khoản mỗi lần.');
    }
    $ids = [];
    foreach ($raw as $id) {
        if (!is_scalar($id) || !ctype_digit((string)$id) || (int)$id <= 0
            || (string)(int)$id !== (string)$id) {
            throw new InvalidArgumentException('Danh sách tài khoản không hợp lệ.');
        }
        $ids[(int)$id] = (int)$id;
    }
    if (!$ids) {
        throw new InvalidArgumentException('Chưa chọn tài khoản nào.');
    }
    return array_values($ids);
}

function xoaNhieuTaiKhoan(PDO $account, PDO $game, array $ids, string $admin): array
{
    $kq = ['xoa' => [], 'cho' => [], 'bo_qua' => [], 'loi' => []];
    foreach ($ids as $id) {
        $ten = '#' . $id;
        try {
            $st = $account->prepare('SELECT id, username FROM team_user WHERE id = ? LIMIT 1');
            $st->execute([$id]);
            $tk = $st->fetch();
            if (!$tk) {
                $kq['bo_qua'][] = $ten . ' (không còn tồn tại)';
                continue;
            }
            $ten = (string)$tk['username'];
            if (laTaiKhoanAdmin($ten) || strcasecmp($ten, $admin) === 0) {
                $kq['bo_qua'][] = $ten . ' (quản trị)';
                continue;
            }
            $st = $game->prepare('SELECT id, charname FROM tob_char WHERE userId = ?');
            $st->execute([$id]);
            $nhanVat = $st->fetchAll();
            if ($nhanVat) {
                taoBangLenhNhanVat($game);
                $cho = $game->prepare('SELECT id FROM web_lenh_nhan_vat WHERE char_id = ? AND loai = ? AND trang_thai IN (0, 4) LIMIT 1');
                foreach ($nhanVat as $nv) {
                    $cho->execute([(int)$nv['id'], LENH_LOAI_XOA_NHAN_VAT]);
                    if (!$cho->fetchColumn()) {
                        guiLenhXoaNhanVat($game, (int)$nv['id'], (string)$nv['charname'], $admin);
                    }
                }
            }
            // Khong cho tung nhan vat: tranh treo yeu cau khi chon nhieu tai khoan.
            $st = $game->prepare('SELECT COUNT(*) FROM tob_char WHERE userId = ?');
            $st->execute([$id]);
            if ((int)$st->fetchColumn() > 0) {
                $kq['cho'][] = $ten;
                ghiNhatKy('xoa_nhieu_tai_khoan_cho', $ten, 'đã gửi lệnh xoá nhân vật; chưa xoá tài khoản');
                continue;
            }
            $game->prepare('DELETE FROM `5h_active` WHERE username = ? OR userID = ?')->execute([$ten, $id]);
            $account->prepare('DELETE FROM team_user WHERE id = ? AND username = ?')->execute([$id, $ten]);
            $kq['xoa'][] = $ten;
            ghiNhatKy('xoa_nhieu_tai_khoan', $ten, 'đã xoá tài khoản sau khi kiểm tra không còn nhân vật');
        } catch (Throwable $ex) {
            $kq['loi'][] = $ten;
            ghiNhatKy('xoa_nhieu_tai_khoan_loi', $ten, 'chưa hoàn tất xoá tài khoản');
        }
    }
    return $kq;
}
