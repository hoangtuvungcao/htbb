import com.htbb.pokemon.MucTieuNhiemVu;
import data.InfoQuestTemplate;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;
import real.Char;
import real.MucTieuNhiemVuGame;
import real.NewCharQuest;

/**
 * TIEN DO NHIEM VU (nhanh sua-tien-do-nv, bao cua nguoi choi 0.9.13: "nhận nhiệm vụ rồi mà không xem được tiến độ",
 * "đánh 8 chục con vẫn chưa báo hoàn thành", "không biết trả ở đâu", "vô NPC không thấy Nhiệm vụ của tôi").
 * <ul>
 *   <li>Chu muc tieu (com.htbb.pokemon.MucTieuNhiemVu): "Hạ 20 Weedle ở Lavender", "Hạ Litwick ở Cerulean, nhặt 5 Gói
 *       hàng", tra "Machamp (Pewter)", mo ta chi tiet, ten cho client cu, hop nhac cua ai.</li>
 *   <li>real.MucTieuNhiemVuGame: ban do lay tu bang sinh quai (khong tu scontent), ly do khoa ban do.</li>
 *   <li>Goi -64 (MessageCreator.createMsgCharQuest) doc dung nhu client: client &lt; 915 ten + " · Weedle ở Lavender",
 *       mo ta "Mục tiêu: …"; client &gt;= 915 ten goc + duoi (muc / tra / khoa / co) dung so dong; client cu khong
 *       nhan duoi.</li>
 *   <li>Ma nguon: tien do gui lai khi tang, cua ai o khe doi + chan huy, nhac cua ai qua hop 95, "Nhiệm vụ của tôi" o NPC
 *       doi thuong (-90), thu_thap cua So tay so voi quai_roi.</li>
 * </ul>
 * Bot dau-cuoi: tools/pokemon/kiem_tai/KichBanTienDoNhiemVu (tdnv_*).
 */
final class KiemTienDoNhiemVu {

    private KiemTienDoNhiemVu() {
    }

    public static void chay() {
        Kiem.nhom("Tien do nhiem vu: muc tieu / ban do quai that / tra o dau trong so Nhiem vu, cua ai, Nhiem vu cua toi");
        try {
            thuan();
            game();
            maNguon();
        } catch (final Exception e) {
            e.printStackTrace(System.out);
            Kiem.dung("KiemTienDoNhiemVu chay het: " + e, false);
        }
    }

