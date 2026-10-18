import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.htbb.pokemon.ThuongHieu;

/**
 * Hinh con sot + thuong hieu (viec G7.B va G7.E trong docs/pokemon/KE_HOACH_CHUYEN_DOI.md muc 15).
 * Doi chieu bang CSV (nguon cho nguoi doc + tools/pokemon/tai_sprite_pmd.py) voi bang viet cung
 * trong client va voi file da dong goi trong Resources/poke/:
 *  - B1 bang_vat_the.csv  <-> VatThePokemon.cs (QUAI, NPC) <-> vt_*.bytes; du 16 vat_the cua bang_quai.csv
 *  - B2 bang_npc_su_kien.csv <-> Npc_Server.POKE_DEX_THEO_IDIMG; moi idImg ma server gui qua
 *       createMsgNpc (lenh -50) deu co dong
 *  - B3 bang_pet_cu.csv <-> AnimalMove.POKE_DEX_PET_CU; moi idImg trong Map.loadPetTemplate deu co dong
 *  - E1 ThuongHieu.java va ThuongHieu.cs cung TEN_GAME
 *
 * VI SAO: bang client viet cung (khong co goi tin), sua mot noi quen noi kia thi NPC/pet/vat the
 * am tham quay ve hinh goc cu (hoac ve Pokemon chua dong goi) ma khong bao loi.
 */
public final class KiemHinhPokemon {

    private static final String POKE = "../client_pc_ios/ClientUnity/Assets/Resources/poke/";
    private static final String GAME = "../client_pc_ios/ClientUnity/Assets/Scripts/Game/";

    public static void chay() {
        Kiem.nhom("Hinh con sot + thuong hieu (G7.B/E: vat_the, NPC su kien, pet cu, ten game)");
        kiemVatThe();
        kiemNpcSuKien();
        kiemPetCu();
        kiemThuongHieu();
    }

    // ------------------------------------------------------------------ B1

    private static void kiemVatThe() {
        final List<String[]> quai = csv("../docs/pokemon/bang_quai.csv");
        final List<String[]> vt = csv("../docs/pokemon/bang_vat_the.csv");
        Kiem.dung("co bang_quai.csv va bang_vat_the.csv", quai != null && vt != null);
        if (quai == null || vt == null) {
            return;
        }
        final TreeSet<Integer> canVt = new TreeSet<>();
        for (final String[] r : quai) {
            if (r.length > 4 && "vat_the".equals(r[4])) {
                canVt.add(Integer.valueOf(r[0]));
            }
        }
        Kiem.bang("so quai vat_the trong bang_quai.csv", Integer.valueOf(16), Integer.valueOf(canVt.size()));
        // loai,id,ten_cu,nguon_rawasset,file_client,rong_khung,cao,so_khung,ti_le,ghi_chu
        final TreeMap<Integer, String> csvQuai = new TreeMap<>();
        final TreeMap<Integer, String> csvNpc = new TreeMap<>();
        int anhLoi = 0;
        for (final String[] r : vt) {
            final int id = Integer.parseInt(r[1]);
            final String mongDoiFile = ("quai".equals(r[0]) ? "vt_" : "vt_npc") + id + ".bytes";
            Kiem.bang("bang_vat_the.csv " + r[0] + " " + id + " ten file", mongDoiFile, r[4]);
            final int[] wh = kichThuocPng(new File(POKE + r[4]));
            if (wh == null || wh[0] != Integer.parseInt(r[5]) * Integer.parseInt(r[7]) || wh[1] != Integer.parseInt(r[6])) {
                anhLoi++;
                Kiem.dung(r[4] + " la PNG khop " + r[5] + "x" + r[6] + " x" + r[7], false);
            }
            ("quai".equals(r[0]) ? csvQuai : csvNpc).put(Integer.valueOf(id), r[8] + "," + r[7]);
        }
        Kiem.bang("so anh vt_*.bytes loi/lech", Integer.valueOf(0), Integer.valueOf(anhLoi));
        Kiem.bang("bang_vat_the.csv phu du quai vat_the", canVt, new TreeSet<>(csvQuai.keySet()));
        final String cs = docChuoi(GAME + "VatThePokemon.cs");
        Kiem.dung("co VatThePokemon.cs", cs != null);
        if (cs == null) {
            return;
        }
        Kiem.bang("VatThePokemon.QUAI khop bang_vat_the.csv (id -> ti_le,so_khung)", csvQuai, bangCs(cs, "QUAI"));
        Kiem.bang("VatThePokemon.NPC khop bang_vat_the.csv (idImg -> ti_le,so_khung)", csvNpc, bangCs(cs, "NPC"));
        final String mon = docChuoi(GAME + "Monster.cs");
        Kiem.dung("Monster.paintPokemon re sang paintVatThe", mon != null && mon.contains("return paintVatThe(g);"));
    }

