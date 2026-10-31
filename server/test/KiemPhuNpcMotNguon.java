import com.htbb.pokemon.LauDai;
import com.htbb.pokemon.NhiemVuLauDai;
import com.htbb.pokemon.NhiemVuMau;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import real.Char;
import real.NewCharQuest;
import real.NhiemVuEngine;

/**
 * CHU GAME 31/10/2026 (bot cb2: bot muc tieu phu cap = cap nhiem vu + 3, luyen len vai cap la nhiem vu phu bien khoi danh
 * sach NPC; nhan qua So tay thi buoc khong hien o NPC "Đang làm"). Kiem:
 * <ul>
 *   <li>NhiemVuLauDai.hienNpc / locPhu: danh sach NPC (-64) hien nhiem vu phu cap - 15 .. cap + 3 (ca nhiem vu da vuot cap
 *       chua lam), gan cap nhat truoc, toi da 30; nhan van theo hopCap (truoc toi 10 cap, khong chan duoi);</li>
 *   <li>NhiemVuEngine.phuSoTayChoNpc / maPhuSoTay / traPhuSoTayQuaNpc: buoc phu nhan o So tay hien o NPC "Đang làm"
 *       (tien do engine), xong -&gt; "Hoàn thành", tra o NPC = nhan thuong engine mot lan; buoc dang o khe NPC cu, da nhan
 *       thuong, khong phai loai phu -&gt; khong them dong;</li>
 *   <li>noi day: MessageCreator.createMsgCharQuest (loai 1 / 2), Char.OnQuest (main_sub 1, tra / huy).</li>
 * </ul>
 */
public final class KiemPhuNpcMotNguon {

    private KiemPhuNpcMotNguon() {
    }

    static final int ID = 987_654_323;
    static final short ID_NPC = 31_999;

    public static void chay() {
        System.out.println("== Nhiem vu phu: danh sach NPC gui het cap -15..+3; buoc nhan o So tay hien o NPC (chu game 31/10/2026)");
        danhSach();
        try {
            motNguon();
        } catch (final Throwable e) {
            Kiem.dung("khong nem loi: " + e, false);
        }
        try {
            goiLon();
        } catch (final Throwable e) {
            Kiem.dung("goi -64 lon: khong nem loi: " + e, false);
        }
        noiDay();
    }

    /** Nguoi choi thu: giu goi tin gui di. */
    static final class NguoiChoi extends Char {
        final List<io.Message> goi = new ArrayList<io.Message>();

        NguoiChoi() {
            super(null);
            charDBID = 0; // khong doc CSDL tien do engine
            charname = "kiemgoilon";
            isBot = -1;
            lvDetail = new real.LevelDetail();
            lvDetail.lv = 70;
            mainQuest = new NewCharQuest(true);
            mainQuest.state_quest = NewCharQuest.DONE;
            subQuest = new NewCharQuest(true);
            clanQuestNew = new NewCharQuest(true);
        }

        @Override
        public void sendMessage(final io.Message m) {
            goi.add(m);
        }
    }

