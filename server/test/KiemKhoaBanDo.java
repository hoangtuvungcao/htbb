import com.htbb.pokemon.KhoaBanDo;
import com.htbb.pokemon.NhiemVuMau;
import com.htbb.pokemon.TuDong;
import io.Message;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import real.Char;
import real.KhoaBanDoGame;
import real.LevelDetail;
import real.RealController;

/**
 * Khoa ban do theo cap + cua ai (nhanh khoa-map): luat thuan, moi duong vao ban do (that + doc ma), mien tru nhan vat
 * cu, xong cua ai mo ban do, Tu dong danh theo ban do, goi -108, du lieu data_ban_do_mo_khoa / cua ai trong CSDL day du.
 */
public final class KiemKhoaBanDo {

    /** Nhan vat gia: bat thong bao -26 (createServerAlertAutoOffMessage) + goi -108. */
    static final class NguoiChoi extends Char {
        final List<String> bao = new ArrayList<String>();
        final List<Message> goi = new ArrayList<Message>();

        NguoiChoi(final int id, final int cap) {
            super(null);
            charDBID = id;
            charname = "kiem" + id;
            lvDetail = new LevelDetail();
            lvDetail.lv = cap;
        }

        @Override
        public void sendMessage(final Message m) {
            goi.add(m);
            if (m.command == -26) {
                try {
                    final DataInputStream in = new DataInputStream(new ByteArrayInputStream(m.toByteArray()));
                    in.readByte(); // lenh
                    in.readShort(); // do dai
                    bao.add(in.readUTF());
                } catch (final Exception e) {
                    bao.add("?");
                }
            }
        }

        String cuoi() {
            return bao.isEmpty() ? "" : bao.get(bao.size() - 1);
        }
    }

    public static void main(final String[] args) {
        try {
            real.Map.loadGemTemplate(); // chay rieng: Map can bang ngoc (Kiem.main da nap)
        } catch (final Throwable ignored) {
        }
        chay();
        System.exit(Kiem.tongKet());
    }

    public static void chay() {
        Kiem.nhom("Khoa ban do: cap + cua ai, moi duong vao, mien tru, Tu dong theo ban do");
        try {
            luatThuan();
            duongVao();
            tuDong();
            duLieu();
            docMa();
        } catch (final Throwable e) {
            e.printStackTrace();
            Kiem.dung("KiemKhoaBanDo chay het: " + e, false);
        }
    }

    // ------------------------------------------------------------------ luat thuan

    static void luatThuan() {
        final KhoaBanDo.Luat l = new KhoaBanDo.Luat(10, 27, "CA-05.3", "Cửa ải: Đường sang Johto", true, "");
        Kiem.dung("khong luat = mo", KhoaBanDo.mo(null, 1, false, 0));
        Kiem.dung("thieu ca cap lan cua ai = khoa", !KhoaBanDo.mo(l, 20, false, 1));
        Kiem.bang("bao du hai dieu kien",
                "Cần hoàn thành nhiệm vụ «Cửa ải: Đường sang Johto» và đạt cấp 27 để vào New Bark. Mở Sổ tay huấn luyện → Thử thách cửa ải.",
                KhoaBanDo.lyDo(l, 20, false, 1, "New Bark"));
        Kiem.bang("chi thieu cap", "Cần đạt cấp 27 để vào New Bark.", KhoaBanDo.lyDo(l, 20, true, 1, "New Bark"));
        Kiem.dung("du cap, chua xong cua ai = khoa", !KhoaBanDo.mo(l, 30, false, 1));
        Kiem.dung("du cap + xong cua ai = mo", KhoaBanDo.mo(l, 27, true, 1));
        Kiem.dung("mien tru (moc >= cap ban do) = mo du chua lam cua ai", KhoaBanDo.mo(l, 40, false, 40));
        Kiem.dung("mien tru duoi cap ban do khong mo", !KhoaBanDo.mo(l, 26, false, 26));
        Kiem.bang("moc mien lan dau = max(cap, cap ban do dang luu)", 30, KhoaBanDo.mocMienDau(25, new KhoaBanDo.Luat(11, 30, "", "", true, "")));
        Kiem.bang("moc mien lan dau nhan vat moi = 1", 1, KhoaBanDo.mocMienDau(1, null));
        final KhoaBanDo.Luat td = new KhoaBanDo.Luat(111, 0, "", "", false, "Hang boss: boss phải đánh tay.");
        Kiem.dung("ban do chi khoa tu dong khong khoa vao", !td.coKhoa() && KhoaBanDo.mo(td, 1, false, 0));
        Kiem.bang("cau bao tu dong", "Bản đồ này không cho phép tự động đánh: Hang boss: boss phải đánh tay.", KhoaBanDo.lyDoTuDong(td));
        // buoc cua ai
        final NhiemVuMau m = new NhiemVuMau();
        m.loai = "cua_ai";
        m.ma = "CA-01.1";
        m.kieu = "giet";
        m.dieuKien = NhiemVuMau.docDieuKien("ban_do=1");
        Kiem.dung("cua ai khong chiem khe nhiem vu phu", m.laCuaAi() && !m.chiemKhe());
        Kiem.bang("cua ai chi tinh o ban do 1", 1, m.banDoChi());
    }

