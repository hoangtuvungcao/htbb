/**
 * Kich ban bot GIFTCODE AN TOAN (0.9.9, real.GiftcodeAnToan / bang tob_giftcode, sv.lau_dai=1) tren MAY CHU THU RIENG
 * (-Ddb de chen ma + doi chieu CSDL). Gui dung goi client gui (-31: i16 id nhan vat, i8 7, UTF ma - HanhDong.moGiftcode):
 *   1. ma hop le (LUONGKHOA:5, chu HOA + khoang trang nhu nguoi go) -> "Doi giftcode thanh cong: +5 luong khoa" (the
 *      phan thuong o client), luong khoa +5, 1 dong tob_giftcode_log,
 *      da_dung +1;
 *   2. nhap lai cung ma -> "da nhap ma qua tang nay roi", khong cong them;
 *   3. ma khong co -> "khong ton tai hoac da het han"; 4. ma het han -> cung cau; 5. ma tat (bat=0) -> cung cau;
 *   6. ma can cap cao -> "Can dat cap"; 7. ma het luot -> "het luot su dung"; 8. bam lai < 2 s -> "doi 2 giay" (khong im lang).
 * Moi lan nhap cach >= 2,1 s (Char.GIFTCODE_COOLDOWN_MS: nhanh hon thi may chu im lang bo qua).
 *
 *   java -cp /tmp/bot -Ddb=htbb_db -Ddbport=3328 BotChucNang 127.0.0.1 19149 kt giftcode
 */
final class KichBanGiftcode {

    private KichBanGiftcode() {
    }

    static void dangKy() {
        BotChucNang.dk("giftcode", false, KichBanGiftcode::chay);
    }

    static String nhap(BotChucNang.Bot b, String ma, String... cho) throws Exception {
        Thread.sleep(2100);
        int m = b.moc();
        b.gui(-31, (short) b.actor, (byte) 7, ma);
        String t = b.doiChu(m, 8000, cho);
        return t != null ? t : "(khong thay: " + b.chuTu(m) + ")";
    }

