import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import real.NpcTinhBanDo;
import real.SuKien;
import real.SuKienNpc;

/**
 * NPC va menu NPC (kiem toan 19/10/2026, nhanh npcfix): moi NPC co mat trong game deu dung duoc.
 *
 *  - NPC tinh (cMap) doc bang real.NpcTinhBanDo = data_npc.ban_do (SQL); moi NPC giao / tra nhiem vu (data_quest_moi)
 *    dung tren it nhat mot ban do ngoai troi (client chi hien nhiem vu o ban do ngoai troi) - vd Heracross (25).
 *  - Lenh dich vu NPC (mua/ban/ren/kho...) o ban do ngoai troi chay qua than OfflineMap khi dung canh NPC dung loai.
 *  - Client J2ME + Unity: bam NPC tinh o ban do ngoai troi mo dung chuc nang (moChucNangNpc), Pidgeot khong co
 *    tuyen san thi xin server, Meowth la ngan hang.
 *  - NPC su kien -49: MOT thu tu uu tien (real.SuKienNpc) cho menu va xu ly; moi su kien co NPC tren ban do.
 *  - Muc da go (chi bao "dang phat trien" / "sap ra mat" / khong xu ly) khong con trong menu; chu menu khong kiem hiep.
 *
 * Bot kiem dau-cuoi: tools/pokemon/kiem_tai/KichBanNPC.java (npc_tat_ca).
 */
public final class KiemNpc {

    private KiemNpc() {
    }

    public static void chay() {
        Kiem.nhom("NPC + menu NPC (moi NPC, moi muc deu dung duoc)");
        try {
            chayThat();
        } catch (final Exception e) {
            e.printStackTrace();
            Kiem.dung("khong nem loi: " + e, false);
        }
    }

    static String doc(final String p) throws Exception {
        return new String(Files.readAllBytes(new File(p).toPath()), StandardCharsets.UTF_8);
    }

    /** loai NPC tinh -> tap ban do (id cMap) co NPC do; ban do offline (trong nha) danh dau rieng. */
    static Map<Integer, Set<Integer>> quetCMap(final Set<Integer> offline) throws Exception {
        final Map<Integer, Set<Integer>> kq = new TreeMap<Integer, Set<Integer>>();
        for (final File f : new File("cMap").listFiles()) {
            if (!f.getName().matches("\\d+")) {
                continue;
            }
            final int id = Integer.parseInt(f.getName());
            final com.htbb.pokemon.DinhDangBanDo d = com.htbb.pokemon.DinhDangBanDo.doc(Files.readAllBytes(f.toPath()));
            if (d.offline) {
                offline.add(id);
            }
            for (final int[] n : NpcTinhBanDo.cua(id)) {
                Set<Integer> s = kq.get(n[2]);
                if (s == null) {
                    kq.put(n[2], s = new TreeSet<Integer>());
                }
                s.add(id);
            }
        }
        return kq;
    }