    // ------------------------------------------------------------------ B2

    private static void kiemNpcSuKien() {
        final List<String[]> bang = csv("../docs/pokemon/bang_npc_su_kien.csv");
        final List<String[]> vt = csv("../docs/pokemon/bang_vat_the.csv");
        final String cs = docChuoi(GAME + "Npc_Server.cs");
        Kiem.dung("co bang_npc_su_kien.csv va Npc_Server.cs", bang != null && cs != null && vt != null);
        if (bang == null || cs == null || vt == null) {
            return;
        }
        // id_img,ten_cu,noi_gui,dex,shiny,vat_the,ten_moi,ghi_chu
        final TreeMap<Integer, String> dex = new TreeMap<>();
        final TreeSet<Integer> doVat = new TreeSet<>();
        final TreeSet<Integer> coDong = new TreeSet<>();
        for (final String[] r : bang) {
            final Integer id = Integer.valueOf(r[0]);
            coDong.add(id);
            final boolean coDex = !r[3].isEmpty();
            final boolean coVt = !r[5].isEmpty();
            Kiem.dung("bang_npc_su_kien.csv idImg " + id + " co DUNG MOT trong dex/vat_the", coDex != coVt);
            if (coDex) {
                dex.put(id, r[3]);
                kiemSprite("NPC su kien idImg " + id, Integer.parseInt(r[3]));
            } else {
                doVat.add(id);
                Kiem.bang("vat_the cua idImg " + id, "vt_npc" + id, r[5]);
            }
        }
        Kiem.bang("Npc_Server.POKE_DEX_THEO_IDIMG khop bang_npc_su_kien.csv", dex, bangCs(cs, "POKE_DEX_THEO_IDIMG"));
        final TreeSet<Integer> npcVt = new TreeSet<>();
        for (final String[] r : vt) {
            if ("npc_su_kien".equals(r[0])) {
                npcVt.add(Integer.valueOf(r[1]));
            }
        }
        Kiem.bang("NPC do vat trong bang_npc_su_kien.csv = bang_vat_the.csv", doVat, npcVt);

        // Moi idImg server gui qua createMsgNpc(ten, x, y, w, h, nFrame, idImg, id, (byte) 1)
        final Pattern p = Pattern.compile("createMsgNpc\\((.*?),\\s*(\\d+)\\s*,\\s*(-?\\d+)\\s*,\\s*\\(byte\\)\\s*\\d+\\s*\\)", Pattern.DOTALL);
        final TreeSet<Integer> guiDi = new TreeSet<>();
        for (final String f : new String[] {"src/real/Map.java", "src/real/MapLienDau.java", "src/real/plugins/addNPC.java"}) {
            final String s = docChuoi(f);
            if (s == null) {
                Kiem.dung("doc " + f, false);
                continue;
            }
            final StringBuilder khongChuThich = new StringBuilder();
            for (final String dong : s.split("\n")) {
                if (!dong.trim().startsWith("//")) {
                    khongChuThich.append(dong).append('\n');
                }
            }
            final Matcher m = p.matcher(khongChuThich);
            while (m.find()) {
                if (m.group(1).contains("final String name")) {
                    continue;
                }
                guiDi.add(Integer.valueOf(m.group(2)));
            }
        }
        Kiem.dung("tim duoc idImg NPC su kien trong server (" + guiDi + ")", guiDi.size() >= 10);
        final TreeSet<Integer> thieu = new TreeSet<>(guiDi);
        thieu.removeAll(coDong);
        Kiem.bang("idImg NPC su kien server gui ma chua co trong bang_npc_su_kien.csv", new TreeSet<Integer>(), thieu);
    }

    // ------------------------------------------------------------------ B3

    private static void kiemPetCu() {
        final List<String[]> bang = csv("../docs/pokemon/bang_pet_cu.csv");
        final String cs = docChuoi(GAME + "AnimalMove.cs");
        final String map = docChuoi("src/real/Map.java");
        Kiem.dung("co bang_pet_cu.csv, AnimalMove.cs, Map.java", bang != null && cs != null && map != null);
        if (bang == null || cs == null || map == null) {
            return;
        }
        // id_img,ten_mau_cu,dex,shiny,ten_moi,ghi_chu
        final TreeMap<Integer, String> dex = new TreeMap<>();
        for (final String[] r : bang) {
            dex.put(Integer.valueOf(r[0]), r[2]);
            kiemSprite("pet cu idImg " + r[0], Integer.parseInt(r[2]));
        }
        Kiem.bang("AnimalMove.POKE_DEX_PET_CU khop bang_pet_cu.csv", dex, bangCs(cs, "POKE_DEX_PET_CU"));
        // new PetTemplate((byte) id, "ten", han, (byte) idImg, ...
        final Matcher m = Pattern.compile("new PetTemplate\\(\\(byte\\) \\d+, \"[^\"]*\", \\d+, \\(byte\\) (\\d+),").matcher(map);
        final TreeSet<Integer> idImg = new TreeSet<>();
        while (m.find()) {
            idImg.add(Integer.valueOf(m.group(1)));
        }
        Kiem.dung("tim duoc idImg trong Map.loadPetTemplate (" + idImg.size() + ")", idImg.size() >= 15);
        Kiem.bang("bang_pet_cu.csv phu du idImg cua PetTemplate", idImg, new TreeSet<>(dex.keySet()));
        final String pet = docChuoi(GAME + "Pet.cs");
        Kiem.dung("Pet (pet-tool) khong dung bang pet cu", pet != null && pet.contains("protected override int dexPetCu()"));
    }