    static void chay() throws Exception {
        if (BotChucNang.DB.isEmpty()) {
            BotChucNang.ket("giftcode", null, "can -Ddb (chen ma thu vao tob_giftcode)");
            return;
        }
        String[][] ma = {
            {"kbgcok", "LUONGKHOA:5", "NULL", "100", "0", "1", "1"},
            {"kbgchethan", "LUONGKHOA:5", "'2020-01-01 00:00:00'", "100", "0", "1", "1"},
            {"kbgctat", "LUONGKHOA:5", "NULL", "100", "0", "1", "0"},
            {"kbgccap", "LUONGKHOA:5", "NULL", "100", "0", "250", "1"},
            {"kbgchetluot", "LUONGKHOA:5", "NULL", "1", "1", "1", "1"}};
        for (String[] r : ma) {
            BotChucNang.sql("DELETE FROM tob_giftcode WHERE ma = '" + r[0] + "'");
            BotChucNang.sql("DELETE FROM tob_giftcode_log WHERE ma = '" + r[0] + "'");
            BotChucNang.sql("INSERT INTO tob_giftcode (ma, phan_thuong, han, tong_luot, da_dung, cap_toi_thieu, bat, ghi_chu) VALUES ('"
                    + r[0] + "', '" + r[1] + "', " + r[2] + ", " + r[3] + ", " + r[4] + ", " + r[5] + ", " + r[6] + ", 'bot giftcode')");
        }
        // tai khoan MOI dang ky that: bo dem nhap sai (5 lan / 10 phut / tai khoan, GiftcodeAnToan.ghiSai) nam trong RAM may chu
        // - tai khoan bot dung lai (kt1xx) mang so lan sai cua nhom / lan chay truoc -> "Nhập sai quá nhiều lần" giua kich ban
        BotChucNang.Bot b = HoiQuy.nguoiChoiMoi("gk", 0, 0);
        KichBanHuongDan.roiSan(b);
        try {
            String uid = BotChucNang.sql1("SELECT userId FROM tob_char WHERE id = " + b.charDb);
            BotChucNang.sql("DELETE FROM tob_giftcode_log WHERE user_id = " + uid);
            boolean daKh = "1".equals(BotChucNang.sql1("SELECT COUNT(*) FROM 5h_active WHERE userID = " + uid
                    + " AND time_end <> '-1' AND CAST(time_end AS UNSIGNED) > UNIX_TIMESTAMP()"));
            if (!daKh) {
                // kich_hoat.chuc_nang co "giftcode": tai khoan chua kich hoat phai nhan cau ro rang (khong im lang)
                String t0 = nhap(b, "kbgcok", "kich hoat", "thanh cong", "khong");
                BotChucNang.ket("giftcode: chua kich hoat", t0.contains("kích hoạt") || t0.contains("thành công"), t0);
                if (t0.contains("thành công")) {
                    BotChucNang.sql("DELETE FROM tob_giftcode_log WHERE user_id = " + uid);
                    BotChucNang.sql("UPDATE tob_giftcode SET da_dung = 0 WHERE ma = 'kbgcok'");
                }
                BotChucNang.sql("INSERT INTO 5h_active (userID, username, time_end) VALUES (" + uid + ", '" + b.user + "', '"
                        + (System.currentTimeMillis() / 1000 + 86400L * 365) + "')");
                Thread.sleep(31000);   // ThanhVien.NHO_MS: may chu nho trang thai kich hoat 30 s
            }
            int lk0 = b.lk;
            String t = nhap(b, "  KBGCOK ", "thanh cong", "da nhap", "khong", "het", "can dat");
            b.lamMoi();
            b.cho(x -> x.lk >= 0 && x.lk != lk0, 4000);
            String log = BotChucNang.sql1("SELECT COUNT(*) FROM tob_giftcode_log WHERE ma = 'kbgcok' AND id_char = " + b.charDb);
            String dd = BotChucNang.sql1("SELECT da_dung FROM tob_giftcode WHERE ma = 'kbgcok'");
            BotChucNang.ket("giftcode: ma dung", t.contains("Đổi giftcode thành công: +5 lượng khoá") && b.lk == lk0 + 5 && "1".equals(log) && "1".equals(dd),
                    t + " | luong khoa " + lk0 + " -> " + b.lk + ", log " + log + ", da_dung " + dd);
            int lk1 = b.lk;
            t = nhap(b, "kbgcok", "da nhap", "thanh cong", "khong");
            b.lamMoi();
            Thread.sleep(800);
            BotChucNang.ket("giftcode: nhap lai", t.contains("đã nhập mã quà tặng này rồi") && b.lk == lk1, t + " | luong khoa " + b.lk);
            t = nhap(b, "khongcomanay", "khong ton tai", "thanh cong");
            BotChucNang.ket("giftcode: ma sai", t.contains("không tồn tại hoặc đã hết hạn"), t);
            t = nhap(b, "kbgchethan", "khong ton tai", "thanh cong");
            BotChucNang.ket("giftcode: het han", t.contains("không tồn tại hoặc đã hết hạn"), t);
            t = nhap(b, "kbgctat", "khong ton tai", "thanh cong");
            BotChucNang.ket("giftcode: ma tat", t.contains("không tồn tại hoặc đã hết hạn"), t);
            t = nhap(b, "kbgccap", "can dat cap", "thanh cong", "khong");
            BotChucNang.ket("giftcode: chua du cap", t.contains("Cần đạt cấp 250"), t);
            t = nhap(b, "kbgchetluot", "het luot", "thanh cong", "khong");
            BotChucNang.ket("giftcode: het luot", t.contains("hết lượt"), t);
            // 8. bam lai ngay (< 2 s): bao "doi 2 giay" thay vi im lang
            int m = b.moc();
            b.gui(-31, (short) b.actor, (byte) 7, "kbgchetluot");
            t = b.doiChu(m, 8000, "doi 2 giay", "het luot");
            BotChucNang.ket("giftcode: bam qua nhanh", t != null && t.contains("đợi 2 giây"), String.valueOf(t));
        } finally {
            b.close();
        }
    }
}
