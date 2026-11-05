import com.htbb.cauhinh.CauHinhTrungTam;
import com.htbb.cauhinh.DinhNghiaKhoa;
import com.htbb.pokemon.ChuyenLuyen;
import com.htbb.pokemon.NapTienLogic;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * CHUYEN LUYEN (chu game 05/11/2026) - phan THUAN com.htbb.pokemon.ChuyenLuyen (dieu kien, cap ket qua co / khong bua,
 * so bua, gia xu / da, khoa) + du lieu (khoa danh muc chuyen_luyen.* o SQL phan A + B / gen.ts / docs, bua 1262, Kecleon +
 * cua hang luong, menu Machamp, tai lieu). Luong game (tru nguyen tu, chi so, khoa, pity): test/real/KiemChuyenLuyenGame.
 */
public final class KiemChuyenLuyen {

    private KiemChuyenLuyen() {
    }

    static String doc(final String f) {
        try {
            return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }

    static ChuyenLuyen.Mon m(final int id, final int type, final int cap, final int plus) {
        return new ChuyenLuyen.Mon(id, type, cap, plus, false, false);
    }

    public static void chay() {
        Kiem.nhom("Chuyen luyen (ChuyenLuyen): dieu kien, cap ket qua, bua, gia xu / da, khoa, du lieu");
        try {
            dieuKien();
            gia();
            khoa();
            duLieu();
        } catch (final Throwable e) {
            e.printStackTrace();
            Kiem.bang("KiemChuyenLuyen khong nem loi", "", String.valueOf(e));
        }
    }

    static void dieuKien() {
        Kiem.bang("o: vu khi 3..7 cung mot o", Integer.valueOf(ChuyenLuyen.o(3)), Integer.valueOf(ChuyenLuyen.o(7)));
        Kiem.bang("o: khan (0) rieng", Integer.valueOf(0), Integer.valueOf(ChuyenLuyen.o(0)));
        Kiem.bang("o: ruy bang 19", Integer.valueOf(19), Integer.valueOf(ChuyenLuyen.o(19)));
        Kiem.bang("o: cuoc 13 khong", Integer.valueOf(-1), Integer.valueOf(ChuyenLuyen.o(13)));
        Kiem.bang("o: do thu cuoi 14 khong", Integer.valueOf(-1), Integer.valueOf(ChuyenLuyen.o(14)));
        Kiem.bang("o: canh 29 khong", Integer.valueOf(-1), Integer.valueOf(ChuyenLuyen.o(29)));
        final ChuyenLuyen.Mon a = m(1, 3, 30, 10);
        Kiem.bang("vu khi +10 cap 30 -> vu khi khac lop cap 40 +0: duoc", null, ChuyenLuyen.lyDo(a, m(2, 5, 40, 0)));
        Kiem.bang("cung cap: duoc", null, ChuyenLuyen.lyDo(a, m(2, 3, 30, 3)));
        Kiem.dung("khac o (vu khi -> khan): khong", ChuyenLuyen.lyDo(a, m(2, 0, 40, 0)) != null);
        Kiem.dung("khan -> dai: khong", ChuyenLuyen.lyDo(m(1, 0, 30, 5), m(2, 1, 30, 0)) != null);
        Kiem.dung("cap nhan thap hon: khong", ChuyenLuyen.lyDo(a, m(2, 3, 29, 0)) != null);
        Kiem.dung("nhan da +10: khong", ChuyenLuyen.lyDo(a, m(2, 3, 40, 10)) != null);
        Kiem.dung("nhan +12 > nguon: khong", ChuyenLuyen.lyDo(a, m(2, 3, 40, 12)) != null);
        Kiem.dung("cung mot mon: khong", ChuyenLuyen.lyDo(a, m(1, 3, 30, 0)) != null);
        Kiem.dung("nguon +0: khong", ChuyenLuyen.lyDoNguon(m(1, 3, 30, 0)) != null);
        Kiem.dung("nguon dang rao ban: khong", ChuyenLuyen.lyDoNguon(new ChuyenLuyen.Mon(1, 3, 30, 5, true, false)) != null);
        Kiem.dung("nhan dang rao ban: khong", ChuyenLuyen.lyDo(a, new ChuyenLuyen.Mon(2, 3, 40, 0, true, false)) != null);
        Kiem.dung("nguon thoi trang / thu cuoi / bao vat: khong", ChuyenLuyen.lyDoNguon(new ChuyenLuyen.Mon(1, 3, 30, 5, false, true)) != null);
        Kiem.dung("nhan thoi trang: khong", ChuyenLuyen.lyDo(a, new ChuyenLuyen.Mon(2, 3, 40, 0, false, true)) != null);
        Kiem.dung("nguon null: khong", ChuyenLuyen.lyDo(null, m(2, 3, 40, 0)) != null);
        // cap ket qua
        Kiem.bang("khong bua: +10 -> +9", Integer.valueOf(9), Integer.valueOf(ChuyenLuyen.capKetQua(10, false)));
        Kiem.bang("co bua: +10 -> +10", Integer.valueOf(10), Integer.valueOf(ChuyenLuyen.capKetQua(10, true)));
        Kiem.bang("khong bua +1 -> +0", Integer.valueOf(0), Integer.valueOf(ChuyenLuyen.capKetQua(1, false)));
        Kiem.sai("nguon +5 nhan +4: khong bua khong loi gi (ra +4)", ChuyenLuyen.coLoi(m(1, 3, 30, 5), m(2, 3, 30, 4), false));
        Kiem.dung("nguon +5 nhan +4: co bua len +5", ChuyenLuyen.coLoi(m(1, 3, 30, 5), m(2, 3, 30, 4), true));
        Kiem.sai("nguon +1: khong bua khong loi", ChuyenLuyen.coLoi(m(1, 3, 30, 1), m(2, 3, 30, 0), false));
        Kiem.dung("nguon +1: co bua -> +1", ChuyenLuyen.coLoi(m(1, 3, 30, 1), m(2, 3, 30, 0), true));
        Kiem.bang("so bua +1..+9 / +10..+12 / +13..+15", "1/1/2/2/3/3", ChuyenLuyen.soBua(1) + "/" + ChuyenLuyen.soBua(9) + "/"
                + ChuyenLuyen.soBua(10) + "/" + ChuyenLuyen.soBua(12) + "/" + ChuyenLuyen.soBua(13) + "/" + ChuyenLuyen.soBua(15));
    }

    static void gia() {
        final long[] c = new long[16];
        for (int k = 1; k <= 15; k++) {
            c[k] = 1000L * k;
        }
        Kiem.bang("xu: 35% tong +1..+3 (6000) = 2100", Long.valueOf(2100L), Long.valueOf(ChuyenLuyen.xu(c, 3, 35)));
        Kiem.bang("xu: lam tron len (1000 x 35% x ... +1)", Long.valueOf(350L), Long.valueOf(ChuyenLuyen.xu(c, 1, 35)));
        Kiem.bang("xu: R = 0 -> 0", Long.valueOf(0L), Long.valueOf(ChuyenLuyen.xu(c, 0, 35)));
        Kiem.bang("xu: tang theo R", Boolean.TRUE, Boolean.valueOf(ChuyenLuyen.xu(c, 10, 35) < ChuyenLuyen.xu(c, 11, 35)));
        Kiem.bang("xu: kep 100%", Long.valueOf(ChuyenLuyen.xu(c, 5, 100)), Long.valueOf(ChuyenLuyen.xu(c, 5, 500)));
        final int[] da = {1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 4, 4, 4, 4, 4};
        Kiem.bang("da: duoi +10 khong can", Integer.valueOf(0), Integer.valueOf(ChuyenLuyen.da(da, 9, 50)));
        Kiem.bang("da 50%: +10..+15 = 1/3/5/7/9/11", "1/3/5/7/9/11", ChuyenLuyen.da(da, 10, 50) + "/" + ChuyenLuyen.da(da, 11, 50) + "/"
                + ChuyenLuyen.da(da, 12, 50) + "/" + ChuyenLuyen.da(da, 13, 50) + "/" + ChuyenLuyen.da(da, 14, 50) + "/" + ChuyenLuyen.da(da, 15, 50));
        Kiem.bang("da 0% -> 0", Integer.valueOf(0), Integer.valueOf(ChuyenLuyen.da(da, 15, 0)));
        Kiem.bang("mac dinh 35% xu / 50% da / bua 1262 / 20 luong", "35/50/1262/20", ChuyenLuyen.XU_PHAN_TRAM_MAC_DINH + "/"
                + ChuyenLuyen.DA_PHAN_TRAM_MAC_DINH + "/" + ChuyenLuyen.ID_BUA + "/" + ChuyenLuyen.GIA_LUONG);
    }

    static void khoa() {
        Kiem.bang("nguon mo, nhan mo -> mo", "0/0", s(ChuyenLuyen.khoaSau(0, 0, 0, 0, false)));
        Kiem.bang("nguon khoa vinh vien -> nhan khoa vinh vien", "1/0", s(ChuyenLuyen.khoaSau(1, 0, 0, 0, false)));
        Kiem.bang("nguon khoa do mac -> nhan khoa do mac (pha khoa duoc nhu nguon)", "1/1", s(ChuyenLuyen.khoaSau(1, 1, 0, 0, false)));
        Kiem.bang("nguon khoa do mac, nhan khoa vinh vien -> giu vinh vien", "1/0", s(ChuyenLuyen.khoaSau(1, 1, 1, 0, false)));
        Kiem.bang("nguon mo, dung da / bua khoa -> nhan khoa vinh vien", "1/0", s(ChuyenLuyen.khoaSau(0, 0, 0, 0, true)));
        Kiem.bang("nguon mo, nhan khoa do mac -> giu", "1/1", s(ChuyenLuyen.khoaSau(0, 0, 1, 1, false)));
    }

    static String s(final int[] k) {
        return k[0] + "/" + k[1];
    }

    static void duLieu() {
        final String sql = doc("../sql/htbb.sql");
        final String gen = doc("../web/shared/utils/cauHinhTrungTam.gen.ts");
        final String md = doc("../docs/cau-hinh.md");
        final String[] md0 = {"1", String.valueOf(ChuyenLuyen.XU_PHAN_TRAM_MAC_DINH), String.valueOf(ChuyenLuyen.DA_PHAN_TRAM_MAC_DINH)};
        for (int i = 0; i < ChuyenLuyen.KHOA.length; i++) {
            final String k = ChuyenLuyen.KHOA[i];
            final DinhNghiaKhoa d = CauHinhTrungTam.dinhNghia(k);
            Kiem.dung("danh muc co " + k, d != null);
            if (d != null) {
                Kiem.bang(k + " mac dinh", md0[i], d.macDinh);
                Kiem.bang(k + " nhom kinh_te", "kinh_te", d.nhom);
            }
            Kiem.dung("phan A + B sql co " + k + " (chinh)", sql.contains("('" + k + "','kinh_te'") && sql.matches("(?s).*\\('"
                    + java.util.regex.Pattern.quote(k) + "', 'kinh_te'[^\\n]*'real.ChuyenLuyenGame', \\d+, 'chinh', 0\\);.*"));
            Kiem.dung("gen.ts co " + k, gen.contains("khoa: \"" + k + "\""));
            Kiem.dung("docs/cau-hinh.md co " + k, md.contains("| `" + k + "` |"));
        }
        final String s = DayDu.phanNangCap("them_chuyen_luyen.sql");
        Kiem.dung("sql/htbb.sql co khoi them_chuyen_luyen.sql", s.length() > 500);
        Kiem.sai("khoi chuyen luyen: khong DROP / DELETE / TRUNCATE", s.contains("DROP ") || s.contains("DELETE ") || s.contains("TRUNCATE"));
        Kiem.dung("khoi chuyen luyen: luat bang nguoi choi", KiemDuLieuSql.khongPhaNguoiChoi(s));
        Kiem.bang("data_item 1262 = Bua chuyen luyen type 28", "Bùa chuyển luyện|28",
                DayDu.giaTri("data_item", "id", "1262", "name") + "|" + DayDu.giaTri("data_item", "id", "1262", "type"));
        Kiem.bang("data_vat_pham_lau_dai 1262 nhom bua, hieu ung chuyen_luyen", "bua|chuyen_luyen:1",
                DayDu.giaTri("data_vat_pham_lau_dai", "id_item", "1262", "nhom") + "|" + DayDu.giaTri("data_vat_pham_lau_dai", "id_item", "1262", "hieu_ung"));
        Kiem.bang("Kecleon 131: 20 luong khoa -> 1 bua", "luong_khoa:" + ChuyenLuyen.GIA_LUONG_KHOA + "|ITEM:1262:1",
                DayDu.giaTri("data_cua_hang_lau_dai", "id", "131", "chi_phi") + "|" + DayDu.giaTri("data_cua_hang_lau_dai", "id", "131", "thuong"));
        Kiem.bang("cua hang luong: bua_chuyen_luyen 20 luong, trao bua_chuyen_luyen", "20|bua_chuyen_luyen",
                DayDu.giaTri("nap_cua_hang", "ma", "bua_chuyen_luyen", "gia_luong") + "|" + DayDu.giaTri("nap_cua_hang", "ma", "bua_chuyen_luyen", "trao"));
        Kiem.dung("luong >= luong khoa: gia luong <= gia luong khoa", ChuyenLuyen.GIA_LUONG <= ChuyenLuyen.GIA_LUONG_KHOA);
        Kiem.dung("trao bua_chuyen_luyen trong whitelist (Java + web)", java.util.Arrays.asList(NapTienLogic.TRAO_HOP_LE)
                .contains(NapTienLogic.TRAO_BUA_CHUYEN_LUYEN) && doc("../web/server/utils/napTienThuan.ts").contains("'bua_chuyen_luyen'] as const"));
        Kiem.dung("kiemMon tu choi bua chuyen luyen ma khac", NapTienLogic.kiemMon(new NapTienLogic.Mon("bua_khac", "Bùa chuyển luyện",
                "tien_ich", NapTienLogic.TRAO_BUA_CHUYEN_LUYEN, 20, 0, 0, 0, false)) != null);
        Kiem.dung("kiemMon tu choi bua chuyen luyen co han / giao dich", NapTienLogic.kiemMon(new NapTienLogic.Mon(ChuyenLuyen.MA_CUA_HANG,
                "Bùa chuyển luyện", "tien_ich", NapTienLogic.TRAO_BUA_CHUYEN_LUYEN, 20, 7, 0, 0, true)) != null);
        final String nt = doc("src/com/htbb/pokemon/NapTienGame.java");
        Kiem.dung("mua bang luong: bua KHONG khoa", nt.contains("Char.createItemSoLuong(ChuyenLuyen.ID_BUA, 1, p, false)"));
        // menu Machamp + duong vao
        final String rq = doc("src/real/cmd/RequestNPCInfoHandler.java");
        Kiem.dung("Machamp: muc Chuyen luyen sau Pha khoa, truoc Che tao", rq.indexOf("ChuyenLuyenGame.themMucThoRen(menu)") > 0
                && rq.indexOf("ChuyenLuyenGame.themMucThoRen(menu)") < rq.indexOf("BoNgocGame.themMucThoRen(menu)"));
        final String dm = doc("src/real/MapDoMenu.java");
        Kiem.dung("MapDoMenu: case 2 muc 3 -> ChuyenLuyenGame.moMenu, NPC ao -> doMenu",
                dm.contains("idOptionMenu == ChuyenLuyenGame.VI_TRI_MUC_THO_REN") && dm.contains("idNpc == ChuyenLuyenGame.NPC_ID"));
        final String g = doc("src/real/ChuyenLuyenGame.java");
        final String[] m = real.ChuyenLuyenGame.themMucThoRen(new String[]{"Mua đồ cầm tay", "Mua trang bị thú", real.PhaKhoaGame.TEN_MUC});
        Kiem.bang("menu Machamp: muc 4 = Chuyển luyện", "Chuyển luyện", m.length > 3 ? m[3] : "");
        Kiem.bang("vi tri muc = 3", Integer.valueOf(3), Integer.valueOf(real.ChuyenLuyenGame.VI_TRI_MUC_THO_REN));
        Kiem.bang("menu Machamp: muc 5 = Mua Bùa chuyển luyện", "Mua Bùa chuyển luyện", m.length > 4 ? m[4] : "");
        Kiem.dung("MapDoMenu: case 2 muc 4 -> ChuyenLuyenGame.moMuaBua(player, false)",
                dm.contains("idOptionMenu == ChuyenLuyenGame.VI_TRI_MUA_BUA_THO_REN") && dm.contains("ChuyenLuyenGame.moMuaBua(player, false)"));
        Kiem.dung("NpcDichVu: Kecleon muc 3 -> ChuyenLuyenGame.moMuaBua(p, true)", doc("src/real/NpcDichVu.java")
                .contains("chon == VeDichChuyenGame.MUC_KECLEON_BUA") && doc("src/real/NpcDichVu.java").contains("ChuyenLuyenGame.moMuaBua(p, true)"));
        Kiem.dung("mua bua dung chung CuaHangLauDai.mua (khong lap logic tien)", g.contains("CuaHangLauDai.mua(p, r, n)")
                && !g.contains("subLuongLock") && !g.contains("subLuong("));
        Kiem.dung("game: tru va doi mon trong MOT p.truNeuDu", g.contains("final boolean xong = p.truNeuDu(") && g.indexOf("NangCapLauDaiGame.doiChiSo(b")
                > g.indexOf("final boolean xong = p.truNeuDu("));
        final String tk = doc("../docs/thiet-ke-game.md");
        Kiem.dung("thiet-ke-game.md: muc Chuyen luyen co so lieu", tk.contains("### Chuyển luyện") && tk.contains("Bùa chuyển luyện")
                && tk.contains("35%") && tk.contains("+(N−1)"));
        Kiem.dung("sql/README.md liet ke them_chuyen_luyen.sql", doc("../sql/README.md").contains("them_chuyen_luyen.sql"));
    }
}
