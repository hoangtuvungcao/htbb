import com.htbb.pokemon.DaTienHoa;
import com.htbb.pokemon.GoiPokemon;
import com.htbb.pokemon.HePokemon;
import com.htbb.pokemon.LoaiPokemon;
import com.htbb.pokemon.SatThuongHe;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Kiem thu phan SERVER cua che do Pokemon (giai doan 2 + I2 trong
 * docs/kien-truc.md): game luon la Pokemon (khoa cu sv.che_do_pokemon bi bo qua), he so khac
 * he trong sat thuong, doi he ve ngu hanh cu khi gui client, ghi byte lenh -90/-91/-92, da tien
 * hoa Eevee.
 */
public final class KiemPokemonServer {

    public static void chay() {
        try {
            kiemCo();
            kiemSatThuong();
            kiemNguHanhCu();
            kiemGoiTin();
            kiemDaTienHoa();
            kiemMaNguon();
        } catch (final Exception e) {
            Kiem.dung("khong nem loi: " + e, false);
        }
    }

    private static void kiemCo() {
        Kiem.nhom("Game luon Pokemon - doi he gui client (HePokemon.heGuiClient)");
        Kiem.bang("heGuiClient(Lua) = Hoa(2)", Byte.valueOf((byte) 2), Byte.valueOf(HePokemon.heGuiClient(HePokemon.LUA)));
        for (int he = -1; he <= 17; he++) {
            final int g = HePokemon.heGuiClient((byte) he);
            Kiem.dung("heGuiClient(" + he + ") trong 0..4", g >= 0 && g <= 4);
        }
        Kiem.sai("khong con lop CheDoPokemon", new java.io.File("src/com/htbb/pokemon/CheDoPokemon.java").exists());
    }

    private static void kiemSatThuong() {
        Kiem.nhom("com.htbb.pokemon.SatThuongHe");
        Kiem.bang("Nuoc -> Lua: 1000 x1.5", Integer.valueOf(1500),
                Integer.valueOf(SatThuongHe.apDung(1000, HePokemon.NUOC, HePokemon.LUA, HePokemon.KHONG_CO)));
        Kiem.bang("Lua -> Nuoc: 1000 x0.7", Integer.valueOf(700),
                Integer.valueOf(SatThuongHe.apDung(1000, HePokemon.LUA, HePokemon.NUOC, HePokemon.KHONG_CO)));
        Kiem.bang("Dien -> Dat (mien nhiem lam diu): x0.3", Integer.valueOf(300),
                Integer.valueOf(SatThuongHe.apDung(1000, HePokemon.DIEN, HePokemon.DAT, HePokemon.KHONG_CO)));
        Kiem.bang("Da -> Charizard (Lua/Bay, x4 lam diu): x2", Integer.valueOf(2000),
                Integer.valueOf(SatThuongHe.apDung(1000, HePokemon.DA, HePokemon.LUA, HePokemon.BAY)));
        Kiem.bang("Co -> Charizard (x0.25 lam diu): x0.5", Integer.valueOf(500),
                Integer.valueOf(SatThuongHe.apDung(1000, HePokemon.CO, HePokemon.LUA, HePokemon.BAY)));
        Kiem.bang("Thuong -> Thuong: giu nguyen", Integer.valueOf(1234),
                Integer.valueOf(SatThuongHe.apDung(1234, HePokemon.THUONG, HePokemon.THUONG, HePokemon.KHONG_CO)));
        Kiem.bang("he loi -> giu nguyen", Integer.valueOf(1234),
                Integer.valueOf(SatThuongHe.apDung(1234, 99, -5, -1)));
        Kiem.bang("sat thuong am van nhan dung", Integer.valueOf(-150),
                Integer.valueOf(SatThuongHe.apDung(-100, HePokemon.NUOC, HePokemon.LUA, HePokemon.KHONG_CO)));
        Kiem.bang("dam lon khong tran int (x2)", Integer.valueOf(Integer.MAX_VALUE),
                Integer.valueOf(SatThuongHe.apDung(Integer.MAX_VALUE, HePokemon.DA, HePokemon.LUA, HePokemon.BAY)));
        Kiem.bang("2e9 x0.7 tinh bang long", Integer.valueOf(1400000000),
                Integer.valueOf(SatThuongHe.apDung(2000000000, HePokemon.LUA, HePokemon.NUOC, HePokemon.KHONG_CO)));
        Kiem.bang("heSo = heSoTrongGame", Integer.valueOf(HePokemon.heSoTrongGame(HePokemon.BANG, HePokemon.RONG, HePokemon.BAY)),
                Integer.valueOf(SatThuongHe.heSo(HePokemon.BANG, HePokemon.RONG, HePokemon.BAY)));
        // Moi o deu nam trong khoang da lam diu.
        boolean trongKhoang = true;
        for (int a = 0; a < HePokemon.SO_HE; a++) {
            for (int d1 = 0; d1 < HePokemon.SO_HE; d1++) {
                for (int d2 = -1; d2 < HePokemon.SO_HE; d2++) {
                    final int h = SatThuongHe.heSo(a, d1, d2);
                    trongKhoang = trongKhoang && h >= 30 && h <= 200;
                }
            }
        }
        Kiem.dung("moi to hop he so trong 30..200", trongKhoang);
    }