    static void thuan() {
        final MucTieuNhiemVu.Muc[] weedle = {new MucTieuNhiemVu.Muc("Weedle", 20, null, "Lavender")};
        Kiem.bang("giet: muc", "Hạ 20 Weedle ở Lavender", MucTieuNhiemVu.muc(MucTieuNhiemVu.GIET, weedle, 0, 12, "Hạ 20 Weedle ở Pewter"));
        Kiem.bang("giet: ngan (ten client cu)", "Weedle ở Lavender", MucTieuNhiemVu.ngan(MucTieuNhiemVu.GIET, weedle, 0));
        final MucTieuNhiemVu.Muc[] goi = {new MucTieuNhiemVu.Muc("Litwick", 5, "Gói hàng", "Cerulean")};
        Kiem.bang("thu thap: muc", "Hạ Litwick ở Cerulean, nhặt 5 Gói hàng", MucTieuNhiemVu.muc(MucTieuNhiemVu.THU, goi, 0, 5, ""));
        Kiem.bang("thu thap: ngan", "Gói hàng (Litwick, Cerulean)", MucTieuNhiemVu.ngan(MucTieuNhiemVu.THU, goi, 0));
        Kiem.bang("noi chuyen: dung scontent", "Nói chuyện với Kecleon ở Pewter",
                MucTieuNhiemVu.muc(MucTieuNhiemVu.NOI, new MucTieuNhiemVu.Muc[0], 0, 5, "Nói chuyện với Kecleon ở Pewter"));
        Kiem.bang("giet theo cap", "Hạ 25 Pokémon hoang dã chênh không quá 5 cấp",
                MucTieuNhiemVu.muc(MucTieuNhiemVu.GIET_CAP, new MucTieuNhiemVu.Muc[]{new MucTieuNhiemVu.Muc("", 25, null, "")}, 5, 10, ""));
        Kiem.bang("khong ban do: khong ghi ' ở'", "Hạ 3 Skorupi", MucTieuNhiemVu.muc(MucTieuNhiemVu.GIET,
                new MucTieuNhiemVu.Muc[]{new MucTieuNhiemVu.Muc("Skorupi", 3, null, "")}, 0, 1, ""));
        Kiem.bang("tra", "Machamp (Pewter)", MucTieuNhiemVu.tra("Machamp", "Pewter"));
        Kiem.bang("tra khong noi", "Machamp", MucTieuNhiemVu.tra("Machamp", ""));
        Kiem.bang("ten ngan NPC", "[Chansey, Kecleon, Xatu, Golem, Farfetch'd, Snorlax]", Arrays.asList(
                MucTieuNhiemVu.tenNgan("Y tá Chansey"), MucTieuNhiemVu.tenNgan("Kecleon tạp hoá"), MucTieuNhiemVu.tenNgan("Trưởng làng Xatu"),
                MucTieuNhiemVu.tenNgan("Golem bán hàng 1"), MucTieuNhiemVu.tenNgan("Farfetch'd thợ cả"), MucTieuNhiemVu.tenNgan("Ông Snorlax")).toString());
        Kiem.bang("mo ta dang lam", "Mục tiêu: Hạ 20 Weedle ở Lavender · Trả: Machamp (Pewter).",
                MucTieuNhiemVu.moTa("Hạ 20 Weedle ở Lavender", "Machamp (Pewter)", "", false, ""));
        Kiem.bang("mo ta xong", "Xong! Về trả Kecleon (Pewter).", MucTieuNhiemVu.moTa("x", "Kecleon (Pewter)", "khoa", true, ""));
        Kiem.dung("mo ta kem ly do khoa", MucTieuNhiemVu.moTa("Hạ Litwick ở Cerulean, nhặt 5 Gói hàng", "Kecleon (Pewter)",
                "Cần hoàn thành nhiệm vụ «Cửa ải: Rời đồng cỏ Pallet» để vào Cerulean.", false, "").endsWith("để vào Cerulean."));
        Kiem.bang("ten client cu", "Chứng tỏ sức mạnh · Weedle ở Lavender", MucTieuNhiemVu.tenCu("Chứng tỏ sức mạnh", "Weedle ở Lavender"));
        Kiem.bang("ten client cu, khong muc: giu nguyen", "Xe hàng lật", MucTieuNhiemVu.tenCu("Xe hàng lật", ""));
        Kiem.dung("ngan khong qua " + MucTieuNhiemVu.NGAN_TOI_DA + " ky tu", MucTieuNhiemVu.ngan(MucTieuNhiemVu.GIET,
                new MucTieuNhiemVu.Muc[]{new MucTieuNhiemVu.Muc("Pokémon có tên rất dài", 3, null, "Route 120 - Viện Thời tiết")}, 0).length()
                <= MucTieuNhiemVu.NGAN_TOI_DA);
        final String nhac = MucTieuNhiemVu.nhacCuaAi("Torchic", "Rời đồng cỏ Pallet", 1, 2, "hạ 20 Torchic ở Pallet");
        Kiem.dung("hop nhac cua ai: ten quai + cua ai + 'Nhận ngay?'", nhac.startsWith("Bạn đang hạ Torchic nhưng chưa nhận cửa ải «Rời đồng cỏ Pallet»")
                && nhac.endsWith("Nhận ngay?") && nhac.contains("bước 1/2"));
        Kiem.bang("id cua ai CA-01.2", "30012", String.valueOf(MucTieuNhiemVuGame.idCuaAi("CA-01.2")));
        Kiem.bang("id cua ai CA-12.3", "30123", String.valueOf(MucTieuNhiemVuGame.idCuaAi("CA-12.3")));
        Kiem.dung("id cua ai: ngoai khoang id nhiem vu doi (0..11) va trong short", MucTieuNhiemVuGame.laIdCuaAi(30012)
                && !MucTieuNhiemVuGame.laIdCuaAi(11) && MucTieuNhiemVuGame.idCuaAi("C03.2") == -1 && 30000 + 2699 < Short.MAX_VALUE);
    }

