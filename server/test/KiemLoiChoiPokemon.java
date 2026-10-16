import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import com.kpah.pokemon.ChieuPokemon;
import com.kpah.pokemon.DaTienHoa;
import com.kpah.pokemon.HePokemon;
import com.kpah.pokemon.Pokedex;
import com.kpah.pokemon.SatThuongHe;

/**
 * Kiem thu luong viec G7.D "Loi choi": he theo tung chieu + STAB (ChieuPokemon), Pokedex
 * (dem gap/ha, top, ghi theo lo) va thuong moc Pokedex.
 */
public final class KiemLoiChoiPokemon {

    public static void chay() {
        kiemBangChieuKhopCsv();
        kiemStab();
        kiemPokedex();
        kiemMoc();
    }

    private static void kiemBangChieuKhopCsv() {
        Kiem.nhom("com.kpah.pokemon.ChieuPokemon - khop docs/pokemon/bang_chieu.csv (cot so_he)");
        try {
            final List<String> dong = Files.readAllLines(new File("docs/pokemon/bang_chieu.csv").toPath(), StandardCharsets.UTF_8);
            final int[] demTheoClass = new int[5];
            int dem = 0;
            for (int i = 1; i < dong.size(); i++) {
                final String[] c = dong.get(i).split(",");
                if (c.length < 7) {
                    continue;
                }
                final int cls = Integer.parseInt(c[0].trim());
                final int sk = Integer.parseInt(c[2].trim());
                final int he = Integer.parseInt(c[6].trim());
                dem++;
                demTheoClass[cls]++;
                Kiem.bang("class " + cls + " skill " + sk + " (" + c[4] + ")", Integer.valueOf(he),
                        Integer.valueOf(ChieuPokemon.heChieu(cls, sk)));
                Kiem.bang("ten he khop so_he " + cls + "/" + sk, c[5].trim(), HePokemon.TEN[he]);
            }
            Kiem.bang("47 dong CSV", Integer.valueOf(47), Integer.valueOf(dem));
            for (int cls = 0; cls < 5; cls++) {
                Kiem.bang("class " + cls + ": so chieu bang Java = CSV", Integer.valueOf(demTheoClass[cls]),
                        Integer.valueOf(ChieuPokemon.soChieu(cls)));
            }
        } catch (final Exception e) {
            Kiem.dung("doc bang_chieu.csv: " + e, false);
        }
        Kiem.bang("skill ngoai bang -> KHONG_CO", Integer.valueOf(ChieuPokemon.KHONG_CO), Integer.valueOf(ChieuPokemon.heChieu(0, 9)));
        Kiem.bang("skill am -> KHONG_CO", Integer.valueOf(ChieuPokemon.KHONG_CO), Integer.valueOf(ChieuPokemon.heChieu(0, -1)));
        Kiem.bang("class ngoai bang -> KHONG_CO", Integer.valueOf(ChieuPokemon.KHONG_CO), Integer.valueOf(ChieuPokemon.heChieu(5, 0)));
        Kiem.bang("class ngoai bang 0 chieu", Integer.valueOf(0), Integer.valueOf(ChieuPokemon.soChieu(-1)));
    }

