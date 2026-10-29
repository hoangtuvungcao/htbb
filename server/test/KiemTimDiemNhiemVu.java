import com.htbb.pokemon.DinhDangBanDo;
import com.htbb.pokemon.KhuVucNhiemVu;
import data.InfoQuestTemplate;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import real.Char;
import real.KhuVucNhiemVuGame;
import real.NewCharQuest;

/**
 * BAO LOI NGUOI CHOI 29/10/2026 (khoi sua_tim_nhiem_vu.sql + real.KhuVucNhiemVuGame / MucTieuNhiemVuGame / MessageCreator):
 * <ol>
 *   <li>"Lam sao toi o 53,19": buoc dia diem ghi vi tri bang loi ("góc trên bên phải bản đồ Pallet"), so nhiem vu client
 *       moi (duoi muc tieu) co chi duong, diem dich (duoi thu hai -64) cho dau sao / nut "Đi tới dấu sao"; khu xa khong
 *       cong -> nhac Arcanine. Moi vung (khong ca ban do) rong >= 2 o moi phia tam; o tam C01.5 di duoc va toi duoc tu cong.</li>
 *   <li>"Chua sua loi nv nha Pewter": C05.2 chi tinh nha 100 (khong co cua o dau ca) -> moi buoc co vung tren ban do
 *       trong nha phai co it nhat mot vung la nha CO CUA (102 / 72 / 82 o ba lang); chu buoc noi duong di.</li>
 *   <li>Vuon / trung: muc huong dan dung nhan menu that cua He sinh thai.</li>
 *   <li>Ve tau 15.000 -> 5.000 xu (UPDATE theo gia cu).</li>
 * </ol>
 */
public final class KiemTimDiemNhiemVu {

    private KiemTimDiemNhiemVu() {
    }

    public static void chay() {
        Kiem.nhom("Tim diem nhiem vu / nha Pewter / vuon - trung / ve tau (bao loi 29/10/2026)");
        try {
            viTri();
            game();
            final String khoi = DayDu.phanNangCap("sua_tim_nhiem_vu.sql");
            Kiem.dung("co khoi sua_tim_nhiem_vu.sql trong sql/htbb.sql", khoi.length() > 0);
            duLieu(DayDu.sql(), khoi);
        } catch (final Exception e) {
            e.printStackTrace();
            Kiem.dung("khong nem loi: " + e, false);
        }
    }

    static void viTri() {
        final KhuVucNhiemVu.Vung c015 = new KhuVucNhiemVu.Vung("C01.5", 1, 50, 16, 56, 22, false, "Pallet");
        Kiem.bang("53,19 tren 80x100 = goc tren ben phai", "góc trên bên phải", KhuVucNhiemVu.viTri(c015, 80, 100));
        Kiem.bang("giua", "giữa", KhuVucNhiemVu.viTri(new KhuVucNhiemVu.Vung("X", 1, 37, 47, 43, 53, false, ""), 80, 100));
        Kiem.bang("goc duoi ben trai", "góc dưới bên trái", KhuVucNhiemVu.viTri(new KhuVucNhiemVu.Vung("X", 1, 2, 80, 6, 90, false, ""), 80, 100));
        Kiem.bang("ca ban do: khong vi tri", "", KhuVucNhiemVu.viTri(new KhuVucNhiemVu.Vung("X", 1, 0, 0, 255, 255, false, ""), 80, 100));
        Kiem.bang("khong ro kich thuoc", "", KhuVucNhiemVu.viTri(c015, 0, 0));
    }

    static InfoQuestTemplate mau(final String ma, final short id, final int mainSub) {
        final InfoQuestTemplate t = KiemSuaTuyen.mau(ma, id);
        t.main_sub = (byte) mainSub;
        t.shortContent = "Ra đồng cỏ Pallet";
        t.monsterKill = new java.util.Vector<Short>();
        t.totalMonsKilled = new java.util.Vector<Short>();
        t.itemget = new java.util.Vector<Short>();
        t.totalitemget = new java.util.Vector<Short>();
        return t;
    }