    static final List<io.Message> GOI = new ArrayList<io.Message>();

    static InfoQuestTemplate mau(final int type, final String ten, final int quai, final int can, final int vatPham, final int npc,
            final String sc) {
        final InfoQuestTemplate t = new InfoQuestTemplate();
        t.type = (byte) type;
        t.name = ten;
        t.shortContent = sc;
        t.idNpcRcv = (short) npc;
        t.idNpcRes = (short) npc;
        t.lv = 5;
        t.monsterKill = new Vector<Short>(Arrays.asList((short) quai));
        if (type == 2) {
            t.itemget = new Vector<Short>(Arrays.asList((short) vatPham));
            t.totalitemget = new Vector<Short>(Arrays.asList((short) can));
            t.totalMonsKilled = new Vector<Short>();
        } else {
            t.itemget = new Vector<Short>();
            t.totalitemget = new Vector<Short>();
            t.totalMonsKilled = new Vector<Short>(Arrays.asList((short) can));
        }
        t.potiongift = new short[0];
        t.npotiongift = new short[0];
        t.idItemgift = new short[0];
        return t;
    }

    static real.MonsterTemplate quai(final int id, final String ten) {
        final real.MonsterTemplate t = new real.MonsterTemplate();
        t.id = id;
        t.name = ten;
        return t;
    }

    /** Doc goi -64 nhom 2 nhu GameScr.onQuest; tra {ten[], moTa[], duoi[] (muc|tra|khoa|co) hoac null}. */
    static List<String[]> docDangLam(final io.Message m) throws Exception {
        final byte[] b = m.toByteArray();
        final DataInputStream d = new DataInputStream(new ByteArrayInputStream(b, 3, b.length - 3));
        final List<String[]> kq = new ArrayList<String[]>();
        final int nhom = d.readByte();
        final int n = d.readByte();
        for (int i = 0; i < n; i++) {
            final int id = d.readShort();
            final int ms = d.readByte();
            final String ten = d.readUTF();
            if (nhom != 2) {
                throw new IllegalStateException("chi doc nhom 2");
            }
            final int type = d.readByte();
            final String mt = d.readUTF();
            d.readByte();
            d.readByte();
            d.readUTF();
            String td = "";
            if (type == 2 || type == 0) {
                final int k = d.readByte();
                for (int j = 0; j < k; j++) {
                    td += d.readShort() + ":" + d.readShort() + "/" + d.readShort();
                }
            }
            kq.add(new String[]{String.valueOf(id), String.valueOf(ms), ten, mt, td, "", "", "", ""});
        }
        if (d.available() > 0) {
            final int k = d.readByte();
            if (k != n) {
                throw new IllegalStateException("duoi " + k + " != " + n);
            }
            for (int i = 0; i < k; i++) {
                final String[] r = kq.get(i);
                r[5] = d.readUTF();
                r[6] = d.readUTF();
                r[7] = d.readUTF();
                r[8] = String.valueOf(d.readByte());
            }
        }
        if (d.available() != 0) {
            throw new IllegalStateException("thua " + d.available() + " byte");
        }
        return kq;
    }