    // ------------------------------------------------------------------ duong vao (that)

    static real.Map banDo(final int id, final int goc) {
        final real.Map m = new real.Map();
        m.mapId = id;
        m.mapIDLoadMap = goc;
        RealController.mapList.put(Integer.valueOf(id), m);
        return m;
    }

    @SuppressWarnings("unchecked")
    static void datXong(final Char p, final String ma) throws Exception {
        final java.lang.reflect.Field f = real.NhiemVuEngine.class.getDeclaredField("TD");
        f.setAccessible(true);
        final ConcurrentHashMap<Integer, ConcurrentHashMap<String, long[]>> td = (ConcurrentHashMap<Integer, ConcurrentHashMap<String, long[]>>) f.get(null);
        ConcurrentHashMap<String, long[]> t = td.get(Integer.valueOf(p.charDBID));
        if (t == null) {
            t = new ConcurrentHashMap<String, long[]>();
            td.put(Integer.valueOf(p.charDBID), t);
        }
        t.put(ma, new long[]{1L, 2L, 0L, 0L});
    }

    static void duongVao() throws Exception {
        final Map<Integer, KhoaBanDo.Luat> luat = new HashMap<Integer, KhoaBanDo.Luat>();
        luat.put(9001, new KhoaBanDo.Luat(9001, 27, "CA-05.3", "Cửa ải: Đường sang Johto", true, ""));
        luat.put(9002, new KhoaBanDo.Luat(9002, 0, "", "", false, "Hang boss: boss phải đánh tay."));
        final Map<Integer, KhoaBanDo.Luat> cu = new HashMap<Integer, KhoaBanDo.Luat>(KhoaBanDo.bang());
        KhoaBanDo.datBang(luat);
        try {
            final real.Map thiTran = banDo(9000, 9000);
            banDo(9001, 9001);
            banDo(9011, 9001); // ban sao theo khu: theo luat ban goc
            banDo(9002, 9002);

            final NguoiChoi p = new NguoiChoi(990001, 20);
            p.map = thiTran;
            p.mapID = 9000;
            p.x = 160;
            p.y = 160;
            KhoaBanDoGame.datMien(p.charDBID, 1);
            Kiem.dung("cap 20 chua cua ai: bi chan vao 9001", KhoaBanDoGame.chan(p, 9001));
            Kiem.dung("thong bao co dieu kien", p.cuoi().startsWith("Cần hoàn thành nhiệm vụ «Cửa ải: Đường sang Johto» và đạt cấp 27"));
            Kiem.dung("ban sao theo khu cung bi chan", KhoaBanDoGame.chan(p, 9011));
            Kiem.dung("ban do khong khoa vao (chi khoa tu dong) vao duoc", !KhoaBanDoGame.chan(p, 9002));
            Kiem.dung("o lai ban do hien tai khong bi chan", !KhoaBanDoGame.chan(p, 9000));
            Kiem.dung("ban do khong co luat vao duoc", !KhoaBanDoGame.chan(p, 9003));

            // move2Map (chot cuoi: bay, Hoa Tieu, vat pham, toi nguoi choi...): o lai cho cu
            try {
                thiTran.move2Map(p, 3, 4, 9001, 0);
            } catch (final Throwable ignored) {
                // nap lai map can CSDL / phien: chi kiem vi tri
            }
            Kiem.bang("move2Map toi ban do khoa: van o 9000", 9000, p.mapID);

            // len cap nhung chua xong cua ai
            p.lvDetail.lv = 30;
            Kiem.dung("cap 30 chua xong cua ai: van chan", KhoaBanDoGame.chan(p, 9001));
            Kiem.dung("bao chi con nhiem vu", p.cuoi().startsWith("Cần hoàn thành nhiệm vụ «Cửa ải: Đường sang Johto» để vào"));
            datXong(p, "CA-05.3");
            Kiem.dung("xong buoc cuoi cua ai: mo", !KhoaBanDoGame.chan(p, 9001));
            final int soBao = p.bao.size();
            p.mapID = 9000;
            try {
                thiTran.move2Map(p, 3, 4, 9001, 0);
            } catch (final Throwable ignored) {
            }
            Kiem.bang("move2Map sau khi mo: khong con thong bao chan", soBao, p.bao.size());

            // admin
            final NguoiChoi gm = new NguoiChoi(990002, 1);
            gm.map = thiTran;
            gm.mapID = 9000;
            gm.isAdmin = true;
            Kiem.dung("admin khong bi chan", !KhoaBanDoGame.chan(gm, 9001));

            // mien tru nhan vat cu: cap 40, chua tung lam cua ai
            final NguoiChoi cuu = new NguoiChoi(990003, 40);
            cuu.map = thiTran;
            cuu.mapID = 9000;
            KhoaBanDoGame.datMien(cuu.charDBID, KhoaBanDo.mocMienDau(40, null));
            Kiem.dung("nhan vat cu cap 40 (mien tru): vao 9001 duoc", !KhoaBanDoGame.chan(cuu, 9001));
            // nhan vat dang luu o ban do khoa (cap 20 o 9001): mien tru lan dau nang moc len cap ban do
            final NguoiChoi ket = new NguoiChoi(990004, 20);
            ket.mapID = 9001;
            KhoaBanDoGame.datMien(ket.charDBID, KhoaBanDo.mocMienDau(20, KhoaBanDoGame.luatCua(9001)));
            ket.map = thiTran;
            ket.mapID = 9000;
            Kiem.dung("nhan vat luu o ban do khoa: van vao lai duoc (khong bi bo roi)", !KhoaBanDoGame.chan(ket, 9001));

            // nhan diem menu bay
            Kiem.bang("nhan diem khoa trong menu bay", "Khoá: New Bark (100 xu) · AUTO", KhoaBanDoGame.nhanDiem(new NguoiChoi(990005, 1), 9001, "New Bark (100 xu)"));
            Kiem.bang("nhan diem khong AUTO", "Hang · Không AUTO", KhoaBanDoGame.nhanDiem(p, 9002, "Hang"));

            // tu dong theo ban do
            Kiem.bang("ly do khong tu dong", "Bản đồ này không cho phép tự động đánh: Hang boss: boss phải đánh tay.",
                    KhoaBanDoGame.lyDoKhongTuDong(9002));
            Kiem.dung("ban do luyen cho tu dong", KhoaBanDoGame.lyDoKhongTuDong(9001) == null);

            // goi -108
            final NguoiChoi moi = new NguoiChoi(990006, 1);
            moi.map = thiTran;
            moi.mapID = 9000;
            KhoaBanDoGame.datMien(moi.charDBID, 1);
            final Set<Integer> khoa = new TreeSet<Integer>();
            final LinkedHashMap<String, List<Integer>> n = KhoaBanDoGame.nhom(moi, khoa);
            final Message g = KhoaBanDoGame.taoGoi(n);
            final DataInputStream in = new DataInputStream(new ByteArrayInputStream(g.toByteArray()));
            Kiem.bang("goi -108 lenh", -108, (int) g.command);
            in.readByte();
            in.readShort();
            Kiem.bang("goi -108 phien ban", 1, (int) in.readByte());
            final int soNhom = in.readShort();
            final Map<Integer, Integer> co = new HashMap<Integer, Integer>();
            String yc9001 = "";
            for (int i = 0; i < soNhom; i++) {
                final int c = in.readByte();
                final String yc = in.readUTF();
                in.readUTF();
                final int k = in.readShort();
                for (int j = 0; j < k; j++) {
                    final int id = in.readShort();
                    co.put(id, c);
                    if (id == 9001) {
                        yc9001 = yc;
                    }
                }
            }
            Kiem.bang("9001 + ban sao 9011 khoa (co 1)", "1/1", co.get(9001) + "/" + co.get(9011));
            Kiem.bang("9002 chi khoa tu dong (co 2)", Integer.valueOf(2), co.get(9002));
            Kiem.dung("thi tran khong co luat khong gui", !co.containsKey(9000));
            Kiem.dung("goi co dieu kien vao", yc9001.contains("đạt cấp 27"));
            Kiem.dung("ban do goc khoa duoc ghi de bao 'Đã mở'", khoa.contains(9001));
        } finally {
            for (final int id : new int[]{9000, 9001, 9011, 9002}) {
                RealController.mapList.remove(Integer.valueOf(id));
            }
            KhoaBanDo.datBang(cu);
        }
    }