    static void game() {
        final HashMap<String, List<KhuVucNhiemVu.Vung>> m = new HashMap<String, List<KhuVucNhiemVu.Vung>>();
        m.put("TD.1", new ArrayList<KhuVucNhiemVu.Vung>(Arrays.asList(new KhuVucNhiemVu.Vung("TD.1", 1, 50, 16, 56, 22, false, "Pallet"))));
        m.put("TD.2", new ArrayList<KhuVucNhiemVu.Vung>(Arrays.asList(new KhuVucNhiemVu.Vung("TD.2", 118, 0, 0, 255, 255, false, "Đường Victory"))));
        KhuVucNhiemVuGame.dat(m);
        final real.GemTemplate[] gemCu = real.Map.gemTemplate;
        if (real.Map.gemTemplate == null) {
            real.Map.gemTemplate = new real.GemTemplate[0];
        }
        final short id = (short) 31990;
        try {
            final InfoQuestTemplate a = mau("TD.1", id, 0);
            final Char p = new Char(null);
            p.setSession(new io.Session(null, null, true));
            p.getSession().banClient = KhuVucNhiemVuGame.BAN_DAU_SAO;
            final real.Map ban = new real.Map();
            ban.mapIDLoadMap = 1;
            p.map = ban;
            p.mapID = 1;
            p.x = 3 * 16 + 8;
            p.y = 61 * 16 + 8;
            final NewCharQuest q = new NewCharQuest(true);
            q.id_quest = id;
            q.main_sub = 0;
            q.state_quest = NewCharQuest.WORKING;
            p.mainQuest = q;
            real.Map.allMainQuest.put(Short.valueOf(id), a);
            final String md = KhuVucNhiemVuGame.moTaDangLam(p, q, a);
            Kiem.dung("cung ban do: con N o + huong (" + md + ")", md.contains("còn khoảng") && md.contains("phía Đông Bắc"));
            Kiem.dung("cung ban do: vi tri bang loi (" + md + ")", md.contains("góc trên bên phải bản đồ Pallet"));
            Kiem.dung("cung ban do: nhac dau sao", md.contains("dấu sao vàng"));
            p.getSession().banClient = KhuVucNhiemVuGame.BAN_DAU_SAO - 1;
            final String cu = KhuVucNhiemVuGame.moTaDangLam(p, q, a);
            Kiem.dung("client cu (chua ve dau sao): co vi tri bang loi, KHONG nhac dau sao (" + cu + ")", cu.contains("góc trên bên phải") && !cu.contains("dấu sao"));
            p.getSession().banClient = KhuVucNhiemVuGame.BAN_DAU_SAO;
            Kiem.bang("diem dich (ban do, tam, nua rong / cao, ca ban do)", "[1, 53, 19, 3, 3, 0]", Arrays.toString(KhuVucNhiemVuGame.dich(p, q, a)));
            Kiem.bang("ten diem dich", "Pallet (góc trên bên phải)", KhuVucNhiemVuGame.tenDich(p, a));
            final String muc = real.MucTieuNhiemVuGame.cua(p, a, 2).muc;
            Kiem.dung("so nhiem vu client moi: muc co chi duong (" + muc + ")", muc.startsWith("Ra đồng cỏ Pallet") && muc.contains("góc trên bên phải"));
            Kiem.sai("nhom 1 (da toi) khong chi duong", real.MucTieuNhiemVuGame.cua(p, a, 1).muc.contains("còn khoảng"));
            // ban do khac
            ban.mapIDLoadMap = 0;
            p.mapID = 0;
            final String khac = KhuVucNhiemVuGame.moTaDangLam(p, q, a);
            Kiem.dung("ban do khac: toi Pallet + vi tri (" + khac + ")", khac.contains("hãy tới Pallet") && khac.contains("góc trên bên phải bản đồ Pallet"));
            Kiem.sai("ban do khac co cong: khong nhac Arcanine", khac.contains("Arcanine"));
            // da toi / khong dang lam: khong diem dich
            q.state_quest = NewCharQuest.FINISH;
            Kiem.dung("da toi: khong diem dich", KhuVucNhiemVuGame.dich(p, q, a) == null);
            q.state_quest = NewCharQuest.WORKING;
            // khu xa khong cong
            final InfoQuestTemplate b = mau("TD.2", (short) (id + 1), 0);
            final String xa = KhuVucNhiemVuGame.moTaDangLam(p, q, b);
            Kiem.dung("khu xa (Duong Victory): nhac Arcanine (" + xa + ")", xa.contains("Arcanine"));
            Kiem.bang("khu xa: diem ca ban do", "1", String.valueOf(KhuVucNhiemVuGame.dich(p, q, b)[5]));
            // buoc thuong (khong vung)
            final InfoQuestTemplate c = mau("KHONG_VUNG", (short) (id + 2), 0);
            Kiem.dung("buoc khong vung: khong diem dich", KhuVucNhiemVuGame.dich(p, q, c) == null);
        } finally {
            real.Map.allMainQuest.remove(Short.valueOf(id));
            real.Map.gemTemplate = gemCu;
            KhuVucNhiemVuGame.dat(null);
        }
        // goi -64: duoi thu hai sau duoi muc tieu (client cu doc xong duoi thu nhat la dung -> khong anh huong)
        try {
            final String mc = KiemSuaTuyen.doc("src/real/MessageCreator.java");
            final int d1 = mc.indexOf("m.dos.writeByte(Integer.parseInt(d[3]));");
            final int d2 = mc.indexOf("m.dos.writeShort(d == null ? -1 : d[0]);");
            Kiem.dung("-64: duoi diem dich viet SAU duoi muc tieu", d1 > 0 && d2 > d1);
            final String gs = KiemSuaTuyen.doc("../client_jar/src/game/render/screen/GameScr.java");
            Kiem.dung("client doc duoi thu hai (docDiemNhiemVu)", gs.contains("docDiemNhiemVu(m, qs);"));
        } catch (final Exception e) {
            Kiem.dung("doc ma -64: " + e, false);
        }
    }

