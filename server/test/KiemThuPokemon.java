import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import com.htbb.pokemon.GoiPokemon;
import com.htbb.pokemon.ThuPokemon;

/**
 * Kiem thu thu cuoi Pokemon (com.htbb.pokemon.ThuPokemon) + goi -93 POKE_CUOI.
 *
 * Mang ThuPokemon.DEX duoc chep tay tu du_lieu/bang_thu.csv - test nay doi chieu
 * TUNG dong "thu_cuoi" trong CSV voi mang Java, va so luong voi so hang so trong
 * data.Animal (BACH_MA..SU_TU_BANG = 21). Sua mot ben ma quen ben kia la do ngay.
 */
public final class KiemThuPokemon {

    public static void chay() {
        Kiem.nhom("com.htbb.pokemon.ThuPokemon - khop bang_thu.csv");
        Kiem.bang("21 loai thu cuoi", Integer.valueOf(21), Integer.valueOf(ThuPokemon.soLoai()));
        Kiem.bang("ngoai bang -> 0", Integer.valueOf(0), Integer.valueOf(ThuPokemon.dex(21)));
        Kiem.bang("am -> 0", Integer.valueOf(0), Integer.valueOf(ThuPokemon.dex(-1)));
        try {
            final List<String> dong = Files.readAllLines(new File("../du_lieu/bang_thu.csv").toPath(), StandardCharsets.UTF_8);
            int dem = 0;
            for (final String d : dong) {
                final String[] c = d.split(",");
                if (c.length < 6 || !"thu_cuoi".equals(c[0])) {
                    continue;
                }
                dem++;
                final int id = Integer.parseInt(c[1]);
                Kiem.bang("Animal " + c[2] + " (" + id + ") dex", Integer.valueOf(Integer.parseInt(c[3])),
                        Integer.valueOf(ThuPokemon.dex(id)));
                Kiem.bang("Animal " + c[2] + " shiny", Boolean.valueOf("1".equals(c[4])),
                        Boolean.valueOf(ThuPokemon.shiny(id)));
                Kiem.dung("co sprite da dong goi: " + c[3], new File(String.format(
                        "../client_pc_ios/Assets/Resources/poke/%04d%s_dl.bytes", Integer.parseInt(c[3]), "1".equals(c[4]) ? "s" : "")).isFile());
            }
            Kiem.bang("CSV co du 21 dong thu_cuoi", Integer.valueOf(21), Integer.valueOf(dem));
        } catch (final Exception e) {
            Kiem.dung("doc bang_thu.csv: " + e, false);
        }

        Kiem.nhom("Ten chieu Pokemon - server, client va bang_chieu.csv phai khop");
        try {
            final List<String> dong = Files.readAllLines(new File("../du_lieu/bang_chieu.csv").toPath(), StandardCharsets.UTF_8);
            final String client = new String(Files.readAllBytes(
                    new File("../client_pc_ios/Assets/Scripts/Assembly-CSharp/SkillManager.cs").toPath()), StandardCharsets.UTF_8);
            int dem = 0;
            for (final String d : dong.subList(1, dong.size())) {
                final String[] c = d.split(",");
                final int lop = Integer.parseInt(c[0]);
                final int so = Integer.parseInt(c[2]);
                Kiem.bang("server SKILL_NAME[" + lop + "][" + so + "]", c[4], real.MessageCreator.SKILL_NAME[lop][so]);
                Kiem.dung("client co \"" + c[4] + "\"", client.contains("\"" + c[4] + "\""));
                dem++;
            }
            int tong = 0;
            for (final String[] l : real.MessageCreator.SKILL_NAME) {
                tong += l.length;
            }
            Kiem.bang("CSV du moi skill server", Integer.valueOf(tong), Integer.valueOf(dem));
            for (int lop = 0; lop < 5; lop++) {
                final String[] l = real.MessageCreator.SKILL_NAME[lop];
                for (int k = 0; k < 3; k++) {
                    Kiem.bang("nameSkillLearn[" + lop + "][" + k + "] = 3 skill cuoi", l[l.length - 3 + k], data.Text.nameSkillLearn[lop][k]);
                }
            }
        } catch (final Exception e) {
            Kiem.dung("doi chieu ten chieu: " + e, false);
        }

        Kiem.nhom("com.htbb.pokemon.DaTienHoa - roi da khi Eevee ha quai");
        final int ti = com.htbb.pokemon.DaTienHoa.TI_LE_ROI_PHAN_NGHIN;
        Kiem.bang("Eevee 25 trung ti le -> da dau", Integer.valueOf(960),
                Integer.valueOf(com.htbb.pokemon.DaTienHoa.daRoiKhiHaQuai(133, 25, 25, 0, 0)));
        Kiem.bang("rDa 7 -> Ruy Bang Tien 967", Integer.valueOf(967),
                Integer.valueOf(com.htbb.pokemon.DaTienHoa.daRoiKhiHaQuai(133, 30, 30, ti - 1, 7)));
        Kiem.bang("ngoai ti le -> 0", Integer.valueOf(0),
                Integer.valueOf(com.htbb.pokemon.DaTienHoa.daRoiKhiHaQuai(133, 30, 30, ti, 0)));
        Kiem.bang("Eevee cap 24 -> 0", Integer.valueOf(0),
                Integer.valueOf(com.htbb.pokemon.DaTienHoa.daRoiKhiHaQuai(133, 24, 24, 0, 0)));
        Kiem.bang("khong phai Eevee -> 0", Integer.valueOf(0),
                Integer.valueOf(com.htbb.pokemon.DaTienHoa.daRoiKhiHaQuai(4, 40, 40, 0, 0)));
        Kiem.bang("da tien hoa roi (Vaporeon) -> 0", Integer.valueOf(0),
                Integer.valueOf(com.htbb.pokemon.DaTienHoa.daRoiKhiHaQuai(134, 40, 40, 0, 0)));
        Kiem.bang("quai thap hon 11 cap -> 0", Integer.valueOf(0),
                Integer.valueOf(com.htbb.pokemon.DaTienHoa.daRoiKhiHaQuai(133, 40, 29, 0, 0)));
        Kiem.bang("quai thap hon 10 cap van roi", Integer.valueOf(960),
                Integer.valueOf(com.htbb.pokemon.DaTienHoa.daRoiKhiHaQuai(133, 40, 30, 0, 0)));
        try {
            final String mon = new String(Files.readAllBytes(new File("src/real/Monster.java").toPath()), StandardCharsets.UTF_8);
            Kiem.dung("Monster.onDropItem goi RoiDaTienHoa", mon.contains("RoiDaTienHoa.khiHaQuai(p, this);"));
            // 3 boss ghi de onDropItem KHONG goi super -> phai tu goi 2 hook (roi da + Pokedex)
            for (final String b : new String[]{"BossTruRong", "BossTruRongMoba", "BossNguoiTuyet"}) {
                final String src = new String(Files.readAllBytes(new File("src/real/" + b + ".java").toPath()), StandardCharsets.UTF_8);
                Kiem.dung(b + ".onDropItem goi RoiDaTienHoa", src.contains("RoiDaTienHoa.khiHaQuai(p, this);"));
                Kiem.dung(b + ".onDropItem goi PokedexPokemon", src.contains("PokedexPokemon.khiHaQuai(p, this);"));
            }
        } catch (final Exception e) {
            Kiem.dung("doc Monster.java: " + e, false);
        }

        Kiem.nhom("com.htbb.pokemon.IconPokemon - icon PMD dai 14000+");
        Kiem.dung("14000 thuoc dai", com.htbb.pokemon.IconPokemon.thuocDai(14000));
        Kiem.sai("13999 (skin thu) khong thuoc dai", com.htbb.pokemon.IconPokemon.thuocDai(13999));
        Kiem.sai("15000 ngoai dai", com.htbb.pokemon.IconPokemon.thuocDai(15000));
        try {
            int dem = 0;
            for (final java.util.Map<String, String> r : DayDu.dong("data_item")) {
                final int id = Integer.parseInt(r.get("xstart"));
                if (!com.htbb.pokemon.IconPokemon.thuocDai(id)) {
                    continue;
                }
                dem++;
                if (!new File(com.htbb.pokemon.IconPokemon.THU_MUC + (id - com.htbb.pokemon.IconPokemon.ID_DAU) + ".png").isFile()) {
                    Kiem.dung("co file icon cho " + id + " (data_item " + r.get("id") + ")", false);
                }
            }
            Kiem.dung("data_item dung icon Pokemon (dai 14000+, du file) - " + dem + " dong", dem >= 22);
            final String dg = new String(Files.readAllBytes(new File("src/data/DataGame.java").toPath()), StandardCharsets.UTF_8);
            Kiem.dung("sendByteImg phuc vu dai icon Pokemon", dg.contains("IconPokemon.thuocDai(idImg)"));
            Kiem.dung("nIconItem van la 866 (khong dung dai cu)", dg.contains("nIconItem = 866;"));
        } catch (final Exception e) {
            Kiem.dung("doc data_item tu sql/htbb_day_du.sql: " + e, false);
        }

        Kiem.nhom("com.htbb.pokemon.TenMapPokemon - doi ten dia danh");
        Kiem.bang("Dao Châu", "Pallet", com.htbb.pokemon.TenMapPokemon.doi("Dao Châu"));
        Kiem.bang("Đông Dao Châu giu huong", "Đông Pallet", com.htbb.pokemon.TenMapPokemon.doi("Đông Dao Châu"));
        Kiem.bang("Đông sơn (khong phai huong)", "Saffron", com.htbb.pokemon.TenMapPokemon.doi("Đông sơn"));
        Kiem.bang("Đông Đông sơn", "Đông Saffron", com.htbb.pokemon.TenMapPokemon.doi("Đông Đông sơn"));
        Kiem.bang("Nam Sơn Nam", "Nam Pewter", com.htbb.pokemon.TenMapPokemon.doi("Nam Sơn Nam"));
        Kiem.bang("Tiên du (thuong)", "Viridian", com.htbb.pokemon.TenMapPokemon.doi("Tiên du"));
        Kiem.bang("ten chung giu nguyen", "Bến tàu", com.htbb.pokemon.TenMapPokemon.doi("Bến tàu"));
        Kiem.bang("rong giu nguyen", "", com.htbb.pokemon.TenMapPokemon.doi(""));
        try {
            final String cs = new String(Files.readAllBytes(new File("../client_pc_ios/Assets/Scripts/Assembly-CSharp/TenMapPokemon.cs").toPath()), StandardCharsets.UTF_8);
            for (final String[] d : com.htbb.pokemon.TenMapPokemon.BANG) {
                Kiem.dung("client co dong " + d[0], cs.contains("{ \"" + d[0] + "\", \"" + d[1] + "\" }"));
            }
            final java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\t\\tnew string\\[\\] \\{ \"").matcher(cs);
            int dem = 0;
            while (m.find()) {
                dem++;
            }
            Kiem.bang("client cung so dong", Integer.valueOf(com.htbb.pokemon.TenMapPokemon.BANG.length), Integer.valueOf(dem));
        } catch (final Exception e) {
            Kiem.dung("doc TenMapPokemon.cs: " + e, false);
        }

        Kiem.nhom("com.htbb.pokemon.LoaiPokemon - can bang theo giai doan (B1)");
        Kiem.bang("Charmander gd1", Integer.valueOf(1), Integer.valueOf(com.htbb.pokemon.LoaiPokemon.giaiDoan(4)));
        Kiem.bang("Charmeleon gd2", Integer.valueOf(2), Integer.valueOf(com.htbb.pokemon.LoaiPokemon.giaiDoan(5)));
        Kiem.bang("Charizard gd3", Integer.valueOf(3), Integer.valueOf(com.htbb.pokemon.LoaiPokemon.giaiDoan(6)));
        Kiem.bang("Raichu gd3", Integer.valueOf(3), Integer.valueOf(com.htbb.pokemon.LoaiPokemon.giaiDoan(26)));
        Kiem.bang("Eevee gd1", Integer.valueOf(1), Integer.valueOf(com.htbb.pokemon.LoaiPokemon.giaiDoan(133)));
        Kiem.bang("Sylveon gd2", Integer.valueOf(2), Integer.valueOf(com.htbb.pokemon.LoaiPokemon.giaiDoan(700)));
        Kiem.bang("% Charmander 0", Integer.valueOf(0), Integer.valueOf(com.htbb.pokemon.LoaiPokemon.phanTramGiaiDoan(4)));
        Kiem.bang("% Ivysaur 8", Integer.valueOf(8), Integer.valueOf(com.htbb.pokemon.LoaiPokemon.phanTramGiaiDoan(2)));
        Kiem.bang("% Blastoise 16", Integer.valueOf(16), Integer.valueOf(com.htbb.pokemon.LoaiPokemon.phanTramGiaiDoan(9)));
        Kiem.bang("% Umbreon 12", Integer.valueOf(12), Integer.valueOf(com.htbb.pokemon.LoaiPokemon.phanTramGiaiDoan(197)));
        Kiem.bang("% loai ngoai bang 0", Integer.valueOf(0), Integer.valueOf(com.htbb.pokemon.LoaiPokemon.phanTramGiaiDoan(999)));
        for (int c = 0; c < 5; c++) {
            final int dau = com.htbb.pokemon.LoaiPokemon.khoiDau(c);
            Kiem.bang("khoi dau class " + c + " la gd1", Integer.valueOf(1), Integer.valueOf(com.htbb.pokemon.LoaiPokemon.giaiDoan(dau)));
        }

        Kiem.nhom("com.htbb.pokemon.GoiPokemon - goi -93 POKE_CUOI");
        Kiem.bang("so lenh -93", Integer.valueOf(-93), Integer.valueOf(GoiPokemon.POKE_CUOI));
        try {
            final ByteArrayOutputStream bo = new ByteArrayOutputStream();
            GoiPokemon.ghiCuoi(new DataOutputStream(bo), 300, 149, true);
            final byte[] b = bo.toByteArray();
            Kiem.bang("5 byte", Integer.valueOf(5), Integer.valueOf(b.length));
            Kiem.bang("charId big-endian", Integer.valueOf(300), Integer.valueOf(((b[0] & 0xff) << 8) | (b[1] & 0xff)));
            Kiem.bang("dexThu", Integer.valueOf(149), Integer.valueOf(((b[2] & 0xff) << 8) | (b[3] & 0xff)));
            Kiem.bang("shiny", Integer.valueOf(1), Integer.valueOf(b[4]));
        } catch (final Exception e) {
            Kiem.dung("ghiCuoi: " + e, false);
        }
        try {
            final String mh = new String(Files.readAllBytes(new File("../client_pc_ios/Assets/Scripts/Assembly-CSharp/MessageHandler.cs").toPath()), StandardCharsets.UTF_8);
            Kiem.dung("client xu ly case -93", mh.contains("case -93:"));
            Kiem.dung("client doc i16,i16,u8 cho -93", mh.contains("c.pokeThuDex = dexThu;"));
        } catch (final Exception e) {
            Kiem.dung("doc MessageHandler.cs: " + e, false);
        }
    }
}
