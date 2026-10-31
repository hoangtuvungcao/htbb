import com.htbb.pokemon.HoTongNhiemVu;
import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.NhiemVuMau;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import real.Char;
import real.HoTongNhiemVuGame;
import real.MonsterVantieu;
import real.NhiemVuEngine;

/**
 * CHU GAME 31/10/2026 (bot cb2: giao xe nhiem vu N-3.36 o cap 49 nhan ca thuong van tieu 3.000.000 EXP + giap thu cuoi tren
 * thuong nhiem vu): xe tu LUOT MIEN PHI cua nhiem vu ho tong chi tra thuong nhiem vu (buoc van tinh); xe tu luot van tieu
 * thuong trong ngay: thuong van tieu nhu cu + buoc van tinh. Kiem:
 * <ul>
 *   <li>HoTongNhiemVu.traThuongVanTieu;</li>
 *   <li>Char.doResponseTieu voi xe mien phi: khong EXP / vat pham / luong, buoc ho_tong len 1/1, co cau bao;</li>
 *   <li>xe mien phi bi cuop: khong roi vat pham van tieu (MonsterVantieu.dropItem rong);</li>
 *   <li>noi day: HoTongNhiemVuGame.nhanHang danh dau xe CHI khi vua cap luot mien phi; Char.doResponseTieu / dropItem.</li>
 * </ul>
 */
public final class KiemHoTongThuongMienPhi {

    private KiemHoTongThuongMienPhi() {
    }

    static final int ID = 987_654_322;

    public static void chay() {
        System.out.println("== Ho tong: xe tu luot mien phi nhiem vu chi tra thuong nhiem vu (chu game 31/10/2026)");
        Kiem.sai("xe mien phi nhiem vu: khong tra thuong van tieu", HoTongNhiemVu.traThuongVanTieu(true));
        Kiem.dung("xe tu luot van tieu thuong: tra thuong van tieu", HoTongNhiemVu.traThuongVanTieu(false));
        try {
            giao();
        } catch (final Throwable e) {
            Kiem.dung("khong nem loi: " + e, false);
        }
        noiDay();
    }

    static MonsterVantieu xe(final real.Map m) {
        try {
            return new MonsterVantieu(m, new real.MonsterTemplate(), 0, 0, 0);
        } catch (final Throwable e) {
            return null;
        }
    }

    static void giao() {
        final boolean batCu = LauDai.bat();
        final real.GemTemplate[] gemCu = real.Map.gemTemplate;
        if (real.Map.gemTemplate == null) {
            real.Map.gemTemplate = new real.GemTemplate[0];
        }
        LauDai.dat(true);
        final NhiemVuMau m = KiemHoTong.mau();
        final ConcurrentHashMap<String, long[]> td = new ConcurrentHashMap<String, long[]>();
        try {
            final Char p = new Char(null);
            p.charDBID = ID;
            p.isBot = -1;
            p.charname = "kiemhtmp";
            p.lvDetail.lv = 49;
            final real.Map lang = new real.Map();
            lang.mapIDLoadMap = 0;
            p.map = lang;
            NhiemVuEngine.datKiem(Collections.singletonList(m), ID, td);
            final long[] v = new long[]{0L, 0L, 0L, 0L};
            td.put(m.ma, v);
            final MonsterVantieu x = xe(lang);
            Kiem.dung("tao duoc xe hang thu", x != null);
            if (x == null) {
                return;
            }
            x.mienPhiNhiemVu = true;
            p.monster = x;
            final long xp0 = p.xp;
            final int doVat0 = p.iItems.size();
            p.doResponseTieu(p, false, 0, 49);
            Kiem.bang("xe mien phi: buoc N-3.36 van len 1/1", 1L, v[0]);
            Kiem.bang("xe mien phi: khong EXP van tieu", xp0, (long) p.xp);
            Kiem.bang("xe mien phi: khong vat pham van tieu", doVat0, p.iItems.size());
            Kiem.dung("xe mien phi bi cuop: khong roi vat pham van tieu", x.dropItem().isEmpty());
        } finally {
            NhiemVuEngine.xoaKiem(ID);
            HoTongNhiemVuGame.xoaMienPhi();
            LauDai.dat(batCu);
            real.Map.gemTemplate = gemCu;
        }
    }

    static String doc(final String f) {
        try {
            return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }

    static void noiDay() {
        final String g = doc("src/real/HoTongNhiemVuGame.java");
        Kiem.dung("nhanHang: danh dau xe chi khi vua cap luot mien phi (capMienPhi = true)",
                g.contains("final boolean mienPhi = dang && capMienPhi(p);") && g.contains("p.monster.mienPhiNhiemVu = true;"));
        final String c = doc("src/real/Char.java");
        final int i = c.indexOf("public void doResponseTieu(");
        final String than = i < 0 ? "" : c.substring(i, Math.min(c.length(), i + 1200));
        Kiem.dung("Char.doResponseTieu: xe mien phi -> chi diem moc nhiem vu, return truoc thuong van tieu",
                than.indexOf("traThuongVanTieu(pReceive.monster.mienPhiNhiemVu)") >= 0
                        && than.indexOf("traThuongVanTieu(pReceive.monster.mienPhiNhiemVu)") < than.indexOf("CharSuKien.doResponseTieu"));
        Kiem.dung("Char.doResponseTieu: xe thuong van goi CharSuKien + diem moc", than.contains("CharSuKien.doResponseTieu(this, pReceive,isThief,type,lvOpen);")
                && than.contains("DiemMocNhiemVu.khiHoTong(pReceive); // diem moc ho_tong"));
        final String mv = doc("src/real/MonsterVantieu.java");
        Kiem.dung("MonsterVantieu.dropItem: xe mien phi khong roi vat pham", mv.contains("if (!com.htbb.pokemon.HoTongNhiemVu.traThuongVanTieu(this.mienPhiNhiemVu))"));
    }
}