    static final Pattern KHUNG_UPDATE = Pattern.compile("UPDATE `data_nhiem_vu_khu_vuc` SET `map_id` = (\\d+), `x1` = (-?\\d+), `y1` = (-?\\d+), `x2` = (\\d+), `y2` = (\\d+)[^;]*WHERE `ma` = '([^']+)' AND `stt` = (\\d+)");

    static void duLieu(final String du, final String khoi) throws Exception {
        // --- vung dich hieu luc: INSERT INTO (phan A + sua_tuyen) + INSERT IGNORE cua khoi nay + UPDATE khung cua mo_duong
        final Map<String, int[]> vung = new LinkedHashMap<String, int[]>();
        for (final List<String> r : KiemSuaTuyen.dong(du, "data_nhiem_vu_khu_vuc")) {
            vung.put(r.get(0) + "#" + r.get(1).trim(), new int[]{so(r.get(2)), so(r.get(3)), so(r.get(4)), so(r.get(5)), so(r.get(6)), so(r.get(8))});
        }
        final String ig = khoi.replace("INSERT IGNORE INTO `data_nhiem_vu_khu_vuc`", "INSERT INTO `data_nhiem_vu_khu_vuc`");
        final List<List<String>> moi = KiemSuaTuyen.dong(ig, "data_nhiem_vu_khu_vuc");
        for (final List<String> r : moi) {
            vung.put(r.get(0) + "#" + r.get(1).trim(), new int[]{so(r.get(2)), so(r.get(3)), so(r.get(4)), so(r.get(5)), so(r.get(6)), so(r.get(8))});
        }
        final Matcher mu = KHUNG_UPDATE.matcher(du);
        while (mu.find()) {
            final int[] v = vung.get(mu.group(6) + "#" + mu.group(7));
            if (v != null) {
                for (int i = 0; i < 5; i++) {
                    v[i] = Integer.parseInt(mu.group(i + 1));
                }
            }
        }
        Kiem.dung("doc duoc vung dich (" + vung.size() + ")", vung.size() >= 100);
        // (b) ban kinh >= 2 o moi phia tam
        final List<String> hep = new ArrayList<String>();
        for (final Map.Entry<String, int[]> e : vung.entrySet()) {
            final int[] v = e.getValue();
            final boolean ca = v[1] <= 0 && v[2] <= 0 && v[3] >= 250 && v[4] >= 250;
            if (!ca && (v[3] - v[1] < 4 || v[4] - v[2] < 4)) {
                hep.add(e.getKey());
            }
        }
        Kiem.bang("moi vung dung_toa_do rong >= 2 o quanh tam (khong phai dung dung mot o)", "[]", hep.toString());
        // o tam C01.5 / C02.4 di duoc va lien thong voi cong (vao duoc tu Pewter / Blackthorn / Cianwood)
        final DinhDangBanDo p1 = KiemSuaTuyen.banDo(1);
        final boolean[] vao = KiemSuaTuyen.vao(1);
        Kiem.dung("Pallet o 53,19 di duoc + toi duoc tu cong", vao[19 * p1.w + 53]);
        // cua vao tung ban do (o cong tren MOI tep cMap, toM = id ban do trong nha)
        final Set<Integer> coCua = new HashSet<Integer>();
        final File[] tep = new File("cMap").listFiles();
        for (final File f : tep == null ? new File[0] : tep) {
            if (f.getName().matches("\\d+")) {
                try {
                    for (final DinhDangBanDo.Cong c : KiemSuaTuyen.banDo(Integer.parseInt(f.getName())).cong) {
                        coCua.add(Integer.valueOf(c.toM));
                    }
                } catch (final Exception ignored) {
                    // tep hong: KiemSuaTuyen / KiemMapPokemon bao
                }
            }
        }
        final Set<Integer> trongNha = new HashSet<Integer>();
        for (final List<String> r : KiemSuaTuyen.dong(du, "data_map_ten")) {
            if ("trong_nha".equals(r.get(3).trim())) {
                trongNha.add(Integer.valueOf(so(r.get(0))));
            }
        }
        Kiem.dung("doc duoc ban do trong nha (" + trongNha.size() + ")", trongNha.contains(Integer.valueOf(100)) && trongNha.contains(Integer.valueOf(102)));
        Kiem.sai("nha 100 (Bao tang Pewter) KHONG co cua (chi Arcanine) - ly do loi C05.2", coCua.contains(Integer.valueOf(100)));
        final Map<String, Boolean> maCoCua = new HashMap<String, Boolean>();
        final Map<String, Boolean> maTrongNha = new HashMap<String, Boolean>();
        for (final Map.Entry<String, int[]> e : vung.entrySet()) {
            if (e.getValue()[5] != 1) {
                continue;
            }
            final String ma = e.getKey().substring(0, e.getKey().indexOf('#'));
            final Integer mp = Integer.valueOf(e.getValue()[0]);
            if (trongNha.contains(mp)) {
                maTrongNha.put(ma, Boolean.TRUE);
                if (coCua.contains(mp)) {
                    maCoCua.put(ma, Boolean.TRUE);
                }
            }
        }
        final List<String> khongCua = new ArrayList<String>();
        for (final String ma : maTrongNha.keySet()) {
            if (!maCoCua.containsKey(ma)) {
                khongCua.add(ma);
            }
        }
        Kiem.bang("buoc dich trong nha: co it nhat mot nha co cua di bo vao", "[]", khongCua.toString());
        for (final int nha : new int[]{102, 72, 82}) {
            final int[] v = vung.get("C05.2#" + (nha == 102 ? 1 : nha == 72 ? 2 : 3));
            Kiem.dung("C05.2 tinh nha " + nha + " (bat)", v != null && v[0] == nha && v[5] == 1);
            Kiem.dung("nha " + nha + " co cua", coCua.contains(Integer.valueOf(nha)));
        }
        // nha co cua cua ba lang dung la nha co Kecleon (loai 1) dung truoc cua
        for (final int[] lc : new int[][]{{0, 102}, {70, 72}, {80, 82}}) {
            final DinhDangBanDo l = KiemSuaTuyen.banDo(lc[0]);
            boolean gan = false;
            for (final DinhDangBanDo.Cong c : l.cong) {
                if (c.toM == lc[1]) {
                    for (final DinhDangBanDo.Diem d : l.npc) {
                        gan |= d.v == 1 && Math.abs(d.x - c.x) <= 3 && Math.abs(d.y - c.y) <= 3;
                    }
                }
            }
            Kiem.dung("lang " + lc[0] + ": Kecleon dung truoc cua nha " + lc[1] + " (chu buoc dung)", gan);
        }
        Kiem.dung("chu C05.2 noi Kecleon + Arcanine", khoi.contains("Vào ngôi nhà lớn có Kecleon đứng trước cửa") && khoi.contains("nhờ Arcanine gác cổng"));
        Kiem.dung("chu C01.5 / C02.4 sua 'phia bac' -> cong phia Dong", khoi.contains("WHERE `id` = 1005") && khoi.contains("ngoài cổng phía Đông làng")
                && khoi.contains("`content` = '1Kecleon: Kẻ giả mạo hay quay lại bãi cỏ phía bắc lúc vắng người."));
        // Pallet that su o cong phia Dong ca ba lang (cong ra ban do nhom 1)
        for (final int lang : new int[]{0, 70, 80}) {
            final DinhDangBanDo l = KiemSuaTuyen.banDo(lang);
            boolean dong = false;
            for (final DinhDangBanDo.Cong c : l.cong) {
                dong |= c.toM == 1 && c.x >= l.w - 2;
            }
            Kiem.dung("lang " + lang + ": cong ra Pallet o mep phia Dong", dong);
        }
        // --- huong dan: nhan menu that
        final String hst = KiemSuaTuyen.doc("src/com/htbb/pokemon/HeSinhThaiMenu.java");
        for (final String nhan : new String[]{"Nhà trẻ và trứng", "Gửi pet đang sở hữu", "Trả phí hôm nay", "Xem trứng", "Cho trứng nở",
            "Chợ sinh thái", "Mua: Trứng", "Mua: Berry", "Vườn nhà và bạn bè"}) {
            Kiem.dung("huong dan dung nhan menu that: " + nhan, khoi.contains(nhan) && hst.contains("\"" + nhan));
        }
        Kiem.dung("muc huong dan npc.18..20 (So tay > cong dung cac NPC)", khoi.contains("('npc.18_diem_nhiem_vu', 47") && khoi.contains("('npc.19_trung', 48")
                && khoi.contains("('npc.20_vuon', 49"));
        Kiem.dung("huong dan giu chu admin da sua (sua_boi)", khoi.contains("IF(`sua_boi` = '', VALUES(`noi_dung`), `noi_dung`)"));
        Kiem.sai("he sinh thai khong con noi 'khu vuon (o dat) san co'", hst.contains("khu vườn (ô đất) sẵn có"));
        // --- ve tau
        Kiem.dung("ve tau 15.000 -> 5.000 (UPDATE theo gia cu)", khoi.contains("UPDATE `data_potion` SET `price` = 5000 WHERE `id` = 20 AND `price` = 15000;"));
        Kiem.dung("phan A van 15.000 (khoi doi gia, admin sua thi giu)", du.contains("(20,13,0,0,'Vé tàu','vetau',15000,-1)"));
        // 5.000 xu <= 1 gio thu nhap thiet ke o cap can tau (27), >= 1/4 gio (van la cho tieu)
        long gio27 = 0;
        for (final List<String> r : KiemSuaTuyen.dong(du, "data_cap_do_lau_dai")) {
            if (so(r.get(0)) == 27) {
                gio27 = (long) so(r.get(4)) * so(r.get(5)); // quai_moi_gio x xu_moi_quai
            }
        }
        Kiem.dung("ve tau 5.000 trong [1/4, 1] gio thu nhap cap 27 (" + gio27 + " xu/gio)", 5000L <= gio27 && 5000L * 4 >= gio27);
    }

    static int so(final String s) {
        return Integer.parseInt(s.trim());
    }
}
