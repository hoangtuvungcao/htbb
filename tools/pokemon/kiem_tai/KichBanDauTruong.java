import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Kich ban bot DAU TRUONG BAO BOI 3v3 (real.DauTruongGame, goi -109, client &gt;= 908). Chay tren MAY CHU THU RIENG (GM slv),
 * CSDL da nap sql/htbb.sql (khoi them_dau_truong.sql), bot gui so ban -Dban=908:
 *   dt_tran   hai bot (Hop PC 3 con nap qua SQL) mo NPC Dau truong (-31) -> "Đấu trường Bảo Bối" (-109 kieu 0, kho 3 con)
 *             -> luu doi tan cong + phong thu -> bot A tim doi thu (thay B) -> thach dau -> 3 luot tu chon chieu -> 1 luot
 *             bo trong (het 20 giay may chon) -> bat Tu dong -> ket qua (-109 kieu 4); CSDL: tob_dau_truong_tran co hat +
 *             hanh dong, tob_dau_truong_mua hai ben; B (online) nhan thong bao; luong khoa dung luat (thang 3 / thua 0)
 *             -> cuoi kich ban: du 5 luot hom nay -> thach dau bi tu choi "Hết 5 lượt"
 *
 *   java -cp /tmp/bot -Dban=908 -Ddb=htbb_db -Ddbport=3416 -Dnoi=&lt;repo&gt; KichBanDauTruong 127.0.0.1 19416 kt [dt_tran]
 */
public final class KichBanDauTruong {

    private KichBanDauTruong() {
    }