    // ------------------------------------------------------------------ tu dong (TuDong.kiemBat)

    static void tuDong() {
        final TuDong.CauHinh c = new TuDong.CauHinh();
        c.bat = true;
        c.chiMapDaMo = false;
        c.capToiThieu = 0;
        final TuDong.Phien ph = new TuDong.Phien();
        final TuDong.NguCanh n = new TuDong.NguCanh();
        n.map = 111;
        n.khoaMap = "Bản đồ này không cho phép tự động đánh: Hang boss: boss phải đánh tay.";
        Kiem.bang("bat tu dong o ban do khoa: tu choi + ly do", n.khoaMap, TuDong.kiemBat(c, ph, n, 1));
    }

    // ------------------------------------------------------------------ du lieu

    static void duLieu() {
        final List<Map<String, String>> ds = DayDu.dong("data_ban_do_mo_khoa");
        final Map<String, Map<String, String>> theoMa = new HashMap<String, Map<String, String>>();
        for (final Map<String, String> d : DayDu.dong("data_quest_lau_dai")) {
            theoMa.put(d.get("ma"), d);
        }
        final Set<String> tenMap = new HashSet<String>();
        for (final Map<String, String> d : DayDu.dong("data_map_ten")) {
            tenMap.add(d.get("map_id"));
        }
        final Map<Integer, Map<String, String>> luat = new HashMap<Integer, Map<String, String>>();
        for (final Map<String, String> d : ds) {
            luat.put(Integer.parseInt(d.get("map_id")), d);
        }
        final Set<String> thieu = new TreeSet<String>(tenMap);
        for (final Integer id : luat.keySet()) {
            thieu.remove(String.valueOf(id));
        }
        Kiem.bang("moi ban do trong data_map_ten co luat (vao + tu dong)", "[]", thieu.toString());
        final List<String> loi = new ArrayList<String>();
        int soKhoa = 0;
        for (final Map<String, String> d : ds) {
            final String ma = d.get("ma_nhiem_vu");
            final int cap = Integer.parseInt(d.get("cap_toi_thieu"));
            if (!ma.isEmpty()) {
                soKhoa++;
                if (!theoMa.containsKey(ma)) {
                    loi.add(d.get("map_id") + ": khong co nhiem vu " + ma);
                } else if (Integer.parseInt(theoMa.get(ma).get("cap_toi_thieu")) > cap) {
                    loi.add(d.get("map_id") + ": cua ai " + ma + " can cap " + theoMa.get(ma).get("cap_toi_thieu") + " > cap ban do " + cap);
                }
                if (d.get("ten_nhiem_vu").trim().isEmpty()) {
                    loi.add(d.get("map_id") + ": thieu ten_nhiem_vu");
                }
            }
            if ("0".equals(d.get("cho_phep_tu_dong")) && d.get("ly_do_tu_dong").trim().isEmpty()) {
                loi.add(d.get("map_id") + ": khoa tu dong khong co ly do");
            }
        }
        Kiem.bang("luat ban do tro dung nhiem vu, cap cua ai <= cap ban do, co ly do", "[]", loi.toString());
        Kiem.dung(">= 50 ban do co khoa nhiem vu (" + soKhoa + ")", soKhoa >= 50);

        // cua ai: buoc ha quai o dung ban do co quai do, ban do do vao duoc TRUOC ban do ma cua ai mo
        final List<String> loiCa = new ArrayList<String>();
        final Map<String, Map<String, String>> nd = new HashMap<String, Map<String, String>>();
        for (final Map<String, String> d : DayDu.dong("data_nhiem_vu_noi_dung")) {
            nd.put(d.get("ma"), d);
        }
        int buoc = 0;
        int gym = 0;
        for (final Map<String, String> q : theoMa.values()) {
            if (!"cua_ai".equals(q.get("loai"))) {
                continue;
            }
            buoc++;
            if ("ha_gym".equals(q.get("kieu_muc_tieu"))) {
                gym++;
                continue;
            }
            final Map<String, String> n = nd.get(q.get("ma"));
            final int map = n == null ? -1 : NhiemVuMau.docDieuKien(n.get("dieu_kien")).containsKey("ban_do")
                    ? Integer.parseInt(NhiemVuMau.docDieuKien(n.get("dieu_kien")).get("ban_do")) : -1;
            if (map < 0) {
                loiCa.add(q.get("ma") + ": thieu ban_do");
                continue;
            }
            if (!quaiTrenMap(map).contains(Integer.parseInt(q.get("muc_tieu_id")))) {
                loiCa.add(q.get("ma") + ": quai " + q.get("muc_tieu_id") + " khong co tren map " + map);
            }
            final String cuaAi = q.get("chuong") + ".";
            final Map<String, String> lm = luat.get(map);
            if (lm != null && lm.get("ma_nhiem_vu").startsWith(cuaAi)) {
                loiCa.add(q.get("ma") + ": ban do " + map + " lai can chinh cua ai nay (vong lap)");
            }
        }
        Kiem.bang("cua ai: quai muc tieu co tren ban do, khong vong lap", "[]", loiCa.toString());
        Kiem.bang("29 buoc cua ai, 14 tran Gym danh tay", "29/14", buoc + "/" + gym);
    }

