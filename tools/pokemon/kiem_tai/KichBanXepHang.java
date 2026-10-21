import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Kich ban bot DAU XEP HANG + THUONG MUA PvP (0.9.7: real.XepHangPvpGame). Chay tren MAY CHU THU RIENG (GM: slv / mto),
 * da nap sql/them_danh_hieu.sql:
 *   xh_ghep   hai bot lech 2 cap mo NPC Dau truong (-31) -> "Đấu xếp hạng" -> vao hang -> may chu ghep va dua ca hai
 *             vao Dau truong 202 (thong bao "Trận xếp hạng: gặp ...")
 *   xh_roi    vao hang roi chon lai -> "Rời hàng" (menu doi chu) -> khong bi ghep
 *   xh_mua    dat dong tob_pvp_mua mua truoc (Elo Master, 12 tran) -> dang nhap -> nhan luong khoa + danh hieu mua,
 *             tob_pvp_thuong co dong; dang nhap lai -> KHONG nhan lan nua
 *
 *   java -cp /tmp/bot -Dban=907 -Ddb=htbb_db -Ddbport=3397 -Dtu=1 -Dden=6 KichBanXepHang 127.0.0.1 27729 kt [xh_...]
 */
public final class KichBanXepHang {

    private KichBanXepHang() {
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

    /** Mo menu NPC Dau truong o Pewter, chon muc chua `chu`. -1 neu khong thay. */
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

    static BotChucNang.Bot bot(int lv) throws Exception {
        BotChucNang.Bot b = BotChucNang.botMoi(0);
        b.gm("slv " + b.ten + " " + lv + " 0", 800);
        b.den(0, 22, 33);
        b.bom(800);
        return b;
    }

    static void ghep() throws Exception {
        try (BotChucNang.Bot a = bot(40); BotChucNang.Bot c = bot(42)) {
            int ma = a.moc(), mc = c.moc();
            int ia = chonDauTruong(a, "Đấu xếp hạng");
            BotChucNang.ket("xh.ghep.menu", ia >= 0, "muc=" + ia + " " + a.menuChu);
            BotChucNang.ket("xh.ghep.vao_hang", toast(a, ma).contains("hàng chờ"), toast(a, ma));
            chonDauTruong(c, "Đấu xếp hạng");
            boolean vao = a.cho(x -> x.mapId == 202, 8000) && c.cho(x -> x.mapId == 202, 4000);
            BotChucNang.ket("xh.ghep.dua_vao_dau_truong", vao, "a map " + a.mapId + " c map " + c.mapId);
            BotChucNang.ket("xh.ghep.bao_doi_thu", toast(a, ma).contains("gặp " + c.ten) && toast(c, mc).contains("gặp " + a.ten),
                    toast(a, ma) + " || " + toast(c, mc));
        }
    }

    static void roi() throws Exception {
        try (BotChucNang.Bot a = bot(60)) {
            int m = a.moc();
            chonDauTruong(a, "Đấu xếp hạng");
            int i = chonDauTruong(a, "Rời hàng");
            BotChucNang.ket("xh.roi", i >= 0 && toast(a, m).contains("Đã rời hàng"), toast(a, m));
            a.bom(3000);
            BotChucNang.ket("xh.roi.khong_ghep", a.mapId == 0, "map " + a.mapId);
        }
    }

    /** Dang nhap lai tai khoan (phien cu vua dong co the con giu vai giay: thu lai). */
    static BotChucNang.Bot vaoLai(String user) throws Exception {
        for (int lan = 0; lan < 5; lan++) {
            BotChucNang.Bot b = new BotChucNang.Bot(user);
            String l = b.dangNhap("123456");
            if (l == null) {
                l = b.vaoGame(0);
            }
            if (l == null) return b;
            System.out.println("vao lai " + user + ": " + l);
            b.close();
            Thread.sleep(3000);
        }
        throw new IllegalStateException("khong vao lai duoc " + user);
    }

    static void mua() throws Exception {
        String user;
        int db;
        int lk0;
        try (BotChucNang.Bot a = bot(50)) {
            user = a.user;
            db = a.charDb;
        }
        Thread.sleep(4000);
        // mua hien tai cua may chu (tob_pvp_mua ghi theo mua); dat dong mua truoc
        String muaNay = BotChucNang.sql1("SELECT COALESCE(MAX(mua), 0) FROM tob_pvp_mua");
        int muaTruoc = (muaNay == null ? 0 : Integer.parseInt(muaNay.trim())) - 1;
        String daCo = BotChucNang.sql1("SELECT COUNT(*) FROM tob_pvp_mua WHERE id_char = " + db);
        BotChucNang.sql("INSERT INTO tob_pvp_mua (mua, id_char, elo, thang, thua) VALUES (" + muaTruoc + ", " + db + ", 1650, 9, 3)"
                + " ON DUPLICATE KEY UPDATE elo = 1650, thang = 9, thua = 3");
        BotChucNang.sql("DELETE FROM tob_pvp_thuong WHERE id_char = " + db);
        String lkTruoc = BotChucNang.sql1("SELECT luonglock FROM tob_char WHERE id = " + db);
        BotChucNang.Bot b = vaoLai(user);
        int m = 0; // thuong phat ngay luc vao ban do: doc tu goi dau tien cua phien
        b.bom(6000); // thuong phat luc vao ban do (DanhHieuGame.khiVaoBanDo)
        String t = toast(b, m);
        String dong = BotChucNang.sql1("SELECT CONCAT(bac, ',', luong_khoa, ',', danh_hieu) FROM tob_pvp_thuong WHERE id_char = " + db
                + " AND mua = " + muaTruoc);
        BotChucNang.ket("xh.mua.thong_bao", t.contains("Thưởng mùa PvP") && t.contains("Master Ball"), t);
        BotChucNang.ket("xh.mua.ghi_csdl", "3,100,9745".equals(dong == null ? null : dong.trim()), "dong=" + dong + " (pvp_mua cu " + daCo + ")");
        b.close();
        Thread.sleep(4000); // luu nhan vat khi thoat
        String lkSau = BotChucNang.sql1("SELECT luonglock FROM tob_char WHERE id = " + db);
        lk0 = lkTruoc == null ? -1 : Integer.parseInt(lkTruoc.trim());
        BotChucNang.ket("xh.mua.luong_khoa", lkSau != null && Integer.parseInt(lkSau.trim()) == lk0 + 100, "luonglock " + lkTruoc + " -> " + lkSau);
        BotChucNang.ket("xh.mua.danh_hieu", t.contains("Master Ball mùa trước"), t);
        BotChucNang.Bot b2 = vaoLai(user);
        int m2 = 0;
        b2.bom(6000);
        String t2 = toast(b2, m2);
        String so = BotChucNang.sql1("SELECT COUNT(*) FROM tob_pvp_thuong WHERE id_char = " + db);
        BotChucNang.ket("xh.mua.khong_phat_lai", !t2.contains("Thưởng mùa PvP") && "1".equals(so == null ? null : so.trim()), t2 + " so dong=" + so);
        b2.close();
    }

    public static void main(String[] a) throws Exception {
        BotChucNang.dk("xh_ghep", true, KichBanXepHang::ghep);
        BotChucNang.dk("xh_roi", true, KichBanXepHang::roi);
        BotChucNang.dk("xh_mua", true, KichBanXepHang::mua);
        List<String> chon = new ArrayList<>();
        for (int i = 3; i < a.length; i++) chon.add(a[i]);
        if (chon.isEmpty()) chon.addAll(List.of("xh_ghep", "xh_roi", "xh_mua"));
        String[] b = new String[3 + chon.size()];
        b[0] = a[0];
        b[1] = a[1];
        b[2] = a[2];
        for (int i = 0; i < chon.size(); i++) b[3 + i] = chon.get(i);
        BotChucNang.main(b);
    }
}