    /**
     * Chu game 31/10/2026: gui het cua so -15..+3 (bang that toi da 117 nhiem vu phu / cap, cap 70). Dung goi -64 loai 0 THAT
     * (MessageCreator.createMsgCharQuest) voi 117 nhiem vu chu dai nhat bang that (ten 35, loi giao 302 ky tu, co dau), roi doc
     * dung nhu client J2ME (GameScr.onQuest: so luong la byte CO DAU, moi dong short/byte/UTF/byte/UTF tach '>'/byte/byte,
     * duoi docDuoiNhiemVu) - het goi, kich thuoc trong khung 2 / 4 byte cua Session_ME.readMessage.
     */
    static void goiLon() throws Exception {
        final boolean batCu = LauDai.bat();
        final real.GemTemplate[] gemCu = real.Map.gemTemplate;
        if (real.Map.gemTemplate == null) {
            real.Map.gemTemplate = new real.GemTemplate[0];
        }
        LauDai.dat(true);
        final java.util.Map<Short, data.InfoQuestTemplate> cu = new java.util.HashMap<Short, data.InfoQuestTemplate>(real.Map.allSubQuest);
        real.Map.allSubQuest.clear();
        final StringBuilder loi = new StringBuilder();
        while (loi.length() < 302) {
            loi.append("1Hãy hạ giúp ta đàn quái quấy phá ở bìa rừng phía đông, chúng đã phá vườn mọng nhiều đêm liền>");
        }
        try {
            for (int i = 0; i < 117; i++) {
                final data.InfoQuestTemplate t = new data.InfoQuestTemplate();
                t.id = (short) (30_001 + i);
                t.ma = "P-GOI." + i;
                t.main_sub = 1;
                t.lv = (short) (55 + i % 19); // cua so cap 70: 55..73
                t.idNpcRcv = (byte) (1 + i % 30);
                t.name = "Nhiệm vụ phụ dài nhất số " + i + " ở đây";
                t.content = loi.substring(0, 302);
                t.monsterKill = new java.util.Vector<Short>(Collections.singletonList(Short.valueOf((short) 16)));
                t.totalMonsKilled = new java.util.Vector<Short>(Collections.singletonList(Short.valueOf((short) 10)));
                t.itemget = new java.util.Vector<Short>();
                t.totalitemget = new java.util.Vector<Short>();
                real.Map.allSubQuest.put(Short.valueOf(t.id), t);
            }
            final NguoiChoi p = new NguoiChoi();
            final long t0 = System.nanoTime();
            real.MessageCreator.createMsgCharQuest(p, 0);
            final long ms = (System.nanoTime() - t0) / 1_000_000L;
            Kiem.bang("mot goi -64", 1, p.goi.size());
            if (p.goi.isEmpty()) {
                return;
            }
            final byte[] b = p.goi.get(0).toByteArray();
            final java.io.DataInputStream r0 = new java.io.DataInputStream(new java.io.ByteArrayInputStream(b));
            int cmd = r0.readByte();
            final int len;
            if (cmd == -128) {
                cmd = r0.readByte();
                len = r0.readInt();
            } else {
                len = r0.readUnsignedShort();
            }
            Kiem.bang("lenh -64", -64, cmd);
            // 117 dong deu dai nhat bang that (~54 KB) la tran tren; bang that o cap 70 ~30 KB. Session_ME.readMessage doc khung 2 byte
            // (<= 65535) hoac -128 + 4 byte; capPhat thieu bo nho thi giai phong roi cap lai (goi anh / ban do con lon hon).
            Kiem.dung("kich thuoc " + len + " B (tran tren, chu dai nhat x 117) <= 65535: vua khung 2 byte cua client", len > 0 && len <= 65535);
            final java.io.DataInputStream r = new java.io.DataInputStream(new java.io.ByteArrayInputStream(b, b.length - len, len));
            final byte loai = r.readByte();
            final byte so = r.readByte(); // J2ME: byte size = m.reader().readByte()
            Kiem.bang("loai 0 (co the nhan)", 0, (int) loai);
            Kiem.bang("so luong doc bang byte co dau = 117 (khong am)", 117, (int) so);
            final int[] moiNpc = new int[256];
            int dong = 0;
            for (int i = 0; i < so; i++) {
                r.readShort();
                r.readByte();
                r.readUTF();
                moiNpc[r.readByte() & 255]++;
                r.readUTF();
                r.readByte();
                r.readByte();
                dong++;
            }
            if (r.available() > 0) { // duoi docDuoiNhiemVu (client >= 915)
                final int n = r.readUnsignedByte();
                for (int i = 0; i < n; i++) {
                    r.readUTF();
                    r.readUTF();
                    r.readUTF();
                    r.readByte();
                }
            }
            if (r.available() > 0) { // duoi thu hai (client >= 0.9.23)
                final int n = r.readUnsignedByte();
                for (int i = 0; i < n; i++) {
                    r.readShort();
                    r.readShort();
                    r.readShort();
                    r.readByte();
                    r.readByte();
                    r.readByte();
                    r.readUTF();
                }
            }
            Kiem.bang("doc het 117 dong nhu J2ME", 117, dong);
            Kiem.bang("doc het goi (khong thua / thieu byte)", 0, r.available());
            int max = 0;
            for (final int c : moiNpc) {
                max = Math.max(max, c);
            }
            Kiem.dung("moi NPC giao chi hien nhiem vu cua minh: toi da " + max + " dong / NPC (30 NPC)", max <= 4);
            Kiem.dung("dung goi 117 dong nhanh (" + ms + " ms)", ms < 2000);
            System.out.println("   (goi -64 loai 0, 117 nhiem vu phu: " + len + " B, " + ms + " ms)");
        } finally {
            real.Map.allSubQuest.clear();
            real.Map.allSubQuest.putAll(cu);
            LauDai.dat(batCu);
            real.Map.gemTemplate = gemCu;
        }
    }