    static void game() throws Exception {
        if (real.Map.gemTemplate == null) {
            real.Map.gemTemplate = new real.GemTemplate[0];
        }
        real.Map.monsterTemplates.put(6, quai(6, "Litwick"));
        real.Map.monsterTemplates.put(11, quai(11, "Weedle"));
        real.Map.monsterTemplates.put(4, quai(4, "Torchic"));
        final HashMap<Integer, TreeSet<Integer>> o = new HashMap<Integer, TreeSet<Integer>>();
        o.put(6, new TreeSet<Integer>(Arrays.asList(4)));
        o.put(11, new TreeSet<Integer>(Arrays.asList(5)));
        o.put(4, new TreeSet<Integer>(Arrays.asList(1)));
        MucTieuNhiemVuGame.datBangQuai(o);
        final java.util.Map<Integer, String> tenMap = new HashMap<Integer, String>();
        tenMap.put(0, "Pewter");
        tenMap.put(1, "Pallet");
        tenMap.put(4, "Cerulean");
        tenMap.put(5, "Lavender");
        final java.util.Map<Integer, String> tenNpc = new HashMap<Integer, String>();
        tenNpc.put(1, "Kecleon tạp hoá");
        tenNpc.put(2, "Machamp thợ rèn");
        final java.util.Map<Integer, String> mapCu = com.htbb.pokemon.TenHien.bangMap();
        final java.util.Map<Integer, String> npcCu = com.htbb.pokemon.TenHien.bangNpc();
        com.htbb.pokemon.TenHien.datBang(tenMap, tenNpc);
        final String[] tenVpCu = data.ItemQuest.NAME_ITEM_QUEST;
        final String[] vp = Arrays.copyOf(tenVpCu == null ? new String[0] : tenVpCu, Math.max(11, tenVpCu == null ? 0 : tenVpCu.length));
        vp[10] = "Gói hàng";
        data.ItemQuest.NAME_ITEM_QUEST = vp;
        final short idC = 30001, idP = 30002; // id gia, khong dung bang that
        try {
            final InfoQuestTemplate c032 = mau(2, "Gom hàng rơi", 6, 5, 10, 1, "Thu 5 Gói hàng (rơi từ Litwick) ở Cerulean");
            c032.main_sub = 0;
            final InfoQuestTemplate p042 = mau(0, "Chứng tỏ sức mạnh", 11, 20, 0, 2, "Hạ 20 Weedle ở Pewter");
            p042.main_sub = 1;
            real.Map.allMainQuest.put(idC, c032);
            real.Map.allSubQuest.put(idP, p042);
            final MucTieuNhiemVuGame.KetQua a = MucTieuNhiemVuGame.cua(null, c032, 2);
            Kiem.bang("C03.2: muc tu du lieu dem + ban do quai that", "Hạ Litwick ở Cerulean, nhặt 5 Gói hàng", a.muc);
            Kiem.dung("C03.2: tra o Kecleon", a.tra.startsWith("Kecleon"));
            final MucTieuNhiemVuGame.KetQua b = MucTieuNhiemVuGame.cua(null, p042, 2);
            Kiem.bang("P-04.0.2: scontent 'ở Pewter' sai -> ban do quai that Lavender", "Hạ 20 Weedle ở Lavender", b.muc);
            Kiem.dung("P-04.0.2: tra o Machamp", b.tra.startsWith("Machamp"));

            final Char p = new Char(null);
            final int[] ban = {913};
            p.setSession(new io.Session(null, null, true) {
                @Override
                public void sendMessage(final io.Message m) {
                    GOI.add(m);
                }
            });
            p.isBot = -1;
            p.charDBID = 987650;
            p.charname = "kiemtdnv";
            p.lvDetail.lv = 12;
            p.mainQuest = new NewCharQuest(idC, (byte) 0);
            p.mainQuest.state_quest = NewCharQuest.WORKING;
            p.subQuest = new NewCharQuest(idP, (byte) 1);
            p.subQuest.state_quest = NewCharQuest.WORKING;
            p.clanQuestNew = new NewCharQuest(true);
            for (final int bc : new int[]{913, 914, 915}) {
                ban[0] = bc;
                p.getSession().banClient = bc;
                GOI.clear();
                real.MessageCreator.createMsgCharQuest(p, 2);
                Kiem.bang("client " + bc + ": mot goi -64", "1", String.valueOf(GOI.size()));
                final List<String[]> r = docDangLam(GOI.get(0));
                Kiem.bang("client " + bc + ": 2 dong dang lam", "2", String.valueOf(r.size()));
                final boolean moi = bc >= MucTieuNhiemVu.BAN_CLIENT;
                Kiem.bang("client " + bc + ": ten chinh tuyen", moi ? "Gom hàng rơi" : "Gom hàng rơi · Gói hàng (Litwick, Cerulean)", r.get(0)[2]);
                Kiem.bang("client " + bc + ": ten phu", moi ? "Chứng tỏ sức mạnh" : "Chứng tỏ sức mạnh · Weedle ở Lavender", r.get(1)[2]);
                Kiem.dung("client " + bc + ": mo ta chi tiet 'Mục tiêu: Hạ 20 Weedle ở Lavender · Trả: Machamp'",
                        r.get(1)[3].startsWith("Mục tiêu: Hạ 20 Weedle ở Lavender · Trả: Machamp"));
                Kiem.bang("client " + bc + ": tien do 0/5 (so may chu kiem)", "6:0/5", r.get(0)[4].replace("10:", "6:"));
                if (moi) {
                    Kiem.bang("client 915: duoi muc", "Hạ 20 Weedle ở Lavender", r.get(1)[5]);
                    Kiem.dung("client 915: duoi tra", r.get(1)[6].startsWith("Machamp"));
                } else {
                    Kiem.bang("client " + bc + ": khong co duoi (client cu khong doc)", "", r.get(1)[5]);
                }
            }
        } finally {
            real.Map.allMainQuest.remove(idC);
            real.Map.allSubQuest.remove(idP);
            com.htbb.pokemon.TenHien.datBang(mapCu, npcCu);
            data.ItemQuest.NAME_ITEM_QUEST = tenVpCu;
        }
    }