    private static void kiemNguHanhCu() {
        Kiem.nhom("com.htbb.pokemon.HePokemon.nguHanhCu");
        for (int he = 0; he < HePokemon.SO_HE; he++) {
            final int n = HePokemon.nguHanhCu(he);
            Kiem.dung(HePokemon.TEN_EN[he] + " -> ngu hanh 0..4", n >= 0 && n <= 4);
        }
        Kiem.bang("Nuoc -> Thuy(0)", Integer.valueOf(0), Integer.valueOf(HePokemon.nguHanhCu(HePokemon.NUOC)));
        Kiem.bang("Co -> Moc(1)", Integer.valueOf(1), Integer.valueOf(HePokemon.nguHanhCu(HePokemon.CO)));
        Kiem.bang("Lua -> Hoa(2)", Integer.valueOf(2), Integer.valueOf(HePokemon.nguHanhCu(HePokemon.LUA)));
        Kiem.bang("Dat -> Tho(3)", Integer.valueOf(3), Integer.valueOf(HePokemon.nguHanhCu(HePokemon.DAT)));
        Kiem.bang("Thep -> Kim(4)", Integer.valueOf(4), Integer.valueOf(HePokemon.nguHanhCu(HePokemon.THEP)));
        Kiem.bang("he loi -> Tho(3)", Integer.valueOf(3), Integer.valueOf(HePokemon.nguHanhCu(-1)));
    }