    static Set<Integer> quaiTrenMap(final int map) {
        final Set<Integer> s = new HashSet<Integer>();
        try {
            final byte[] b = Files.readAllBytes(Paths.get("map/map" + map));
            final int w = b[0] & 255;
            final int h = b[1] & 255;
            for (int i = 2; i < 2 + w * h && i < b.length; i++) {
                final int v = b[i] & 255;
                if (v > 0 && v != 255) {
                    s.add(v);
                }
            }
        } catch (final Exception e) {
            // khong co tep: rong
        }
        return s;
    }

    // ------------------------------------------------------------------ doc ma: moi duong vao co chot

    static String doc(final String f) throws Exception {
        return new String(Files.readAllBytes(Paths.get(f)), StandardCharsets.UTF_8);
    }

    static void docMa() throws Exception {
        Kiem.dung("di bo qua cong (goi 12): MapXuLyLenh.doChangeMap chan", doc("src/real/MapXuLyLenh.java").contains("KhoaBanDoGame.chan(player, toMapID)"));
        Kiem.dung("san Lien minh: MapLienDau.doChangeMap chan", doc("src/real/MapLienDau.java").contains("KhoaBanDoGame.chan(player, toMapID)"));
        final String map = doc("src/real/Map.java");
        Kiem.dung("chot cuoi Map.move2Map", map.contains("if (mapID != player.mapID && KhoaBanDoGame.chan(player, mapID))"));
        Kiem.dung("xa phu cu (goi 79) chan truoc khi tru xu + nap lai map", map.contains("KhoaBanDoGame.giuTaiCho(player); // chua tru xu"));
        final String npc = doc("src/real/NpcDichVu.java");
        Kiem.dung("Pidgeot bay + Lapras do chan truoc khi tru phi", npc.contains("return; // chua tru xu") && npc.contains("return; // chua tru ve"));
        Kiem.dung("menu bay / do hien diem khoa", npc.split("KhoaBanDoGame.nhanDiem").length == 3);
        Kiem.dung("bai luyen chan truoc khi tru xu", doc("src/real/BaiLuyenTap.java").contains("KhoaBanDoGame.chan(player, bai.mapId)"));
        Kiem.dung("vat pham toi ban doi: chan + tra vat pham (2 lop)", doc("src/real/doUsePotion.java").contains("KhoaBanDoGame.chan(player, p1.mapID)")
                && doc("src/real/MapDungThuoc.java").contains("KhoaBanDoGame.chan(player, p1.mapID)"));
        Kiem.dung("dang nhap: mien tru (khong dich chuyen)", doc("src/real/CharNapDuLieu.java").contains("KhoaBanDoGame.khiNap(self)"));
        Kiem.dung("dang nhap + chon nhan vat gui goi -108", doc("src/real/cmd/LoginHandler.java").contains("KhoaBanDoGame.guiDanhSach(p)")
                && doc("src/real/cmd/SelectCharHandler.java").contains("KhoaBanDoGame.guiDanhSach(p)"));
        Kiem.dung("len cap / xong cua ai cap nhat", doc("src/real/MapXuLyLenh.java").contains("KhoaBanDoGame.capNhat(p)")
                && doc("src/real/NhiemVuEngine.java").contains("KhoaBanDoGame.capNhat(p)"));
        Kiem.dung("Tu dong: bat va dang chay deu xem luat ban do", doc("src/real/TuDongGame.java").split("KhoaBanDoGame.lyDoKhongTuDong").length == 3);
        final String kj = doc("../client_jar/src/game/pokemon/KhoaMap.java");
        Kiem.dung("client doc goi -108 cung thu tu (byte, short, [byte, UTF, UTF, short, short*])",
                kj.contains("LENH = -108") && kj.indexOf("readByte()") < kj.indexOf("readShort()") && kj.contains("readUTF()"));
        Kiem.dung("client MessageHandler goi KhoaMap.nhan", doc("../client_jar/src/game/networklogic/MessageHandler.java").contains("game.pokemon.KhoaMap.nhan(msg)"));
        Kiem.dung("client: cong ra ban do khoa khong gui doi map", doc("../client_jar/src/game/render/screen/GameScr.java").contains("game.pokemon.KhoaMap.baoKhoa("));
        Kiem.dung("client: vien AUTO bi khoa khong xin server", doc("../client_jar/shared/mod/game/render/screen/TuDong.java").contains("game.pokemon.KhoaMap.chanTuDong("));
    }
}