    static String toast(BotChucNang.Bot b, int tu) {
        StringBuilder s = new StringBuilder();
        for (int i = tu; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.cmd != -104 || e.d == null) continue;
            try {
                DataInputStream r = new DataInputStream(new ByteArrayInputStream(e.d));
                r.readByte();
                s.append(r.readUTF().replace('\n', '/')).append(" | ");
            } catch (Exception ignored) {
            }
        }
        return s.toString();
    }

    /** Goi -109 kieu {@code kieu} dau tien tu moc {@code tu} (null = chua co). */
    static byte[] goi(BotChucNang.Bot b, int tu, int kieu) {
        for (int i = tu; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.cmd == -109 && e.d != null && e.d.length > 0 && e.d[0] == kieu) return e.d;
        }
        return null;
    }

    static int dem(BotChucNang.Bot b, int tu, int kieu) {
        int n = 0;
        for (int i = tu; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.cmd == -109 && e.d != null && e.d.length > 0 && e.d[0] == kieu) n++;
        }
        return n;
    }

    static byte[] cho(BotChucNang.Bot b, int tu, int kieu, long ms) {
        b.cho(x -> goi(x, tu, kieu) != null, ms);
        return goi(b, tu, kieu);
    }

    /** IV dong goi 6 x 5 bit. */
    static int iv(int v) {
        int g = 0;
        for (int i = 0; i < 6; i++) g |= (v & 31) << (5 * i);
        return g;
    }

    static String hop(int[] dex, int ivDeu, int chuGoc) {
        StringBuilder s = new StringBuilder("v=1;ik=" + (dex.length + 1) + ";mr=0;dt=0");
        for (int i = 0; i < dex.length; i++) {
            s.append(";c").append(i + 1).append('=').append(dex[i]).append(".30.0.20261001.poke.").append(iv(ivDeu)).append('.')
                    .append(i * 6).append('.').append(chuGoc).append(".0");
        }
        return s.toString();
    }

    static BotChucNang.Bot bot(int lv) throws Exception {
        BotChucNang.Bot b = BotChucNang.botMoi(0);
        b.gm("slv " + b.ten + " " + lv + " 0", 800);
        return b;
    }

    /** Mo menu NPC Dau truong, chon muc chua `chu`. */
    static int chonDauTruong(BotChucNang.Bot b, String chu) throws Exception {
        int m = b.moc();
        b.gui(23, (byte) -31);
        int n = b.doiMenu(m, -31, 0, 3000);
        if (n <= 0) return -1;
        int i = b.timMuc(chu);
        if (i < 0) return -2;
        b.menu(-31, 0, i);
        b.bom(800);
        return i;
    }

    static int soKho(byte[] d) {
        try {
            DataInputStream r = new DataInputStream(new ByteArrayInputStream(d));
            r.readByte();
            r.readByte();
            r.readShort();
            r.readShort();
            r.readShort();
            r.readByte();
            r.readUTF();
            r.readInt();
            r.readShort();
            r.readShort();
            r.readByte();
            r.readByte();
            r.readShort();
            r.readShort();
            r.readByte();
            r.readByte();
            for (int k = 0; k < 2; k++) {
                int n = r.readByte();
                for (int i = 0; i < n; i++) r.readInt();
            }
            r.readInt();
            r.readInt();
            return r.readShort();
        } catch (Exception e) {
            return -1;
        }
    }

    static List<Integer> doiThu(byte[] d) {
        List<Integer> l = new ArrayList<>();
        try {
            DataInputStream r = new DataInputStream(new ByteArrayInputStream(d));
            r.readByte();
            int n = r.readByte();
            for (int i = 0; i < n; i++) {
                l.add(r.readInt());
                r.readUTF();
                r.readShort();
                r.readByte();
                r.readInt();
                r.readByte();
                for (int k = 0; k < 3; k++) {
                    r.readShort();
                    r.readByte();
                    r.readByte();
                }
            }
        } catch (Exception ignored) {
        }
        return l;
    }

    static String userA, userB;
    static int dbA, dbB;

    static void tran() throws Exception {
        try (BotChucNang.Bot a = bot(40); BotChucNang.Bot c = bot(42)) {
            userA = a.user;
            userB = c.user;
            dbA = a.charDb;
            dbB = c.charDb;
        }
        Thread.sleep(4000);
        BotChucNang.sql("INSERT INTO tob_hop_pc (id_char, du_lieu, sua_luc) VALUES (" + dbA + ", '" + hop(new int[]{6, 9, 3}, 31, dbA)
                + "', NOW()) ON DUPLICATE KEY UPDATE du_lieu = VALUES(du_lieu)");
        BotChucNang.sql("INSERT INTO tob_hop_pc (id_char, du_lieu, sua_luc) VALUES (" + dbB + ", '" + hop(new int[]{3, 6, 9}, 30, dbB)
                + "', NOW()) ON DUPLICATE KEY UPDATE du_lieu = VALUES(du_lieu)");
        for (String t : new String[]{"tob_dau_truong_doi", "tob_dau_truong_mua"}) {
            BotChucNang.sql("DELETE FROM " + t + " WHERE id_char IN (" + dbA + "," + dbB + ")");
        }
        BotChucNang.sql("DELETE FROM tob_dau_truong_tran WHERE cong IN (" + dbA + "," + dbB + ")");
        BotChucNang.Bot a = KichBanXepHang.vaoLai(userA);
        BotChucNang.Bot c = KichBanXepHang.vaoLai(userB);
        try {
            a.den(0, 22, 33);
            c.den(0, 22, 33);
            a.bom(800);
            c.bom(800);
            // 1. mo man tu NPC
            int ma = a.moc(), mc = c.moc();
            int ia = chonDauTruong(a, "Đấu trường Bảo Bối");
            byte[] tt = cho(a, ma, 0, 4000);
            BotChucNang.ket("dt.menu_npc", ia >= 0, "muc=" + ia + " " + a.menuChu);
            BotChucNang.ket("dt.mo_man", tt != null && tt[1] == 1 && soKho(tt) == 3, tt == null ? "khong co -109" : "kho=" + soKho(tt));
            chonDauTruong(c, "Đấu trường Bảo Bối");
            cho(c, mc, 0, 4000);
            // 2. luu doi (id Hop PC 1..3)
            for (BotChucNang.Bot b : new BotChucNang.Bot[]{a, c}) {
                for (int loai = 0; loai < 2; loai++) {
                    int m = b.moc();
                    b.gui(-109, (byte) 1, (byte) loai, (byte) 3, 1, 2, 3);
                    byte[] d = cho(b, m, 0, 4000);
                    String chu = d == null ? "" : new String(d, java.nio.charset.StandardCharsets.UTF_8);
                    BotChucNang.ket("dt.luu_doi." + (loai == 0 ? "cong" : "thu") + "." + (b == a ? "a" : "b"), d != null && chu.contains("Đã lưu"),
                            toast(b, m));
                }
            }
            String doi = BotChucNang.sql1("SELECT CONCAT(doi_cong, '|', doi_thu, '|', suc_thu) FROM tob_dau_truong_doi WHERE id_char = " + dbB);
            BotChucNang.ket("dt.luu_doi.csdl", doi != null && doi.startsWith("1,2,3|1,2,3|"), "dong=" + doi);
            // 3. tim doi thu
            int m = a.moc();
            a.gui(-109, (byte) 2, (byte) 0);
            byte[] dt = cho(a, m, 1, 5000);
            List<Integer> ds = dt == null ? new ArrayList<>() : doiThu(dt);
            BotChucNang.ket("dt.tim_doi_thu", ds.contains(dbB), "de xuat=" + ds + " B=" + dbB);
            // 4. thach dau
            m = a.moc();
            int lk0 = a.lk;
            a.gui(-109, (byte) 3, dbB, (byte) 0); // 0 = tu chon chieu (mac dinh may danh)
            byte[] bd = cho(a, m, 2, 5000);
            int tranId = -1;
            if (bd != null) {
                DataInputStream r = new DataInputStream(new ByteArrayInputStream(bd));
                r.readByte();
                tranId = r.readInt();
            }
            BotChucNang.ket("dt.bat_dau", bd != null && tranId > 0, "tran=" + tranId + " " + toast(a, m));
            if (tranId <= 0) return;
            // 5. ba luot tu chon chieu
            int luot = 0;
            for (int i = 0; i < 3; i++) {
                a.bom(1600); // mot hanh dong moi luot: may chu bo goi bam trong 1,2 giay sau luot truoc
                int mm = a.moc();
                a.gui(-109, (byte) 4, tranId, (byte) 0, (byte) (i % 2));
                byte[] l = cho(a, mm, 3, 5000);
                if (l != null) luot++;
                if (goi(a, mm, 4) != null) break;
            }
            BotChucNang.ket("dt.chon_chieu", luot >= 1, "luot nhan=" + luot);
            // 6. bo trong mot luot: may chon sau 20 giay
            boolean xong = goi(a, m, 4) != null;
            if (!xong) {
                int mm = a.moc();
                // dong ho luot (20 giay) chay tu luc may chu gui ket qua luot TRUOC (goi -109 kieu 3), khong tu luc bot
                // bat dau doi (cong kiem 24/10: "sau 13 giay" = bot da mat ~7 giay o vong chon chieu)
                long t0 = System.currentTimeMillis();
                for (int i = a.su.size() - 1; i >= m; i--) {
                    BotChucNang.SuKien e = a.su.get(i);
                    if (e.cmd == -109 && e.d != null && e.d.length > 0 && (e.d[0] == 3 || e.d[0] == 2)) { t0 = e.luc; break; }
                }
                byte[] l = cho(a, mm, 3, 26000);
                long giay = (System.currentTimeMillis() - t0) / 1000;
                BotChucNang.ket("dt.het_gio_may_chon", l != null && giay >= 17, "sau " + giay + " giay");
                xong = goi(a, m, 4) != null;
            }
            // 7. tu dong den het tran
            if (!xong) a.gui(-109, (byte) 5, tranId, (byte) 1);
            byte[] kq = cho(a, m, 4, 150000);
            int thang = -1, dTruoc = 0, dSau = 0, luong = -1;
            if (kq != null) {
                DataInputStream r = new DataInputStream(new ByteArrayInputStream(kq));
                r.readByte();
                r.readInt();
                thang = r.readByte();
                dTruoc = r.readShort();
                dSau = r.readShort();
                r.readShort();
                luong = r.readShort();
            }
            BotChucNang.ket("dt.ket_qua", kq != null, "thang=" + thang + " diem " + dTruoc + "->" + dSau + " luong " + luong + " luot -109/3="
                    + dem(a, m, 3));
            BotChucNang.ket("dt.diem", thang == 1 ? dSau > dTruoc : dSau < dTruoc, dTruoc + "->" + dSau);
            BotChucNang.ket("dt.luong_luat", thang == 1 ? luong == 3 : luong == 0, "luong=" + luong);
            a.bom(1500);
            BotChucNang.ket("dt.luong_khoa_vao_vi", lk0 < 0 || a.lk < 0 || a.lk == lk0 + Math.max(0, luong), "lk " + lk0 + " -> " + a.lk);
            String row = BotChucNang.sql1("SELECT CONCAT(ket_qua, '|', luot, '|', hat <> 0, '|', LENGTH(hanh_dong) > 0, '|', he_so) FROM tob_dau_truong_tran"
                    + " WHERE id = " + tranId);
            BotChucNang.ket("dt.csdl_tran", row != null && row.matches("[01]\\|[1-9][0-9]*\\|1\\|1\\|1000"), "dong=" + row);
            String mua = BotChucNang.sql1("SELECT CONCAT((SELECT so_tran FROM tob_dau_truong_mua WHERE id_char = " + dbA
                    + " ORDER BY mua DESC LIMIT 1), '|', (SELECT thu_thang + thu_thua FROM tob_dau_truong_mua WHERE id_char = " + dbB
                    + " ORDER BY mua DESC LIMIT 1))");
            BotChucNang.ket("dt.csdl_mua_hai_ben", "1|1".equals(mua == null ? null : mua.trim()), "tan cong so_tran | phong thu tran = " + mua);
            c.bom(2000); // bot chi doc goi khi bom / cho
            BotChucNang.ket("dt.bao_phong_thu", toast(c, mc).contains("Đấu trường:"), toast(c, mc));
            // 8. luot ngay: du 5 -> tu choi
            String ngay = BotChucNang.sql1("SELECT ngay FROM tob_dau_truong_tran WHERE id = " + tranId);
            for (int i = 0; i < 4; i++) {
                BotChucNang.sql("INSERT INTO tob_dau_truong_tran (mua, tuan, ngay, cong, thu, ket_qua, bat_dau) VALUES (0, 0, " + ngay.trim() + ", "
                        + dbA + ", 999999, 0, NOW())");
            }
            m = a.moc();
            a.gui(-109, (byte) 2, (byte) 0);
            cho(a, m, 1, 5000);
            a.gui(-109, (byte) 3, dbB);
            a.bom(2500);
            BotChucNang.ket("dt.het_luot_ngay", goi(a, m, 2) == null && toast(a, m).contains("Hết 5 lượt"), toast(a, m));
        } finally {
            a.close();
            c.close();
        }
    }

    public static void main(String[] a) throws Exception {
        BotChucNang.dk("dt_tran", true, KichBanDauTruong::tran);
        List<String> chon = new ArrayList<>();
        for (int i = 3; i < a.length; i++) chon.add(a[i]);
        if (chon.isEmpty()) chon.add("dt_tran");
        String[] b = new String[3 + chon.size()];
        b[0] = a[0];
        b[1] = a[1];
        b[2] = a[2];
        for (int i = 0; i < chon.size(); i++) b[3 + i] = chon.get(i);
        BotChucNang.main(b);
    }
}