    private static void kiemGoiTin() throws IOException {
        Kiem.nhom("com.htbb.pokemon.GoiPokemon (-90/-91/-92)");
        Kiem.bang("so lenh POKE_THAN", Integer.valueOf(-90), Integer.valueOf(GoiPokemon.POKE_THAN));
        Kiem.bang("so lenh POKE_QUAI", Integer.valueOf(-91), Integer.valueOf(GoiPokemon.POKE_QUAI));
        Kiem.bang("so lenh POKE_TIEN_HOA", Integer.valueOf(-92), Integer.valueOf(GoiPokemon.POKE_TIEN_HOA));

        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        GoiPokemon.ghiThan(new DataOutputStream(bo), 1234, 6, 1);
        Kiem.bang("-90: i16 id, i16 dex, u8 shiny = 5 byte", "04d2000601", hex(bo.toByteArray()));
        bo = new ByteArrayOutputStream();
        GoiPokemon.ghiThan(new DataOutputStream(bo), -5, 700, 7);
        Kiem.bang("-90: id am (bot), shiny khac 0 -> 1", "fffb02bc01", hex(bo.toByteArray()));

        bo = new ByteArrayOutputStream();
        GoiPokemon.ghiTienHoa(new DataOutputStream(bo), 7, 4, 5);
        Kiem.bang("-92: i16 id, i16 cu, i16 moi", "000700040005", hex(bo.toByteArray()));

        bo = new ByteArrayOutputStream();
        final int n = GoiPokemon.ghiQuai(new DataOutputStream(bo),
                new int[]{0, 1, 2, 120, 300, 5},
                new int[]{0, 27, 0, 384, 25, 10},
                new int[]{0, 0, 0, 1, 0, 0},
                new int[]{0, 100, 100, 5000, 100, 20000000});
        Kiem.bang("-91: bo template dex 0 va id > 255", Integer.valueOf(3), Integer.valueOf(n));
        final DataInputStream di = new DataInputStream(new ByteArrayInputStream(bo.toByteArray()));
        Kiem.bang("-91: n", Integer.valueOf(3), Integer.valueOf(di.readUnsignedByte()));
        Kiem.bang("-91 dong 1: id", Integer.valueOf(1), Integer.valueOf(di.readUnsignedByte()));
        Kiem.bang("-91 dong 1: dex", Integer.valueOf(27), Integer.valueOf(di.readShort()));
        Kiem.bang("-91 dong 1: shiny", Integer.valueOf(0), Integer.valueOf(di.readUnsignedByte()));
        Kiem.bang("-91 dong 1: ti le quai thuong", Integer.valueOf(100), Integer.valueOf(di.readUnsignedByte()));
        Kiem.bang("-91 dong 2: id 120 (u8)", Integer.valueOf(120), Integer.valueOf(di.readUnsignedByte()));
        Kiem.bang("-91 dong 2: dex", Integer.valueOf(384), Integer.valueOf(di.readShort()));
        Kiem.bang("-91 dong 2: shiny", Integer.valueOf(1), Integer.valueOf(di.readUnsignedByte()));
        Kiem.bang("-91 dong 2: Tru Rong (BossTruRong) ve 160%", Integer.valueOf(160), Integer.valueOf(di.readUnsignedByte()));
        Kiem.bang("-91 dong 3: id", Integer.valueOf(5), Integer.valueOf(di.readUnsignedByte()));
        di.readShort();
        di.readUnsignedByte();
        Kiem.bang("-91 dong 3: maxhp >= 10tr -> boss", Integer.valueOf(160), Integer.valueOf(di.readUnsignedByte()));
        Kiem.bang("-91: het du lieu", Integer.valueOf(0), Integer.valueOf(di.available()));

        Kiem.dung("template 46 (cong thanh) KHONG la boss", !GoiPokemon.laTemplateBoss(46, 100));
        Kiem.dung("template 94 (BossDusknoir) la boss", GoiPokemon.laTemplateBoss(94, 100));
        Kiem.bang("bot 0 -> Chansey", Integer.valueOf(113), Integer.valueOf(GoiPokemon.dexBot(0)));
        Kiem.bang("bot 6 -> Kecleon", Integer.valueOf(352), Integer.valueOf(GoiPokemon.dexBot(6)));
        Kiem.dung("bot id am van ra dex hop le", GoiPokemon.dexBot(-7) > 0);
    }

    private static void kiemDaTienHoa() {
        Kiem.nhom("com.htbb.pokemon.DaTienHoa (Eevee)");
        Kiem.bang("8 da", Integer.valueOf(8), Integer.valueOf(DaTienHoa.ID_ITEM.length));
        Kiem.bang("8 dex", Integer.valueOf(8), Integer.valueOf(DaTienHoa.DEX.length));
        Kiem.bang("8 ten", Integer.valueOf(8), Integer.valueOf(DaTienHoa.TEN.length));
        for (int i = 0; i < DaTienHoa.DEX.length; i++) {
            Kiem.dung("da " + DaTienHoa.ID_ITEM[i] + " ra nhanh Eevee", LoaiPokemon.laNhanhEevee(DaTienHoa.DEX[i]));
            Kiem.dung("id " + DaTienHoa.ID_ITEM[i] + " > 954 (id lon nhat dang dung)", DaTienHoa.ID_ITEM[i] > 954);
        }
        Kiem.bang("Da Nuoc -> Vaporeon", Integer.valueOf(134), Integer.valueOf(DaTienHoa.dexTheoDa(960)));
        Kiem.bang("Ruy Bang Tien -> Sylveon", Integer.valueOf(700), Integer.valueOf(DaTienHoa.dexTheoDa(967)));
        Kiem.bang("item khac -> KHONG", Integer.valueOf(LoaiPokemon.KHONG), Integer.valueOf(DaTienHoa.dexTheoDa(735)));
        Kiem.bang("Eevee cap 25 dung Da Lua -> Flareon", Integer.valueOf(136),
                Integer.valueOf(DaTienHoa.ketQuaDung(133, 25, 962)));
        Kiem.bang("Eevee cap 24 -> khong duoc", Integer.valueOf(LoaiPokemon.KHONG),
                Integer.valueOf(DaTienHoa.ketQuaDung(133, 24, 962)));
        Kiem.bang("Charmander cap 50 -> khong duoc", Integer.valueOf(LoaiPokemon.KHONG),
                Integer.valueOf(DaTienHoa.ketQuaDung(4, 50, 962)));
        Kiem.bang("Vaporeon dung tiep -> khong duoc", Integer.valueOf(LoaiPokemon.KHONG),
                Integer.valueOf(DaTienHoa.ketQuaDung(134, 50, 961)));
        Kiem.bang("thong bao", "Chỉ Eevee từ cấp 25 mới dùng được", DaTienHoa.THONG_BAO_KHONG_DUNG_DUOC);

        // Doi chieu file SQL: dung id, dung ten, type 28.
        final String sql = doc("../sql/pokemon_vat_pham_eevee.sql");
        Kiem.dung("doc duoc sql/pokemon_vat_pham_eevee.sql", sql.length() > 100);
        for (int i = 0; i < DaTienHoa.ID_ITEM.length; i++) {
            Kiem.dung("SQL co dong " + DaTienHoa.ID_ITEM[i] + " '" + DaTienHoa.TEN[i] + "' type 28",
                    sql.contains("(" + DaTienHoa.ID_ITEM[i] + ",'" + DaTienHoa.TEN[i] + "',28,"));
        }
        final String seed = doc("../sql/gamedb7_clean.sql");
        for (final int id : DaTienHoa.ID_ITEM) {
            Kiem.sai("ban seed chua dung id " + id, seed.contains("\n(" + id + ",'"));
        }

        // Tien hoa theo cap (S5) - dung chung bang LoaiPokemon.
        Kiem.bang("Charmander cap 16 -> Charmeleon", Integer.valueOf(5), Integer.valueOf(LoaiPokemon.dangTheoCap(4, 16)));
        Kiem.bang("Charmander cap 40 (GM dat cap) -> Charizard", Integer.valueOf(6), Integer.valueOf(LoaiPokemon.dangTheoCap(4, 40)));
        Kiem.bang("Eevee cap 50 khong tu tien hoa", Integer.valueOf(133), Integer.valueOf(LoaiPokemon.dangTheoCap(133, 50)));
    }

