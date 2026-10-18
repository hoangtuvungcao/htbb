import com.htbb.pokemon.LoaiPokemon;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Doi chieu phan Pokemon cua client Unity voi server.
 *
 * VI SAO: client tu suy ra Pokemon o man chon nhan vat (LoaiPokemonClient.cs, ban C#
 * cua LoaiPokemon.java) va giu bang NPC type -> dex (NPC.cs) song song voi
 * docs/pokemon/bang_npc.csv. Hai ban sao viet tay khong co gi bat lech nhau, sua
 * mot ben quen ben kia thi man chon nhan vat hien sai loai ma khong bao loi.
 * Kiem bang cach doc chu ma nguon C# (khong chay duoc Unity trong bo test Java).
 */
public final class KiemPokemonClient {

    private static final String F_LOAI = "../client_pc_ios/ClientUnity/Assets/Scripts/Game/LoaiPokemonClient.cs";
    private static final String F_NPC = "../client_pc_ios/ClientUnity/Assets/Scripts/Game/NPC.cs";
    private static final String F_BANG_NPC = "../docs/pokemon/bang_npc.csv";
    private static final String F_LENH = "../client_pc_ios/ClientUnity/Assets/Scripts/Game/MessageHandler.cs";

    public static void chay() {
        Kiem.nhom("Client Unity - Pokemon (LoaiPokemonClient, NPC, lenh -90..-92)");
        final String loai = doc(F_LOAI);
        Kiem.dung("co " + F_LOAI, loai != null);
        if (loai != null) {
            kiemKhoiDau(loai);
            kiemTienHoa(loai);
        }
        kiemBangNpc();
        final String lenh = doc(F_LENH);
        Kiem.dung("MessageHandler xu ly -90, -91, -92", lenh != null
                && lenh.contains("case -90:") && lenh.contains("case -91:") && lenh.contains("case -92:"));
    }

    private static void kiemKhoiDau(final String ma) {
        final Matcher m = Pattern.compile("KHOI_DAU_THEO_CLASS\\s*=\\s*new int\\[\\d+\\]\\s*\\{([^}]*)\\}").matcher(ma);
        final boolean thay = m.find();
        Kiem.dung("tim thay KHOI_DAU_THEO_CLASS", thay);
        if (!thay) {
            return;
        }
        final int[] cs = soNguyen(m.group(1));
        Kiem.bang("so class", Integer.valueOf(LoaiPokemon.KHOI_DAU_THEO_CLASS.length), Integer.valueOf(cs.length));
        for (int c = 0; c < cs.length && c < 5; c++) {
            Kiem.bang("khoi dau class " + c, Integer.valueOf(LoaiPokemon.khoiDau(c)), Integer.valueOf(cs[c]));
        }
    }

    /** Moi dong {dex, capTienHoa, dexSau} cua C# phai cho cung ket qua tienHoaKeTiep cua Java o moi cap. */
    private static void kiemTienHoa(final String ma) {
        final Matcher m = Pattern.compile("new int\\[3\\]\\s*\\{\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+|KHONG)\\s*\\}").matcher(ma);
        final List<int[]> dong = new ArrayList<>();
        while (m.find()) {
            final int sau = "KHONG".equals(m.group(3)) ? LoaiPokemon.KHONG : Integer.parseInt(m.group(3));
            dong.add(new int[]{Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), sau});
        }
        Kiem.bang("so dong tien hoa (12 loai khong ke Eevee)", Integer.valueOf(12), Integer.valueOf(dong.size()));
        int lech = 0;
        for (final int[] d : dong) {
            for (int cap = 1; cap <= 91; cap++) {
                final int cs = (d[1] <= 0 || cap < d[1]) ? LoaiPokemon.KHONG : d[2];
                if (cs != LoaiPokemon.tienHoaKeTiep(d[0], cap)) {
                    lech++;
                }
            }
        }
        Kiem.bang("tien hoa C# khop Java o moi cap 1..91", Integer.valueOf(0), Integer.valueOf(lech));
        // Moi loai tien hoa cua Java (tru nhanh Eevee) deu phai co dong ben C#.
        int thieu = 0;
        for (final int dex : LoaiPokemon.tatCaDex()) {
            if (dex == LoaiPokemon.EEVEE || LoaiPokemon.laNhanhEevee(dex)) {
                continue;
            }
            boolean co = false;
            for (final int[] d : dong) {
                co |= d[0] == dex;
            }
            if (!co) {
                thieu++;
            }
        }
        Kiem.bang("C# du cac loai khong phai Eevee", Integer.valueOf(0), Integer.valueOf(thieu));
    }

    private static void kiemBangNpc() {
        final String npc = doc(F_NPC);
        final String csv = doc(F_BANG_NPC);
        Kiem.dung("co NPC.cs va bang_npc.csv", npc != null && csv != null);
        if (npc == null || csv == null) {
            return;
        }
        final Matcher m = Pattern.compile("POKE_DEX_THEO_TYPE\\s*=\\s*new short\\[\\d+\\]\\s*\\{([^}]*)\\}").matcher(npc);
        final Matcher ten = Pattern.compile("NPCNAME\\s*=\\s*new string\\[(\\d+)\\]").matcher(npc);
        final boolean thay = m.find() && ten.find();
        Kiem.dung("tim thay POKE_DEX_THEO_TYPE va NPCNAME", thay);
        if (!thay) {
            return;
        }
        final int[] bang = soNguyen(m.group(1));
        Kiem.bang("bang dex du moi type NPC", Integer.valueOf(Integer.parseInt(ten.group(1))), Integer.valueOf(bang.length));
        final String[] dongCsv = csv.trim().split("\n");
        Kiem.bang("bang_npc.csv du moi type", Integer.valueOf(bang.length), Integer.valueOf(dongCsv.length - 1));
        int lech = 0;
        for (int i = 1; i < dongCsv.length; i++) {
            final String[] cot = tachCsv(dongCsv[i]);
            final int type = Integer.parseInt(cot[0]);
            final int dex = Integer.parseInt(cot[4]);
            if (type < 0 || type >= bang.length || bang[type] != dex) {
                lech++;
            }
        }
        Kiem.bang("NPC.cs khop bang_npc.csv", Integer.valueOf(0), Integer.valueOf(lech));
    }

    private static String[] tachCsv(final String dong) {
        final List<String> kq = new ArrayList<>();
        final StringBuilder sb = new StringBuilder();
        boolean trongNhay = false;
        for (int i = 0; i < dong.length(); i++) {
            final char c = dong.charAt(i);
            if (c == '"') {
                trongNhay = !trongNhay;
            } else if (c == ',' && !trongNhay) {
                kq.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        kq.add(sb.toString());
        return kq.toArray(new String[0]);
    }

    private static int[] soNguyen(final String s) {
        final String[] p = s.split(",");
        final int[] kq = new int[p.length];
        for (int i = 0; i < p.length; i++) {
            kq[i] = Integer.parseInt(p[i].trim());
        }
        return kq;
    }

    private static String doc(final String duong) {
        try {
            return new String(Files.readAllBytes(Paths.get(duong)), StandardCharsets.UTF_8).replace("\r", "");
        } catch (final Exception e) {
            return null;
        }
    }
}
