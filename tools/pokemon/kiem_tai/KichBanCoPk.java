import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Kich ban bot CO PK (khan PK, binh 14..18 - lenh 22; may chu real.CoPkGame). Client 0.9.7 (PC / APK / J2ME) bat / tat
 * PK DUY NHAT bang cach dung khan (Hanh trang moi: cham hang thuoc = dung ngay; cua so cu: "Sử dụng" / "Tháo khăn"),
 * cung goi 22 &lt;loai&gt; cho moi nen tang. Lenh 67 (do sat) client khong gui. Chay tren MAY CHU THU RIENG (GM slv / mto /
 * spt), co -Ddb (doc / dat tob_char.killer):
 *   cp_bat_tat      deo khan (65 use=1) -> dung lai ngay (2 giay) = THAO (65 use=0), KHONG thanh ten do (67 co=1 / nghiep)
 *   cp_danh_roi_tat deo khan, danh nguoi deo khan khac mau -> thao ngay bi tu choi kem "còn N giây"; nguoi bi danh khong
 *                   tu bat / doi trang thai; sau 10 giay thao duoc
 *   cp_vao_lai      deo khan -> thoat -> vao lai: khan da thao (dung lai = DEO), khong ten do
 *   cp_ten_do       nghiep 300 (CSDL) -> vao game ten do; deo khan bi tu choi CO THONG BAO; khan khong bat
 *
 *   java -cp /tmp/bot -Dban=907 -Ddb=htbb_db -Ddbport=3425 KichBanCoPk 127.0.0.1 19425 kt [cp_...]
 *   (bo -Dban = client khong co thong bao nhanh -104: thong bao di bang hop thoai 37)
 */
public final class KichBanCoPk {

    private KichBanCoPk() {
    }

    static final int KHAN_DO = 14, KHAN_XANH = 15;
    /** Ban do PK (Viridian ngoai troi, khong phai thi tran / lang). */
    static final int MAP_PK = 2;

