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
            hopCuaAi();
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
                "Cần hoàn thành nhiệm vụ «Cửa ải: Đường sang Johto» và đạt cấp 27 để vào New Bark. Nhận ở: Quầy đổi thưởng → Nhiệm vụ của tôi → Thử thách cửa ải.",
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

    // ------------------------------------------------------------------ hop cua ai (bi chan vi chua xong cua ai)

    static NhiemVuMau mauCa(final String ma, final int cap, final String truoc, final String ten, final String mucTieu, final int so) {
        final NhiemVuMau m = new NhiemVuMau();
        m.ma = ma;
        m.loai = "cua_ai";
        m.kieu = "giet";
        m.cap = cap;
        m.truoc = truoc;
        m.ten = ten;
        m.mucTieuText = mucTieu;
        m.soLuong = so;
        m.hoiThoaiBatDau = "Bắt đầu " + ma + ".";
        return m;
    }

    @SuppressWarnings("unchecked")
    static ConcurrentHashMap<String, long[]> tienDo(final Char p) throws Exception {
        final java.lang.reflect.Field f = real.NhiemVuEngine.class.getDeclaredField("TD");
        f.setAccessible(true);
        final ConcurrentHashMap<Integer, ConcurrentHashMap<String, long[]>> td = (ConcurrentHashMap<Integer, ConcurrentHashMap<String, long[]>>) f.get(null);
        ConcurrentHashMap<String, long[]> t = td.get(Integer.valueOf(p.charDBID));
        if (t == null) {
            t = new ConcurrentHashMap<String, long[]>();
            td.put(Integer.valueOf(p.charDBID), t);
        }
        return t;
    }

    /** Doc goi -32 / 37 / -30: chu cuoi cung cua lenh {@code lenh} tu chi so {@code tu}; null neu khong co. */
    static String chuGoi(final NguoiChoi p, final int lenh, final int tu) throws Exception {
        String kq = null;
        for (int i = tu; i < p.goi.size(); i++) {
            final Message m = p.goi.get(i);
            if (m.command != lenh) {
                continue;
            }
            final DataInputStream in = new DataInputStream(new ByteArrayInputStream(m.toByteArray()));
            in.readByte();
            in.readShort();
            if (lenh == -32) {
                in.readShort();
                final int id = in.readByte();
                kq = id + "|" + in.readUTF();
            } else if (lenh == -30) {
                in.readShort();
                in.readByte();
                final int n = in.readByte();
                final StringBuilder sb = new StringBuilder();
                for (int j = 0; j < n; j++) {
                    sb.append(j > 0 ? "\u0001" : "").append(in.readUTF());
                }
                kq = sb.toString();
            } else {
                kq = in.readUTF();
            }
        }
        return kq;
    }

    static int dem(final NguoiChoi p, final int lenh) {
        int n = 0;
        for (final Message m : p.goi) {
            if (m.command == lenh) {
                n++;
            }
        }
        return n;
    }

    @SuppressWarnings("unchecked")
    static void hopCuaAi() throws Exception {
        // ---- chu thuan
        final KhoaBanDo.Luat ca = new KhoaBanDo.Luat(4, 3, "CA-01.2", "Cửa ải: Rời đồng cỏ Pallet", true, "");
        Kiem.bang("ten cua ai bo tien to", "Rời đồng cỏ Pallet", KhoaBanDo.tenCuaAi(ca));
        Kiem.bang("mo ta buoc tu muc tieu", "hạ 20 Torchic ở Pallet", KhoaBanDo.moTaBuoc("Hạ 20 Torchic ở Pallet.", "Cửa ải: 20 Torchic ở Pallet"));
        Kiem.bang("mo ta buoc khi thieu muc tieu: theo ten", "hạ Gym Pewter", KhoaBanDo.moTaBuoc("", "Cửa ải: hạ Gym Pewter"));
        Kiem.bang("chua nhan: ba dong",
                "Để vào Tây Cerulean cần xong cửa ải «Rời đồng cỏ Pallet».|Bước 1/2 (chưa nhận): hạ 20 Torchic ở Pallet.|"
                        + "Nhận ở: Quầy đổi thưởng → Nhiệm vụ của tôi → Thử thách cửa ải.",
                String.join("|", KhoaBanDo.baoCuaAi(ca, 5, "Tây Cerulean", 1, 2, "hạ 20 Torchic ở Pallet", KhoaBanDo.CHUA_NHAN, 0, 20, null)));
        Kiem.bang("dang lam: tien do + duong Dang lam",
                "Để vào Tây Cerulean cần xong cửa ải «Rời đồng cỏ Pallet».|Bước 1/2: hạ 20 Torchic ở Pallet (7/20).|"
                        + "Xem ở: Quầy đổi thưởng → Nhiệm vụ của tôi → Đang làm.",
                String.join("|", KhoaBanDo.baoCuaAi(ca, 5, "Tây Cerulean", 1, 2, "hạ 20 Torchic ở Pallet", KhoaBanDo.DANG_LAM, 7, 20, null)));
        Kiem.bang("buoc 2 dang lam", "Bước 2/2: hạ 20 Ekans ở Viridian (3/20).",
                KhoaBanDo.baoCuaAi(ca, 5, "Tây Cerulean", 2, 2, "hạ 20 Ekans ở Viridian", KhoaBanDo.DANG_LAM, 3, 20, null)[1]);
        Kiem.bang("buoc ha Gym (1 tran): khong ghi 0/1",
                "Bước 2/2: hạ Trưởng Gym Pewter: Golem (Quầy đổi thưởng → Sổ tay huấn luyện → Thách đấu Gym → Gym Pewter).",
                KhoaBanDo.baoCuaAi(ca, 5, "X", 2, 2, KhoaBanDo.moTaBuoc(
                        "Hạ Trưởng Gym Pewter: Golem (Quầy đổi thưởng → Sổ tay huấn luyện → Thách đấu Gym → Gym Pewter).", ""),
                        KhoaBanDo.DANG_LAM, 0, 1, null)[1]);
        Kiem.bang("xong cho nhan thuong", "Bước 1/2 đã xong: nhận thưởng để mở bước tiếp.",
                KhoaBanDo.baoCuaAi(ca, 5, "Tây Cerulean", 1, 2, "x", KhoaBanDo.CHO_THUONG, 20, 20, null)[1]);
        Kiem.bang("buoc cuoi xong cho nhan thuong", "Bước 2/2 đã xong: nhận thưởng để mở bản đồ.",
                KhoaBanDo.baoCuaAi(ca, 5, "Tây Cerulean", 2, 2, "x", KhoaBanDo.CHO_THUONG, 20, 20, null)[1]);
        Kiem.bang("thieu ca cap", "Để vào Tây Cerulean cần xong cửa ải «Rời đồng cỏ Pallet» và đạt cấp 3.",
                KhoaBanDo.baoCuaAi(ca, 1, "Tây Cerulean", 1, 2, "x", KhoaBanDo.CHUA_NHAN, 0, 20, null)[0]);
        Kiem.bang("chua nhan duoc: ly do", "Bước 1/2 (chưa nhận): hạ 30 Quagsire ở Lavender. Cần đạt cấp 30 để nhận.",
                KhoaBanDo.baoCuaAi(ca, 5, "X", 1, 2, "hạ 30 Quagsire ở Lavender", KhoaBanDo.CHUA_NHAN, 0, 30, "Cần đạt cấp 30")[1]);
        Kiem.bang("cau hoi theo trang thai", "Nhận cửa ải ngay?|Xem tiến độ?|Nhận thưởng ngay?|null",
                KhoaBanDo.hoiCuaAi(KhoaBanDo.CHUA_NHAN, null) + "|" + KhoaBanDo.hoiCuaAi(KhoaBanDo.DANG_LAM, null) + "|"
                        + KhoaBanDo.hoiCuaAi(KhoaBanDo.CHO_THUONG, null) + "|" + KhoaBanDo.hoiCuaAi(KhoaBanDo.CHUA_NHAN, "Cần đạt cấp 30"));
        final String[] dai = KhoaBanDo.baoCuaAi(ca, 5, "Tây Cerulean", 1, 2, "hạ 20 Torchic ở Pallet", KhoaBanDo.DANG_LAM, 7, 20, null);
        Kiem.dung("hop gon: moi dong <= 90 ky tu", dai[0].length() <= 90 && dai[1].length() <= 90 && dai[2].length() <= 90);

        // ---- trong game
        final java.lang.reflect.Field fMau = real.NhiemVuEngine.class.getDeclaredField("MAU");
        fMau.setAccessible(true);
        final Object mauCu = fMau.get(null);
        final LinkedHashMap<String, NhiemVuMau> mau = new LinkedHashMap<String, NhiemVuMau>();
        mau.put("CA-01.2", mauCa("CA-01.2", 1, "CA-01.1", "Cửa ải: 20 Ekans ở Viridian", "Hạ 20 Ekans ở Viridian.", 20));
        mau.put("CA-01.1", mauCa("CA-01.1", 1, "", "Cửa ải: 20 Torchic ở Pallet", "Hạ 20 Torchic ở Pallet.", 20));
        mau.put("CA-03.1", mauCa("CA-03.1", 30, "", "Cửa ải: 30 Quagsire ở Lavender", "Hạ 30 Quagsire ở Lavender.", 30));
        mau.put("CA-03.2", mauCa("CA-03.2", 30, "CA-03.1", "Cửa ải: hạ Gym Pewter", "", 1));
        final Map<Integer, KhoaBanDo.Luat> luat = new HashMap<Integer, KhoaBanDo.Luat>();
        luat.put(9101, new KhoaBanDo.Luat(9101, 3, "CA-01.2", "Cửa ải: Rời đồng cỏ Pallet", true, ""));
        luat.put(9102, new KhoaBanDo.Luat(9102, 3, "CA-03.2", "Cửa ải: Thử thách Pewter", true, ""));
        final Map<Integer, KhoaBanDo.Luat> cu = new HashMap<Integer, KhoaBanDo.Luat>(KhoaBanDo.bang());
        final boolean lauDaiCu = com.htbb.pokemon.LauDai.bat();
        final NguoiChoi p = new NguoiChoi(990101, 5);
        final NguoiChoi q = new NguoiChoi(990102, 5);
        fMau.set(null, mau);
        KhoaBanDo.datBang(luat);
        com.htbb.pokemon.LauDai.dat(true);
        try {
            final real.Map nha = banDo(9100, 9100);
            banDo(9101, 9101);
            banDo(9102, 9102);
            for (final NguoiChoi x : new NguoiChoi[]{p, q}) {
                x.map = nha;
                x.mapID = 9100;
                x.x = 160;
                x.y = 160;
                KhoaBanDoGame.datMien(x.charDBID, 1);
                KhoaBanDoGame.xoaHoi(x.charDBID);
            }
            final ConcurrentHashMap<String, long[]> t = tienDo(p);
            t.clear();
            tienDo(q).clear();

            // di bo vao cong: chan, hop cho toi khi client nap xong map
            Kiem.dung("di bo: chua nhan cua ai -> chan", KhoaBanDoGame.chanDiBo(p, 9101));
            Kiem.bang("di bo: chua gui hop truoc khi nap lai map (onMap dong hop)", 0, dem(p, -32));
            KhoaBanDoGame.sauKhiVaoMap(p);
            String hop = chuGoi(p, -32, 0);
            Kiem.dung("nap xong (-500): hop -32 id 95", hop != null && hop.startsWith(KhoaBanDoGame.POPUP_CUA_AI + "|"));
            Kiem.dung("hop: chua nhan + buoc 1/2 + duong + hoi nhan",
                    hop != null && hop.contains("cần xong cửa ải «Rời đồng cỏ Pallet»")
                            && hop.contains("\nBước 1/2 (chưa nhận): hạ 20 Torchic ở Pallet.\n")
                            && hop.contains("Nhận ở: Quầy đổi thưởng → Nhiệm vụ của tôi → Thử thách cửa ải.")
                            && hop.endsWith("\nNhận cửa ải ngay?"));
            KhoaBanDoGame.sauKhiVaoMap(p);
            Kiem.bang("-500 lan nua khong gui lai", 1, dem(p, -32));

            // cho: dam bien lien tuc khong mo lai hop, chi the nhanh
            final int soBao = p.bao.size();
            Kiem.dung("dam lai trong 10 giay: van chan", KhoaBanDoGame.chanDiBo(p, 9101));
            KhoaBanDoGame.sauKhiVaoMap(p);
            Kiem.bang("trong 10 giay: khong hop moi", 1, dem(p, -32));
            Kiem.dung("trong 10 giay: chi the nhanh (dieu kien + buoc)", p.bao.size() == soBao + 1
                    && p.cuoi().startsWith("Để vào") && p.cuoi().contains("Bước 1/2 (chưa nhận)"));
            Kiem.dung("duong ngay (menu NPC) trong 10 giay: cung khong hop", KhoaBanDoGame.chan(p, 9101) && dem(p, -32) == 1);
            Kiem.bang("bam Huy: khong nhan", "false", String.valueOf(t.containsKey("CA-01.1")));

            // So tay "Thu thach cua ai": hang cua ai vua can len dau, danh dau
            final java.lang.reflect.Method ds = real.NhiemVuEngine.class.getDeclaredMethod("dsCoTheNhan", Char.class, int.class);
            ds.setAccessible(true);
            int truoc = p.goi.size();
            ds.invoke(null, p, 7);
            final String menu = chuGoi(p, -30, truoc);
            Kiem.dung("So tay cua ai: hang CA-01.1 to dam o dau", menu != null && menu.startsWith("» Cửa ải: 20 Torchic ở Pallet"));

            // nhan tu hop = nhan o So tay
            KhoaBanDoGame.luiHoi(p.charDBID, KhoaBanDoGame.CHO_HOI_MS + 1);
            Kiem.dung("het 10 giay: duong ngay (NPC) hoi luon", KhoaBanDoGame.chan(p, 9101) && dem(p, -32) == 2);
            truoc = p.goi.size();
            KhoaBanDoGame.khiTraLoi(p, true);
            final long[] v = t.get("CA-01.1");
            Kiem.dung("Dong y: nhan buoc 1 (dang lam 0/20)", v != null && v[0] == 0 && v[1] == 0);
            Kiem.dung("Dong y: khong nhan buoc 2 truoc khi xong buoc 1", !t.containsKey("CA-01.2"));
            final java.lang.reflect.Method nhan = real.NhiemVuEngine.class.getDeclaredMethod("nhan", Char.class, String.class);
            nhan.setAccessible(true);
            final String chuNhan = (String) nhan.invoke(null, q, "CA-01.1");
            Kiem.bang("Dong y: cung cau tra loi nhu nhan o So tay", chuNhan, chuGoi(p, 37, truoc));
            Kiem.bang("Dong y: cung dong tien do nhu nhan o So tay", tienDo(q).get("CA-01.1")[0] + "/" + tienDo(q).get("CA-01.1")[1]
                    + "/" + tienDo(q).get("CA-01.1")[2], v[0] + "/" + v[1] + "/" + v[2]);
            truoc = p.goi.size();
            KhoaBanDoGame.khiTraLoi(p, true);
            Kiem.bang("bam Dong y lan hai: khong lam gi", truoc, p.goi.size());
            Kiem.bang("nhan lai o So tay: tu choi", "Đã nhận nhiệm vụ này.", (String) nhan.invoke(null, p, "CA-01.1"));

            // dang lam: tien do, Dong y = xem tien do (khong nhan lai)
            v[0] = 7;
            KhoaBanDoGame.luiHoi(p.charDBID, KhoaBanDoGame.CHO_HOI_MS + 1);
            KhoaBanDoGame.chanDiBo(p, 9101);
            KhoaBanDoGame.sauKhiVaoMap(p);
            hop = chuGoi(p, -32, 0);
            Kiem.dung("dang lam: hop co tien do 7/20 + duong Dang lam + Xem tien do",
                    hop.contains("Bước 1/2: hạ 20 Torchic ở Pallet (7/20).") && hop.contains("→ Đang làm.") && hop.endsWith("Xem tiến độ?"));
            truoc = p.goi.size();
            KhoaBanDoGame.khiTraLoi(p, true);
            final String xem = chuGoi(p, 37, truoc);
            Kiem.dung("Dong y khi dang lam: hien tien do (7/20), khong nhan lai", xem != null && xem.contains("(7/20)") && t.get("CA-01.1") == v && v[0] == 7);

            // xong buoc 1 cho nhan thuong
            v[0] = 20;
            v[1] = 1;
            KhoaBanDoGame.luiHoi(p.charDBID, KhoaBanDoGame.CHO_HOI_MS + 1);
            KhoaBanDoGame.chan(p, 9101);
            Kiem.dung("buoc 1 xong: hop nhan thuong", chuGoi(p, -32, 0).contains("Bước 1/2 đã xong: nhận thưởng để mở bước tiếp.")
                    && chuGoi(p, -32, 0).endsWith("Nhận thưởng ngay?"));
            KhoaBanDoGame.xoaHoi(p.charDBID);

            // buoc 2
            v[1] = 2;
            KhoaBanDoGame.chanDiBo(p, 9101);
            KhoaBanDoGame.sauKhiVaoMap(p);
            Kiem.dung("buoc 2 chua nhan", chuGoi(p, -32, 0).contains("\nBước 2/2 (chưa nhận): hạ 20 Ekans ở Viridian.\n"));
            KhoaBanDoGame.khiTraLoi(p, true);
            Kiem.dung("Dong y: nhan buoc 2 (buoc 1 da xong)", t.containsKey("CA-01.2") && t.get("CA-01.2")[1] == 0);
            t.get("CA-01.2")[1] = 2;
            Kiem.dung("xong ca hai buoc: vao duoc", !KhoaBanDoGame.chanDiBo(p, 9101));

            // buoc chua nhan duoc (cap): chi the nhanh, khong hop, Dong y gia khong nhan
            final int soHop = dem(p, -32);
            KhoaBanDoGame.xoaHoi(p.charDBID);
            Kiem.dung("cua ai can cap 30: van chan", KhoaBanDoGame.chan(p, 9102));
            Kiem.bang("cua ai chua nhan duoc: khong hop", soHop, dem(p, -32));
            Kiem.dung("cua ai chua nhan duoc: the nhanh co ly do", p.cuoi().contains("Cần đạt cấp 30 để nhận."));
            KhoaBanDoGame.khiTraLoi(p, true);
            Kiem.dung("Dong y khong co hop: khong nhan", !t.containsKey("CA-03.1"));

            // het han cho -500
            KhoaBanDoGame.xoaHoi(q.charDBID);
            tienDo(q).clear();
            KhoaBanDoGame.chanDiBo(q, 9101);
            final java.lang.reflect.Field fCho = KhoaBanDoGame.class.getDeclaredField("CHO");
            fCho.setAccessible(true);
            final Object h = ((ConcurrentHashMap<Integer, Object>) fCho.get(null)).get(Integer.valueOf(q.charDBID));
            final java.lang.reflect.Field fLuc = h.getClass().getDeclaredField("luc");
            fLuc.setAccessible(true);
            final int soQ = dem(q, -32);
            // gia lap qua han: thay bang hop cung noi dung nhung tao 20 giay truoc
            final java.lang.reflect.Constructor<?> c = h.getClass().getDeclaredConstructors()[0];
            c.setAccessible(true);
            ((ConcurrentHashMap<Integer, Object>) fCho.get(null)).put(Integer.valueOf(q.charDBID),
                    c.newInstance(9101, "CA-01.2", "a", "b", "c", fLuc.getLong(h) - 20000L));
            KhoaBanDoGame.sauKhiVaoMap(q);
            Kiem.bang("hop cho qua 15 giay: bo", soQ, dem(q, -32));

            // So tay tat: chi the nhanh nhu cu
            com.htbb.pokemon.LauDai.dat(false);
            KhoaBanDoGame.xoaHoi(q.charDBID);
            KhoaBanDoGame.chan(q, 9101);
            Kiem.dung("So tay tat: khong hop, the nhanh cu", dem(q, -32) == soQ && q.cuoi().startsWith("Cần hoàn thành nhiệm vụ «Cửa ải: Rời đồng cỏ Pallet»"));
        } finally {
            com.htbb.pokemon.LauDai.dat(lauDaiCu);
            fMau.set(null, mauCu);
            KhoaBanDo.datBang(cu);
            for (final int id : new int[]{9100, 9101, 9102}) {
                RealController.mapList.remove(Integer.valueOf(id));
            }
            for (final NguoiChoi x : new NguoiChoi[]{p, q}) {
                KhoaBanDoGame.xoaHoi(x.charDBID);
                real.NhiemVuEngine.khiThoat(x);
            }
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

        // 0.9.8 (ra soat huong dan): moi buoc ha quai cua CHUOI CHINH co it nhat mot ban do co quai do MO (cap) o dung cap
        // cua buoc - chuoi chinh khong bao gio chi vao ban do con khoa cap
        final Map<Integer, Set<Integer>> quaiMap = new HashMap<Integer, Set<Integer>>();
        for (final Integer m : luat.keySet()) {
            for (final Integer qq : quaiTrenMap(m)) {
                Set<Integer> v = quaiMap.get(qq);
                if (v == null) {
                    quaiMap.put(qq, v = new TreeSet<Integer>());
                }
                v.add(m);
            }
        }
        final List<String> loiChinh = new ArrayList<String>();
        int soBuoc = 0;
        for (final Map<String, String> q : theoMa.values()) {
            final String k = q.get("kieu_muc_tieu");
            if (!"chinh".equals(q.get("loai")) || !q.get("muc_tieu_id").matches("\\d+")
                    || !(k.equals("giet") || k.equals("giet_lech_cap") || k.equals("giet_tinh_anh") || k.equals("ha_alpha"))) {
                continue;
            }
            final Set<Integer> maps = quaiMap.get(Integer.valueOf(q.get("muc_tieu_id")));
            if (maps == null) {
                continue;
            }
            soBuoc++;
            final int capBuoc = Integer.parseInt(q.get("cap_toi_thieu"));
            int capMo = Integer.MAX_VALUE;
            for (final Integer m : maps) {
                capMo = Math.min(capMo, Integer.parseInt(luat.get(m).get("cap_toi_thieu")));
            }
            if (capMo > capBuoc) {
                loiChinh.add(q.get("ma") + " (cap " + capBuoc + "): quai " + q.get("muc_tieu_ten") + " chi o ban do " + maps + " mo tu cap " + capMo);
            }
        }
        Kiem.dung("chuoi chinh: co buoc ha quai de kiem (" + soBuoc + ")", soBuoc > 50);
        Kiem.bang("chuoi chinh: ban do cua moi buoc ha quai mo dung cap cua buoc", "[]", loiChinh.toString());
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
        Kiem.dung("di bo qua cong (goi 12): MapXuLyLenh.doChangeMap chan", doc("src/real/MapXuLyLenh.java").contains("KhoaBanDoGame.chanDiBo(player, toMapID)"));
        Kiem.dung("san Lien minh: MapLienDau.doChangeMap chan", doc("src/real/MapLienDau.java").contains("KhoaBanDoGame.chanDiBo(player, toMapID)"));
        final String map = doc("src/real/Map.java");
        Kiem.dung("chot cuoi Map.move2Map", map.contains("mapID != player.mapID ? KhoaBanDoGame.chanMove2Map(player, mapID) : 0"));
        Kiem.dung("xa phu: chan kieu di bo (hop sau khi nap lai map)", map.contains("if (KhoaBanDoGame.chanDiBo(player, mapID)) {\n                        KhoaBanDoGame.giuTaiCho(player);"));
        Kiem.dung("hop cua ai tra loi o Map.doPopup", map.contains("idpopup == KhoaBanDoGame.POPUP_CUA_AI && idActor == player.id")
                && map.contains("KhoaBanDoGame.khiTraLoi(player, ok == 1)"));
        Kiem.dung("hop cua ai gui khi client nap xong map (-500, ca san Lien minh)",
                doc("src/real/MapXuLyLenh.java").contains("KhoaBanDoGame.sauKhiVaoMap(player)")
                && doc("src/real/MapLienDau.java").contains("KhoaBanDoGame.sauKhiVaoMap(player)"));
        final String kh = doc("../client_jar/src/game/networklogic/MessageHandler.java");
        Kiem.dung("client: goi -32 = hop Co/Khong tra doCustomPopup (khong can client moi)",
                kh.contains("case -32:") && kh.contains("Canvas.startYesNoDlg(text5") && kh.contains("doCustomPopup(idB, idPopup, text5, 1)"));
        Kiem.dung("xa phu cu (goi 79) chan truoc khi tru xu + nap lai map", map.contains("KhoaBanDoGame.giuTaiCho(player); // chua tru xu"));
        final String npc = doc("src/real/NpcDichVu.java");
        Kiem.dung("Pidgeot bay + Lapras do chan truoc khi tru phi", npc.contains("return; // chua tru xu") && npc.contains("return; // chua tru ve"));
        Kiem.dung("menu bay / do / Arcanine gac cong hien diem khoa", npc.split("KhoaBanDoGame.nhanDiem").length == 4);
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
