import java.util.ArrayList;
import java.util.List;

/**
 * Kich ban bot "KET TREN THUYEN" (bao loi 24/10/2026, J2ME cap 32 kazuhas4): nhan vat THUONG cap 32 CHUA lam cua ai
 * «Đường sang Johto» (CA-05.3 - New Bark 10 / Rung Viridian 11) len tau o Ben tau 106 -> thuyen 107 -> thuyen cap ben
 * New Bark bi chan -> ban cu nap lai thuyen MOI nhip vong lap ban do (map nap lien tuc, menu tu dong).
 *   kt_len_tau   o Ben tau 106 co ve tau, gui "Lên tàu" (lenh 82): bi tu choi kem dieu kien cua ai, khong len thuyen
 *                (khong co goi 82 dem gio / khong sang 107 sau 6 giay), van giu ve
 *   kt_cuu_107   nhan vat luu o thuyen 107 (nhu kazuhas4) dang nhap: duoc dua ve lang khoi dau + bao "Bạn đã được đưa
 *                về ... vì chưa mở New Bark", trong 10 giay sau KHONG co goi nap map (12) nao nua (ban cu: hang chuc)
 *   kt_cuu_khoa  nhan vat luu o New Bark (khoa voi no) dang nhap: ve lang, khong nap lai lien tuc
 *
 *   javac -d /tmp/bot tools/pokemon/kiem_tai/BotChucNang.java tools/pokemon/kiem_tai/KichBanKetTau.java
 *   java -cp /tmp/bot -Ddb=htbb_db -Ddbport=3381 -DtuThuong=101 -DdenThuong=110 KichBanKetTau 127.0.0.1 18389 kg (tai khoan GM kg001.., thuong kg171..)
 */
public final class KichBanKetTau {

    private KichBanKetTau() {
    }

    static final int CAP = 32;

    /** Doi truong {@code i} (CSV) cua cot {@code cot} tob_char. */
    static void doiTruong(int charDb, String cot, int[] i, String[] gt) {
        String v = BotChucNang.sql1("SELECT " + cot + " FROM tob_char WHERE id = " + charDb);
        if (v == null) throw new IllegalStateException("khong doc duoc " + cot);
        String[] a = v.split(",", -1);
        for (int k = 0; k < i.length; k++) a[i[k]] = gt[k];
        BotChucNang.sql("UPDATE tob_char SET " + cot + " = '" + String.join(",", a) + "' WHERE id = " + charDb);
    }

    /** Nhan vat thuong cap 32 (GM slv), thoat ra, dat ban do luu {@code map} o (x,y) + 2 ve tau; tra ve ten tai khoan. */
    static String chuanBi(int map, int x, int y) throws Exception {
        String user;
        int db;
        try (BotChucNang.Bot g = BotChucNang.botMoi(0); BotChucNang.Bot b = BotChucNang.botThuong()) {
            g.gm("slv " + b.ten + " " + CAP + " 0", 1500);
            b.bom(800);
            user = b.user;
            db = b.charDb;
        }
        Thread.sleep(2500); // server luu nhan vat khi thoat
        // chua lam cua ai Duong sang Johto, moc mien tru = 1 (nhan vat moi)
        BotChucNang.sql("DELETE FROM tob_nhiem_vu_lau_dai WHERE id_char = " + db + " AND ma LIKE 'CA-05%'");
        BotChucNang.sql("UPDATE tob_ban_do_mo SET moc_mien = 1 WHERE id_char = " + db);
        doiTruong(db, "pInfo", new int[]{8, 9, 10}, new String[]{"" + map, "" + (x * 16 + 8), "" + (y * 16 + 8)});
        doiTruong(db, "potion", new int[]{20}, new String[]{"2"});
        return user + ":" + db;
    }

    static BotChucNang.Bot vao(String user) throws Exception {
        BotChucNang.Bot b = new BotChucNang.Bot(user);
        String l = b.dangNhap("123456");
        if (l != null) throw new IllegalStateException(user + " dang nhap: " + l);
        l = b.vaoGame(0);
        if (l != null) throw new IllegalStateException(user + " " + l);
        return b;
    }