    // ------------------------------------------------------------------ E1

    private static void kiemThuongHieu() {
        final String cs = docChuoi(GAME + "ThuongHieu.cs");
        Kiem.dung("co ThuongHieu.cs", cs != null);
        if (cs != null) {
            final Matcher m = Pattern.compile("const string TEN_GAME = \"([^\"]*)\"").matcher(cs);
            Kiem.bang("TEN_GAME client = server", ThuongHieu.TEN_GAME, m.find() ? m.group(1) : null);
        }
        Kiem.bang("tenGame(true)", ThuongHieu.TEN_GAME, ThuongHieu.tenGame(true));
        Kiem.bang("tenGame(false)", ThuongHieu.TEN_GAME_CU, ThuongHieu.tenGame(false));
        Kiem.dung("ten game moi khong rong va khac ten cu", !ThuongHieu.TEN_GAME.trim().isEmpty()
                && !ThuongHieu.TEN_GAME.equals(ThuongHieu.TEN_GAME_CU));
        for (final String f : new String[] {"ServerListScr.cs", "LoginScr.cs", "Canvas.cs"}) {
            final String s = docChuoi(GAME + f);
            Kiem.dung(f + " ve tieu de ThuongHieu", s != null && s.contains("ThuongHieu.veTieuDe("));
        }
        final String xd = docChuoi("../client_pc_ios/ClientUnity/Assets/Editor/XayDung.cs");
        Kiem.dung("XayDung.cs lay productName tu ThuongHieu", xd != null && xd.contains("ThuongHieu.tenSanPham()"));
    }

    // ------------------------------------------------------------------ tien ich

    private static void kiemSprite(final String moTa, final int dex) {
        final String k = String.format("%04d", Integer.valueOf(dex));
        Kiem.dung(moTa + ": co sprite " + k + "_dl/_anh trong Resources/poke",
                new File(POKE + k + "_dl.bytes").isFile() && new File(POKE + k + "_anh.bytes").isFile());
    }

    /** Bang C# "TEN = new short[][] { new short[] { a, b[, c] }, ... };" -> a -> "b" hoac "b,c". */
    private static TreeMap<Integer, String> bangCs(final String s, final String ten) {
        final TreeMap<Integer, String> kq = new TreeMap<>();
        final int a = s.indexOf(" " + ten + " = new short[][]");
        if (a < 0) {
            return kq;
        }
        final int b = s.indexOf("};", a);
        final Matcher m = Pattern.compile("new short\\[\\] \\{ (\\d+), (\\d+)(?:, (\\d+))? \\}").matcher(s.substring(a, b));
        while (m.find()) {
            kq.put(Integer.valueOf(m.group(1)), m.group(3) == null ? m.group(2) : m.group(2) + "," + m.group(3));
        }
        return kq;
    }

    private static String docChuoi(final String f) {
        try {
            return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return null;
        }
    }

    /** Dong du lieu (bo dong tieu de). Tach CSV don gian: cac bang nay khong co dau phay trong o. */
    private static List<String[]> csv(final String f) {
        final String s = docChuoi(f);
        if (s == null) {
            return null;
        }
        final List<String[]> kq = new ArrayList<>();
        final String[] dong = s.split("\r?\n");
        for (int i = 1; i < dong.length; i++) {
            if (!dong[i].trim().isEmpty()) {
                kq.add(dong[i].split(",", -1));
            }
        }
        return kq;
    }

    private static int[] kichThuocPng(final File f) {
        if (!f.isFile()) {
            return null;
        }
        try (DataInputStream in = new DataInputStream(new FileInputStream(f))) {
            final byte[] dau = new byte[16];
            in.readFully(dau);
            if ((dau[1] & 0xff) != 'P' || (dau[2] & 0xff) != 'N' || (dau[3] & 0xff) != 'G') {
                return null;
            }
            return new int[] {in.readInt(), in.readInt()};
        } catch (final IOException e) {
            return null;
        }
    }
}