    /** Goi 65 (khan) cua actor id sau moc: {use, loai} hoac null. Lay goi CUOI. */
    static int[] goi65(BotChucNang.Bot b, int tu, int id) {
        int[] kq = null;
        for (int i = tu; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.cmd != 65 || e.d == null) continue;
            try {
                DataInputStream r = new DataInputStream(new ByteArrayInputStream(e.d));
                if (r.readShort() == id) kq = new int[]{r.readByte(), r.readByte()};
            } catch (Exception ignored) {
            }
        }
        return kq;
    }

    /** Goi 67 (ten do) cua actor id sau moc: {co, nghiep} hoac null. Lay goi CUOI. */
    static int[] goi67(BotChucNang.Bot b, int tu, int id) {
        int[] kq = null;
        for (int i = tu; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.cmd != 67 || e.d == null) continue;
            try {
                DataInputStream r = new DataInputStream(new ByteArrayInputStream(e.d));
                if (r.readShort() == id) kq = new int[]{r.readByte(), r.readShort()};
            } catch (Exception ignored) {
            }
        }
        return kq;
    }

    static String s(int[] a) {
        return a == null ? "-" : a[0] + "/" + a[1];
    }

    /** Thong bao (toast -104 hoac chu 37/27...) sau moc. */
    static String tb(BotChucNang.Bot b, int tu) {
        return KichBanXepHang.toast(b, tu) + b.chuTu(tu);
    }

    static void dung(BotChucNang.Bot b, int loai) throws Exception {
        b.gui(22, (byte) loai);
        b.bom(1200);
    }

    /** Nghiep luu trong tob_char.pInfo (phan tu 11, CharNapDuLieu), khong phai cot killer (bo trong). */
    static String nghiep(int db) {
        String p = BotChucNang.sql1("SELECT pInfo FROM tob_char WHERE id = " + db);
        if (p == null) return "?";
        String[] a = p.trim().split(",", -1);
        return a.length > 11 ? a[11] : "?";
    }

    static String nghiep(BotChucNang.Bot b) {
        return nghiep(b.charDb);
    }

    static void datNghiep(int db, int k) {
        String p = BotChucNang.sql1("SELECT pInfo FROM tob_char WHERE id = " + db);
        if (p == null) return;
        String[] a = p.trim().split(",", -1);
        if (a.length <= 11) return;
        a[11] = String.valueOf(k);
        BotChucNang.sql("UPDATE tob_char SET pInfo = '" + String.join(",", a).replace("'", "''") + "' WHERE id = " + db);
    }

    /** Bot GM cap lv o ngoai lang (ban do PK), co khan. */
    static BotChucNang.Bot bot(int lv, int khan) throws Exception {
        BotChucNang.Bot b = null;
        for (int lan = 0; lan < 8; lan++) { // nhan vat chua co quoc gia (ban do 117 / 2641) thi mto khong toi: lay tai khoan ke
            b = KichBanDS4.botLang(lv);
            b.gm("slv " + b.ten + " " + lv + " 0", 800);
            if (b.den(MAP_PK, 20, 20)) break;
            KichBanDS2.raNgoai(b); // dang trong nha / ban do offline (vd 117): ra cong truoc
            if (b.den(MAP_PK, 20, 20)) break;
            System.out.println(b.user + " khong toi duoc map " + MAP_PK + " (dang o " + b.mapId + "), doi tai khoan");
            b.close();
            b = null;
        }
        if (b == null) {
            throw new IllegalStateException("khong bot nao toi duoc map " + MAP_PK);
        }
        b.gm("spt " + b.ten + " " + khan + " 1", 800);
        return b;
    }

    static void batTat() throws Exception {
        try (BotChucNang.Bot a = bot(30, KHAN_DO)) {
            int khan0 = a.soThuoc(KHAN_DO);
            int m = a.moc();
            dung(a, KHAN_DO);
            int[] p = goi65(a, m, a.actor);
            BotChucNang.ket("cp.bat_tat.deo", p != null && p[0] == 1 && p[1] == KHAN_DO, "65=" + s(p) + " " + tb(a, m));
            Thread.sleep(1000);
            int m2 = a.moc();
            dung(a, KHAN_DO);
            int[] p2 = goi65(a, m2, a.actor);
            int[] k2 = goi67(a, m2, a.actor);
            BotChucNang.ket("cp.bat_tat.thao_ngay", p2 != null && p2[0] == 0, "65=" + s(p2) + " 67=" + s(k2) + " " + tb(a, m2));
            BotChucNang.ket("cp.bat_tat.khong_ten_do", k2 == null || k2[0] == 0, "67=" + s(k2));
            int m3 = a.moc();
            dung(a, KHAN_DO);
            int[] p3 = goi65(a, m3, a.actor);
            BotChucNang.ket("cp.bat_tat.deo_lai", p3 != null && p3[0] == 1, "65=" + s(p3) + " " + tb(a, m3));
            int m4 = a.moc();
            dung(a, KHAN_DO);
            int[] p4 = goi65(a, m4, a.actor);
            BotChucNang.ket("cp.bat_tat.thao_lai", p4 != null && p4[0] == 0, "65=" + s(p4) + " " + tb(a, m4));
            BotChucNang.ket("cp.bat_tat.con_khan", khan0 > 0 && a.soThuoc(KHAN_DO) == khan0, "so khan " + khan0 + " -> " + a.soThuoc(KHAN_DO));
        }
    }

    static void danhRoiTat() throws Exception {
        try (BotChucNang.Bot a = KichBanDS4.botLang(30); BotChucNang.Bot c = KichBanDS4.botCungQuoc(30, a)) {
            a.gm("slv " + a.ten + " 30 0", 800);
            c.gm("slv " + c.ten + " 30 0", 800);
            a.gm("spt " + a.ten + " " + KHAN_DO + " 1", 800);
            c.gm("spt " + c.ten + " " + KHAN_XANH + " 1", 800);
            if (!a.den(MAP_PK, 20, 20) || !c.den(MAP_PK, 20, 21)) {
                BotChucNang.ket("cp.danh", null, "khong toi duoc map " + MAP_PK);
                return;
            }
            c.bom(500);
            a.bom(500);
            int mc0 = c.moc();
            dung(a, KHAN_DO);
            dung(c, KHAN_XANH);
            c.lamMoi();
            int hp0 = c.hp;
            int ma = a.moc();
            int mc = c.moc();
            for (int i = 0; i < 6; i++) {
                a.gui(6, (short) c.actor, (byte) 0);
                a.bom(450);
            }
            c.bom(300);
            c.lamMoi();
            BotChucNang.ket("cp.danh.trung", c.hp < hp0, "hp B " + hp0 + " -> " + c.hp + " " + tb(a, ma));
            int[] cDoi65 = goi65(c, mc, c.actor);
            int[] cDoi67 = goi67(c, mc, c.actor);
            BotChucNang.ket("cp.danh.bi_danh_khong_doi", cDoi65 == null && (cDoi67 == null || cDoi67[0] == 0),
                    "B sau khi bi danh: 65=" + s(cDoi65) + " 67=" + s(cDoi67) + " (truoc: " + s(goi65(c, mc0, c.actor)) + ")");
            int m2 = a.moc();
            dung(a, KHAN_DO);
            int[] p2 = goi65(a, m2, a.actor);
            String t2 = tb(a, m2);
            BotChucNang.ket("cp.danh.thao_cho", (p2 == null || p2[0] == 1) && t2.matches("(?s).*còn \\d+ giây.*"), "65=" + s(p2) + " " + t2);
            Thread.sleep(10500);
            int m3 = a.moc();
            dung(a, KHAN_DO);
            int[] p3 = goi65(a, m3, a.actor);
            BotChucNang.ket("cp.danh.thao_sau_10s", p3 != null && p3[0] == 0, "65=" + s(p3) + " " + tb(a, m3));
            int[] k3 = goi67(a, ma, a.actor);
            BotChucNang.ket("cp.danh.khong_ten_do", k3 == null || k3[0] == 0, "67=" + s(k3));
        }
    }

    static void vaoLai() throws Exception {
        String user;
        try (BotChucNang.Bot a = bot(30, KHAN_DO)) {
            user = a.user;
            int m = a.moc();
            dung(a, KHAN_DO);
            int[] p = goi65(a, m, a.actor);
            BotChucNang.ket("cp.vao_lai.deo", p != null && p[0] == 1, "65=" + s(p));
        }
        Thread.sleep(4000);
        BotChucNang.Bot b = KichBanXepHang.vaoLai(user);
        try {
            b.bom(2000);
            int[] k = goi67(b, 0, b.actor);
            if (!b.den(MAP_PK, 20, 20)) {
                BotChucNang.ket("cp.vao_lai", null, "khong toi duoc map " + MAP_PK);
                return;
            }
            int m = b.moc();
            dung(b, KHAN_DO);
            int[] p = goi65(b, m, b.actor);
            BotChucNang.ket("cp.vao_lai.da_thao", p != null && p[0] == 1, "dung khan sau khi vao lai: 65=" + s(p) + " " + tb(b, m));
            BotChucNang.ket("cp.vao_lai.khong_ten_do", (k == null || k[0] == 0) && "0".equals(nghiep(b)), "67 luc vao=" + s(k) + " killer=" + nghiep(b));
            dung(b, KHAN_DO);
        } finally {
            b.close();
        }
    }

    static void tenDo() throws Exception {
        String user;
        int db;
        try (BotChucNang.Bot a = bot(30, KHAN_DO)) {
            user = a.user;
            db = a.charDb;
        }
        Thread.sleep(4000);
        datNghiep(db, 300);
        BotChucNang.ket("cp.ten_do.dat_csdl", "300".equals(nghiep(db)), "pInfo[11]=" + nghiep(db));
        BotChucNang.Bot b = KichBanXepHang.vaoLai(user);
        try {
            b.bom(1500);
            if (!b.den(MAP_PK, 20, 20)) {
                BotChucNang.ket("cp.ten_do", null, "khong toi duoc map " + MAP_PK);
                return;
            }
            int m = b.moc();
            dung(b, KHAN_DO);
            int[] p = goi65(b, m, b.actor);
            String t = tb(b, m);
            BotChucNang.ket("cp.ten_do.khong_deo", p == null || p[0] == 0, "65=" + s(p) + " " + t);
            BotChucNang.ket("cp.ten_do.co_thong_bao", t.contains("tên đỏ") || t.contains("nghiệp"), t);
        } finally {
            b.close();
            Thread.sleep(3000);
            datNghiep(db, 0);
        }
    }

    public static void main(String[] a) throws Exception {
        BotChucNang.dk("cp_bat_tat", true, KichBanCoPk::batTat);
        BotChucNang.dk("cp_danh_roi_tat", true, KichBanCoPk::danhRoiTat);
        BotChucNang.dk("cp_vao_lai", true, KichBanCoPk::vaoLai);
        BotChucNang.dk("cp_ten_do", true, KichBanCoPk::tenDo);
        List<String> chon = new ArrayList<>();
        for (int i = 3; i < a.length; i++) chon.add(a[i]);
        if (chon.isEmpty()) chon.addAll(List.of("cp_bat_tat", "cp_danh_roi_tat", "cp_vao_lai", "cp_ten_do"));
        String[] b = new String[3 + chon.size()];
        b[0] = a[0];
        b[1] = a[1];
        b[2] = a[2];
        for (int i = 0; i < chon.size(); i++) b[3 + i] = chon.get(i);
        BotChucNang.main(b);
    }
}