    static int dem12(BotChucNang.Bot b, int tu) {
        int n = 0;
        for (int i = tu; i < b.moc(); i++) if (b.su.get(i).cmd == 12) n++;
        return n;
    }

    static boolean laLang(int m) {
        return m == 0 || m == 70 || m == 80 || m == 301 || m == 1701 || m == 1901 || m == 302 || m == 1702 || m == 1902;
    }

    static void lenTau() throws Exception {
        String[] u = chuanBi(106, 20, 12).split(":");
        try (BotChucNang.Bot b = vao(u[0])) {
            if (b.mapId != 106) { BotChucNang.ket("kt.len_tau", null, "khong vao duoc Ben tau, map " + b.mapId); return; }
            int m = b.moc();
            b.gui(82);
            String t = b.doiChu(m, 3000, "Đường sang Johto", "New Bark");
            b.bom(6500);
            boolean coDemGio = false;
            for (int i = m; i < b.moc(); i++) if (b.su.get(i).cmd == 82) coDemGio = true;
            String ve = BotChucNang.sql1("SELECT potion FROM tob_char WHERE id = " + u[1]);
            BotChucNang.ket("kt.len_tau", t != null && !coDemGio && b.mapId == 106,
                    "bao: " + t + " | dem gio 82: " + coDemGio + " | map sau 6 giay " + b.mapId + " | " + b.chuTu(m));
            BotChucNang.ket("kt.len_tau.giu_ve", ve != null && "2".equals(ve.split(",")[20]), "ve tau trong CSDL: " + (ve == null ? null : ve.split(",")[20]));
        }
    }

    static void cuu(String ten, int map) throws Exception {
        String[] u = chuanBi(map, 20, 12).split(":");
        BotChucNang.Bot b = new BotChucNang.Bot(u[0]);
        try {
            String l = b.dangNhap("123456");
            if (l != null) throw new IllegalStateException(l);
            int m = b.moc();
            l = b.vaoGame(0);
            if (l != null) { BotChucNang.ket(ten, false, l); return; }
            int sau = b.moc();
            String bao = b.doiChu(m, 3000, "đã được đưa về");
            b.bom(10000);
            int lap = dem12(b, sau);
            BotChucNang.ket(ten, laLang(b.mapId) && bao != null && lap == 0,
                    "luu map " + map + " -> vao map " + b.mapId + " | nap lai trong 10 giay: " + lap + " | bao: " + bao);
            // them 3 giay: van khong nap lai (menu / hop khong bi GameScr.onMap dong)
            int m2 = b.moc();
            b.bom(3000);
            BotChucNang.ket(ten + ".khong_nap_lai", dem12(b, m2) == 0, "goi 12 sau 3 giay: " + dem12(b, m2));
        } finally {
            b.close();
        }
    }

    static void cuu107() throws Exception {
        cuu("kt.cuu_107", 107);
    }

    static void cuuKhoa() throws Exception {
        cuu("kt.cuu_khoa", 10);
    }

    public static void main(String[] a) throws Exception {
        BotChucNang.dk("kt_len_tau", true, KichBanKetTau::lenTau);
        BotChucNang.dk("kt_cuu_107", true, KichBanKetTau::cuu107);
        BotChucNang.dk("kt_cuu_khoa", true, KichBanKetTau::cuuKhoa);
        List<String> chon = new ArrayList<>();
        for (int i = 3; i < a.length; i++) chon.add(a[i]);
        if (chon.isEmpty()) chon.addAll(List.of("kt_len_tau", "kt_cuu_107", "kt_cuu_khoa"));
        String[] b = new String[3 + chon.size()];
        b[0] = a[0];
        b[1] = a[1];
        b[2] = a[2];
        for (int i = 0; i < chon.size(); i++) b[3 + i] = chon.get(i);
        BotChucNang.main(b);
    }
}
