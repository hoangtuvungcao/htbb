import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import com.kpah.pokemon.GoiPokemon;
import com.kpah.pokemon.ThuPokemon;

/**
 * Kiem thu thu cuoi Pokemon (com.kpah.pokemon.ThuPokemon) + goi -93 POKE_CUOI.
 *
 * Mang ThuPokemon.DEX duoc chep tay tu docs/pokemon/bang_thu.csv - test nay doi chieu
 * TUNG dong "thu_cuoi" trong CSV voi mang Java, va so luong voi so hang so trong
 * data.Animal (BACH_MA..SU_TU_BANG = 21). Sua mot ben ma quen ben kia la do ngay.
 */
public final class KiemThuPokemon {

    public static void chay() {
        Kiem.nhom("com.kpah.pokemon.ThuPokemon - khop bang_thu.csv");
        Kiem.bang("21 loai thu cuoi", Integer.valueOf(21), Integer.valueOf(ThuPokemon.soLoai()));
        Kiem.bang("ngoai bang -> 0", Integer.valueOf(0), Integer.valueOf(ThuPokemon.dex(21)));
        Kiem.bang("am -> 0", Integer.valueOf(0), Integer.valueOf(ThuPokemon.dex(-1)));
        try {
            final List<String> dong = Files.readAllLines(new File("docs/pokemon/bang_thu.csv").toPath(), StandardCharsets.UTF_8);
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
                        "ClientUnity/Assets/Resources/poke/%04d%s_dl.bytes", Integer.parseInt(c[3]), "1".equals(c[4]) ? "s" : "")).isFile());
            }
            Kiem.bang("CSV co du 21 dong thu_cuoi", Integer.valueOf(21), Integer.valueOf(dem));
        } catch (final Exception e) {
            Kiem.dung("doc bang_thu.csv: " + e, false);
        }

        Kiem.nhom("Ten chieu Pokemon - server, client va bang_chieu.csv phai khop");
        try {
            final List<String> dong = Files.readAllLines(new File("docs/pokemon/bang_chieu.csv").toPath(), StandardCharsets.UTF_8);
            final String client = new String(Files.readAllBytes(
                    new File("ClientUnity/Assets/Scripts/Game/SkillManager.cs").toPath()), StandardCharsets.UTF_8);
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

        Kiem.nhom("com.kpah.pokemon.GoiPokemon - goi -93 POKE_CUOI");
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
            final String mh = new String(Files.readAllBytes(new File("ClientUnity/Assets/Scripts/Game/MessageHandler.cs").toPath()), StandardCharsets.UTF_8);
            Kiem.dung("client xu ly case -93", mh.contains("case -93:"));
            Kiem.dung("client doc i16,i16,u8 cho -93", mh.contains("c.pokeThuDex = dexThu;"));
        } catch (final Exception e) {
            Kiem.dung("doc MessageHandler.cs: " + e, false);
        }
    }
}