    static void danhSach() {
        Kiem.dung("cap 40: phu cap 43 hien o NPC", NhiemVuLauDai.hienNpc(43, 40));
        Kiem.sai("cap 40: phu cap 44 khong hien o NPC (So tay)", NhiemVuLauDai.hienNpc(44, 40));
        Kiem.dung("cap 40: phu cap 25 (vuot 15 cap) van hien", NhiemVuLauDai.hienNpc(25, 40));
        Kiem.sai("cap 40: phu cap 24 khong hien o NPC (So tay)", NhiemVuLauDai.hienNpc(24, 40));
        Kiem.dung("nhan van theo hopCap: cap 40 nhan phu cap 50 / cap 1 (So tay)", NhiemVuLauDai.hopCap(50, 40) && NhiemVuLauDai.hopCap(1, 40));
        // bot cb2: nhiem vu cap 9, nguoi choi cap 12 -> 18 (luyen chuoi chinh): truoc day 20 muc gan nhat
        final int[] it = {9, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21};
        for (final int cap : new int[]{12, 18, 24}) {
            final List<Integer> c = NhiemVuLauDai.locPhu(it, cap, NhiemVuLauDai.PHU_TOI_DA);
            Kiem.dung("it nhiem vu, cap " + cap + ": phu cap 9 (vuot " + (cap - 9) + " cap) van o danh sach NPC", c.contains(Integer.valueOf(0)));
        }
        final int[] lv = new int[413];
        for (int i = 0; i < lv.length; i++) {
            lv[i] = 1 + i / 4; // ~4 nhiem vu / cap nhu bang that
        }
        final List<Integer> c = NhiemVuLauDai.locPhu(lv, 40, NhiemVuLauDai.PHU_TOI_DA);
        Kiem.bang("gui het cua so cap 25..43 (19 cap x 4 = 76 muc)", Integer.valueOf(76), Integer.valueOf(c.size()));
        Kiem.bang("muc dau la phu cap 40 (gan nhat)", Integer.valueOf(40), Integer.valueOf(lv[c.get(0).intValue()]));
        boolean dung = true;
        for (int i = 1; i < c.size(); i++) {
            dung &= Math.abs(lv[c.get(i).intValue()] - 40) >= Math.abs(lv[c.get(i - 1).intValue()] - 40);
        }
        Kiem.dung("xep gan cap nhat truoc", dung);
        Kiem.dung("PHU_TOI_DA + 2 <= 127 (J2ME doc so luong bang byte CO DAU)", NhiemVuLauDai.PHU_TOI_DA + 2 <= NhiemVuLauDai.GOI_TOI_DA);
    }

    static NhiemVuMau mau(final String ma, final String loai) {
        final NhiemVuMau m = new NhiemVuMau();
        m.ma = ma;
        m.loai = loai;
        m.ten = "Kiem phu " + ma;
        m.kieu = "ha_quai";
        m.mucTieuId = "16";
        m.soLuong = 5;
        m.cap = 10;
        m.mucTieuText = "Hạ 5 Skorupi";
        return m;
    }