    private static void chayThat() throws Exception {
        // ---------------------------------------------------------------- NPC tinh tren ban do
        final Set<Integer> offline = new TreeSet<Integer>();
        final Map<Integer, Set<Integer>> theoLoai = quetCMap(offline);
        Kiem.bang("Chansey/Kecleon/Machamp/Xatu o Rustboro (45, ngoai troi)", "[0, 1, 2, 5]", loaiTren(45).toString());
        Kiem.dung("Heracross (25) dung o Cao nguyen Indigo (201)", loaiTren(201).contains(25));
        Kiem.dung("Rustboro la ban do ngoai troi (truoc day NPC o day chi chao)", !offline.contains(45));

        final Map<Integer, String[]> npc = KiemTenHien.dongDump(DayDu.sql(), "data_npc", 7);
        final List<String> lech = new ArrayList<String>();
        for (final Map.Entry<Integer, String[]> e : npc.entrySet()) {
            final Set<Integer> sql = new TreeSet<Integer>();
            for (final String x : e.getValue()[6].trim().split(",")) {
                if (!x.trim().isEmpty()) {
                    sql.add(Integer.parseInt(x.trim()));
                }
            }
            final Set<Integer> that = theoLoai.containsKey(e.getKey()) ? theoLoai.get(e.getKey()) : new TreeSet<Integer>();
            if (!sql.equals(that)) {
                lech.add(e.getKey() + ": sql " + sql + " cMap " + that);
            }
        }
        Kiem.bang("data_npc.ban_do = NPC tren cMap (moi loai)", "[]", lech.toString());

        // Moi NPC giao / tra nhiem vu (data_quest_moi) dung tren ban do ngoai troi (client chi hien nhiem vu o do).
        final Map<Integer, String[]> nv = KiemTenHien.dongDump(DayDu.sql(), "data_quest_moi", 4);
        final Set<String> thieu = new TreeSet<String>();
        for (final String[] r : nv.values()) {
            for (final int k : new int[]{2, 3}) {
                final int loai = Integer.parseInt(r[k].trim());
                if (loai == 32000 || loai < 0) {
                    continue;
                }
                boolean ngoai = false;
                if (theoLoai.containsKey(loai)) {
                    for (final int m : theoLoai.get(loai)) {
                        ngoai |= !offline.contains(m);
                    }
                }
                if (!ngoai) {
                    thieu.add("npc " + loai + " (nv " + r[0].trim() + ")");
                }
            }
        }
        Kiem.bang("moi NPC nhiem vu dung tren ban do ngoai troi", "[]", rutGon(thieu));
        Kiem.dung("doc du data_quest_moi (>= 700 dong)", nv.size() >= 700);

        // ---------------------------------------------------------------- lenh dich vu -> than OfflineMap
        final Map<Integer, String> lenh = new TreeMap<Integer, String>();
        for (final int c : new int[]{24, 28, 68, 69, 70, 71, 72, 74, 75, 76, 77, 78, 92, 94, 95, -34, -35, -36, -68, -69}) {
            lenh.put(c, String.valueOf(loaiLenh(c) != null));
        }
        Kiem.dung("moi lenh mua/ban/ren/kho la lenh dich vu NPC", !lenh.containsValue("false"));
        Kiem.dung("lenh di chuyen (4) / chat (27) / menu (-30) khong doi ban do", loaiLenh(4) == null && loaiLenh(27) == null && loaiLenh(-30) == null);
        final String xuLy = doc("src/real/MapXuLyLenh.java");
        Kiem.dung("MapXuLyLenh chuyen lenh dich vu sang OfflineMap", xuLy.contains("NpcTinhBanDo.banDoDichVu(map, player, message.command)"));
        final String epNgoc = doc("src/real/MapEpNgoc.java") + doc("src/real/MapCheTao.java");
        Kiem.sai("luyen / them dong khong con chi cho trong nha (isMapOffline)", epNgoc.contains("player.map.isMapOffline"));

        // ---------------------------------------------------------------- client: NPC tinh o ban do ngoai troi
        for (final String f : new String[]{"../client_jar/src/game/render/screen/GameScr.java",
            "../client_pc_ios/Assets/Scripts/Assembly-CSharp/GameScr.cs"}) {
            final String s = doc(f);
            final String ten = f.contains("client_jar") ? "J2ME" : "Unity";
            Kiem.dung(ten + ": co moChucNangNpc", s.contains("moChucNangNpc(int type)"));
            Kiem.sai(ten + ": khong con dieu kien cu chi cho NPC 7/10/22/31/21/25 mo menu ngoai troi", s.contains("getTypeNpc() != 22 && "));
            Kiem.dung(ten + ": Pidgeot khong co tuyen san -> xin server", s.contains("requestNPCInfo(7)") || s.contains("requestNPCInfo(type);\n\t\t\t\t}\n\t\t\t};") || s.contains("xin server danh sach diem bay"));
            Kiem.dung(ten + ": Meowth = ngan hang (khong con nap SMS)", s.contains("\"Ngân hàng xu, lượng\""));
        }

        // ---------------------------------------------------------------- NPC su kien -49
        final String req = doc("src/real/cmd/RequestNPCInfoHandler.java");
        final String rc = doc("src/real/RealController.java");
        final String mdm = doc("src/real/MapDoMenu.java");
        Kiem.dung("menu -49 = SuKienNpc.menu", req.contains("real.SuKienNpc.menu(p)"));
        Kiem.dung("RealController -49 = SuKienNpc.xuLy", rc.contains("SuKienNpc.xuLy(p.map, p, idNpc, idMenu, idOptionMenu)"));
        Kiem.dung("MapDoMenu -49 = SuKienNpc.xuLy", mdm.contains("SuKienNpc.xuLy(map, player, idNpc, idMenu, idOptionMenu)"));
        Kiem.dung("Map.sendInfoNpc dat NPC su kien theo SuKienNpc", doc("src/real/Map.java").contains("SuKienNpc.tenNpcGoi50()"));

        final String[][] thuTu = {
            {SuKien.TET_2017, String.valueOf(SuKienNpc.TET)},
            {SuKien.NOEL, String.valueOf(SuKienNpc.NOEL)},
            {SuKien.GIO_TO, String.valueOf(SuKienNpc.GIO_TO)},
            {SuKien.HALLOWEEN, String.valueOf(SuKienNpc.HALLOWEEN)},
            {SuKien.TRUNG_THU, String.valueOf(SuKienNpc.TRUNG_THU)},
            {SuKien.SINH_NHAT_2011, String.valueOf(SuKienNpc.NHA_GIAO)},
            {SuKien.HE_2017, String.valueOf(SuKienNpc.HE)},
        };
        final List<String> sai = new ArrayList<String>();
        for (int i = 0; i < thuTu.length; i++) {
            final Map<String, Boolean> mot = new HashMap<String, Boolean>();
            mot.put(thuTu[i][0], Boolean.TRUE);
            SuKien.dat(mot);
            if (SuKienNpc.dangChay() != Integer.parseInt(thuTu[i][1])) {
                sai.add(thuTu[i][0] + " -> " + SuKienNpc.dangChay());
            }
            // (Trung Thu: NPC la bot dang ky luc bat may - tenNpcGoi50 can Map.npcVilage, khong goi trong test)
            if (SuKienNpc.dangChay() != SuKienNpc.TRUNG_THU && SuKienNpc.tenNpcGoi50() == null) {
                sai.add(thuTu[i][0] + ": khong co NPC tren ban do");
            }
            for (int j = i + 1; j < thuTu.length; j++) {
                final Map<String, Boolean> hai = new HashMap<String, Boolean>(mot);
                hai.put(thuTu[j][0], Boolean.TRUE);
                SuKien.dat(hai);
                if (SuKienNpc.dangChay() != Integer.parseInt(thuTu[i][1])) {
                    sai.add(thuTu[i][0] + "+" + thuTu[j][0] + " -> " + SuKienNpc.dangChay());
                }
            }
        }
        SuKien.dat(new HashMap<String, Boolean>());
        Kiem.bang("su kien -49: moi su kien co NPC, hai su kien chong nhau theo dung thu tu", "[]", sai.toString());
        Kiem.bang("khong su kien: NPC -49 khong hien", null, SuKienNpc.tenNpcGoi50());
        Kiem.bang("khong su kien: dangChay KHONG", SuKienNpc.KHONG, SuKienNpc.dangChay());
        for (final String t : new String[]{SuKien.TET_2017, SuKien.NOEL, SuKien.GIO_TO, SuKien.HALLOWEEN, SuKien.HE_2017, SuKien.SINH_NHAT_2011}) {
            final Map<String, Boolean> mot = new HashMap<String, Boolean>();
            mot.put(t, Boolean.TRUE);
            SuKien.dat(mot);
            final String ten = (String) SuKienNpc.tenNpcGoi50()[0];
            Kiem.dung("ten NPC su kien co dau, Pokemon: " + ten, !ten.equals(java.text.Normalizer.normalize(ten, java.text.Normalizer.Form.NFD).replaceAll("\\p{M}", "")) && ten.matches(".*(Meowth|Delibird|Gardevoir|Litleo).*"));
        }
        SuKien.dat(new HashMap<String, Boolean>());

        // ---------------------------------------------------------------- muc da go / chu menu
        Kiem.sai("-8: bo 'Hoạt động giờ' (chi bao Sắp ra mắt)", req.contains("\"Hoạt động giờ\""));
        Kiem.sai("-517: bo 'Khóa' (khong co xu ly)", rc.contains("\"Tháo\",\"Khóa\"") || rc.contains("\"Tháo\", \"Khóa\""));
        Kiem.dung("-10: bo 'Đăng ký giải Liên minh' khi khong co may chu Lien minh", req.contains("p.boMucTongQuan = 5;"));
        Kiem.dung("-10: an 'Nhận mốc nạp' khi qua moc nap da ngung (LauDai)", req.contains("if (!com.htbb.pokemon.LauDai.bat()) {\n                        String[] menuMocNap"));
        final String[] tuCu = {"Bút\", \"Búa\", \"Cung", "Trang bị ma", "vât lý", "VKTB", "Dương đông", "Sơn nam", "Lâm tây", "Bình kiều", "Chiến hạm",
            "\"Hồi hồ", "Trường giang", "Thuý Vũ", "Tuần Lộc", "(150L)", "(300l)", "(1t xu)", "(10l)", "lkd 30"};
        final String chuMenu = req + doc("src/real/NpcDichVu.java") + doc("src/real/SuKienNpc.java") + doc("src/real/MapCheTao.java")
                + mdm.substring(mdm.indexOf("case -19:"), mdm.indexOf("case -19:") + 4000);
        final List<String> con = new ArrayList<String>();
        for (final String t : tuCu) {
            if (chuMenu.contains(t)) {
                con.add(t);
            }
        }
        Kiem.bang("chu menu NPC khong con tu kiem hiep / viet tat", "[]", con.toString());
    }

    static Set<Integer> loaiTren(final int id) {
        final Set<Integer> s = new TreeSet<Integer>();
        for (final int[] n : NpcTinhBanDo.cua(id)) {
            s.add(n[2]);
        }
        return s;
    }

    static int[] loaiLenh(final int cmd) throws Exception {
        final java.lang.reflect.Method m = NpcTinhBanDo.class.getDeclaredMethod("loaiChoLenh", int.class);
        m.setAccessible(true);
        return (int[]) m.invoke(null, cmd);
    }

    static String rutGon(final Set<String> s) {
        final List<String> l = new ArrayList<String>(s);
        return l.size() <= 12 ? l.toString() : l.subList(0, 12) + " ... (" + l.size() + ")";
    }

    @SuppressWarnings("unused")
    private static String chuoi(final int[] a) {
        return Arrays.toString(a);
    }
}