    private static void kiemMaNguon() {
        Kiem.nhom("Chi con nhanh Pokemon trong ma nguon");
        final String chiSo = doc("src/real/CharChiSo.java");
        final String live = doc("src/real/LiveActor.java");
        Kiem.sai("CharChiSo khong con khoi ngu hanh cu", chiSo.contains("(self.he + 2) % 5 == actor.he"));
        Kiem.sai("LiveActor khong con khoi ngu hanh cu", live.contains("(this.he + 2) % 5 == actor.he"));
        Kiem.dung("LiveActor co truong he2 mac dinh -1", live.contains("public byte he2 = -1;"));

        final String sv = doc("src/server/TeamServer.java");
        Kiem.sai("TeamServer khong doc sv.che_do_pokemon (dong cu trong server.ini/CSDL bi bo qua)",
                sv.contains("p.getProperty(\"sv.che_do_pokemon\")"));
        final String tmpl = doc("../vps/server.ini.tmpl");
        Kiem.sai("ban mau khong con sv.che_do_pokemon", tmpl.contains("sv.che_do_pokemon"));

        final String mig = doc("../sql/them_cot_pokemon.sql");
        Kiem.dung("migration: tob_char.dex SMALLINT DEFAULT 0", mig.contains("`dex`   SMALLINT NOT NULL DEFAULT 0"));
        Kiem.dung("migration: data_monster.he2 DEFAULT -1", mig.contains("`he2`   TINYINT  NOT NULL DEFAULT -1"));
        Kiem.dung("migration: ALTER ca tob_char lan data_monster",
                mig.contains("ALTER TABLE `tob_char`") && mig.contains("ALTER TABLE `data_monster`"));

        // Moi spawn quai trong src/ (tru Moba - lop jar CharChienTruong.setTimeBuff dung he
        // 0..4 lam chi so) di qua heKhiSinh, khong con gan random thang.
        final String[] tep = {"src/real/MapNapTaiNguyen.java", "src/real/BossDusknoir.java", "src/real/BossNguoiTuyet.java",
            "src/real/Map.java", "src/real/MapTown.java"};
        for (final String t : tep) {
            final String ma = doc(t);
            Kiem.sai(t + " khong con gan he random thang", ma.matches("(?s).*\\.he\\s*=\\s*he\\[[^\\n]*nextInt.*"));
        }
    }

    private static String hex(final byte[] b) {
        final StringBuilder s = new StringBuilder();
        for (final byte x : b) {
            s.append(String.format("%02x", x & 0xff));
        }
        return s.toString();
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }

    private KiemPokemonServer() {
    }
}