    static void motNguon() {
        final boolean batCu = LauDai.bat();
        final real.GemTemplate[] gemCu = real.Map.gemTemplate;
        if (real.Map.gemTemplate == null) {
            real.Map.gemTemplate = new real.GemTemplate[0];
        }
        LauDai.dat(true);
        final data.InfoQuestTemplate info = new data.InfoQuestTemplate();
        info.id = ID_NPC;
        info.ma = "P-KIEM.1";
        info.main_sub = 1;
        info.name = "Kiem phu NPC";
        info.monsterKill = new java.util.Vector<Short>(Collections.singletonList(Short.valueOf((short) 16)));
        info.totalMonsKilled = new java.util.Vector<Short>(Collections.singletonList(Short.valueOf((short) 5)));
        info.itemget = new java.util.Vector<Short>();
        info.totalitemget = new java.util.Vector<Short>();
        final data.InfoQuestTemplate cu = real.Map.allSubQuest.put(Short.valueOf(ID_NPC), info);
        final ConcurrentHashMap<String, long[]> td = new ConcurrentHashMap<String, long[]>();
        try {
            final List<NhiemVuMau> ds = new ArrayList<NhiemVuMau>();
            ds.add(mau("P-KIEM.1", "phu"));
            ds.add(mau("H-KIEM.1", "huyen_thoai"));
            final Char p = new Char(null);
            p.charDBID = ID;
            p.isBot = -1;
            p.charname = "kiemphunpc";
            p.lvDetail.lv = 20;
            NhiemVuEngine.datKiem(ds, ID, td);
            Kiem.bang("chua nhan: khong dong NPC", 0, NhiemVuEngine.phuSoTayChoNpc(p, 2).size());
            final long[] v = new long[]{3L, 0L, 0L, 0L};
            td.put("P-KIEM.1", v);
            td.put("H-KIEM.1", new long[]{1L, 0L, 0L, 0L});
            final List<NewCharQuest> dang = NhiemVuEngine.phuSoTayChoNpc(p, 2);
            Kiem.bang("nhan o So tay: hien MOT dong o NPC Đang làm (khong tinh loai khac phu)", 1, dang.size());
            if (!dang.isEmpty()) {
                Kiem.bang("dong NPC: id mau NPC cung ma", Integer.valueOf(ID_NPC), Integer.valueOf(dang.get(0).id_quest));
                Kiem.bang("dong NPC: tien do engine 3", Integer.valueOf(3), Integer.valueOf(dang.get(0).monsterKilled[0]));
                Kiem.bang("dong NPC: dang lam", Integer.valueOf(NewCharQuest.WORKING), Integer.valueOf(dang.get(0).state_quest));
            }
            Kiem.bang("chua xong: khong o Hoàn thành", 0, NhiemVuEngine.phuSoTayChoNpc(p, 1).size());
            Kiem.bang("maPhuSoTay(id NPC) = ma", "P-KIEM.1", NhiemVuEngine.maPhuSoTay(p, ID_NPC));
            v[0] = 5;
            v[1] = 1;
            Kiem.bang("xong: chuyen sang Hoàn thành", 1, NhiemVuEngine.phuSoTayChoNpc(p, 1).size());
            Kiem.bang("xong: roi Đang làm", 0, NhiemVuEngine.phuSoTayChoNpc(p, 2).size());
            NhiemVuEngine.traPhuSoTayQuaNpc(p, "P-KIEM.1");
            Kiem.bang("tra o NPC = nhan thuong engine (da nhan)", 2L, v[1]);
            Kiem.bang("da nhan thuong: khong con dong NPC", 0, NhiemVuEngine.phuSoTayChoNpc(p, 1).size() + NhiemVuEngine.phuSoTayChoNpc(p, 2).size());
            Kiem.dung("da nhan thuong: maPhuSoTay null", NhiemVuEngine.maPhuSoTay(p, ID_NPC) == null);
            NhiemVuEngine.traPhuSoTayQuaNpc(p, "P-KIEM.1");
            Kiem.bang("tra lan hai: khong nhan lai", 2L, v[1]);
            // buoc dang o khe NPC cu (p.subQuest cung ma) -> khong them dong (khong hien hai lan)
            v[1] = 0;
            p.subQuest = new NewCharQuest(ID_NPC, (byte) 1);
            Kiem.bang("cung ma voi khe NPC cu: khong them dong", 0, NhiemVuEngine.phuSoTayChoNpc(p, 2).size());
        } finally {
            NhiemVuEngine.xoaKiem(ID);
            if (cu == null) {
                real.Map.allSubQuest.remove(Short.valueOf(ID_NPC));
            } else {
                real.Map.allSubQuest.put(Short.valueOf(ID_NPC), cu);
            }
            LauDai.dat(batCu);
            real.Map.gemTemplate = gemCu;
        }
    }

    static String doc(final String f) {
        try {
            return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
        } catch (final Exception e) {
            return "";
        }
    }

    static void noiDay() {
        final String mc = doc("src/real/MessageCreator.java");
        Kiem.dung("createMsgCharQuest loai 1 / 2 them NhiemVuEngine.phuSoTayChoNpc", mc.contains("allQuest.addAll(NhiemVuEngine.phuSoTayChoNpc(p, type));"));
        final String c = doc("src/real/Char.java");
        Kiem.dung("OnQuest main_sub 1: tra dong So tay -> traPhuSoTayQuaNpc", c.contains("NhiemVuEngine.maPhuSoTay(this, questID)")
                && c.contains("NhiemVuEngine.traPhuSoTayQuaNpc(this, maSoTay);"));
        final String g = doc("src/real/NhiemVuLauDaiGame.java");
        Kiem.dung("danh sach NPC dung locPhu (hienNpc)", g.contains("NhiemVuLauDai.locPhu(lv, p.lvDetail.lv, NhiemVuLauDai.PHU_TOI_DA)"));
    }
}
