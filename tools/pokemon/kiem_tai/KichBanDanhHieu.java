import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Kich ban bot DANH HIEU (0.9.7: real.DanhHieuGame, com.htbb.pokemon.DanhHieuLogic, lenh -110 S->C / -111 C->S).
 * Chay tren MAY CHU THU RIENG da nap sql/them_danh_hieu.sql (can GM: slv / mto / die):
 *   dh_cap     len cap 55 (GM slv) + mo bang -> mo cap 10 / 30 / 50 + "Huấn luyện viên tập sự" + "Huấn luyện viên lành nghề" ngay (thong bao -104, so moi -110),
 *              danh hieu dau tien tu deo (nhan -110 cho chinh minh)
 *   dh_bang    xin bang (-111 0): co danh hieu da co / chua co, danh hieu an "???" khong lo ten
 *   dh_deo     deo (-111 1) -> nhan moi; deo danh hieu chua co -> tu choi; thao (-111 2) -> nhan rong; da xem -> so moi 0
 *   dh_nguoi   bot thu hai den canh: nhan danh hieu cua bot dau den nguoi gan
 *   dh_luu     guc 1 lan, thoat -> tob_danh_hieu_dem co 'guc' = 1, tob_danh_hieu co danh hieu, tob_danh_hieu_log co dong
 *
 *   javac -encoding UTF-8 -d /tmp/bot tools/pokemon/kiem_tai/*.java
 *   java -cp /tmp/bot -Dban=906 -Ddb=htbb_db -Ddbport=3397 -Dtu=1 -Dden=6 KichBanDanhHieu 127.0.0.1 27729 kt [dh_...]
 */
public final class KichBanDanhHieu {

    private KichBanDanhHieu() {
    }

    static final int LENH = -110, KHACH = -111;

    /** Dong bang da doc. */
    static final class Dong {
        int id, doHiem, trangThai, tienDo, can, phut;
        boolean an, moi;
        String ten, chiSo;
    }

