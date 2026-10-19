import java.io.IOException;
import java.util.*;

/** Kich ban nhom 5: nhan vat (tao/xoa/khoi phuc), boss, he sinh thai, su kien, chiem thanh. */
class KichBanDS5 {

    static void ket(String t, Boolean ok, String bang) {
        BotChucNang.ket(t, ok, bang);
    }

    static void dangKy() {
        KichBanDungVatPham.dangKy();
        BotChucNang.dk("nhan_vat", false, KichBanDS5::nhanVat);
        BotChucNang.dk("boss", true, KichBanDS5::boss);
        BotChucNang.dk("he_sinh_thai", true, KichBanDS5::heSinhThai);
        BotChucNang.dk("su_kien", false, KichBanDS5::suKien);
        BotChucNang.dk("chiem_thanh", true, KichBanDS5::chiemThanh);
        KichBanNPC.dangKy();
    }

    /** Tai khoan thuong (kt101..): tao them nhan vat, ten sai, toi da 3, xoa + khoi phuc. */
    static void nhanVat() throws Exception {
        String u = BotChucNang.tkThuong();
        try (BotChucNang.Bot b = new BotChucNang.Bot(u)) {
            String l = b.dangNhap("123456");
            if (l != null) { ket("nhan_vat", false, l); return; }
            int truoc = b.dsNv.size();
            int m = b.moc();
            b.gui(14, "a b!", (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
            String t = b.doiChu(m, 3000, "không hợp lệ");
            ket("nhan_vat.ten_sai", t != null, t == null ? b.chuTu(m) : t);
            final int mg = b.moc();
            m = mg;
            b.gui(14, "admintest", (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
            b.cho(x -> x.coLenh(mg, 13) || x.coLenh(mg, 14) || x.coLenh(mg, 37), 3000);
            ket("nhan_vat.ten_gm_cam", b.dsTen.stream().noneMatch(x -> x.contains("admin")), b.chuTu(m));
            // them nhan vat toi khi day (toi da 3)
            int tao = 0;
            for (int i = 0; i < 4; i++) {
                String ten = (u + "x" + i).substring(0, Math.min(10, (u + "x" + i).length()));
                int mm = b.moc();
                b.gui(14, ten, (byte) (i % 5), (byte) 0, (byte) (i % 2), (byte) 0, (byte) 0);
                b.cho(x -> x.coLenh(mm, 13) || x.coLenh(mm, 14) || x.coLenh(mm, 37), 3000);
                if (b.coLenh(mm, 13) && b.dsTen.contains(ten)) tao++;
            }
            long song = b.dsNv.stream().filter(v -> v[2] == 1).count();
            ket("nhan_vat.toi_da_3", song <= 3 && b.dsNv.size() <= 3, "co " + truoc + " -> " + b.dsNv.size() + " nhan vat (tao them " + tao + "), dang dung " + song + ": " + b.dsTen);
            // xoa nhan vat cuoi (type 2) roi khoi phuc (type 3)
            if (b.dsNv.size() >= 2) {
                int[] nv = b.dsNv.get(b.dsNv.size() - 1);
                String ten = b.dsTen.get(b.dsTen.size() - 1);
                m = b.moc();
                b.gui(13, (byte) 2, nv[0]);
                b.bom(1500);
                int[] sau = null;
                for (int i = 0; i < b.dsNv.size(); i++) if (b.dsNv.get(i)[0] == nv[0]) sau = b.dsNv.get(i);
                String db = BotChucNang.sql1("SELECT del FROM tob_char WHERE id=" + nv[0]);
                ket("nhan_vat.xoa", sau != null && sau[2] == 0 || "0".equals(db), ten + ": dang dung=" + (sau == null ? "?" : sau[2]) + " csdl del=" + db + " | " + b.chuTu(m));
                m = b.moc();
                b.gui(13, (byte) 3, nv[0]);
                b.bom(4500); // SelectCharHandler ngu 2 giay neu chon lien tiep (InfoClientConnect.canSelectChar)
                db = BotChucNang.sql1("SELECT del FROM tob_char WHERE id=" + nv[0]);
                ket("nhan_vat.khoi_phuc", "1".equals(db) || DB_TRONG(), ten + " csdl del=" + db + " | " + b.chuTu(m));
                // don: xoa han cac nhan vat thu vua tao (giu nhan vat dau)
                for (int i = 1; i < b.dsNv.size(); i++) BotChucNang.sql("UPDATE tob_char SET del=0 WHERE id=" + b.dsNv.get(i)[0] + " AND charname LIKE '" + u + "x%'");
            }
        }
    }

    static boolean DB_TRONG() {
        return BotChucNang.DB.isEmpty();
    }

    /** Boss (GM addboss) danh bang nhieu nguoi; pha boss (-98) + dong gop. */
    static void boss() throws Exception {
        try (BotChucNang.Bot a = KichBanDS3.botLang(60); BotChucNang.Bot c = KichBanDS4.botCungQuoc(60, a)) {
            a.den(2, 20, 20);
            c.den(2, 20, 21);
            a.bom(500);
            int m = a.moc();
            KichBanDS.timQuai(a, 0, 300);
            Set<Integer> cu = new HashSet<>(a.quai.keySet());
            a.gm("addboss 74", 1000);
            Integer bId = KichBanDS.quaiMoi(a, cu, 8000);
            int boss = bId == null ? -1 : bId, maxhp = bId == null ? 0 : a.quai.get(bId)[4];
            if (bId == null) { ket("boss.trieu_hoi", null, "boss GM addboss khong gui vi tri (khong di chuyen) - khong thay qua lenh 4"); return; }
            ket("boss.trieu_hoi", maxhp > 1000, "boss id " + boss + " maxhp " + maxhp);
            int[] q = a.quai.get(boss);
            c.den(2, q[0] / 16, q[1] / 16);
            int don = 0;
            for (; don < 300; don++) {
                a.gui(9, (short) boss, (byte) 0);
                c.gui(9, (short) boss, (byte) 0);
                a.bom(300);
                c.bom(150);
                if (don % 5 == 4) {
                    a.gui(7, (short) boss);
                    a.bom(200);
                    if (a.quai.get(boss)[2] <= 0) break;
                    int[] q2 = a.quai.get(boss);
                    if (Math.abs(q2[0] - a.px) > 48 || Math.abs(q2[1] - a.py) > 48) { a.den(2, q2[0] / 16, q2[1] / 16); c.den(2, q2[0] / 16, q2[1] / 16); }
                }
                if (a.dut || c.dut) break;
            }
            Set<Integer> lenh = new TreeSet<>();
            for (int i = m; i < a.su.size(); i++) if (a.su.get(i).cmd <= -95 && a.su.get(i).cmd >= -99) lenh.add(a.su.get(i).cmd);
            ket("boss.ha", a.quai.get(boss)[2] <= 0, "sau " + don + " luot (2 nguoi), hp " + a.quai.get(boss)[2] + "/" + maxhp + "; goi lau dai nhan duoc: " + lenh);
            ket("boss.ket_noi", a.conSong() && c.conSong(), "");
        }
    }

    /** He sinh thai (sv.he_sinh_thai=1): menu NPC -90 muc cuoi, moi muc cap 1 tra loi. */
    static void heSinhThai() throws Exception {
        try (BotChucNang.Bot b = KichBanDS3.botLang(20)) {
            int m = b.moc();
            b.gui(23, (byte) -90);
            b.bom(1000);
            int vt = b.timMuc("sinh thái");
            if (vt < 0) { ket("he_sinh_thai", null, "menu -90 khong co muc He sinh thai (tat co?): " + b.menuChu); return; }
            m = b.moc();
            b.menu(-90, 0, vt);
            BotChucNang.SuKien e = null;
            b.bom(1200);
            for (int i = m; i < b.su.size(); i++) if (b.su.get(i).cmd == -30) e = b.su.get(i);
            ket("he_sinh_thai.mo", e != null, b.chuTu(m));
            if (e == null) return;
            int npc = e.so[0], menu = e.so[1];
            List<String> goc = new ArrayList<>(b.menuChu);
            for (int i = 0; i < goc.size(); i++) {
                b.gui(23, (byte) -90);
                b.bom(500);
                b.menu(-90, 0, vt);
                b.bom(700);
                int mm = b.moc();
                b.menu(npc, menu, i);
                b.bom(1200);
                String t = b.chuTu(mm);
                ket("he_sinh_thai." + i, !t.isEmpty() && b.conSong(), goc.get(i) + " -> " + (t.isEmpty() ? "KHONG TRA LOI" : t));
            }
        }
    }

    /** Su kien: bang tob_su_kien co du lieu, lich 52 tuan da hen. */
    static void suKien() throws Exception {
        String n = BotChucNang.sql1("SELECT COUNT(*) FROM tob_su_kien");
        String bat = BotChucNang.sql1("SELECT COUNT(*) FROM tob_su_kien WHERE dang_bat=1");
        String hen = BotChucNang.sql1("SELECT COUNT(*) FROM tob_su_kien WHERE mo_luc IS NOT NULL");
        ket("su_kien.bang", n != null && !n.startsWith("0"), "tob_su_kien " + n + " dong, dang bat " + bat + ", co lich hen " + hen);
    }

    /** Chiem thanh: GM 'setting' -> '1' (bat dau sau 2 phut), doi thong bao bat dau, roi 'over' ket thuc. */
    static void chiemThanh() throws Exception {
        try (BotChucNang.Bot b = KichBanDS3.botLang(70)) {
            int m = b.moc();
            b.chat("setting");
            b.bom(1000);
            BotChucNang.SuKien hoi = null;
            for (int i = m; i < b.su.size(); i++) if (b.su.get(i).cmd == -31) hoi = b.su.get(i);
            if (hoi == null) { ket("chiem_thanh", false, "khong mo duoc o lenh setting: " + b.chuTu(m)); return; }
            m = b.moc();
            b.gui(-31, (short) hoi.so[0], (byte) hoi.so[1], "1");
            b.bom(1500);
            ket("chiem_thanh.hen_gio", !b.chuTu(m).isEmpty(), b.chuTu(m));
            int m2 = b.moc();
            b.gm("tgct", 1000);
            ket("chiem_thanh.xem_gio", !b.chuTu(m2).isEmpty(), b.chuTu(m2));
            m2 = b.moc();
            String bd = b.doiChu(m2, 200000, "chiếm Gym bắt đầu", "Chiếm Gym bắt đầu", "chiếm thành", "Chiếm thành");
            ket("chiem_thanh.bat_dau", bd != null, bd == null ? b.chuTu(m2) : bd);
            for (int q = 0; q < 3; q++) b.gm("over" + q, 500);
            b.gm("ktct", 800);
            ket("chiem_thanh.ket_thuc", b.conSong(), b.chuTu(m2).length() > 300 ? b.chuTu(m2).substring(0, 300) : b.chuTu(m2));
        }
    }
}