    private static void kiemStab() {
        Kiem.nhom("com.kpah.pokemon.ChieuPokemon - STAB x1.2 + khac he theo chieu");
        Kiem.dung("Lua danh boi Charmander (Lua) co STAB", ChieuPokemon.coStab(HePokemon.LUA, HePokemon.LUA, -1));
        Kiem.dung("STAB theo he 2", ChieuPokemon.coStab(HePokemon.BAY, HePokemon.LUA, HePokemon.BAY));
        Kiem.sai("Thuong danh boi Lua khong STAB", ChieuPokemon.coStab(HePokemon.THUONG, HePokemon.LUA, -1));
        Kiem.sai("he chieu -1 khong STAB (he2 = -1)", ChieuPokemon.coStab(-1, HePokemon.LUA, -1));
        Kiem.bang("he so STAB 120", Integer.valueOf(120), Integer.valueOf(ChieuPokemon.heSoStab(1, 1, -1)));
        Kiem.bang("khong STAB 100", Integer.valueOf(100), Integer.valueOf(ChieuPokemon.heSoStab(0, 1, -1)));

        // Charmander (Lua) dung Than hong (class 0 skill 1, Lua) vao Bulbasaur (Co/Doc): x1.5 * 1.2.
        final int lua = HePokemon.LUA;
        Kiem.bang("Than hong vao Co/Doc: 1000 -> 1800", Integer.valueOf(1000 * SatThuongHe.heSo(lua, HePokemon.CO, HePokemon.DOC) / 100 * 120 / 100),
                Integer.valueOf(ChieuPokemon.apDung(1000, 0, 1, lua, -1, HePokemon.CO, HePokemon.DOC)));
        Kiem.bang("Than hong vao Co/Doc = 1800", Integer.valueOf(1800),
                Integer.valueOf(ChieuPokemon.apDung(1000, 0, 1, lua, -1, HePokemon.CO, HePokemon.DOC)));
        // Cao (Thuong) cua Charmander vao Ma: x0.3, khong STAB.
        Kiem.bang("Cao (Thuong) vao Ma = 300", Integer.valueOf(300),
                Integer.valueOf(ChieuPokemon.apDung(1000, 0, 0, lua, -1, HePokemon.MA, -1)));
        // Skill 7 cua Charmander la Rong -> vao Rong x1.5, khong STAB.
        Kiem.bang("Chieu Rong vao Rong = 1500", Integer.valueOf(1500),
                Integer.valueOf(ChieuPokemon.apDung(1000, 0, 7, lua, -1, HePokemon.RONG, -1)));
        // Skill ngoai bang: y het S4 cu (he 1 nguoi danh, khong STAB).
        Kiem.bang("skill ngoai bang = SatThuongHe cu", Integer.valueOf(SatThuongHe.apDung(1000, lua, HePokemon.CO, -1)),
                Integer.valueOf(ChieuPokemon.apDung(1000, 0, 12, lua, -1, HePokemon.CO, -1)));
        Kiem.bang("dam lon khong tran int", Integer.valueOf(Integer.MAX_VALUE),
                Integer.valueOf(ChieuPokemon.apDung(Integer.MAX_VALUE, 0, 1, lua, -1, HePokemon.CO, -1)));
        // Pikachu (Dien) dung chieu Sieu linh (class 4 skill 4) vao Doc: x1.5, khong STAB.
        Kiem.bang("Pikachu chieu Sieu linh vao Doc = 1500", Integer.valueOf(1500),
                Integer.valueOf(ChieuPokemon.apDung(1000, 4, 4, HePokemon.DIEN, -1, HePokemon.DOC, -1)));
    }