    static List<Dong> bang(BotChucNang.Bot b, int tu) {
        for (int i = b.su.size() - 1; i >= tu; i--) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.cmd != LENH || e.d == null || e.d.length == 0 || e.d[0] != 0) continue;
            try {
                DataInputStream r = new DataInputStream(new ByteArrayInputStream(e.d));
                r.readByte();
                r.readByte();
                r.readShort();
                int nh = r.readByte();
                for (int k = 0; k < nh; k++) r.readUTF();
                int n = r.readShort();
                List<Dong> ds = new ArrayList<>();
                for (int k = 0; k < n; k++) {
                    Dong d = new Dong();
                    d.id = r.readShort();
                    d.ten = r.readUTF();
                    r.readUTF();
                    r.readUTF();
                    d.doHiem = r.readByte();
                    r.readUTF();
                    d.trangThai = r.readByte();
                    d.an = r.readByte() != 0;
                    d.tienDo = r.readInt();
                    d.can = r.readInt();
                    d.phut = r.readInt();
                    d.chiSo = r.readUTF();
                    r.readUTF();
                    d.moi = r.readByte() != 0;
                    ds.add(d);
                }
                return ds;
            } catch (Exception ex) {
                return null;
            }
        }
        return null;
    }

    /** Nhan -110 loai 1 cuoi cung cho nhan vat actor: {id danh hieu} hoac null. */
    static int[] nhan(BotChucNang.Bot b, int tu, int actor) {
        int[] kq = null;
        for (int i = tu; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.cmd != LENH || e.d == null || e.d.length < 5 || e.d[0] != 1) continue;
            try {
                DataInputStream r = new DataInputStream(new ByteArrayInputStream(e.d));
                r.readByte();
                int c = r.readShort();
                int id = r.readShort();
                if (c == (short) actor) kq = new int[]{id};
            } catch (Exception ignored) {
            }
        }
        return kq;
    }

    static int soMoi(BotChucNang.Bot b, int tu) {
        int kq = -1;
        for (int i = tu; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.cmd == LENH && e.d != null && e.d.length >= 3 && e.d[0] == 2) kq = ((e.d[1] & 255) << 8) | (e.d[2] & 255);
        }
        return kq;
    }

    /** Chu cua cac thong bao -104 (DungVatPham) tu moc. */
    static String toast(BotChucNang.Bot b, int tu) {
        StringBuilder s = new StringBuilder();
        for (int i = tu; i < b.su.size(); i++) {
            BotChucNang.SuKien e = b.su.get(i);
            if (e.cmd != -104 || e.d == null) continue;
            try {
                DataInputStream r = new DataInputStream(new ByteArrayInputStream(e.d));
                r.readByte();
                s.append(r.readUTF()).append(" | ");
            } catch (Exception ignored) {
            }
        }
        return s.toString();
    }

    static Dong tim(List<Dong> ds, int id) {
        if (ds == null) return null;
        for (Dong d : ds) if (d.id == id) return d;
        return null;
    }

    static BotChucNang.Bot bot;

    static BotChucNang.Bot a() throws Exception {
        if (bot == null) {
            bot = BotChucNang.botMoi(0);
            bot.den(2, 20, 20);
            bot.bom(1500);
        }
        return bot;
    }

    static void cap() throws Exception {
        BotChucNang.Bot b = a();
        int m = 0; // tu dau phien: danh hieu theo cap da co the mo ngay luc vao ban do (DanhHieuGame.khiVaoBanDo)
        b.gm("slv " + b.ten + " 55 0", 2500);
        // GM slv khong qua duong len cap that (SoTayLauDai.khiLenCap -> datMuc thanh_tuu_cap): mo bang = danh gia ngay
        b.gui(KHACH, (byte) 0);
        b.bom(2000);
        String t = toast(b, m);
        BotChucNang.ket("dh.cap.thong_bao", t.contains("Huấn luyện viên tập sự") && t.contains("Huấn luyện viên lành nghề")
                && t.contains("Huấn luyện viên tinh anh"), t);
        BotChucNang.ket("dh.cap.so_moi", soMoi(b, m) >= 3, "so moi=" + soMoi(b, m));
        int[] n = nhan(b, m, b.actor);
        BotChucNang.ket("dh.cap.tu_deo", n != null && n[0] == 9700, n == null ? "khong co nhan" : "nhan " + n[0]);
    }

    static void bangDh() throws Exception {
        BotChucNang.Bot b = a();
        int m = b.moc();
        b.gui(KHACH, (byte) 0);
        b.cho(x -> bang(x, m) != null, 4000);
        List<Dong> ds = bang(b, m);
        BotChucNang.ket("dh.bang.co", ds != null && ds.size() > 100, ds == null ? "khong co bang" : ds.size() + " dong");
        Dong c10 = tim(ds, 9700), c30 = tim(ds, 9701), c50 = tim(ds, 9703), an = tim(ds, 9714), cu = tim(ds, 8767);
        BotChucNang.ket("dh.bang.da_co", c10 != null && c10.trangThai == 2 && c30 != null && c30.trangThai == 1 && c30.moi,
                c10 == null || c30 == null ? "thieu" : c10.trangThai + "/" + c30.trangThai + " moi=" + c30.moi);
        BotChucNang.ket("dh.bang.tien_do", c50 != null && c50.trangThai == 0 && c50.tienDo == 55 && c50.can == 70,
                c50 == null ? "thieu" : c50.tienDo + "/" + c50.can);
        BotChucNang.ket("dh.bang.an", an != null && an.an && "???".equals(an.ten) && an.tienDo == 0, an == null ? "thieu" : an.ten + " an=" + an.an);
        BotChucNang.ket("dh.bang.cu_chi_khi_co", cu == null, cu == null ? "" : "lo danh hieu cu chua co");
        BotChucNang.ket("dh.bang.chi_so", c30 != null && c30.chiSo.contains("%"), c30 == null ? "" : c30.chiSo);
    }

    static void deo() throws Exception {
        BotChucNang.Bot b = a();
        int m = b.moc();
        b.gui(KHACH, (byte) 1, (short) 9701);
        b.bom(1500);
        int[] n = nhan(b, m, b.actor);
        Dong d = tim(bang(b, m), 9701);
        BotChucNang.ket("dh.deo", n != null && n[0] == 9701 && d != null && d.trangThai == 2, (n == null ? "khong nhan" : "nhan " + n[0]) + " " + toast(b, m));
        int m2 = b.moc();
        b.gui(KHACH, (byte) 1, (short) 9714);
        b.bom(1200);
        BotChucNang.ket("dh.deo.chua_co", toast(b, m2).contains("chưa có") && nhan(b, m2, b.actor) == null, toast(b, m2));
        int m3 = b.moc();
        b.gui(KHACH, (byte) 2);
        b.bom(1200);
        int[] n3 = nhan(b, m3, b.actor);
        BotChucNang.ket("dh.thao", n3 != null && n3[0] == -1, n3 == null ? "khong nhan" : "nhan " + n3[0]);
        b.gui(KHACH, (byte) 1, (short) 9701);
        b.bom(1000);
        int m4 = b.moc();
        b.gui(KHACH, (byte) 3);
        b.bom(1000);
        BotChucNang.ket("dh.da_xem", soMoi(b, m4) == 0, "so moi=" + soMoi(b, m4));
    }

    static void nguoi() throws Exception {
        BotChucNang.Bot b = a();
        try (BotChucNang.Bot c = BotChucNang.botMoi(0)) {
            int m = c.moc();
            c.den(2, 20, 21);
            c.bom(2500);
            int[] n = nhan(c, m, b.actor);
            StringBuilder lenh = new StringBuilder();
            for (int i = m; i < c.su.size(); i++) {
                BotChucNang.SuKien e = c.su.get(i);
                if (e.cmd <= -90 && e.cmd >= -110 && e.d != null && e.d.length >= 3) lenh.append(e.cmd).append(":").append(java.util.Arrays.toString(java.util.Arrays.copyOf(e.d, Math.min(8, e.d.length)))).append(",");
            }
            // goi -90 cua A (chuoi -90 -> ... -> -110) chi den khi may chu gui thong tin A cho B (A di chuyen / client xin):
            // chua co -90 cua A thi chua kiem duoc duong nay (BO QUA), duong doi danh hieu ben duoi van kiem
            boolean coThan = false;
            for (int i = m; i < c.su.size(); i++) {
                BotChucNang.SuKien e = c.su.get(i);
                if (e.cmd == -90 && e.d != null && e.d.length >= 2 && (short) (((e.d[0] & 255) << 8) | (e.d[1] & 255)) == (short) b.actor) coThan = true;
            }
            BotChucNang.ket("dh.nguoi_gan", !coThan && n == null ? null : Boolean.valueOf(n != null && n[0] == 9701), n == null ? "khong nhan nhan cua " + b.ten + " (actor " + b.actor + ") lenh " + lenh : "nhan " + n[0]);
            int m2 = c.moc();
            b.gui(KHACH, (byte) 1, (short) 9700);
            c.bom(2000);
            int[] n2 = nhan(c, m2, b.actor);
            BotChucNang.ket("dh.nguoi_gan.doi", n2 != null && n2[0] == 9700, n2 == null ? "khong cap nhat" : "nhan " + n2[0]);
        }
    }

    static void luu() throws Exception {
        BotChucNang.Bot b = a();
        b.gm("die " + b.ten, 1500);
        b.bom(1500);
        int db = b.charDb;
        b.close();
        bot = null;
        Thread.sleep(6000);
        String dem = BotChucNang.sql1("SELECT gia_tri FROM tob_danh_hieu_dem WHERE id_char = " + db + " AND khoa = 'guc'");
        BotChucNang.ket("dh.luu.bo_dem_guc", "1".equals(dem), "guc=" + dem);
        String so = BotChucNang.sql1("SELECT iddh FROM tob_danh_hieu WHERE id_char = " + db);
        BotChucNang.ket("dh.luu.so_huu", so != null && so.contains("9700") && so.contains("9701"), "iddh=" + so);
        String log = BotChucNang.sql1("SELECT COUNT(*) FROM tob_danh_hieu_log WHERE id_char = " + db);
        BotChucNang.ket("dh.luu.nhat_ky", log != null && Integer.parseInt(log.trim()) >= 2, "log=" + log);
        String moi = BotChucNang.sql1("SELECT IFNULL(tap,'') FROM tob_danh_hieu_dem WHERE id_char = " + db + " AND khoa = '#moi'");
        BotChucNang.ket("dh.luu.da_xem", moi == null || moi.trim().isEmpty(), "moi=" + moi);
    }

    public static void main(String[] a) throws Exception {
        BotChucNang.dk("dh_cap", true, KichBanDanhHieu::cap);
        BotChucNang.dk("dh_bang", true, KichBanDanhHieu::bangDh);
        BotChucNang.dk("dh_deo", true, KichBanDanhHieu::deo);
        BotChucNang.dk("dh_nguoi", true, KichBanDanhHieu::nguoi);
        BotChucNang.dk("dh_luu", true, KichBanDanhHieu::luu);
        List<String> chon = new ArrayList<>();
        for (int i = 3; i < a.length; i++) chon.add(a[i]);
        if (chon.isEmpty()) chon.addAll(List.of("dh_cap", "dh_bang", "dh_deo", "dh_nguoi", "dh_luu"));
        String[] b = new String[3 + chon.size()];
        b[0] = a[0];
        b[1] = a[1];
        b[2] = a[2];
        for (int i = 0; i < chon.size(); i++) b[3 + i] = chon.get(i);
        BotChucNang.main(b);
    }
}