    static String doc(final String f) throws Exception {
        return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
    }

    static void maNguon() throws Exception {
        final String ch = doc("src/real/Char.java");
        Kiem.dung("Char.checkFinishQuest: tien do tang -> gui lai Dang lam", ch.contains("guiTienDo = true;")
                && ch.contains("if (guiTienDo) {\n            MessageCreator.createMsgCharQuest(this, 2);"));
        Kiem.dung("Char.xuLyNhiemVu: id cua ai o khe doi -> MucTieuNhiemVuGame (khong NPE / khong nham nhiem vu doi)",
                ch.contains("main_sub == 2 && MucTieuNhiemVuGame.laIdCuaAi(questID)"));
        Kiem.dung("Char: 'Chưa xong' o NPC kem muc tieu + tien do", ch.contains("MucTieuNhiemVuGame.kemMucTieu(this, info, this.subQuest"));
        final String mc = doc("src/real/MessageCreator.java");
        Kiem.dung("-64: duoi chi cho client >= 915", mc.contains("final boolean moi = MucTieuNhiemVuGame.clientMoi(p);") && mc.contains("if (moi) {"));
        Kiem.dung("-64: cua ai o khe doi", mc.contains("MucTieuNhiemVuGame.cuaAiTrongSo(p, type)"));
        Kiem.dung("RoiLauDai: ha quai -> nhac cua ai", doc("src/real/RoiLauDai.java").contains("MucTieuNhiemVuGame.khiHaQuai(p, mt);"));
        Kiem.dung("NhiemVuEngine.luu: cua ai doi -> gui lai so", doc("src/real/NhiemVuEngine.java").contains("MucTieuNhiemVuGame.khiDoiCuaAi(p, m);"));
        Kiem.dung("KhoaBanDoGame.hoiNhac: hop 95 (Đồng ý = khiDongYCuaAi)", doc("src/real/KhoaBanDoGame.java").contains("static void hoiNhac("));
        final String npc = doc("src/real/cmd/RequestNPCInfoHandler.java");
        Kiem.dung("-90: muc 'Nhiệm vụ của tôi' thang", npc.contains("real.SoTayLauDai.TEN_NHIEM_VU") && npc.contains("real.SoTayLauDai.viTriNhiemVu()"));
        Kiem.dung("MapDoMenu: -90 muc Nhiệm vụ của tôi -> NhiemVuEngine.moMenu",
                doc("src/real/MapDoMenu.java").contains("idOptionMenu == SoTayLauDai.viTriNhiemVu()) {\n                NhiemVuEngine.moMenu(player);"));
        final com.htbb.pokemon.NhiemVuMau m = new com.htbb.pokemon.NhiemVuMau();
        m.kieu = "thu_thap";
        m.mucTieuId = "10";
        m.quaiRoi = "1";
        Kiem.dung("So tay thu_thap: dem theo quai_roi (Sandshrew 1), khong theo id vat pham (10 = Oddish)", m.khop("thu_thap", "1") && !m.khop("thu_thap", "10"));
        m.quaiRoi = "";
        Kiem.dung("thu_thap khong quai_roi: nhu cu", m.khop("thu_thap", "10"));
    }
}
