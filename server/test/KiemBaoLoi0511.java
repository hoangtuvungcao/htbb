import com.htbb.cauhinh.CauHinhTrungTam;
import com.htbb.cauhinh.DinhNghiaKhoa;
import com.htbb.pokemon.BatPokemon;
import com.htbb.pokemon.NangCapLauDai;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bao loi nguoi choi 05/11/2026 toi:
 * <ul>
 *   <li>kazuhas4 "+9 len +10 bao thieu da luyen": da luon can 2 vien tu +6..+10 (khong doi), nhung cau bao chi liet ke
 *   GIA nen nguoi co 2.199 da ma thieu xu tuong la thieu da -> {@link NangCapLauDai#chuThieu} noi ro phan thieu.</li>
 *   <li>rongngoo "cap 16 doi danh xuong 30%, danh mot phat chet": "danh giu" ({@link BatPokemon#satThuongGiu},
 *   real.DanhGiuGame) + khoa bat.danh_giu.* + moi duong danh quai cua ban do thuong deu goi.</li>
 * </ul>
 */
public final class KiemBaoLoi0511 {

    private KiemBaoLoi0511() {
    }

    static String doc(final String f) {
        try {
            return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }

    public static void chay() {
        Kiem.nhom("Bao loi 05/11: cau thieu nguyen lieu nang cap + danh giu Pokemon hoang da");
        try {
            thieuNangCap();
            danhGiu();
            duLieu();
            duongDanh();
        } catch (final Throwable e) {
            e.printStackTrace();
            Kiem.bang("KiemBaoLoi0511 khong nem loi", "", String.valueOf(e));
        }
    }

    static void thieuNangCap() {
        // kazuhas4 19:24: con 22.228 xu, +10 bac chuyen gia can 33.553 xu + 2 da, co 2.199 da
        Kiem.bang("thieu xu, du da: chi bao xu", " Còn thiếu: 11325 xu.", NangCapLauDai.chuThieu(22228, 33553, true, 2199, 2, false));
        Kiem.bang("du xu, thieu da", " Còn thiếu: 1 đá luyện kim.", NangCapLauDai.chuThieu(50000, 33553, true, 1, 2, false));
        Kiem.bang("da khoa", " Còn thiếu: 2 đá luyện kim khoá.", NangCapLauDai.chuThieu(50000, 33553, true, 0, 2, true));
        Kiem.bang("chua dat da vao o", " Còn thiếu: 5 xu, chưa đặt đá luyện kim vào ô nâng cấp.",
                NangCapLauDai.chuThieu(10, 15, false, 0, 1, false));
        Kiem.bang("du het (thieu luong / vat lieu phu): rong", "", NangCapLauDai.chuThieu(50000, 33553, true, 5, 2, false));
        final String g = doc("src/real/NangCapLauDaiGame.java");
        Kiem.dung("NangCapLauDaiGame.thu noi them NangCapLauDai.chuThieu vao cau THIEU", g.contains("NangCapLauDai.chuThieu(p.xu, xu, idLkd >= 0"));
    }

    static void danhGiu() {
        final int ng = BatPokemon.NGUONG_HP_PHAN_TRAM;
        final int giu = BatPokemon.DANH_GIU_HP_PHAN_TRAM;
        Kiem.dung("mac dinh giu 10..25% va <= nguong bat", giu >= 10 && giu <= 25 && giu <= ng);
        Kiem.bang("hpGiu 1000 maxhp, 15% -> 150", Long.valueOf(150), Long.valueOf(BatPokemon.hpGiu(1000, ng, giu)));
        Kiem.bang("hpGiu kep <= nguong (giu 80% nguong 35)", Long.valueOf(350), Long.valueOf(BatPokemon.hpGiu(1000, 35, 80)));
        Kiem.bang("hpGiu toi thieu 1 (maxhp 3)", Long.valueOf(1), Long.valueOf(BatPokemon.hpGiu(3, 35, 15)));
        // rongngoo: con day mau 1000, mot phat 5000 -> con 150 (trong o nem bong)
        final int d1 = BatPokemon.satThuongGiu(1000, 1000, 1000, 5000, ng, giu);
        Kiem.bang("day mau, don 5000: sat thuong cat con 850 (con 150 HP)", Integer.valueOf(850), Integer.valueOf(d1));
        Kiem.dung("sau don giu: kiem bat OK (<= 35%)", 150 * 100L <= 1000L * ng);
        Kiem.bang("don sau (150 HP, trong o): chet binh thuong", Integer.valueOf(5000),
                Integer.valueOf(BatPokemon.satThuongGiu(150, 150, 1000, 5000, ng, giu)));
        Kiem.bang("don khong ha (1000 -> 600): giu nguyen", Integer.valueOf(400),
                Integer.valueOf(BatPokemon.satThuongGiu(1000, 1000, 1000, 400, ng, giu)));
        Kiem.bang("don ha dung 0 tu tren nguong: van giu", Integer.valueOf(850),
                Integer.valueOf(BatPokemon.satThuongGiu(1000, 1000, 1000, 1000, ng, giu)));
        Kiem.bang("dau don 40% (tren nguong), ha: con 150", Integer.valueOf(250),
                Integer.valueOf(BatPokemon.satThuongGiu(400, 400, 1000, 999, ng, giu)));
        Kiem.bang("dau don dung nguong 35%: khong giu (da nem duoc)", Integer.valueOf(999),
                Integer.valueOf(BatPokemon.satThuongGiu(350, 350, 1000, 999, ng, giu)));
        // phan sau cua cung mot don (than thu) dung hpDau cua don
        Kiem.bang("phan than thu cung don: hp hien 150, dau don 1000 -> 0 sat thuong", Integer.valueOf(0),
                Integer.valueOf(BatPokemon.satThuongGiu(1000, 150, 1000, 300, ng, giu)));
        Kiem.bang("phan than thu cung don khong ha: giu nguyen", Integer.valueOf(100),
                Integer.valueOf(BatPokemon.satThuongGiu(1000, 400, 1000, 100, ng, giu)));
        Kiem.bang("hp hien da duoi hpGiu (hut HP), phan ha: 0", Integer.valueOf(0),
                Integer.valueOf(BatPokemon.satThuongGiu(1000, 100, 1000, 300, ng, giu)));
        Kiem.bang("sat thuong 0 / am: giu nguyen", Integer.valueOf(0), Integer.valueOf(BatPokemon.satThuongGiu(1000, 1000, 1000, 0, ng, giu)));
        // chot cuoi don
        Kiem.bang("chot: con -200 dau don tren nguong -> 150", Long.valueOf(150), Long.valueOf(BatPokemon.hpSauDon(1000, -200, 1000, ng, giu)));
        Kiem.bang("chot: con song -> khong doi", Long.valueOf(500), Long.valueOf(BatPokemon.hpSauDon(1000, 500, 1000, ng, giu)));
        Kiem.bang("chot: dau don trong o -> chet", Long.valueOf(-5), Long.valueOf(BatPokemon.hpSauDon(300, -5, 1000, ng, giu)));
        Kiem.dung("chot: khong bao gio hoi mau quai (khong vuot dau don)", BatPokemon.hpSauDon(351, 0, 1000, ng, giu) <= 351);
        // EXP: phan sat thuong 2 don = HP that, khong cong 2 lan (Monster.getXpReceive >= 100% tra ca xp)
        Kiem.dung("EXP: don giu < HP con (getXpReceive tinh theo %, khong tra ca xp)", d1 < 1000);
    }

    static void duLieu() {
        final String sql = doc("../sql/htbb.sql");
        final String gen = doc("../web/shared/utils/cauHinhTrungTam.gen.ts");
        final String md = doc("../docs/cau-hinh.md");
        final String[][] k = {{"bat.danh_giu.bat", "1"}, {"bat.danh_giu.hp_phan_tram", String.valueOf(BatPokemon.DANH_GIU_HP_PHAN_TRAM)}};
        for (final String[] x : k) {
            final DinhNghiaKhoa d = CauHinhTrungTam.dinhNghia(x[0]);
            Kiem.dung("danh muc co " + x[0], d != null);
            if (d != null) {
                Kiem.bang(x[0] + " mac dinh", x[1], d.macDinh);
                Kiem.bang(x[0] + " nhom trang_bi", "trang_bi", d.nhom);
            }
            Kiem.dung("phan A sql co " + x[0], sql.contains("('" + x[0] + "','trang_bi'"));
            Kiem.dung("phan B sql co " + x[0] + " (co_dinh)", sql.matches("(?s).*\\('" + Pattern.quote(x[0])
                    + "', 'trang_bi'[^\\n]*'real.DanhGiuGame', \\d+, 'co_dinh', 1\\);.*"));
            Kiem.dung("gen.ts co " + x[0], gen.contains("khoa: \"" + x[0] + "\""));
            Kiem.dung("docs/cau-hinh.md co " + x[0], md.contains("| `" + x[0] + "` |"));
        }
        Kiem.dung("game doc dung khoa", doc("src/real/DanhGiuGame.java").contains("\"bat.danh_giu.bat\"")
                && doc("src/real/DanhGiuGame.java").contains("\"bat.danh_giu.hp_phan_tram\""));
        Kiem.dung("docs/thiet-ke-game.md co muc Danh giu", doc("../docs/thiet-ke-game.md").contains("**Đánh giữ**"));
    }

    /** Moi cho tru HP quai cua ban do thuong (MapDanhNhau / Region) deu qua danh giu. */
    static void duongDanh() {
        for (final String f : new String[]{"src/real/MapDanhNhau.java", "src/real/Region.java"}) {
            final String s = doc(f);
            Kiem.bang(f + ": 2 duong danh quai (don le + nhieu muc tieu) co hpDauGiu", Integer.valueOf(2), Integer.valueOf(dem(s, "final int hpDauGiu = mt.hp;")));
            Kiem.bang(f + ": than thu qua gioiHan", Integer.valueOf(2), Integer.valueOf(dem(s, "DanhGiuGame.gioiHan(p, mt, p.getDamtThanThu(")));
            Kiem.bang(f + ": chot cuoi don", Integer.valueOf(2), Integer.valueOf(dem(s, "DanhGiuGame.chot(p, mt, hpDauGiu);")));
            Kiem.bang(f + ": than thu khong con duong tru truc tiep", Integer.valueOf(0), Integer.valueOf(dem(s, "int damthanthu = p.getDamtThanThu(")));
        }
        Kiem.bang("CharTrangBi: 4 hieu ung do qua gioiHan", Integer.valueOf(4),
                Integer.valueOf(dem(doc("src/real/CharTrangBi.java"), "DanhGiuGame.gioiHan(self, actor, d, actor.hp)")));
        Kiem.dung("CharCapNhat.buffAttackSkill: phan don qua gioiHan", doc("src/real/CharCapNhat.java").contains("DanhGiuGame.gioiHan(self, mt, damPhan, mt.hp)"));
        Kiem.dung("Pokemon hoang da chi o ban do Map thuong (LoaiQuaiGame.duDieuKien)",
                doc("src/real/LoaiQuaiGame.java").contains("m.map.getClass() != Map.class"));
    }

    static int dem(final String s, final String k) {
        int n = 0;
        final Matcher m = Pattern.compile(Pattern.quote(k)).matcher(s);
        while (m.find()) {
            n++;
        }
        return n;
    }
}