    private static void kiemPokedex() {
        Kiem.nhom("com.kpah.pokemon.Pokedex - gap/ha, top, ghi theo lo");
        final Pokedex d = new Pokedex();
        Kiem.bang("moi tao: 0 gap", Integer.valueOf(0), Integer.valueOf(d.soLoaiDaGap()));
        Kiem.dung("lan dau gap Pidgey", d.ghiGap(16, 100));
        Kiem.sai("gap lai khong phai lan dau", d.ghiGap(16, 200));
        Kiem.bang("1 gap, 0 ha", Integer.valueOf(0), Integer.valueOf(d.soLoaiDaHa()));
        Kiem.dung("ha Pidgey lan dau", d.ghiHa(16, 300));
        Kiem.sai("ha Pidgey lan 2", d.ghiHa(16, 400));
        Kiem.dung("ha Rattata (chua gap) lan dau", d.ghiHa(19, 500));
        d.ghiHa(19, 600);
        d.ghiHa(19, 700);
        d.ghiGap(41, 800);
        Kiem.sai("dex 0 khong ghi", d.ghiGap(0, 1));
        Kiem.sai("dex am khong ghi", d.ghiHa(-3, 1));
        Kiem.bang("3 loai gap", Integer.valueOf(3), Integer.valueOf(d.soLoaiDaGap()));
        Kiem.bang("2 loai ha", Integer.valueOf(2), Integer.valueOf(d.soLoaiDaHa()));
        Kiem.bang("so ha Rattata", Integer.valueOf(3), Integer.valueOf(d.soHa(19)));
        Kiem.dung("da gap Zubat", d.daGap(41));
        final List<int[]> top = d.top(5);
        Kiem.bang("top chi gom loai da ha", Integer.valueOf(2), Integer.valueOf(top.size()));
        Kiem.bang("top 1 = Rattata", Integer.valueOf(19), Integer.valueOf(top.get(0)[0]));
        Kiem.bang("top 2 = Pidgey", Integer.valueOf(16), Integer.valueOf(top.get(1)[0]));
        Kiem.bang("top(1) cat con 1", Integer.valueOf(1), Integer.valueOf(d.top(1).size()));

        final List<long[]> ban = d.layDongBan();
        Kiem.bang("3 dong ban", Integer.valueOf(3), Integer.valueOf(ban.size()));
        Kiem.sai("lay xong het ban", d.coBan());
        Kiem.bang("lay lan 2 rong (goi lap lai)", Integer.valueOf(0), Integer.valueOf(d.layDongBan().size()));
        long soHaRattata = -1;
        long lanDauPidgey = -1;
        for (final long[] r : ban) {
            if (r[0] == 19) {
                soHaRattata = r[1];
            }
            if (r[0] == 16) {
                lanDauPidgey = r[2];
            }
        }
        Kiem.bang("dong ban ghi so_ha tuyet doi", Long.valueOf(3), Long.valueOf(soHaRattata));
        Kiem.bang("lan_dau giu lan gap dau", Long.valueOf(100), Long.valueOf(lanDauPidgey));
        d.traLaiBan(ban);
        Kiem.dung("loi ghi -> tra lai ban", d.coBan());
        Kiem.bang("tra lai du 3 dong", Integer.valueOf(3), Integer.valueOf(d.layDongBan().size()));

        final Pokedex nap = new Pokedex();
        nap.napDong(25, 7, 1000);
        nap.napDong(26, 0, 1000);
        nap.napMoc(10);
        Kiem.sai("nap tu CSDL khong ban", nap.coBan());
        Kiem.bang("nap: 2 gap 1 ha", Integer.valueOf(1), Integer.valueOf(nap.soLoaiDaHa()));
        Kiem.dung("nap moc 10", nap.daNhanMoc(10));
        nap.ghiHa(25, 2000);
        Kiem.bang("ha tiep cong vao so cu", Integer.valueOf(8), Integer.valueOf(nap.soHa(25)));

        Kiem.bang("tong loai bo trung + dex 0", Integer.valueOf(3), Integer.valueOf(Pokedex.tongLoai(new int[]{16, 16, 0, 19, 41, -1})));
        Kiem.bang("tong loai null", Integer.valueOf(0), Integer.valueOf(Pokedex.tongLoai(null)));
    }

    private static void kiemMoc() {
        Kiem.nhom("com.kpah.pokemon.Pokedex - thuong moc 10/25/50/100");
        Kiem.bang("4 moc", Integer.valueOf(4), Integer.valueOf(Pokedex.MOC.length));
        for (int i = 1; i < Pokedex.MOC.length; i++) {
            Kiem.dung("moc tang dan " + i, Pokedex.MOC[i] > Pokedex.MOC[i - 1]);
        }
        Kiem.dung("du 10 loai nhan moc 10", Pokedex.nhanDuoc(10, 10, false));
        Kiem.sai("9 loai chua du moc 10", Pokedex.nhanDuoc(10, 9, false));
        Kiem.sai("da nhan khong nhan lai", Pokedex.nhanDuoc(10, 50, true));
        Kiem.sai("so khong phai moc", Pokedex.nhanDuoc(11, 50, false));
        Kiem.dung("moc 100 du 120 loai", Pokedex.nhanDuoc(100, 120, false));
        for (final int moc : Pokedex.MOC) {
            final String s = Pokedex.phanThuong(moc, 3);
            Kiem.dung("moc " + moc + " co thuong", !s.isEmpty());
            for (final String phan : s.split(",")) {
                final String[] t = phan.split(":");
                Kiem.dung("moc " + moc + " cu phap " + phan, t.length == 3 && ("PT".equals(t[0]) || "ITEM".equals(t[0]))
                        && Integer.parseInt(t[2]) > 0);
                if ("ITEM".equals(t[0])) {
                    Kiem.dung("moc " + moc + " vat pham la da Eevee", DaTienHoa.laDa(Integer.parseInt(t[1])));
                }
            }
        }
        Kiem.bang("khong phai moc -> rong", "", Pokedex.phanThuong(7, 0));
        Kiem.dung("rDa am van ra da hop le", Pokedex.phanThuong(50, -13).contains("ITEM:96"));
        Kiem.sai("moc 10 khong can o trong", Pokedex.canOTrong(10));
        Kiem.dung("moc 50 can o trong", Pokedex.canOTrong(50));
        Kiem.dung("moc 100 can o trong", Pokedex.canOTrong(100));
    }
}
